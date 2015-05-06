package dblis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.jfree.ui.RefineryUtilities;
import twitter4j.FilterQuery;
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
        
        final List<String> countryCodes = getCountryCodes(sa);
        final List<String> sports = getSports(sa);
        final List<String> sportsGB = getSportsGB(sa);
        final JSONArray geolocations = getGeolocations(sa);
        final List<String> geoList = getGeolocationsList(sa);
        
        if (useStream) {
            int max = 100;
            geoList.stream().forEach(loc -> {
                sportsGB.stream().forEach(sport -> {
                    try {
                        twitterStream(sa, sport, loc, max);
                    } catch (IOException ex) {
                        System.out.println("Twitter Stream error - " + ex);
                    }
                });
            });
            return;
        }
        
        // debug JSONArray's
        JSONArray mostCommon = getCommonSports(sa, "NL", 5);
        JSONArray popular = getPopularSportCountries(sa, "tennis", 5);
        JSONArray mostCommonSports = getMostCommonSports(sa, "NL");
        JSONArray mostCommonCountries = getMostCommonCountries(sa, "football", 5);
        JSONArray alternatives = getAlternatives(sa, "tennis");
        
        // EXCEL OUTPUT
        /*final Map<String, JSONArray> commonSports = new HashMap();
        countryCodes.stream().forEach(code -> {
            commonSports.put(code, getMostCommonSports(sa, code));
        });
        
        Map<String, List<Object[]>> sorted = sortPopularity(commonSports, sportsGB);
        Object[][] popExcel = popularityToExcelFormat(sorted, sportsGB, countryCodes);
        File popularFile = new File(getWorkingDirectory() + "popular.xls");
        //Excel.writeToExcel(popularFile, popExcel);
        try {
            Excel.pieChart(popularFile, popExcel);
        } catch (IOException ex) {
            
        }
        
        final List<ChartData> chartdata = new ArrayList<>();
        sorted.get("NL").stream().forEach(obj -> {
            chartdata.add(new ChartData((String) obj[0], (int) obj[1]));
        });
        Chart3D pie = new Chart3D("NL", chartdata, "Pie");
        pie.view();
        return;*/
        
        // SEARCHING
        
        // for each location
        geoList.stream().forEach(geo -> {
            // for each sport
            sportsGB.stream().forEach(sport -> {
                final String[] alts = getAlternativesArray(sa, sport);

                // for each alternative word
                for (String altSport : alts) {

                    // getTweets and wait if limit reached
                    long resetTime = 0;
                    long curTime = 0;
                    long wait = 0;
                    while (resetTime >= curTime) {
                        resetTime = getTweets(altSport, geo);
                        curTime = new Date().getTime();
                        try {
                            if (curTime <= resetTime) {
                                storeRest(sa);

                                wait = resetTime - curTime;
                                System.out.println("Sleeping "
                                        + (wait / 1000)
                                        + "s for " + altSport);
                                Thread.sleep(wait + 1000);
                            }
                        } catch (InterruptedException ex) {
                            System.out.println("Error sleeping - " + ex);
                        }
                    }
                    
                    if (resetTime == -1) {
                        storeRest(sa);
                        return;
                    }

                }

            });
        });

        storeRest(sa);
        
        //toSearch.stream().forEach(sport -> getTweets(sport, geocode));
        //getTweets(search);
        
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
    private long getTweets(String search, String geocode) {
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
                    return -1;
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
            return te.getRateLimitStatus().getResetTimeInSeconds() * 1000L;
        }
        return 0;
    }
    
    /**
     * Runs a twitter stream, continously retrieving tweets
     * 
     * @param sa ServerAccess
     * @param search keyword(s) to search
     * @throws IOException on IO error
     */
    private void twitterStream(ServerAccess sa, String search, String geocode, 
            int max) throws IOException {
        final TwitterStream twitterStream = 
                new TwitterStreamFactory(getAuth()).getInstance();
        final Count count = new Count(max);
        
        final StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                count.increment();
                sa.addTweet(new TweetEntity(dataSeperator, status, search));
                sa.addUser(new UserEntity(dataSeperator, status.getUser()));
                
                if (Abort.getInstance().abort() || count.isMax()) {
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
        FilterQuery filterquery = new FilterQuery();
        //filterquery.count(500);
        //String[] searchwords = {"voetbal", "tennis", "basketbal"};
        //filterquery.track(searchwords);
        final String[] alts = getAlternativesArray(sa, search);
        final String[] altsGeo = new String[alts.length];
        for (int i = 0; i < alts.length; i++) {
            altsGeo[i] = alts[i] + " " + geocode;
        }
        filterquery.track(altsGeo);
        filterquery.language(new String[]{"nl", "de", "en", "fr", "es"});
        twitterStream.filter(filterquery);
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
    
    private void storeRest(ServerAccess sa) {
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
        tweets.clear();
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
    
    /**
     * Coverts map [countryCode =&gt; [sport =&gt; number]] to 
     * map [countryCode =&gt; sorted list {sport, number}]
     * 
     * @param map input map
     * @return sorted output map
     */
    private Map<String, List<Object[]>> sortPopularity(
            Map<String, JSONArray> map, List<String> sportsGB) {
        final Map<String, List<Object[]>> rtn = new HashMap();
        final Comparator comp = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Integer) o2[1]).compareTo((Integer) o1[1]);
            }
        };
        
        map.entrySet().stream().forEach(entry -> {
            try {
                final List<Object[]> list = new ArrayList<>();
                final JSONObject json = entry.getValue().getJSONObject(0);
                sportsGB.stream().forEach(sport -> {
                    try {
                        list.add(new Object[]{sport, json.getInt(sport)});
                    } catch (JSONException ex) {
                    }
                });
                list.sort(comp);
                rtn.put(entry.getKey(), list);
            } catch (JSONException ex) {
            }
        });
        
        return rtn;
    }
    
    private Object[][] popularityToExcelFormat(Map<String, List<Object[]>> map,
            List<String> sportsGB, List<String> countryCodes) {
        final Object[][] data = new Object[(sportsGB.size() + 1) * countryCodes.size()]
                [3];
        
        int row = 0;
        int col = 0;
        List<Object[]> list;
        for (String code : countryCodes) {
            data[row][col] = code;
            list = map.get(code);
            for (int i = 0; i < list.size(); i++) {
                data[row][1] = list.get(i)[0];
                data[row][2] = list.get(i)[1];
                row++;
            }
            row++;
        }
        
        return data;
        
        /*final Object[][] data = new Object[sportsGB.size() + 1]
                [countryCodes.size() * 3];
        
        int row = 0;
        int col = 0;
        List<Object[]> list;
        for (String code : countryCodes) {
            data[row][col] = code;
            list = map.get(code);
            for (int i = 0; i < list.size(); i++) {
                data[row + i + 1][col] = list.get(i)[0];
                data[row + i + 1][col + 1] = list.get(i)[1];
            }
            col += 3;
        }
        
        return data;*/
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
    
    private List<String> getSports(ServerAccess sa) {
        final List<String> list = new ArrayList<>();
        try {
            final JSONArray json = sa.getSports();
            for (int i = 0; i < json.length(); i++) {
                list.add(json.getJSONObject(i).getString("Sport"));
            }
        } catch (Exception ex) {
            System.out.println("DBLIS - getSports - " + ex);
        }
        return list;
    }
    
    private List<String> getSportsGB(ServerAccess sa) {
        final List<String> list = new ArrayList<>();
        try {
            final JSONArray json = sa.getSportsGB();
            for (int i = 0; i < json.length(); i++) {
                list.add(json.getJSONObject(i).getString("Sport"));
            }
        } catch (Exception ex) {
            System.out.println("DBLIS - getSports - " + ex);
        }
        return list;
    }
    
    @Deprecated
    private JSONArray getCommonSports(ServerAccess sa, String countryCode, int top) {
        try {
            return sa.getCommonSports(countryCode, top);
        } catch (Exception ex) {
            System.out.println("DBLIS - getCommonSports - " + ex);
        }
        return new JSONArray();
    }
    
    @Deprecated
    private JSONArray getPopularSportCountries(ServerAccess sa, String sport, int top) {
        try {
            return sa.getPopularSportCountries(sport, top);
        } catch (Exception ex) {
            System.out.println("DBLIS - getPopularSportCountries - " + ex);
        }
        return new JSONArray();
    }
    
    private List<String> getGeolocationsList(ServerAccess sa) {
        final JSONArray json = getGeolocations(sa);
        final List<String> list = new ArrayList<>();
        
        JSONObject obj;
        String geo;
        for (int i = 0; i < json.length(); i++) {
            try {
                obj = json.getJSONObject(i);
                geo = toGeocode(obj.getString("latitude"), 
                        obj.getString("longtitude"), 
                        obj.getString("radius"));
                list.add(geo);
            } catch (Exception ex) {
                System.out.println("DBLIS - getAlternativesArray - " + ex);
            }
        }
        
        return list;
    }
    
    private JSONArray getGeolocations(ServerAccess sa) {
        try {
            return sa.getGeolocations();
        } catch (Exception ex) {
            System.out.println("DBLIS - getGeolocations - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getMostCommonSports(ServerAccess sa, String countryCode) {
        try {
            return sa.getMostCommonSports(countryCode);
        } catch (Exception ex) {
            System.out.println("DBLIS - getMostCommonSports - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getMostCommonCountries(ServerAccess sa, 
            String countryCode, int top) {
        try {
            return sa.getMostCommonCountries(countryCode, top);
        } catch (Exception ex) {
            System.out.println("DBLIS - getMostCommonCountries - " + ex);
        }
        return new JSONArray();
    }
    
    private JSONArray getAlternatives(ServerAccess sa, String sport) {
        try {
            return sa.getAlternatives(sport);
        } catch (Exception ex) {
            System.out.println("DBLIS - getAlternatives - " + ex);
        }
        return new JSONArray();
    }
    
    private String[] getAlternativesArray(ServerAccess sa, String sport) {
        final JSONArray json = getAlternatives(sa, sport);
        final List<String> list = new ArrayList<>();
        
        for (int i = 0; i < json.length(); i++) {
            try {
                list.add(json.getJSONObject(i).getString(sport));
            } catch (Exception ex) {
                System.out.println("DBLIS - getAlternativesArray - " + ex);
            }
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    private List<String> getCountryCodes(ServerAccess sa) {
        final List<String> list = new ArrayList<>();
        
        try {
            final JSONArray json = sa.getCountryCodes();
            String code;
            
            for (int i = 0; i < json.length(); i++) {
                try {
                    code = json.getJSONObject(i).getString("countryCode");
                    list.add(code);
                } catch (Exception ex) {
                    System.out.println("DBLIS - getAlternativesArray - " + ex);
                }
            }

        } catch (Exception ex) {
            System.out.println("DBLIS - getCountryCodes - " + ex);
        }
        
        return list;
    }
    
    private class Count {
        
        private final int max;
        private int count;
        
        Count(int max) {
            this.max = max;
            this.count = 0;
        }
        
        synchronized void increment() {
            count++;
        }
        
        synchronized boolean isMax() {
            return count >= max;
        }
        
    }
}