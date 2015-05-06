package dblis;

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
    
    private static String countryCode;
    private static Map<String, List<ChartData>> chartdata;
    
    // Methods
    
    public final String getCountryCode() {
        return countryCode;
    }
    
    public final Map<String, List<ChartData>> getChartData() {
        return chartdata;
    }
    
    public final Map<String, Integer> getSportsByPopularity() {
        final Map<String, Integer> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            entry.getValue().stream().forEach(sport -> {
                if (!pop.containsKey(sport.getName())) {
                    pop.put(sport.getName(), 0);
                }
                pop.put(sport.getName(), pop.get(sport.getName()) + sport.getValue());
            });
        });
        
        return pop;
    }
    
    public final Map<String, Integer> getPopularityInCountries(String sport) {
        final Map<String, Integer> pop = new HashMap();
        
        chartdata.entrySet().stream().forEach(entry -> {
            if (!pop.containsKey(entry.getKey())) {
                pop.put(entry.getKey(), 0);
            }
            pop.put(entry.getKey(), pop.get(entry.getKey()) + 
                    getPopularityOfSport(entry.getValue(), sport));
        });
        
        return pop;
    }
    
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
    
    public final void setCountryCode(String countryCode) {
        SportData.countryCode = countryCode;
    }
    
    public final void setChartData(Map<String, List<ChartData>> chartdata) {
        SportData.chartdata = chartdata;
    }
    
}