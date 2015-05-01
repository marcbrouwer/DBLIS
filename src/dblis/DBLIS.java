package dblis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import twitter4j.HashtagEntity;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Brouwer M.R.
 */
public class DBLIS implements Runnable {
    
    // Timestamp for now
    private final long now = (new Date()).getTime() / 1000;
    private final String nowString = String.valueOf(now);
    
    // Search block size
    private final long blocks15 = 900;
    private final int hours = 4;
    private final int blocks = hours * 4;
    
    // Search settings (CAN BE CHANGED)
    private String geocode = "geocode:51.444,5.491,500km";
    private String search = "cricket";
    private final long starttime = 1430133677;//now - blocks * 900; //1429452000;
    private boolean useStream = false;
    
    // Search setting for auto searching most commonly used hashtags or words
    private final int top = 5;
    private final int searches = 5;
    
    // Data maps and sets
    private final Map<String, Set<Status>> tweets;
    private final Map<String, Integer> countHashtags;
    private final Map<String, Integer> countWords;
    private final Map<String, Integer> countLocation;
    private final Set<String> searched;
    private final Set<String> wordsFilter;
    
    // Storage file paths and data seperator
    private final String tweetsStorePath = getWorkingDirectory() + "tweets.txt";
    private final String usersStorePath = getWorkingDirectory() + "users.txt";
    private final String dataSeperator = ";&;";
    
    // Number of tweets loaded
    private int numberOfTweets = 0;
    
    /** Constructor */
    public DBLIS() {
        this.tweets = new HashMap();
        this.countHashtags = new HashMap();
        this.countWords = new HashMap();
        this.countLocation = new HashMap();
        this.searched = new HashSet();
        this.wordsFilter = new HashSet();
    }
    
    public DBLIS(String keywords, boolean stream) {
        this();
        this.search = keywords;
        this.useStream = stream;
    }
    
    public DBLIS(String keywords, boolean stream, float latitude, 
            float longtitude, float radius) {
        this(keywords, stream);
        this.geocode = "geocode:" + latitude + "," + longtitude + 
                "," + radius + "km";
    }
    
    public static void main(String[] args) {
        new DBLIS().run();
    }
    
