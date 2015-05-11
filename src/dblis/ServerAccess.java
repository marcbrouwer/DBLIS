package dblis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class ServerAccess {

    //Convert an inputstream to a string
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            //Log.e("PHP Client","Error : "+e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e1) {
                //Log.e("PHP Client","Error : "+e1.getMessage());
            }
        }
        return sb.toString();
    }

    //Get the response form the server as an object
    private Object getResponseObject(String method, Map<String, Object> params) {
        try {
            //Create a HTTP Client
            HttpClient httpclient = new DefaultHttpClient();

            //Create and object to Post values to the server
            //The url is specified in the Constants class to increase modifiability
            HttpPost httppost = new HttpPost("http://www.externalhost.nl/ws/"
                    + "dblis/webservice.php?method=" + method);
            
            JSONObject holder = new JSONObject(params);
            
            //convert parameters into JSON object
            //JSONObject holder = getJsonObjectFromMap(params);

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(holder.toString());

            //sets the post request as the resulting string
            httppost.setEntity(se);
            //sets a request header so the page receving the request
            //will know what to do with it
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            //Handles what is returned from the page 
            ResponseHandler responseHandler = new BasicResponseHandler();
            return httpclient.execute(httppost, responseHandler);
        } catch (Exception e) {
            System.out.println("PHP Client - Error in http connection " +e.toString());
            return null;
        }
    }
    
    public final boolean addTweet(TweetEntity entity) {
        final Map<String, Object> params = new HashMap();
        params.put("tweetid", "[" + entity.getID() + "]");
        params.put("retweetid", "[" + entity.getRetweetID() + "]");
        params.put("retweets", entity.getRetweets());
        params.put("favourites", entity.getFavourites());
        params.put("text", entity.getText());
        params.put("creationtime", entity.getTime());
        params.put("countrycode", entity.getCountry());
        params.put("language", entity.getLanguage());
        params.put("userid", "[" + entity.getUserID() + "]");
        params.put("keywords", entity.getKeywords());
        
        final Object obj = getResponseObject("addTweet", params);
        if (!obj.equals("true") && !obj.equals("false")) {
            System.out.println(obj);
        }
        return obj.equals("true");
    }
    
    public final boolean addUser(UserEntity entity) {
        final Map<String, Object> params = new HashMap();
        params.put("userid", "[" + entity.getID() + "]");
        params.put("name", entity.getName());
        params.put("followers", entity.getFollowers());
        params.put("favourites", entity.getFavourites());
        params.put("friends", entity.getFriends());
        
        final Object obj = getResponseObject("addUser", params);
        if (!obj.equals("true") && !obj.equals("false")) {
            System.out.println(obj);
        }
        return obj.equals("true");
    }
    
    public final JSONArray getSports() throws JSONException {
        return new JSONArray(getResponseObject("getSports", null).toString());
    }
    
    public final JSONArray getSportsGB() throws JSONException {
        return new JSONArray(getResponseObject("getSportsGB", null).toString());
    }
    
    @Deprecated
    public final JSONArray getCommonSports(String countryCode, int top) 
            throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("countrycode", countryCode);
        params.put("number", top);
        return new JSONArray(
                getResponseObject("getCommonSports", params).toString());
    }
    
    @Deprecated
    public final JSONArray getPopularSportCountries(String sport, int top) 
            throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("sport", sport);
        params.put("number", top);
        final String response = 
                getResponseObject("getPopularSportCountries", params).toString();
        return new JSONArray(response);
    }
    
    public final JSONArray getGeolocations() throws JSONException {
        return new JSONArray(getResponseObject("getGeolocations", null).toString());
    }
    
    public final JSONArray getMostCommonSports(String countryCode) 
            throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("countrycode", countryCode);
        final String response =
                getResponseObject("getMostCommonSports", params).toString();
        return new JSONArray(response);
    }
    
    public final JSONArray getMostCommonCountries(String sport, int top) 
            throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("sport", sport);
        params.put("number", top);
        final String response = 
                getResponseObject("getMostCommonCountries", params).toString();
        return new JSONArray(response);
    }
   
    public final JSONArray getAlternatives(String sport) throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("sport", sport);
        final String response =
                getResponseObject("getAlts", params).toString();
        return new JSONArray(response);
    }
    
    public final JSONArray getCountryCodes() throws JSONException {
        return new JSONArray(getResponseObject("getCountryCodes", null).toString());
    }
    
    public final int getRelatedTweetsCountryCount(String country, String sport) throws JSONException {
        final Map<String, Object> params = new HashMap();
        params.put("countrycode", country);
        params.put("sport", sport);
        final String response = 
                getResponseObject("getRelatedTweetsCountryCount", params).toString();
        final JSONArray json = new JSONArray(response);
        if (json.getJSONObject(0).isNull("sum")) {
            return 0;
        }
        return json.getJSONObject(0).getInt("sum");
    }
    
}