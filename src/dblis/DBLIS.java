package dblis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Brouwer M.R.
 */
public class DBLIS implements Runnable {
    
    private final long now = (new Date()).getTime() / 1000;
    private final String nowString = String.valueOf(now);
    
    private final String geocode = "geocode:51.444,5.491,100km";
    private final String search = "sport football";
    private final long blocks15 = 900;
    private final int hours = 2;
    private final int blocks = hours * 4;
    private final long starttime = 1429461000; //now - blocks * 900; //1429452000;
    private final int top = 5;
    private final int searches = 5;
    
    private final Map<String, List<Status>> tweets;
    private final Map<String, Integer> countHashtags;
    private final Map<String, Integer> countWords;
    private final Map<String, Integer> countLocation;
    private final Set<String> searched;
    private final Set<String> wordsFilter;
    
    private final String tweetsStorePath = getWorkingDirectory() + "tweets.txt";
    private final String usersStorePath = getWorkingDirectory() + "users.txt";
    private final String dataSeperator = ";&;";
    
    private int numberOfTweets = 0;

    /**
     * Constructor
     */
    public DBLIS() {
        this.tweets = new HashMap();
        this.countHashtags = new HashMap();
        this.countWords = new HashMap();
        this.countLocation = new HashMap();
        this.searched = new HashSet();
        this.wordsFilter = new HashSet();
    }
    
    public static void main(String[] args) {
        new DBLIS().run();
    }
    
    @Override
    public void run() {
        initWordsFilter();
        
        System.out.println("Start time: " + (new Date(starttime * 1000)) + "\n");
        
        getTweets(search);
        
        // Searches for most commonly used hashtags
        final String[] searchParts = search.split(" ");
        searched.addAll(Arrays.asList(searchParts));
        
        for (int s = 0; s < searches - 1; s++) {
            System.out.println("\n\nSearch " + s + " - " + 
                    searched.toString() + "\n");
            printCommon(top);

            getTweetsMostCommonHashTag();
        }
        
        System.out.println("\n\nFinal search - " + searched.toString() + "\n");
        printCommon(top);
        
        //scanLocations();
        storeData();
    }
    
    /**
     * Gets tweets for a given keyword and globally set time frame
     * 
     * @param search keyword to search
     */
    private void getTweets(String search) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("n2g9XOjAr9p44yJwFjXUbeUa2");
        cb.setOAuthConsumerSecret("57FHkBBptp17yBGl1v853lldZO9Kh4osJnDQqQEcXd4d9C3xFA");
        cb.setOAuthAccessToken("113906448-2fx9njfJgzQrGdnRaGchI9GlZTzLMXrayEzFk2ju");
        cb.setOAuthAccessTokenSecret("FJOqMt7dtBp1yuW2VnQDfzksa7IS5h3IxxsJ1ixBGI1ny");
        
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        
        try {
            Query query = new Query(search + " " + geocode);
            query.setSinceId(starttime);
            //query.setUntil(now);
            QueryResult result;
            
            for (int i = 0; i < blocks; i++) {
                result = twitter.search(query);
                
                if (!tweets.containsKey(search)) {
                    tweets.put(search, new ArrayList<>());
                }
                tweets.get(search).addAll(result.getTweets());
                
                updateCommon(result.getTweets());
                searched.add(search.toLowerCase());
                query.setSinceId(starttime + i * blocks15);
            }
            
            //do {
                /*result = twitter.search(query);
                updateCommon(result.getTweets());
                searched.add(search.toLowerCase());*/
                /*List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    System.out.println(
                            "@" + tweet.getUser().getScreenName() + " - " + 
                                    tweet.getText() + " - " +
                                    tweet.getUser().getLocation() + " - " + 
                                    tweet.getCreatedAt());
                }*/
            //} while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te);
            System.out.println("\nRetry at: " + 
                    (new Date(te.getRateLimitStatus()
                            .getResetTimeInSeconds() * 1000L)));
        }
    }
    
    /**
     * Gets the most commonly used hashtag for which is not yet searched
     */
    private void getTweetsMostCommonHashTag() {
        final List<Entry<String, Integer>> list = 
                getCommonList(countHashtags, countHashtags.size());
        list.stream()
                .filter(entry -> !searched.contains(entry.getKey().toLowerCase()))
                .limit(1)
                .forEach(entry -> getTweets(entry.getKey()));
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
    
    /**
     * Adds words to {@code wordsFilter} such that they can be excluded
     */
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
    
    /**
     * Scans through all tweets to check if a location is set (Geo or Place)
     */
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
    public final String getWorkingDirectory() {
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
    
}