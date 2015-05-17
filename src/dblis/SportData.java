package dblis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import twitter4j.JSONException;

/**
 *
 * @author Brouwer M.R.
 */
public class SportData {
    
    // Instance declaration 
    
    /**
     * BarChartSimpleData instance
     */
    private static final SportData instance = new SportData();

    /**
     * Don't let anyone else instantiate this class
     */
    private SportData() {
    }

    /**
     * Gets the BarChartSimpleData instance
     *
     * @return BarChartSimpleData instance
     */
    public synchronized static final SportData getInstance() {
        return instance;
    }
    
    // Variables
    
    /** Temporary country code to pass to chart title */
    private static String countryCode;
    
    /** countryCode => [{sport, popularity}] */
    private static final Map<String, List<ChartData>> retweetCounts = new HashMap();
    
    /** countryCode => [{sport, popularity}] */
    private static final Map<String, List<ChartData>> favCounts = new HashMap();
    
    /** countryCode => [sport => [{keyword, popularity}] ]*/
    private static final Map<String, Map<String, List<ChartData>>> keywordsCountRT = new HashMap();
    
    /** countryCode => area (# geo or radius) */
    @Deprecated
    private static final Map<String, Integer> areas = new HashMap();
    
    // Initialization
    
    /**
     * Initializes SportData
     */
    public void init() {
        SportData.getInstance().setCountryCode("NL");
        final ServerAccess sa = new ServerAccess();
        final List<String> countries = sa.getCountryCodes();
        final List<String> sports = 
                Arrays.asList("football", "hockey", "cycling", "tennis", "skating");//sa.getSportsGB();
        
        countries.stream().forEach(country -> {
            SportData.getInstance().addArea(country, sa.getArea(country));
            sports.stream().forEach(sport -> {
                try {
                    addRetweetCount(country, 
                            new ChartData(sport, 
                                    sa.getRelatedTweetsCountryCount(
                                            country, sport)
                            )
                    );
                    addKeywordsCountRT(country, sport, 
                            sa.getKeywordsPopularityRetweetCount(country, sport));
                } catch (JSONException ex) {
                    System.out.println("init - " + ex);
                }
            });
        });
    }
    
    // Private Methods
    
    /**
     * Adds list of popularity for some country to some map
     * 
     * @param chartdata map to add data to
     * @param country country code
     * @param list list with popularity
     */
    private void addChartDataCountry(Map<String, List<ChartData>> chartdata,
            String country, List<ChartData> list) {
        list.stream().forEach(elem -> addChartDataSport(chartdata, country, elem));
    }
    
    /**
     * Adds some sports popularity for some country to some map
     * 
     * @param chartdata map to add data to
     * @param country country code
     * @param sport sport with popularity
     */
    private void addChartDataSport(Map<String, List<ChartData>> chartdata, 
            String country, ChartData sport) {
        if (!chartdata.containsKey(country)) {
            chartdata.put(country, new ArrayList<>());
        }
        chartdata.get(country).add(sport);
    }
    
    /**
     * Gets popularity as percentage
     * 
     * @param map map with popularity
     * @return map with popularity as percentage
     */
    private Map<String, Double> getAsPercentage(Map<String, Double> map) {
        final Map<String, Double> perc = new HashMap();
        final double total = map.values().stream().mapToDouble(v -> v).sum();
        
        map.entrySet().stream().forEach(entry -> {
            perc.put(entry.getKey(), entry.getValue() * 100.0 / total);
        });
        
        return perc;
    }
    
    /**
     * Gets the popularity for a country depending on the search area
     * 
     * @param country country code
     * @param popularity popularity
     * @return popularity depending on search area
     */
    @Deprecated
    private double getPopularityInArea(String country, int popularity) {
        if (!areas.containsKey(country)) {
            return 0;
        }
        if (areas.get(country) <= 0 || popularity <= 0) {
            return 0;
        }
        return popularity / (double) areas.get(country);
    }
    
