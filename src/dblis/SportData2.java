package dblis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
    
    // PUBLIC Methods
    
    public final void init() {
        final ServerAccess sa = new ServerAccess();
        numberTweets = sa.getTweetsCountNL();
        tweets.addAll(sa.getTweetsNL(numberTweets));
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
        return tweets.size() / numberTweets;
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
    
}