    @Override
    public void run() {
        final ServerAccess sa = new ServerAccess();
        initWordsFilter();
        
        System.out.println("Start time: " + (new Date(starttime * 1000)) + "\n");
        
        if (useStream) {
            try {
                twitterStream(search, false);
            } catch (IOException ex) {
                System.out.println("Twitter Stream error - " + ex);
            }
            return;
        }
        
        JSONArray sports = getSports(sa);
        JSONArray geolocations = getGeolocations(sa);
        
        JSONArray mostCommon = getCommonSports(sa, "NL", 5);
        JSONArray popular = getPopularSportCountries(sa, "tennis", 5);
        
        JSONObject indexObj;
        Iterator iter;
        String code;
        final Map<String, Set<String>> toSearch = new HashMap();
        for (int i = 0; i < sports.length(); i++) {
            try {
                indexObj = sports.getJSONObject(i);
                iter = indexObj.keys();
                while (iter.hasNext()) {
                    /*final String next = iter.next().toString();
                    if (next.equals("NL")) {
                        toSearch.add(indexObj.getString(next));
                    }*/
                    code = iter.next().toString();
                    if (!toSearch.containsKey(code)) {
                        toSearch.put(code, new HashSet());
                    }
                    toSearch.get(code).add(indexObj.getString(code));
                }
            } catch (JSONException ex) {
                System.out.println("DBLIS - run - search sport - " + ex);
            }
        }
        toSearch.remove(null);
        
        // Gets data from database (will be exported to excel in a later stage)
        final Map<String, JSONArray> commonInCountry = new HashMap();
        toSearch.keySet().stream().forEach(ccode -> {
            commonInCountry.put(ccode, getCommonSports(sa, ccode, 5));
        });
        
        System.out.println(commonInCountry);
        
        final Map<String, JSONArray> commonSportCountry = new HashMap();
        toSearch.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(sport -> {
                commonSportCountry.put(sport, 
                        getPopularSportCountries(sa, sport, 5));
            });
        });
        
        System.out.println(commonSportCountry);
        
        // Continue searching
        String latitude, longtitude, radius;
        for (int i = 0; i < geolocations.length(); i++) {
            try {
                indexObj = geolocations.getJSONObject(i);
                final String cc = indexObj.getString("countryCode");
                latitude = indexObj.getString("latitude");
                longtitude = indexObj.getString("longtitude");
                radius = indexObj.getString("radius");
                final String geo = toGeocode(latitude, longtitude, radius);
                toSearch.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(cc) 
                                || entry.getKey().equals("GB"))
                        //.filter(entry -> entry.getKey().equals("ES"))
                        .forEach(entry -> {
                            entry.getValue()
                            .forEach(sport -> getTweets(sport, geo));
                        });
            } catch (JSONException ex) {
                System.out.println("DBLIS - run - search geo - " + ex);
            }
        }
        
        //toSearch.stream().forEach(sport -> getTweets(sport, geocode));
        //getTweets(search);
        tweets.entrySet().stream().forEach(keyword -> {
            keyword.getValue().stream().forEach(status -> {
                if (status.getRetweetedStatus() != null) {
                    if (addTweet(sa, status.getRetweetedStatus(), keyword.getKey())) {
                        if (!addUser(sa, status.getRetweetedStatus().getUser())) {
                            System.out.println("User not saved: " + 
                                    status.getRetweetedStatus().getUser());
                        }
                    } else {
                        System.out.println("Tweet not saved: " + status.getRetweetedStatus());
                    }
                }
                if (addTweet(sa, status, keyword.getKey())) {
                    if (!addUser(sa, status.getUser())) {
                        System.out.println("User not saved: " + status.getUser());
                    }
                } else {
                    System.out.println("Tweet not saved: " + status);
                }
            });
        });
        
        // Searches for most commonly used hashtags
        /*final String[] searchParts = search.split(" ");
        searched.addAll(Arrays.asList(searchParts));
        
        for (int s = 0; s < searches - 1; s++) {
            if (Abort.getInstance().abort()) {
                Abort.getInstance().setAbort(false);
                return;
            }
            
            System.out.println("\n\nSearch " + s + " - "
                    + searched.toString() + "\n");
            printCommon(top);

            getTweetsMostCommonHashTag();
        }
        
        // Final output
        System.out.println("\n\nFinal search - " + searched.toString() + "\n");
        printCommon(top);
        
        //scanLocations();
        storeData();*/
    }
    
    /** Gets configuration builder for authentication */
    private Configuration getAuth() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("n2g9XOjAr9p44yJwFjXUbeUa2");
        cb.setOAuthConsumerSecret("57FHkBBptp17yBGl1v853lldZO9Kh4osJnDQqQEcXd4d9C3xFA");
        cb.setOAuthAccessToken("113906448-2fx9njfJgzQrGdnRaGchI9GlZTzLMXrayEzFk2ju");
        cb.setOAuthAccessTokenSecret("FJOqMt7dtBp1yuW2VnQDfzksa7IS5h3IxxsJ1ixBGI1ny");
        
        return cb.build();
    }
    
    /**
     * Gets tweets for a given keyword and globally set time frame
     * 
     * @param search keyword to search
     */
    private void getTweets(String search, String geocode) {
        TwitterFactory tf = new TwitterFactory(getAuth());
        Twitter twitter = tf.getInstance();
        
        try {
            Query query = new Query(search + " " + geocode);
            query.setSinceId(starttime);
            QueryResult result;
            
            if (!tweets.containsKey(search)) {
                tweets.put(search, new HashSet());
            }
            
            for (int i = 0; i < blocks; i++) {
                if (Abort.getInstance().abort()) {
                    return;
                }
                
                result = twitter.search(query);
                tweets.get(search).addAll(result.getTweets());
                
                //updateCommon(result.getTweets());
                //searched.add(search.toLowerCase());
                query.setSinceId(starttime + i * blocks15);
            }
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te);
            System.out.println("\nRetry at: " + 
                    (new Date(te.getRateLimitStatus()
                            .getResetTimeInSeconds() * 1000L)));
        }
    }
    
    /**
     * Runs a twitter stream, continously retrieving tweets
     * 
     * @param search keyword(s) to search
     * @param append determine whether to append file or start empty
     * @throws IOException on IO error
     */
    private void twitterStream(String search, boolean append) throws IOException {
        final PrintWriter tweetsPrinter = new PrintWriter(
                new BufferedWriter(new FileWriter(tweetsStorePath, append)));
        final PrintWriter usersPrinter = new PrintWriter(
                new BufferedWriter(new FileWriter(usersStorePath, append)));
        final TwitterStream twitterStream = 
                new TwitterStreamFactory(getAuth()).getInstance();
        
        final StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                tweetsPrinter.println(new TweetEntity(dataSeperator, status, search));
                usersPrinter.println(new UserEntity(dataSeperator, status.getUser()));
                
                if (Abort.getInstance().abort()) {
                    Abort.getInstance().setAbort(false);
                    twitterStream.shutdown();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                //System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        // sample() method internally creates a thread which manipulates 
        // TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample();
    }
    
    /** Gets the most commonly used hashtag for which is not yet searched */
    private void getTweetsMostCommonHashTag() {
        final List<Entry<String, Integer>> list = 
                getCommonList(countHashtags, countHashtags.size());
        list.stream()
                .filter(entry -> !searched.contains(entry.getKey().toLowerCase()))
                .limit(1)
                .forEach(entry -> getTweets(entry.getKey(), geocode));
    }
    
    /**
     * Updates the maps containing the words and hashtags count
     * 
     * @param tweetsList list of tweets
     */
    private void updateCommon(List<Status> tweetsList) {
        numberOfTweets += tweetsList.size();
        
        tweetsList.stream().forEach(tweet -> {
            final HashtagEntity[] hashtags = tweet.getHashtagEntities();
            for (HashtagEntity hashtag : hashtags) {
                final String text = hashtag.getText().toLowerCase();
                if (!countHashtags.containsKey(text)) {
                    countHashtags.put(text, 0);
                }
                countHashtags.put(text, countHashtags.get(text) + 1);
            }
            
            final String[] words = tweet.getText().split(" ");
            for (String word : words) {
                word = word.toLowerCase();
                if (!wordsFilter.contains(word)) {
                    if (!countWords.containsKey(word)) {
                        countWords.put(word, 0);
                    }
                    countWords.put(word, countWords.get(word) + 1);
                }
            }
            
            final String location = tweet.getUser().getLocation();
            if (!countLocation.containsKey(location)) {
                countLocation.put(location, 0);
            }
            countLocation.put(location, countLocation.get(location) + 1);
        });
    }
    
    /**
     * Gets a list with the top most commonly used words or hashtags associated 
     * with the number of times they are used
     * 
     * @param count map containing the word or hashtag count
     * @param top determines the number above which should be printed (e.g. top 5)
     * @return list containing the commonly used words or hashtags associated 
     * with the number of times they are used
     */
    private List<Entry<String, Integer>> getCommonList(
            Map<String, Integer> count, int top) {
        
        final List<Entry<String, Integer>> topList = new ArrayList<>();
        
        final Set<Integer> setCount = new HashSet();
        setCount.addAll(count.values());
        setCount.remove(1);
        
        final List<Integer> counts = new ArrayList<>(setCount);
        Collections.sort(counts, Collections.reverseOrder());
        
        if (counts.isEmpty()) {
            return topList;
        }
        
        if (counts.size() < top) {
            top = counts.size();
        }
        
        final int countCommon = counts.get(top - 1);
        
        final Stream<Entry<String, Integer>> common = count.entrySet().stream()
                .filter(entry -> entry.getValue() >= countCommon);
        final Map<String, Integer> commonMap = new HashMap();
        common.forEach(entry -> commonMap.put(entry.getKey(), entry.getValue()));
        
        for (int i = 0; i < top; i++) {
            final int cc = counts.get(i);
            commonMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == cc)
                    .forEach(entry -> topList.add(entry));
        }
        
        return topList;
    }
    
    /**
     * Prints the top commonly used words and hashtags
     * 
     * @param top determines the number above which should be printed (e.g. top 5)
     */
    private void printCommon(int top) {
        printCommonList("HashTags", getCommonList(countHashtags, top));
        printCommonList("Words", getCommonList(countWords, top));
        printCommonList("User location", getCommonList(countLocation, top));
        
        System.out.println("\nNumber of tweets: " + numberOfTweets);
    }
    
    /**
     * Prints the list of commonly used words or hashtags
     * 
     * @param type words or hashtags
     * @param list list containing the commonly used words or hashtags
     */
    private void printCommonList(String type, List<Entry<String, Integer>> list) {
        System.out.println(type + ": ");
        if (list.isEmpty()) {
            System.out.println("Nothing common found.");
        } else {
            list.stream().forEach((entry) -> {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            });
        }
        System.out.println();
    }
    
    /** Adds words to {@code wordsFilter} such that they can be excluded */
    private void initWordsFilter() {
        final String[] filter = new String[]{
            "", "&", "-", "...", ":", 
            "a", "at", "and", "are", "avec",  
            "be", "bij", "by", 
            "dan", "dat", "de", "di", "du", 
            "el", "en", 
            "for", "from", 
            "he", "het", 
            "i", "in", "is", 
            "je", "jij", "just", 
            "la", "le", 
            "of", "on", "op", "or", "out",  
            "rt", "retweet", 
            "so", 
            "tak", "te", "that", "the", "then", "to", "twitter", 
            "un", 
            "van",
            "wat", "we", "what", "with",
            "you", "your", 
            "zijn"
        };
        
        wordsFilter.addAll(Arrays.asList(filter));
    }
    
    /** Scans through all tweets to check if a location is set (Geo or Place) */
    private void scanLocations() {
        final List<Status> withLocation = new ArrayList<>();
        tweets.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(status -> {
                if (status.getGeoLocation() != null || status.getPlace() != null) {
                    withLocation.add(status);
                }
            });
        });
        
        System.out.println("\n\nTweets with location " + withLocation.size() + ":");
        withLocation.stream().forEach(status -> {
            if (status.getGeoLocation() != null) {
                System.out.println("Geo: " + status.getGeoLocation());
            }
            if (status.getPlace() != null) {
                System.out.println("Place: " + status.getPlace());
            }
        });
    }
    
    /** 
     * Stores data to files 
     * 
     * @param clear write from a clear file if true, else appends file
     */
    private void storeData() {
        final File tweetsFile = new File(tweetsStorePath);
        final File usersFile = new File(usersStorePath);
        final Set<TweetEntity> tweetEntities = new HashSet();
        final Set<UserEntity> userEntities = new HashSet();
        
        tweets.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(
                    status -> {
                        tweetEntities.add(
                                new TweetEntity(dataSeperator, status,
                                        entry.getKey()));
                        userEntities.add(
                                new UserEntity(dataSeperator, status.getUser()));
                    });
        });

        try {
            try (PrintWriter tweetWriter = new PrintWriter(tweetsFile)) {
                tweetEntities.stream().forEach(entity -> {
                    tweetWriter.println(entity.toString());
                });
            }
            try (PrintWriter userWriter = new PrintWriter(usersFile)) {
                userEntities.stream().forEach(entity -> {
                    userWriter.println(entity.toString());
                });
            }
        } catch (IOException ex) {
            System.out.println("DBLIS - storeData() - error storing data" + ex);
        }
    }
    
    /**
     * Gets current working directory of executing process
     * 
     * @return directory as String
     */
    private String getWorkingDirectory() {
        try {
            // get working directory as File
            String path = DBLIS.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().getPath();
            File folder = new File(path);
            folder = folder.getParentFile().getParentFile(); // 2x .getParentFile() for debug, 1x for normal

            // directory as String
            return folder.getPath() + "/"; // /dist/ for debug, / for normal
        } catch (URISyntaxException ex) {
            return "./";
        }
    }
    
    private String toGeocode(float latitude, float longtitude, int radius) {
        return "geocode:" + latitude + "," + longtitude + "," + radius + "km";
    }
    
    private String toGeocode(String latitude, String longtitude, String radius) {
        return "geocode:" + latitude + "," + longtitude + "," + radius + "km";
    }
    
    private boolean addTweet(ServerAccess sa, Status status, String search) {
        final TweetEntity entity = new TweetEntity(dataSeperator, status, search);
        return sa.addTweet(entity);
    }
    
    private boolean addUser(ServerAccess sa, User user) {
        final UserEntity entity = new UserEntity(dataSeperator, user);
        return sa.addUser(entity);
    }
    
    private JSONArray getSports(ServerAccess sa) {
        try {
            return sa.getSports();
        } catch (Exception ex) {
            System.out.println("DBLIS - getSports - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getCommonSports(ServerAccess sa, String countryCode, int top) {
        try {
            return sa.getCommonSports(countryCode, top);
        } catch (Exception ex) {
            System.out.println("DBLIS - getCommonSports - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getPopularSportCountries(ServerAccess sa, String sport, int top) {
        try {
            return sa.getPopularSportCountries(sport, top);
        } catch (Exception ex) {
            System.out.println("DBLIS - getPopularSportCountries - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getGeolocations(ServerAccess sa) {
        try {
            return sa.getGeolocations();
        } catch (Exception ex) {
            System.out.println("DBLIS - getGeolocations - " + ex);
        }
        return new JSONArray();
    }
    
}