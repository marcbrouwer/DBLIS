package dblis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
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
    
    // sport => [ alternative => type ]
    private final Map<String, Map<String, String>> relations = new ConcurrentHashMap();
    private final List<String> playoffMatches = Arrays.asList(
                "De Graafschap ;&; Go Ahead Eagles",
                "FC Volendam ;&; FC Eindhoven", 
                "VVV Venlo ;&; NAC Breda",
                "FC Emmen ;&; Roda JC");
    
    // Search variables
    
    private final Map<String, Set<Status>> searchTweets = new ConcurrentHashMap();
    private final Object searchLock = new Object();
    
    // Temp storage
    
    private Date startdate = new Date();
    private Date enddate = new Date();
    private int lineInterval = 1;
    private int interval = 1;
    private int year = 0;
    private boolean yearSelected = false;
    private List<String> selected = new ArrayList<>();
    private boolean footballSeperate = false;
    
    // PUBLIC Methods
    
    public final void init() {
        final ServerAccess sa = new ServerAccess();
        getTweetsJSONFile();
        //numberTweets = sa.getTweetsCountNL();
        //getTweets(sa);
        sports.stream().forEach(sport -> {
            if (!relations.containsKey(sport)) {
                relations.put(sport, new HashMap());
            }
            final Map<String, String> alts = sa.getAlternatives(sport);
            relations.get(sport).putAll(alts);
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
        relations.values().stream().forEach(list -> keywords.addAll(list.keySet()));
        runStoreThread(keywords);
        timeSearch(keywords);
    }
    
    public final List<String> getSports() {
        return sports;
    }
    
    public final List<String> getKeywords() {
        final List<String> list = new ArrayList<>();
        
        sports.stream().forEach(sport -> list.addAll(getKeywords(sport)));
        
        return list;
    }
    
    public final List<String> getKeywords(String sport) {
        sport = sport.toLowerCase();
        if (!relations.containsKey(sport)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(relations.get(sport).keySet());
    }
    
    public final List<String> getTeams(String sport) {
        return getTypes(sport, "T");
    }
    
    public final List<String> getEvents(String sport) {
        return getTypes(sport, "E");
    }
    
    public final List<String> getPlayers(String sport) {
        return getTypes(sport, "P");
    }
    
    public final List<String> getOther(String sport) {
        return getTypes(sport, "O");
    }
    
    public final Map<String, Double> getMostCommonHashtags(List<String> keywords) {
        final Map<String, Double> hashtags = new HashMap();
        
        getRelatedTweets(keywords).forEach(te -> {
            te.getHashtags().stream().forEach(hashtag -> {
                if (!hashtags.containsKey(hashtag)) {
                    hashtags.put(hashtag, 0.0);
                }
                hashtags.put(hashtag, hashtags.get(hashtag) + 1);
            });
        });
        
        keywords.stream().forEach(keyword -> hashtags.remove(keyword));
        
        return hashtags;
    }
    
    public final Map<String, Double> getMostCommonHashtags(List<String> keywords,
            long starttime, long endtime) {
        final Map<String, Double> hashtags = new ConcurrentHashMap();
        
        getRelatedTweets(keywords, starttime, endtime).forEach(te -> {
            te.getHashtags().stream().forEach(hashtag -> {
                if (!hashtags.containsKey(hashtag)) {
                    hashtags.put(hashtag, 0.0);
                }
                hashtags.put(hashtag, hashtags.get(hashtag) + 1);
            });
        });
        
        keywords.stream().forEach(keyword -> hashtags.remove(keyword));
        
        return hashtags;
    }
    
    public final Map<String, Double> getMostCommonHashtags(List<String> keywords,
            int year) {
        final Map<String, Double> hashtags = new ConcurrentHashMap();
        final long starttime = (new Date(getYearTimeStart(year))).getTime();
        final long endtime = (new Date(getYearTimeEnd(year))).getTime();
        
        getRelatedTweets(keywords, starttime, endtime).forEach(te -> {
            te.getHashtags().stream().forEach(hashtag -> {
                if (!hashtags.containsKey(hashtag)) {
                    hashtags.put(hashtag, 0.0);
                }
                hashtags.put(hashtag, hashtags.get(hashtag) + 1);
            });
        });
        
        keywords.stream().forEach(keyword -> hashtags.remove(keyword));
        
        return hashtags;
    }
    
    public final Map<String, Double> getMostCommonHashtagsAsPercentage(
            List<String> keywords) {
        return getAsPercentage(getMostCommonHashtags(keywords));
    }
    
    public final Map<String, Double> getMostCommonHashtagsAsPercentage(
            List<String> keywords, long starttime, long endtime) {
        return getAsPercentage(getMostCommonHashtags(keywords));
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
    
    public final int getPopularity(String sport, int year) {
        final long starttime = (new Date(getYearTimeStart(year))).getTime();
        final long endtime = (new Date(getYearTimeEnd(year))).getTime();
        
        return getRetweetCount(sport, starttime, endtime) 
                + getFavCount(sport, starttime, endtime);
    }
    
    public final int getNumberUsers(String keyword) {
        return (int) getRelatedTweets(keyword)
                .map(TweetEntity::getUserID).distinct().count();
    }
    
    public final int getNumberUsers(String keyword, long starttime, long endtime) {
        return (int) getRelatedTweets(keyword, starttime, endtime)
                .map(TweetEntity::getUserID).distinct().count();
    }
    
    public final int getNumberUsers(String keyword, int year) {
        final long starttime = (new Date(getYearTimeStart(year))).getTime();
        final long endtime = (new Date(getYearTimeEnd(year))).getTime();
        
        return (int) getRelatedTweets(keyword, starttime, endtime)
                .map(TweetEntity::getUserID).distinct().count();
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
        final Map<String, Double> pop = new ConcurrentHashMap();
        
        if (relations.containsKey(sport)) {
            getKeywords(sport).stream().forEach(keyword -> 
                    pop.put(keyword, (double) getPopularity(keyword)));
        }
        
        return pop;
    }
    
    public final Map<String, Double> getPopularityKeywords(String sport, long starttime, 
            long endtime) {
        final Map<String, Double> pop = new ConcurrentHashMap();
        
        if (relations.containsKey(sport)) {
            getKeywords(sport).stream().forEach(keyword -> 
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
    
    public final Map<String, Double> getPopularityMatches(List<String> matches) {
        final Map<String, Double> matchPop = new HashMap();
        matches.stream().forEach(match -> {
            final String[] teams = match.split(" ;&; ");
            final double pop = getPopularity(teams[0]) + getPopularity(teams[1]);
            matchPop.put(match, pop);
        });
        
        return matchPop;
    }
    
    public final Map<String, Double> getPopularityMatches(List<String> matches, 
            long starttime, long endtime) {
        final Map<String, Double> matchPop = new HashMap();
        matches.stream().forEach(match -> {
            final String[] teams = match.split(" ;&; ");
            final double pop = getPopularity(teams[0]) + getPopularity(teams[1], 
                    starttime, endtime);
            matchPop.put(match, pop);
        });
        
        return matchPop;
    }
    
    public final Map<String, Double> getPopularityMatchesAsPercentage(
            List<String> matches) {
        return getAsPercentage(getPopularityMatches(matches));
    }
    
    public final Map<String, Double> getPopularityMatchesAsPercentage(
            List<String> matches, long starttime, long endtime) {
        return getAsPercentage(getPopularityMatches(matches, starttime, endtime));
    }
    
    public final Map<String, Double> getPlayOffsPopMatch() {
        return getPopularityMatchesAsPercentage(playoffMatches);
    }
    
    public final Map<String, Double> getPlayOffsPopMatch(long starttime, long endtime) {
        return getPopularityMatchesAsPercentage(playoffMatches, starttime, endtime);
    }
    
    public final int getNumberOfTweets(List<String> keywords) {
        return (int) tweets.parallelStream()
                .filter(te -> te.isRelatedTo(keywords)).count();
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
        final long starttime = getDayTimestamps(startdate)[0];
        final long endtime = getDayTimestamps(enddate)[1];
        
        long timeS = starttime;
        long timeE = starttime;
        
        if (timeinterval == 30) {
            timeE = getMonthTimeEnd(timeS);
        } else {
            timeE += dayInMilliseconds * timeinterval;
        }
        
        if (timeE > endtime) {
            timeE = endtime;
        }
        
        while (timeS < endtime) {
            count.put(new Date(timeS), (double) getPopularity(sport, timeS, timeE));
            
            if (timeinterval == 30) {
                if (timeE >= endtime) {
                    break;
                }
            }
            
            if (timeinterval == 30) {
                timeS = getMonthTimeStart(timeE + 1);
                timeE = getMonthTimeEnd(timeS);
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
    
    public final Map<Date, Double> getSportsForYear(int year, String sport,
            int timeinterval) {
        final Date startdate = new Date(getYearTimeStart(year));
        final Date enddate = new Date(getYearTimeEnd(year));
        
        return getSportsForDate(startdate, enddate, sport, timeinterval);
    }
    
    public final long[] getDayTimestamps(Date date) {
        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(date.getTime());
        
        Calendar calB = Calendar.getInstance();
        calB.clear();
        calB.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH), 
                calS.get(Calendar.DATE), 0, 0, 0);
        
        Calendar calE = Calendar.getInstance();
        calE.clear();
        calE.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH), 
                calS.get(Calendar.DATE), 23, 59, 59);
        
        return new long[]{calB.getTimeInMillis(), calE.getTimeInMillis()};
    }
    
    public final long[] getLineToPieTimestamps(Date date) {
        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(date.getTime());
        
        Calendar calB = Calendar.getInstance();
        calB.clear();
        calB.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH), 
                calS.get(Calendar.DATE), 0, 0, 0);
        
        Calendar calE = Calendar.getInstance();
        calE.clear();
        calE.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH), 
                calS.get(Calendar.DATE), 23, 59, 59);
        
        if (lineInterval == 7) {
            calE.setTimeInMillis(calE.getTimeInMillis() + 604800000L);
        } else if (lineInterval == 30) {
            calE.setTimeInMillis(getMonthTimeEnd(date.getTime()));
        }
        
        return new long[]{calB.getTimeInMillis(), calE.getTimeInMillis()};
    }
    
    public final int getNumberUsersInterestedIn(List<String> sports, 
            long starttime, long endtime) {
        if (sports.isEmpty()) {
            return 0;
        }
        
        final Set<Long> userids = new HashSet();
        userids.addAll(getUsersInterestedIn(sports.get(0), starttime, endtime));
        
        final Set<Long> newIds = new HashSet();
        final Set<Long> join = new HashSet();
        for (int i = 1; i < sports.size(); i++) {
            if (userids.isEmpty()) {
                return 0;
            }
            newIds.clear();
            join.clear();
            
            newIds.addAll(getUsersInterestedIn(sports.get(i), starttime, endtime));
            join.addAll(userids.stream()
                    .filter(id -> newIds.contains(id))
                    .map(id -> id).collect(Collectors.toCollection(HashSet::new)));
            
            userids.clear();
            userids.addAll(join);
        }
        
        return userids.size();
    }
    
    public final int getNumberUsersInterestedIn(List<String> sports, int year) {
        final long starttime = (new Date(getYearTimeStart(year))).getTime();
        final long endtime = (new Date(getYearTimeEnd(year))).getTime();
        
        return getNumberUsersInterestedIn(sports, starttime, endtime);
    }
    
    public final int getNumberUsersInterestedIn(String sport, 
            long starttime, long endtime) {
        return getUsersInterestedIn(sport, starttime, endtime).size();
    }
    
    public final int getNumberUsersInterestedIn(String sport, int year) {
        final long starttime = (new Date(getYearTimeStart(year))).getTime();
        final long endtime = (new Date(getYearTimeEnd(year))).getTime();
        
        return getNumberUsersInterestedIn(sport, starttime, endtime);
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
        return new ArrayList<>(selected);
    }
    
    public final void setInterval(int interval) {
        this.interval = interval;
    }
    
    public final int getInterval() {
        return interval;
    }
    
    public final void setLineInterval(int interval) {
        this.lineInterval = interval;
    }
    
    public final int getLineInterval() {
        return lineInterval;
    }
    
    public final void setFootballSeperate(boolean seperate) {
        this.footballSeperate = seperate;
    }
    
    public final boolean footballSeperate() {
        return footballSeperate;
    }
    
    public final void setYear(int year) {
        this.year = year;
    }
    
    public final int getYear() {
        return year;
    }
    
    public final void setYearSelected(boolean selected) {
        yearSelected = selected;
    }
    
    public final boolean getYearSelected() {
        return yearSelected;
    }
    
    // PRIVATE Methods
    
    private long getMonthTimeStart(long starttime) {
        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(starttime);
        
        Calendar calE = Calendar.getInstance();
        calE.clear();
        calE.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH), 1, 0, 0, 0);
        
        return calE.getTimeInMillis();
    }
    
    private long getMonthTimeEnd(long starttime) {
        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(starttime);
        
        Calendar calE = Calendar.getInstance();
        calE.clear();
        calE.set(calS.get(Calendar.YEAR), calS.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
        
        return calE.getTimeInMillis() - 1;
    }
    
    private long getYearTimeStart(int year) {
        Calendar calS = Calendar.getInstance();
        calS.clear();
        calS.set(year, 0, 1, 0, 0, 0);
        
        return calS.getTimeInMillis();
    }
    
    private long getYearTimeEnd(int year) {
        Calendar calS = Calendar.getInstance();
        calS.clear();
        calS.set(year + 1, 0, 1, 0, 0, 0);
        
        return calS.getTimeInMillis() - 1;
    }
    
    private Stream<TweetEntity> getRelatedTweets(String sport) {
        final String sportL = sport.toLowerCase();
        if (relations.containsKey(sportL)) {
            return tweets.parallelStream()
                    .filter(te -> te.isRelatedTo(getKeywords(sportL)));
        }
        return tweets.parallelStream()
                .filter(te -> te.isRelatedTo(sportL));
    }
    
    private Stream<TweetEntity> getRelatedTweets(String sport, long starttime, 
            long endtime) {
        final String sportL = sport.toLowerCase();
        if (relations.containsKey(sportL)) {
            return tweets.parallelStream()
                    .filter(te -> te.isInTimeFrame(starttime, endtime))
                    .filter(te -> te.isRelatedTo(getKeywords(sportL)));
        }
        return tweets.parallelStream()
                .filter(te -> te.isInTimeFrame(starttime, endtime))
                .filter(te -> te.isRelatedTo(sportL));
    }
    
    private Stream<TweetEntity> getRelatedTweets(List<String> keywords) {
        return tweets.parallelStream().filter(te -> te.isRelatedTo(keywords));
    }
    
    private Stream<TweetEntity> getRelatedTweets(List<String> keywords, 
            long starttime, long endtime) {
        return tweets.parallelStream()
                .filter(te -> te.isInTimeFrame(starttime, endtime))
                .filter(te -> te.isRelatedTo(keywords));
    }
    
    private Set<Long> getUsersInterestedIn(String sport, long starttime, 
            long endtime) {
        return getRelatedTweets(sport, starttime, endtime)
                .map(TweetEntity::getUserID)
                .collect(Collectors.toCollection(HashSet::new));
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
        
        while (limithigh <= numberTweets && limitlow < limithigh && tries < 3) {
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
    
    private void getTweetsJSONFile() {
        String jsonString = "";
        JSONArray json = new JSONArray();
        int n = 0;
        
        try {
            File file = new File(getWorkingDirectory() 
                    + "Tweets_" + String.format("%02d", n++) + ".json");
            boolean add = false;
            JSONArray jsonPart;
            
            while (file.exists()) {
                jsonString = "";
                final BufferedReader reader = new BufferedReader(
                        new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[")) {
                        add = true;
                    }
                    if (add) {
                        jsonString += line;
                    }
                    if (line.endsWith("]")) {
                        add = false;
                    }
                }
                
                jsonPart = new JSONArray(jsonString);
                for (int i = 0; i < jsonPart.length(); i++) {
                    json.put(jsonPart.get(i));
                }
                
                file = new File(getWorkingDirectory()
                        + "Tweets_" + String.format("%02d", n++) + ".json");
            }
            
        } catch (IOException | JSONException ex) {
            System.out.println("getTweetsJSONFile - " + ex);
        }
        
        JSONObject obj;
        TweetEntity te;
        for (int i = 0; i < json.length(); i++) {
            try {
                obj = json.getJSONObject(i);
                if (obj.getLong("retweetid") != -1) {
                    continue;
                }
                te = new TweetEntity(
                        obj.getLong("id"),
                        obj.getLong("retweetid"),
                        obj.getInt("retweets"),
                        obj.getInt("favourites"),
                        obj.getString("text"),
                        obj.getLong("creationTime"),
                        obj.getString("countryCode"),
                        obj.getString("language"),
                        obj.getLong("userID"),
                        obj.getString("keywords"));
                /*if (tweets.contains(te)) {
                    System.out.println("YES");
                }*/
                tweets.add(te);
            } catch (JSONException ex) {
                System.out.println("getTweetsNL - " + ex);
            }
        }
        
        numberTweets = tweets.size();
    }
    
    /**
     * Gets current working directory of executing process
     * 
     * @return directory as String
     */
    private String getWorkingDirectory() {
        try {
            // get working directory as File
            String path = SportData2.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().getPath();
            File folder = new File(path);
            folder = folder.getParentFile().getParentFile(); // 2x .getParentFile() for debug, 1x for normal

            // directory as String
            return folder.getPath() + "/"; // /dist/ for debug, / for normal
        } catch (URISyntaxException ex) {
            return "./";
        }
    }
    
    private List<String> getTypes(String sport, String type) {
        sport = sport.toLowerCase();
        if (!relations.containsKey(sport)) {
            return new ArrayList<>();
        }
        final List<String> types = relations.get(sport).entrySet().stream()
                .filter(alt -> alt.getValue().equals(type)).map(Entry::getKey)
                .collect(Collectors.toList());
        return types;
    }
    
    // Searching
    
    private void runStoreThread(List<String> sports) {
        DBStore.getInstance().initIDs();
        DBStore.getInstance().setSports(sports);
        final Runnable r = () -> {
            final ServerAccess sa = new ServerAccess();
            while (true) {
                if (Abort.getInstance().abort()) {
                    Abort.getInstance().setAbort(false);
                    return;
                }
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
    
    private void timeSearch(List<String> sports) {
        Collections.shuffle(sports);
        
        final List<String> sports1 = new ArrayList<>();
        for (int i = 0; i < (int) Math.floor(sports.size() / 2); i++) {
            sports1.add(sports.get(i));
        }
        
        //Collections.reverse(sports1);
        final Runnable r1 = () -> timeSearchSports(1, sports1, getAuth());
        
        final List<String> sports2 = new ArrayList<>();
        for (int i = (int) Math.floor(sports.size() / 2); i < sports.size(); i++) {
            sports2.add(sports.get(i));
        }
        //Collections.reverse(sports2);
        final Runnable r2 = () ->  timeSearchSports(2, sports2, getAuth2());
        
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
            final Configuration auth) {
        
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
                    if (resetTime == -1 || Abort.getInstance().abort()) {
                        return true;
                    }
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
            if (resetTime == -1 || Abort.getInstance().abort()) {
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
                if (Abort.getInstance().abort()) {
                    return new RetryQuery(-1, query);
                }
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
            try {
                System.out.println("Failed to search tweets: " + te);
                System.out.println("\nRetry at n = " + n + ": " + 
                        (new Date(te.getRateLimitStatus()
                                .getResetTimeInSeconds() * 1000L)));
                return new RetryQuery(
                        te.getRateLimitStatus().getResetTimeInSeconds() * 1000L,
                        query);
            } catch (NullPointerException ex) {
                return new RetryQuery(0, query);
            }
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