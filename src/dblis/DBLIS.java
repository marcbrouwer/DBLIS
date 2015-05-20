package dblis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.IDs;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Paging;
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
    private final int hours = 1;
    private final int blocks = hours * 4;
    
    // Search settings (CAN BE CHANGED)
    private String geocode = "geocode:51.444,5.491,500km";
    private String search = "cricket";
    private final long starttime = 1431010800;//now - blocks * 900; //1429452000;
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
    
    // Debug search
    private final List<Boolean> firstSearch = new ArrayList<>();
    
    // Time search
    private long searchTime1 = starttime;
    private long searchTime2 = starttime;
    
    /** Constructor */
    public DBLIS() {
        this.tweets = new ConcurrentHashMap();
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
        
        //System.out.println("Start time: " + (new Date(starttime * 1000)) + "\n");
        
        //final List<String> countryCodes = sa.getCountryCodes();
        //final List<String> sportsGB = sa.getSportsGB();
        final List<String> sports = getSports(sa);
        //final JSONArray geolocations = getGeolocations(sa);
        final double[][] geos = getGeolocationsArray(sa);
        
        /*if (useStream) {
            int max = 100;
            geoList.stream().forEach(loc -> {
                sports.stream().forEach(sport -> {
                    try {
                        twitterStream(sa, sport, loc, max);
                    } catch (IOException ex) {
                        System.out.println("Twitter Stream error - " + ex);
                    }
                });
            });
            return;
        }*/
        
        // debug JSONArray's
        /*JSONArray mostCommon = getCommonSports(sa, "NL", 5);
        JSONArray popular = getPopularSportCountries(sa, "tennis", 5);
        JSONArray mostCommonSports = getMostCommonSports(sa, "NL");
        JSONArray mostCommonCountries = getMostCommonCountries(sa, "football", sportsGB.size());
        JSONArray alternatives = getAlternatives(sa, "tennis");
        */
        // EXCEL OUTPUT
        //toExcel(sa, countryCodes, sportsGB);
        
        /*final List<ChartData> chartdata = sorted.get("NL");
        Chart3D pie = new Chart3D("NL", chartdata, "Pie");
        pie.view();*/
        
        /*BarChartSimpleData.setCountryCode("NL");
        BarChartSimpleData.setChartData(chartdata);
        BarChartSimple bar = new BarChartSimple();
        bar.view();*/
        
        /*SportData.getInstance().init();
        PieChartFX pie = new PieChartFX();
        pie.run();
        
        return;*/
        
        /*SportData.getInstance().initPlayOff(1432080000000L, 1432130883000L);
        PlayOffsPieChart pie = new PlayOffsPieChart();
        pie.run();
        return;*/
        
        /*SportData.getInstance().initPlayOff();
        final Map<Date, Double> sportForDate = 
                SportData.getInstance()
                        .getSportsForDate(new Date(1430431200000L), new Date(), 
                                "tennis", 1);
        final Map<Date, Double> sportForDateM = 
                SportData.getInstance()
                        .getSportsForDate(new Date(1430431200000L), new Date(), 
                                "tennis", 30);
        final Double total = sportForDate.values().stream().mapToDouble(d -> d).sum();
        final Double totalM = sportForDateM.values().stream().mapToDouble(d -> d).sum();
        */
        
        // SEARCHING
        
        runStoreThread(sports);
        //userSearch(sa, getAuth2(), "FCBarcelona");
        //twitterStream2(sa, sports);
        timeSearch(sa, geos, sports);
        
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
    
    private void toExcel(ServerAccess sa, List<String> countryCodes, 
            List<String> sportsGB) {
        final Map<String, JSONArray> commonSports = new HashMap();
        countryCodes.stream().forEach(code -> {
            commonSports.put(code, getMostCommonSports(sa, code));
        });
        
        Map<String, List<ChartData>> sorted = sortPopularity(commonSports, sportsGB);
        Object[][] popExcel = popularityToExcelFormat(sorted, sportsGB, countryCodes);
        File popularFile = new File(getWorkingDirectory() + "popular.xls");
        //Excel.writeToExcel(popularFile, popExcel);
        try {
            Excel.pieChart(popularFile, popExcel);
        } catch (IOException ex) {
            
        }
    }
    
    private void userSearch(ServerAccess sa, Configuration auth, String user) {
        Twitter twitter = new TwitterFactory(auth).getInstance();

        int pageno = 1;
        Set<Status> statuses = new HashSet();

        while (true) {
            try {
                int size = statuses.size();
                Paging page = new Paging(pageno++, 100);
                statuses.addAll(twitter.getUserTimeline(user, page));
                if (statuses.size() == size) {
                    break;
                }
            } catch (TwitterException te) {
                try {
                    System.out.println("\nTimeline, Retry at: "
                            + (new Date(te.getRateLimitStatus()
                                    .getResetTimeInSeconds() * 1000L)));
                    Thread.sleep((te.getRateLimitStatus().getSecondsUntilReset() + 1) * 1000L);
                } catch (Exception ex) {
                }
            }
        }
        
        pageno = 1;
        IDs ids;
        User u;
        Set<Long> userIDs = new HashSet();
        for (Status t : statuses) {
            while (true) {
                try {
                    ids = twitter.getRetweeterIds(t.getId(), 100, pageno++);
                    for (long id : ids.getIDs()) {
                        while (true) {
                            try {
                                u = twitter.showUser(id);
                                if (u.getLang().equals("nl")) {
                                    userIDs.add(id);
                                }
                                break;
                            } catch (TwitterException te) {
                                if (!te.exceededRateLimitation()) {
                                    break;
                                }
                                try {
                                    System.out.println("\nStatuses user, Retry at: "
                                            + (new Date(te.getRateLimitStatus()
                                                    .getResetTimeInSeconds() * 1000L)));
                                    Thread.sleep((te.getRateLimitStatus().getSecondsUntilReset() + 1) * 1000L);
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }
                    break;
                } catch (TwitterException te) {
                    if (!te.exceededRateLimitation()) {
                        break;
                    }
                    try {
                        System.out.println("\nStatuses, Retry at: "
                            + (new Date(te.getRateLimitStatus()
                                    .getResetTimeInSeconds() * 1000L)));
                        Thread.sleep((te.getRateLimitStatus().getSecondsUntilReset() + 1) * 1000L);
                    } catch (Exception ex) {
                    }
                }
            }
        }
        
        pageno = 1;
        final Map<Long, Set<Status>> userTweets = new HashMap();

        for (long userid : userIDs) {
            statuses = new HashSet();
            
            while (true) {
                try {
                    int size = statuses.size();
                    Paging page = new Paging(pageno++, 100);
                    statuses.addAll(twitter.getUserTimeline(user, page));
                    if (statuses.size() == size) {
                        break;
                    }
                } catch (TwitterException te) {
                    try {
                        System.out.println("\nLang Users, Retry at: "
                            + (new Date(te.getRateLimitStatus()
                                    .getResetTimeInSeconds() * 1000L))); 
                       Thread.sleep((te.getRateLimitStatus().getSecondsUntilReset() + 1) * 1000L);
                    } catch (Exception ex) {
                    }
                }
            }
            
            statuses.stream().forEach(status -> {
                if (status.getText().contains(user)) {
                    if (!userTweets.containsKey(userid)) {
                        userTweets.put(userid, new HashSet());
                    }
                    userTweets.get(userid).add(status);
                }
            });
        }
        
        synchronized (this) {
            if (!tweets.containsKey(user)) {
                tweets.put(user, new HashSet());
            }
            tweets.get(user).addAll(statuses);
        }
        
        storeRest();
        
        System.out.println(user + ": " + statuses.size());
    }
    
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
    
    private void timeSearch(ServerAccess sa, double[][] geos, List<String> sports) {
        /*sports = Arrays.asList("FC Eindhoven", "FC Volendam", "VVV Venlo", 
            "NAC Breda", "FC Emmen", "Roda JC", "Go Ahead Eagles", "De Graafschap");*/
        
        final long starttime = 1399986000; //13-5-2014 15:00:00
        final long endtime = 1431522000; //13-5-2015 15:00:00
        final long hourdif = 3600;
        final long daydif = 86400;
        final long weekdif = 7*daydif;
        
        final String firstGeo1 = "geocode:52.0813,4.76814,40km";
        final String firstSport1 = "alberto contador";
        final List<String> sports1 = new ArrayList<>();
        boolean start1 = true;
        for (int i = 0; i < (int) Math.floor(sports.size() / 2); i++) {
            if (sports.get(i).equals("depay")) {
                start1 = true;
            }
            if (start1) {
                sports1.add(sports.get(i));
            }
        }
        //Collections.reverse(sports1);
        searchTime1 = starttime;
        firstSearch.add(false);
        final Runnable r1 = () -> {
            //while (searchTime1 <= endtime) {
                //for (int i = 0; i < (int) Math.floor(geos.length / 2); i++) {
                    timeSearchSports(searchTime1, 1, geos[0], firstGeo1, 
                            firstSport1, sports1, sa, getAuth());
                //}
            //    searchTime1 += weekdif;
            //}
        };
        
        final String firstGeo2 = "geocode:50.8899,5.87614,15km";
        final String firstSport2 = "alberto contador";
        final List<String> sports2 = new ArrayList<>();
        boolean start2 = true;
        for (int i = (int) Math.floor(sports.size() / 2); i < sports.size(); i++) {
            if (sports.get(i).equals("")) {
                start2 = true;
            }
            if (start2) {
                sports2.add(sports.get(i));
            }
        }
        //Collections.reverse(sports2);
        searchTime2 = starttime;
        firstSearch.add(false);
        final Runnable r2 = () -> {
            //while (searchTime2 <= endtime) {
                //for (int i = (int) Math.floor(geos.length / 2); i < geos.length; i++) {
                    timeSearchSports(searchTime2, 2, geos[0], firstGeo2, 
                            firstSport2, sports2, sa, getAuth2());
                //}
            //    searchTime2 += weekdif;
            //}
        };
        
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
    
    private boolean timeSearchSports(long searchTime, int n, final double[] geo, 
            final String firstGeo, final String firstSport, 
            final List<String> sports, final ServerAccess sa, 
            final Configuration auth) {
        
        for (String sport : sports) {
                /*final String loc = geo;
                if (firstSearch.get(n - 1)) {
                    if (loc.equals(firstGeo) && sport.equals(firstSport)) {
                        firstSearch.set(n - 1, false);
                    } else {
                        continue;
                    }
                }*/

                // getTweets and wait if limit reached
                long resetTime = 0;
                long curTime = 0;
                long wait = 0;
                RetryQuery rq = new RetryQuery(-2, null);
                while (resetTime >= curTime 
                        || rq.getRetry() == -2 || rq.getQuery() != null) {
                    rq = timeTweets(n, rq.getQuery(), sport, geo, auth);
                    resetTime = rq.getRetry();
                    curTime = new Date().getTime();
                    try {
                        storeRest();
                        if (curTime <= resetTime) {
                            wait = resetTime - curTime;
                            System.out.println("Sleeping "
                                    + (wait / 1000)
                                    + "s for n: " + n
                                    + ", " + searchTime
                                    + ", " + Arrays.toString(geo)
                                    + ", " + sport);
                            Thread.sleep(wait + 1000);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Error sleeping - " + ex);
                    }
                }

                if (resetTime == -1) {
                    storeRest();
                    return true;
                }
        }
        
        return true;
    }
    
    /**
     * Gets tweets for a given keyword and globally set time frame
     */
    private RetryQuery timeTweets(int n, Query lastQuery, String search, double[] geocode, 
            Configuration auth) {
        while (!DBStore.getInstance().isEmpty()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        }
        TwitterFactory tf = new TwitterFactory(auth);
        Twitter twitter = tf.getInstance();
        Query query = new Query(search);
        /*query.setGeoCode(new twitter4j.GeoLocation(geocode[0], geocode[1]), 
         geocode[2], Query.Unit.km);*/
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
                synchronized (this) {
                    if (!tweets.containsKey(search)) {
                        tweets.put(search, new HashSet());
                    }
                    tweets.get(search).addAll(result.getTweets());
                }
                query = result.nextQuery();
                result = twitter.search(query);
            }

            synchronized (this) {
                if (!tweets.containsKey(search)) {
                    tweets.put(search, new HashSet());
                }
                tweets.get(search).addAll(result.getTweets());
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
    
    private void twitterStream2(ServerAccess sa, List<String> sports) {
        final TwitterStream twitterStream = 
                new TwitterStreamFactory(getAuth()).getInstance();
        
        final StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                try {
                    sa.addTweet(new TweetEntity(dataSeperator, status, search));
                } catch (UnsupportedEncodingException ex) {
                }
                try {
                    sa.addUser(new UserEntity(dataSeperator, status.getUser()));
                } catch (UnsupportedEncodingException ex) {
                }
                
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
        FilterQuery filterquery = new FilterQuery();
        filterquery.count(5000);
        filterquery.locations(getGeolocationsArray(sa));
        filterquery.track(sports.toArray(new String[sports.size()]));
        twitterStream.filter(filterquery);
        twitterStream.sample();
    }
    
    private void search(ServerAccess sa, List<String> geoList, List<String> sportsGB) {
        final String firstGeo1 = "geocode:53,6.56667,5km";
        final String firstSport1 = "football";
        final String firstAlt1 = "football";
        firstSearch.add(true);
        final Runnable r1 = () -> {
            for (int i = 0; i < (int) Math.floor(geoList.size() / 2); i++) {
                searchSports(1, geoList.get(i), firstGeo1, firstSport1, firstAlt1,
                        sportsGB, sa, getAuth());
            }
        };
        
        final String firstGeo2 = "geocode:47.05,8.3,5km";
        final String firstSport2 = "football";
        final String firstAlt2 = "weltmeisterschaft";
        firstSearch.add(true);
        final Runnable r2 = () -> {
            for (int i = (int) Math.floor(geoList.size() / 2); i < geoList.size(); i++) {
                searchSports(2, geoList.get(i), firstGeo2, firstSport2, firstAlt2,
                        sportsGB, sa, getAuth2());
            }
        };
        
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
    }
    
    private boolean searchSports(int n, final String geo, final String firstGeo, 
            final String firstSport, final String firstAlt, 
            final List<String> sportsGB, final ServerAccess sa, 
            final Configuration auth) {
        
        sportsGB.stream().forEach(sport -> {
            final String[] alts = getAlternativesArray(sa, sport);

            // for each alternative word
            for (String altSport : alts) {

                final String loc = geo;
                if (firstSearch.get(n - 1)) {
                    if (loc.equals(firstGeo)
                            && sport.equals(firstSport)
                            && altSport.equals(firstAlt)) {
                        firstSearch.set(n - 1, false);
                    } else {
                        continue;
                    }
                }

                // getTweets and wait if limit reached
                long resetTime = 0;
                long curTime = 0;
                long wait = 0;
                while (resetTime >= curTime) {
                    resetTime = getTweets(altSport, geo, auth);
                    curTime = new Date().getTime();
                    try {
                        if (curTime <= resetTime) {
                            storeRest();

                            wait = resetTime - curTime;
                            System.out.println("Sleeping "
                                    + (wait / 1000)
                                    + "s for n: " + n
                                    + ", " + geo
                                    + ", " + sport
                                    + ", " + altSport);
                            Thread.sleep(wait + 1000);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Error sleeping - " + ex);
                    }
                }

                if (resetTime == -1) {
                    storeRest();
                    return;
                }

            }

        });
        
        return true;
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
    
    /**
     * Gets tweets for a given keyword and globally set time frame
     * 
     * @param search keyword to search
     */
    private long getTweets(String search, String geocode, Configuration auth) {
        TwitterFactory tf = new TwitterFactory(auth);
        Twitter twitter = tf.getInstance();
        
        try {
            Query query = new Query(search + " " + geocode);
            query.setSinceId(starttime);
            QueryResult result;
            
            for (int i = 0; i < blocks; i++) {
                if (Abort.getInstance().abort()) {
                    return -1;
                }
                
                result = twitter.search(query);
                
                synchronized (this) {
                    if (!tweets.containsKey(search)) {
                        tweets.put(search, new HashSet());
                    }
                    tweets.get(search).addAll(result.getTweets());
                }
                
                //updateCommon(result.getTweets());
                //searched.add(search.toLowerCase());
                query.setSinceId(starttime + i * blocks15);
            }
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te);
            System.out.println("\nRetry at: " + 
                    (new Date(te.getRateLimitStatus()
                            .getResetTimeInSeconds() * 1000L)));
            return te.getRateLimitStatus().getSecondsUntilReset() * 1000L;
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
                try {
                    sa.addTweet(new TweetEntity(dataSeperator, status, search));
                } catch (UnsupportedEncodingException ex) {
                }
                try {
                    sa.addUser(new UserEntity(dataSeperator, status.getUser()));
                } catch (UnsupportedEncodingException ex) {
                }
                
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
                .forEach(entry -> getTweets(entry.getKey(), geocode, getAuth()));
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
                        try {
                            tweetEntities.add(
                                    new TweetEntity(dataSeperator, status,
                                            entry.getKey()));
                            userEntities.add(
                                    new UserEntity(dataSeperator, status.getUser()));
                        } catch (UnsupportedEncodingException ex) {
                            
                        }
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
    
    private void storeRest() {
        synchronized (this) {
            DBStore.getInstance().addData(tweets);
            tweets.clear();
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
    
    /**
     * Coverts map [countryCode =&gt; [sport =&gt; number]] to 
     * map [countryCode =&gt; sorted list {sport, number}]
     * 
     * @param map input map
     * @return sorted output map
     */
    private Map<String, List<ChartData>> sortPopularity(
            Map<String, JSONArray> map, List<String> sportsGB) {
        final Map<String, List<ChartData>> rtn = new HashMap();
        final Comparator comp = new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());
            }
        };
        
        map.entrySet().stream().forEach(entry -> {
            try {
                final List<ChartData> list = new ArrayList<>();
                final JSONObject json = entry.getValue().getJSONObject(0);
                sportsGB.stream().forEach(sport -> {
                    try {
                        list.add(new ChartData(sport, json.getInt(sport)));
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
    
    private Object[][] popularityToExcelFormat(Map<String, List<ChartData>> map,
            List<String> sportsGB, List<String> countryCodes) {
        final Object[][] data = 
                new Object[(sportsGB.size() + 1) * countryCodes.size()][3];
        
        int row = 0;
        int col = 0;
        List<ChartData> list;
        for (String code : countryCodes) {
            data[row][col] = code;
            list = map.get(code);
            for (int i = 0; i < list.size(); i++) {
                data[row][1] = list.get(i).getName();
                data[row][2] = list.get(i).getValue();
                row++;
            }
            row++;
        }
        
        return data;
    }
    
    private String toGeocode(float latitude, float longtitude, int radius) {
        return "geocode:" + latitude + "," + longtitude + "," + radius + "km";
    }
    
    private String toGeocode(String latitude, String longtitude, String radius) {
        return "geocode:" + latitude + "," + longtitude + "," + radius + "km";
    }
    
    private boolean addTweet(ServerAccess sa, Status status, String search) {
        try {
            final TweetEntity entity = new TweetEntity(dataSeperator, status, search);
            return sa.addTweet(entity);
        } catch (Exception ex) {
            return false;
        }
    }
    
    private boolean addUser(ServerAccess sa, User user) {
        try {
            final UserEntity entity = new UserEntity(dataSeperator, user);
            return sa.addUser(entity);
        } catch (Exception ex) {
            return false;
        }
    }
    
    private List<String> getSports(ServerAccess sa) {
        final List<String> list = new ArrayList<>();
        try {
            final JSONArray json = sa.getSports();
            for (int i = 0; i < json.length(); i++) {
                list.add(json.getJSONObject(i).getString("sport"));
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
                System.out.println("DBLIS - getGeolocationsList - " + ex);
            }
        }
        
        return list;
    }
    
    private double[][] getGeolocationsArray(ServerAccess sa) {
        final JSONArray json = getGeolocations(sa);
        final double[][] array = new double[json.length()][3];
        
        JSONObject obj;
        for (int i = 0; i < json.length(); i++) {
            try {
                obj = json.getJSONObject(i);
                array[i] = new double[]{
                    Double.parseDouble(obj.getString("latitude")),
                    Double.parseDouble(obj.getString("longtitude")),
                    Double.parseDouble(obj.getString("radius"))
                };
            } catch (JSONException | NumberFormatException ex) {
                System.out.println("DBLIS - getGeolocationsArray - " + ex);
            }
        }
        
        return array;
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
                list.add(json.getJSONObject(i).getString("sport"));
            } catch (Exception ex) {
                System.out.println("DBLIS - getAlternativesArray - " + ex);
            }
        }
        
        return list.toArray(new String[list.size()]);
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
    
    private void convertToDbCsv(int column, int id) {
        final File file = new File("D:/Documents/2IOC0/data.csv");
        
        final List<String[]> list = new ArrayList<>();
        try {
            try (BufferedReader bufRdr = new BufferedReader(new FileReader(file))) {
                String line;
                String[] row;
                while ((line = bufRdr.readLine()) != null) {
                    row = new String[6];
                    for (int i = 0; i < row.length; i++) {
                        if (i == 0) {
                            row[i] = String.valueOf(id);
                        } else if (i == column) {
                            row[i] = new String(line.getBytes(), "UTF-8");
                        } else {
                            row[i] = "";
                        }
                    }
                    list.add(row);
                    id++;
                }
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                list.stream().forEach(row -> {
                    String line = row[0];
                    for (int i = 1; i < row.length; i++) {
                        line += "," + row[i];
                    }
                    writer.println(line);
                });
            }
        } catch (IOException ex) {
            System.out.println("DBLIS - convertToDbCsv() - error converting " + ex);
        }
    }
    
}