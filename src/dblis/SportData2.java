package dblis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import twitter4j.JSONException;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Brouwer M.R.
 */
public class SportData2 {
    
    // Instance declaration 
    
    /**
     * SportData2 instance
     */
    private static final SportData2 instance = new SportData2();

    /**
     * Don't let anyone else instantiate this class
     */
    private SportData2() {
    }

    /**
     * Gets the SportData2 instance
     *
     * @return SportData2 instance
     */
    public synchronized static final SportData2 getInstance() {
        return instance;
    }
    
    // Variables
    
    private int numberTweets = 0;
    private final List<String> sports
            = Arrays.asList("football", "hockey", "cycling", "tennis", "skating");
    private final Set<TweetEntity> tweets = new HashSet();
    private final Map<String, List<String>> relations = new ConcurrentHashMap();
    
    // Search variables
    
    private final Map<String, Set<Status>> searchTweets = new ConcurrentHashMap();
    private final Object searchLock = new Object();
    
    // Temp storage
    
    private Date startdate = new Date();
    private Date enddate = new Date();
    private int interval = 1;
    private List<String> selected = new ArrayList<>();
    
    // PUBLIC Methods
    
    public final void init() {
        final ServerAccess sa = new ServerAccess();
        numberTweets = sa.getTweetsCountNL();
        //tweets.addAll(sa.getTweetsNL(numberTweets));
        getTweets(sa);
        sports.stream().forEach(sport -> {
            if (!relations.containsKey(sport)) {
                relations.put(sport, new ArrayList<>());
            }
            final Set<String> alts = sa.getAlternatives(sport);
            relations.get(sport).addAll(alts);
        });
    }
    
    public final int getInitProgress() {
        if (numberTweets == 0) {
            return 0;
        }
        return (int) Math.round(100 * tweets.size() / (numberTweets - 1));
    }
    
    public final void search() {
        final List<String> keywords = new ArrayList<>();
        relations.values().stream().forEach(list -> keywords.addAll(list));
        runStoreThread(keywords);
        timeSearch(new ServerAccess(), keywords);
    }
    
    public final List<String> getSports() {
        return sports;
    }
    
    public final List<String> getKeywords() {
        return new ArrayList(relations.values());
    }
    
    public final int getRetweetCount(String sport) {
        return getRetweetCount(getRelatedTweets(sport));
    }
    
    public final int getRetweetCount(String sport, long starttime, long endtime) {
        return getRetweetCount(getRelatedTweets(sport, starttime, endtime));
    }
    
    public final int getFavCount(String sport) {
        return getFavCount(getRelatedTweets(sport));
    }
    
    public final int getFavCount(String sport, long starttime, long endtime) {
        return getFavCount(getRelatedTweets(sport, starttime, endtime));
    }
    
    public final int getPopularity(String sport) {
        return getRetweetCount(sport) + getFavCount(sport);
    }
    
    public final int getPopularity(String sport, long starttime, long endtime) {
        return getRetweetCount(sport, starttime, endtime) 
                + getFavCount(sport, starttime, endtime);
    }
    
    public final Map<String, Double> getPopularitySports(List<String> sports) {
        final Map<String, Double> pop = new HashMap();
        
        sports.stream().forEach(sport -> {
            pop.put(sport, (double) getPopularity(sport));
        });
        
        return pop;
    }
    
    public final Map<String, Double> getPopularitySports(List<String> sports, 
            long starttime, long endtime) {
        final Map<String, Double> pop = new HashMap();
        
        sports.stream().forEach(sport -> {
            pop.put(sport, (double) getPopularity(sport, starttime, endtime));
        });
        
        return pop;
    }
    
    public final Map<String, Double> getPopularitySportsAsPercentage(List<String> sports) {
        return getAsPercentage(getPopularitySports(sports));
    }
    
    public final Map<String, Double> getPopularitySportsAsPercentage(List<String> sports, 
            long starttime, long endtime) {
        return getAsPercentage(getPopularitySports(sports, starttime, endtime));
    }
    