    /**
     * For some sport, get popularity (as percentage) of that sport in all 
     * countries
     * 
     * @param chartdata map to get popularity from
     * @param sport sport for which the popularity should be returned
     * @return map with countryCode =&gt; popularity of sport (as percentage)
     */
    private Map<String, Double> getPopularityInCountries(
            Map<String, List<ChartData>> chartdata, String sport) {
        final Map<String, Double> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            if (!pop.containsKey(entry.getKey())) {
                pop.put(entry.getKey(), 0.0);
            }
            pop.put(entry.getKey(), pop.get(entry.getKey()) + 
                    getPopularityOfSport(entry.getValue(), entry.getKey(), sport));
        });
        
        return getAsPercentage(pop);
    }
    
    /**
     * Returns the popularity of some sport
     * 
     * @param list list from which popularity is used
     * @param sport sport for which popularity is requested
     * @return popularity of sport
     */
    private double getPopularityOfSport(List<ChartData> list, String country, String sport) {
        int pop = 0;
        
        for (ChartData cd : list) {
            if (cd.getName().equals(sport)) {
                pop = cd.getValue();
                break;
            }
        }
        
        return pop;
    }
    
    /**
     * Gets combined popularity of sport for all countries
     * 
     * @param chartdata map to get popularity from
     * @return popularity of all sports as percentage, map sport =&gt; popularity
     */
    private Map<String, Double> getSportsByPopularity(
            Map<String, List<ChartData>> chartdata) {
        final Map<String, Double> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(sport -> {
                if (!pop.containsKey(sport.getName())) {
                    pop.put(sport.getName(), 0.0);
                }
                pop.put(sport.getName(), pop.get(sport.getName()) + sport.getValue());
            });
        });
        
        return getAsPercentage(pop);
    }
    
    // Public Methods
    
    /**
     * Adds an area for a given country
     * 
     * @param country country code
     * @param area area
     */
    @Deprecated
    public final void addArea(String country, int area) {
        areas.put(country, area);
    }
    
    /**
     * Adds favourite counts for a given country
     * 
     * @param country country code
     * @param list list of sports with favourite counts
     */
    public final void addFavCount(String country, List<ChartData> list) {
        addChartDataCountry(favCounts, country, list);
    }
    
    /**
     * Adds favourite count for a given country and sport
     * 
     * @param country country code
     * @param sport sport with favourite count
     */
    public final void addFavCount(String country, ChartData sport) {
        addChartDataSport(favCounts, country, sport);
    }
    
    /**
     * Adds retweet counts for a given country
     * 
     * @param country country code
     * @param list list of sports with retweet counts
     */
    public final void addRetweetCount(String country, List<ChartData> list) {
        addChartDataCountry(retweetCounts, country, list);
    }
    
    /**
     * Adds retweet count for a given country and sport
     * 
     * @param country country code
     * @param sport sport with retweet count
     */
    public final void addRetweetCount(String country, ChartData sport) {
        addChartDataSport(retweetCounts, country, sport);
    }
    
    public final void addKeywordsCountRT(String country, String sport, 
            List<ChartData> chartdata) {
        if (!keywordsCountRT.containsKey(country)) {
            keywordsCountRT.put(country, new HashMap());
        }
        keywordsCountRT.get(country).put(sport, chartdata);
    }
    
    /**
     * Gets country code
     * 
     * @return country code
     */
    public final String getCountryCode() {
        return countryCode;
    }
    
    /** 
     * For some sport, get popularity (as percentage) of that sport in all 
     * countries using favourite counts
     * 
     * @param sport sport
     * @return map with country =&gt; popularity
     */
    public final Map<String, Double> getCountryPopFavourites(String sport) {
        return getPopularityInCountries(favCounts, sport);
    }
    
    /** 
     * For some sport, get popularity (as percentage) of that sport in all 
     * countries using retweet counts
     * 
     * @param sport sport
     * @return map with country =&gt; popularity
     */
    public final Map<String, Double> getCountryPopRetweets(String sport) {
        return getPopularityInCountries(retweetCounts, sport);
    }
    
    /**
     * Gets map of all favourite counts
     * 
     * @return map of all favourite counts
     */
    public final Map<String, List<ChartData>> getFavouriteCountPopularity() {
        return favCounts;
    }
    
    /**
     * Gets map of all retweet counts
     * 
     * @return map of all retweet counts
     */
    public final Map<String, List<ChartData>> getRetweetCountPopularity() {
        return retweetCounts;
    }
    
    public final Map<String, Double> getKeywordCountRT(String country, String sport) {
        final Map<String, Double> pop = new HashMap();
        
        keywordsCountRT.get(country).get(sport).stream()
                .forEach(data -> pop.put(data.getName(), (double) data.getValue()));
        
        return getAsPercentage(pop);
    }
    
    /**
     * Gets combined popularity (as percentage) of sport for all countries 
     * using favourite counts
     * 
     * @return map with country =&gt; popularity
     */
    public final Map<String, Double> getSportPopFavourites() {
        return getSportsByPopularity(favCounts);
    }
    
    /**
     * Gets combined popularity (as percentage) of sport for all countries 
     * using retweet counts
     * 
     * @return map with country =&gt; popularity
     */
    public final Map<String, Double> getSportPopRetweets() {
        return getSportsByPopularity(retweetCounts);
    }
    
    /**
     * Sets country code
     * 
     * @param countryCode country code
     */
    public final void setCountryCode(String countryCode) {
        SportData.countryCode = countryCode;
    }
    
}