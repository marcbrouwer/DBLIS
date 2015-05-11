package dblis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static Map<String, List<ChartData>> retweetCounts = new HashMap();
    
    /** countryCode => [{sport, popularity}] */
    private static Map<String, List<ChartData>> favCounts = new HashMap();
    
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
    private Map<String, Double> getAsPercentage(Map<String, Integer> map) {
        final Map<String, Double> perc = new HashMap();
        final double total = map.values().stream().mapToInt(v -> v).sum();
        
        map.entrySet().stream().forEach(entry -> {
            perc.put(entry.getKey(), entry.getValue() * 100.0 / total);
        });
        
        return perc;
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
        final Map<String, Integer> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            if (!pop.containsKey(entry.getKey())) {
                pop.put(entry.getKey(), 0);
            }
            pop.put(entry.getKey(), pop.get(entry.getKey()) + 
                    getPopularityOfSport(entry.getValue(), sport));
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
    private int getPopularityOfSport(List<ChartData> list, String sport) {
        int pop = 0;
        
        for (ChartData cd : list) {
            if (cd.getName().equals(sport)) {
                pop = cd.getValue();
                break;
            }
        }
        
        return pop;
    }
    
    private Map<String, Double> getSportsByPopularity(
            Map<String, List<ChartData>> chartdata) {
        final Map<String, Integer> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(sport -> {
                if (!pop.containsKey(sport.getName())) {
                    pop.put(sport.getName(), 0);
                }
                pop.put(sport.getName(), pop.get(sport.getName()) + sport.getValue());
            });
        });
        
        return getAsPercentage(pop);
    }
    
    // Public Methods
    
    public final void addFavCount(String country, List<ChartData> list) {
        addChartDataCountry(favCounts, country, list);
    }
    
    public final void addFavCount(String country, ChartData sport) {
        addChartDataSport(favCounts, country, sport);
    }
    
    public final void addRetweetCount(String country, List<ChartData> list) {
        addChartDataCountry(retweetCounts, country, list);
    }
    
    public final void addRetweetCount(String country, ChartData sport) {
        addChartDataSport(retweetCounts, country, sport);
    }
    
    public final String getCountryCode() {
        return countryCode;
    }
    
    public final Map<String, List<ChartData>> getRetweetCountPopularity() {
        return retweetCounts;
    }
    
    public final Map<String, List<ChartData>> getFavouriteCountPopularity() {
        return favCounts;
    }
    
    public final Map<String, Double> getSportPopRetweets() {
        return getSportsByPopularity(retweetCounts);
    }
    
    public final Map<String, Double> getCountryPopRetweets(String sport) {
        return getPopularityInCountries(retweetCounts, sport);
    }
    
    public final Map<String, Double> getSportPopFavourites() {
        return getSportsByPopularity(favCounts);
    }
    
    public final Map<String, Double> getCountryPopFavourites(String sport) {
        return getPopularityInCountries(favCounts, sport);
    }
    
    public final void setCountryCode(String countryCode) {
        SportData.countryCode = countryCode;
    }
    
    public final void setRetweetCounts(Map<String, List<ChartData>> chartdata) {
        SportData.retweetCounts = chartdata;
    }
    
    public final void setFavouriteCounts(Map<String, List<ChartData>> chartdata) {
        SportData.favCounts = chartdata;
    }
    
}