    public final Map<String, Double> getPopularityAllSports() {
        return getPopularitySports(sports);
    }
    
    public final Map<String, Double> getPopularityAllSports(long starttime, long endtime) {
        return getPopularitySports(sports, starttime, endtime);
    }
    
    public final Map<String, Double> getPopularityAllSportsAsPercentage() {
        return getAsPercentage(getPopularityAllSports());
    }
    
    public final Map<String, Double> getPopularityAllSportsAsPercentage(long starttime, long endtime) {
        return getAsPercentage(getPopularityAllSports(starttime, endtime));
    }
    
    public final Map<String, Double> getPopularityKeywords(String sport) {
        final Map<String, Double> pop = new HashMap();
        
        if (relations.containsKey(sport)) {
            relations.get(sport).stream().forEach(keyword -> 
                    pop.put(keyword, (double) getPopularity(keyword)));
        }
        
        return pop;
    }
    
    public final Map<String, Double> getPopularityKeywords(String sport, long starttime, 
            long endtime) {
        final Map<String, Double> pop = new HashMap();
        
        if (relations.containsKey(sport)) {
            relations.get(sport).stream().forEach(keyword -> 
                    pop.put(keyword, (double) getPopularity(keyword, starttime, endtime)));
        }
        
        return pop;
    }
    
    public final Map<String, Double> getPopularityKeywords(List<String> keywords) {
        final Map<String, Double> pop = new HashMap();
        
        keywords.stream().forEach(keyword -> pop.put(keyword, (double) getPopularity(keyword)));
        
        return pop;
    }
    
    public final Map<String, Double> getPopularityKeywords(List<String> keywords, 
            long starttime, long endtime) {
        final Map<String, Double> pop = new HashMap();
        
        keywords.stream().forEach(keyword -> 
                pop.put(keyword, (double) getPopularity(keyword, starttime, endtime)));
        
        return pop;
    }
    
    public final Map<String, Double> getPopularityKeywordsAsPercentage(String sport) {
        return getAsPercentage(getPopularityKeywords(sport));
    }
    
    public final Map<String, Double> getPopularityKeywordsAsPercentage(String sport, 
            long starttime, long endtime) {
        return getAsPercentage(getPopularityKeywords(sport, starttime, endtime));
    }
    
    public final Map<String, Double> getPopularityKeywordsAsPercentage(List<String> keywords) {
        return getAsPercentage(getPopularityKeywords(keywords));
    }
    
    public final Map<String, Double> getPopularityKeywordsAsPercentage(List<String> keywords, 
            long starttime, long endtime) {
        return getAsPercentage(getPopularityKeywords(keywords, starttime, endtime));
    }
    
    public final Map<String, Double> getPlayOffsPopMatch() {
        final List<String> matches = Arrays.asList(
                "De Graafschap - Go Ahead Eagles",
                "FC Volendam - FC Eindhoven", 
                "VVV Venlo - NAC Breda",
                "FC Emmen - Roda JC");
        
        final Map<String, Double> matchPop = new HashMap();
        matches.stream().forEach(match -> {
            final String[] teams = match.split(" - ");
            final double pop = getPopularity(teams[0]) + getPopularity(teams[1]);
            matchPop.put(match, pop);
        });
        
        return getAsPercentage(matchPop);
    }
    
    public final Map<String, Double> getPlayOffsPopMatch(long starttime, long endtime) {
        final List<String> matches = Arrays.asList(
                "De Graafschap - Go Ahead Eagles",
                "FC Volendam - FC Eindhoven", 
                "VVV Venlo - NAC Breda",
                "FC Emmen - Roda JC");
        
        final Map<String, Double> matchPop = new HashMap();
        matches.stream().forEach(match -> {
            final String[] teams = match.split(" - ");
            final double pop = getPopularity(teams[0], starttime, endtime) 
                    + getPopularity(teams[1], starttime, endtime);
            matchPop.put(match, pop);
        });
        
        return getAsPercentage(matchPop);
    }
    
