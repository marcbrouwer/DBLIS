package dblis;

import java.util.ArrayList;
import java.util.List;
import twitter4j.JSONArray;
import twitter4j.JSONException;

/**
 * WARNING!! Simply copied code from DBLIS for temporary use.
 *
 * @author Brouwer M.R.
 */
public class Data {
    
    private static List<String> getCountryCodes(ServerAccess sa) {
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
    
    private static List<String> getSportsGB(ServerAccess sa) {
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
    
    public static final void fillSportData() {
        SportData.getInstance().setCountryCode("NL");
        final ServerAccess sa = new ServerAccess();
        final List<String> countries = getCountryCodes(sa);
        final List<String> sports = getSportsGB(sa);
        
        countries.stream().forEach(country -> {
            sports.stream().forEach(sport -> {
                try {
                    SportData.getInstance().addRetweetCount(country, 
                            new ChartData(sport, 
                                    sa.getRelatedTweetsCountryCount(
                                            country, sport)
                            )
                    );
                } catch (JSONException ex) {
                    System.out.println("fillSportData - " + ex);
                }
            });
        });
    }
    
}