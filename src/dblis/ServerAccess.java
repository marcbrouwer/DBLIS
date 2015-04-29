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
    public Object getResponseObject(String method, Map<String, Object> params, Class c) {
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
    
    public boolean addTweet(TweetEntity entity) {
        Map<String, Object> params = new HashMap();
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
        
        final Object obj = getResponseObject("addTweet", params, Object.class);
        if (!obj.equals("true") && !obj.equals("false")) {
            System.out.println(obj);
        }
        return obj.equals("true");
    }
    
    public boolean addUser(UserEntity entity) {
        Map<String, Object> params = new HashMap();
        params.put("userid", "[" + entity.getID() + "]");
        params.put("name", entity.getName());
        params.put("followers", entity.getFollowers());
        params.put("favourites", entity.getFavourites());
        params.put("friends", entity.getFriends());
        
        final Object obj = getResponseObject("addUser", params, Object.class);
        if (!obj.equals("true") && !obj.equals("false")) {
            System.out.println(obj);
        }
        return obj.equals("true");
    }
    
}