    /**
     * 
     * @param startdate = tweets should be within startdate and enddate
     * @param enddate = read startdate
     * @param sport = which sport we are actually looking for at the moment
     * @param timeinterval = How many days the interval is.
     *                  Day =1 , week = 7, month = 30
     *                  note: a month is not always 30 days..
     * @return Map<Date, Double>. Date stands for the first day
     *                  Double stands for the amount of tweets.
     * @example : <01-01-15, 53>
     *            <01-02-15, 50>
     *            <01-03-15, 25>
     */
    public final Map<Date, Double> getSportsForDate(Date startdate,
            Date enddate, String sport, int timeinterval){
        final Map<Date, Double> count = new HashMap();
        final long dayInMilliseconds = 86400000L;
        final long starttime = startdate.getTime();
        final long endtime = enddate.getTime();
        
        long timeS = starttime;
        long timeE = starttime;
        long timeM = 0;
        
        if (timeinterval == 30) {
            timeM = getMonthTimeIncr(starttime);
            timeE += timeM;
        } else {
            timeE += dayInMilliseconds * timeinterval;
        }
        
        if (timeE > endtime) {
            timeE = endtime;
        }
        
        while (timeS < endtime) {
            count.put(new Date(timeS), (double) getPopularity(sport, starttime, endtime));
            
            if (timeinterval == 30) {
                timeM = getMonthTimeIncr(starttime);
                timeS += timeE;
                timeE += timeM;
            } else {
                timeS += dayInMilliseconds * timeinterval;
                timeE += dayInMilliseconds * timeinterval;
            }
            
            if (timeE > endtime) {
                timeE = endtime;
            }
        }
        
        return count;
    }
    
    
    // TEMP Setters and Getters
    
    public final void setDates(Date startdate, Date enddate) {
        this.startdate = startdate;
        this.enddate = enddate;
    }
    
    public final Date getStartDate() {
        return startdate;
    }
    
    public final Date getEndDate() {
        return enddate;
    }
    
    public final void setSelected(List<String> sports) {
        this.selected = sports;
    }
    
    public final List<String> getSelected() {
        return selected;
    }
    
    public final void setInterval(int interval) {
        this.interval = interval;
    }
    
    public final int getInterval() {
        return interval;
    }
    
    // PRIVATE Methods
    
    private long getMonthTimeIncr(long starttime) {
        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(starttime);
        
        Calendar calE = Calendar.getInstance();
        calE.clear();
        calE.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
        
        return calE.getTimeInMillis() - starttime;
    }
    
    private Stream<TweetEntity> getRelatedTweets(String sport, long starttime, 
            long endtime) {
        if (relations.containsKey(sport)) {
            return tweets.parallelStream()
                    .filter(te -> starttime <= te.getTime() && te.getTime() <= endtime)
                    .filter(te -> te.isRelatedTo(relations.get(sport)));
        }
        return tweets.parallelStream()
                .filter(te -> starttime <= te.getTime() && te.getTime() <= endtime)
                .filter(te -> te.isRelatedTo(sport));
    }
    
    private Stream<TweetEntity> getRelatedTweets(String sport) {
        if (relations.containsKey(sport)) {
            return tweets.parallelStream()
                    .filter(te -> te.isRelatedTo(relations.get(sport)));
        }
        return tweets.parallelStream()
                .filter(te -> te.isRelatedTo(sport));
    }
    
    private int getRetweetCount(Stream<TweetEntity> data) {
        return data.mapToInt(te -> te.getRetweets() + 1).sum();
    }
    
    private int getFavCount(Stream<TweetEntity> data) {
        return data.mapToInt(te -> te.getFavourites()).sum();
    }
    
    private Map<String, Double> getAsPercentage(Map<String, Double> data) {
        final Map<String, Double> perc = new HashMap();
        final double total = data.values().stream().mapToDouble(v -> v).sum();
        
        data.entrySet().stream().forEach(entry -> {
            perc.put(entry.getKey(), entry.getValue() * 100.0 / total);
        });
        
        return perc;
    }
    
    private void getTweets(ServerAccess sa) {
        final int part = 50000;
        int tries = 0;
        int limitlow = 0;
        int limithigh = part;
        if (limithigh > numberTweets) {
            limithigh = numberTweets;
        }
        
        while (limithigh <= numberTweets && limitlow < limithigh) {
            try {
                tweets.addAll(sa.getTweetsPartNL(limitlow, limithigh));
                limitlow += part;
                limithigh += part;
                if (limithigh > numberTweets) {
                    limithigh = numberTweets;
                }
                tries = 0;
            } catch (JSONException ex) {
                tries++;
                System.out.println("getTweets - " + ex);
            }
        }
    }
    
    // Searching
    
    private void runStoreThread(List<String> sports) {
        DBStore.getInstance().initIDs();
        DBStore.getInstance().setSports(sports);
        final Runnable r = () -> {
            final ServerAccess sa = new ServerAccess();
            while (true) {
                if (DBStore.getInstance().isEmpty()) {
                    if (DBStore.getInstance().isDone()) {
                        return;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    final String sport = DBStore.getInstance().getSport();
                    final Status status = DBStore.getInstance().getElem(sport);
                    if (status != null) {
                        if (status.getRetweetedStatus() != null) {
                            if (addTweet(sa, status.getRetweetedStatus(), sport)) {
                                if (!addUser(sa, status.getRetweetedStatus().getUser())) {
                                    System.out.println("User not saved: "
                                            + status.getRetweetedStatus().getUser());
                                }
                            } else {
                                System.out.println("Tweet not saved: " + status.getRetweetedStatus());
                            }
                        }
                        if (addTweet(sa, status, sport)) {
                            if (!addUser(sa, status.getUser())) {
                                System.out.println("User not saved: " + status.getUser());
                            }
                        } else {
                            System.out.println("Tweet not saved: " + status);
                        }
                    }
                }
            }
        };
        final Thread t = new Thread(r);
        t.start();
    }
    
    private void timeSearch(ServerAccess sa, List<String> sports) {
        Collections.shuffle(sports);
        
        final List<String> sports1 = new ArrayList<>();
        for (int i = 0; i < (int) Math.floor(sports.size() / 2); i++) {
            sports1.add(sports.get(i));
        }
        
        //Collections.reverse(sports1);
        final Runnable r1 = () -> timeSearchSports(1, sports1, sa, getAuth());
        
        
        final List<String> sports2 = new ArrayList<>();
        for (int i = (int) Math.floor(sports.size() / 2); i < sports.size(); i++) {
            sports2.add(sports.get(i));
        }
        //Collections.reverse(sports2);
        final Runnable r2 = () ->  timeSearchSports(2, sports2, sa, getAuth2());
        
        final Thread t1 = new Thread(r1);
        final Thread t2 = new Thread(r2);
        t1.start();
        t2.start();
        
        try {
            t1.join();
        } catch (InterruptedException ex) {
        }
        try {
            t2.join();
        } catch (InterruptedException ex) {
        }
        
        storeRest();
        DBStore.getInstance().setDone();
    }
    
    private boolean timeSearchSports(int n, final List<String> sports,
            final ServerAccess sa, final Configuration auth) {
        
        for (String sport : sports) {
               long resetTime = 0;
                long curTime = 0;
                long wait = 0;
                RetryQuery rq = new RetryQuery(-2, null);
                while (resetTime >= curTime 
                        || rq.getRetry() == -2 || rq.getQuery() != null) {
                    rq = timeTweets(n, rq.getQuery(), sport, auth);
                    resetTime = rq.getRetry();
                    curTime = new Date().getTime();
                    try {
                        storeRest();
                        if (curTime <= resetTime) {
                            wait = resetTime - curTime;
                            System.out.println("Sleeping "
                                    + (wait / 1000)
                                    + "s for n: " + n
                                    + ", " + sport);
                            Thread.sleep(wait + 1000);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Error sleeping - " + ex);
                    }
                }

                storeRest();
                if (resetTime == -1) {
                    return true;
                }
        }
        
        return true;
    }
    
    /**
     * Gets tweets for a given keyword and globally set time frame
     */
    private RetryQuery timeTweets(int n, Query lastQuery, String search, Configuration auth) {
        TwitterFactory tf = new TwitterFactory(auth);
        Twitter twitter = tf.getInstance();
        Query query = new Query(search);
        query.setLang("nl");
        query.count(1000);
        
        if (lastQuery != null) {
            query = lastQuery;
        }
        
        QueryResult result;
        
        if (Abort.getInstance().abort()) {
            return new RetryQuery(-1, query);
        }

        try {
            result = twitter.search(query);
            while (result.nextQuery() != null) {
                synchronized (searchLock) {
                    if (!searchTweets.containsKey(search)) {
                        searchTweets.put(search, new HashSet());
                    }
                    searchTweets.get(search).addAll(result.getTweets());
                }
                query = result.nextQuery();
                result = twitter.search(query);
            }

            synchronized (searchLock) {
                if (!searchTweets.containsKey(search)) {
                    searchTweets.put(search, new HashSet());
                }
                searchTweets.get(search).addAll(result.getTweets());
            }

        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te);
            System.out.println("\nRetry at n = " + n + ": " + 
                    (new Date(te.getRateLimitStatus()
                            .getResetTimeInSeconds() * 1000L)));
            return new RetryQuery(
                    te.getRateLimitStatus().getResetTimeInSeconds() * 1000L,
                    query);
        }
        return new RetryQuery(0, null);
    }
    
    private boolean addTweet(ServerAccess sa, Status status, String search) {
        try {
            final TweetEntity entity = new TweetEntity(status, search);
            return sa.addTweet(entity);
        } catch (Exception ex) {
            return false;
        }
    }
    
    private boolean addUser(ServerAccess sa, User user) {
        try {
            final UserEntity entity = new UserEntity(user);
            return sa.addUser(entity);
        } catch (Exception ex) {
            return false;
        }
    }
    
    private void storeRest() {
        synchronized (searchLock) {
            DBStore.getInstance().addData(searchTweets);
            searchTweets.clear();
        }
    }
    
    /** Gets configuration builder for authentication */
    private Configuration getAuth() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("n2g9XOjAr9p44yJwFjXUbeUa2");
        cb.setOAuthConsumerSecret("57FHkBBptp17yBGl1v853lldZO9Kh4osJnDQqQEcXd4d9C3xFA");
        cb.setOAuthAccessToken("113906448-2fx9njfJgzQrGdnRaGchI9GlZTzLMXrayEzFk2ju");
        cb.setOAuthAccessTokenSecret("FJOqMt7dtBp1yuW2VnQDfzksa7IS5h3IxxsJ1ixBGI1ny");
        
        return cb.build();
    }
    
    private Configuration getAuth2() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("fiQ7TIyf4mUHl8STmT6m9YVTQ");
        cb.setOAuthConsumerSecret("vpuJv3q1Z3PMuZfZoZu9BIF8nm6sK0jRcQfKpaCQyWZIJnHLcw");
        cb.setOAuthAccessToken("79107187-siXbdmIMXXcalZ043Pa7yfHXvUGXRnThoUB6p6EIg");
        cb.setOAuthAccessTokenSecret("8X4Yo8dOigSsuqD8udwoc1WmhRfoDmUoEpyIRbR4SwEEm");
        
        return cb.build();
    }
    
    private class RetryQuery {
        
        private final long retry;
        private final Query query;
        
        public RetryQuery(long retry, Query query) {
            this.retry = retry;
            this.query = query;
        }

        /**
         * @return the retry
         */
        public long getRetry() {
            return retry;
        }

        /**
         * @return the query
         */
        public Query getQuery() {
            return query;
        }
        
    }
    
}