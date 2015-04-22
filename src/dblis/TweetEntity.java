package dblis;

import java.util.Objects;
import twitter4j.Place;
import twitter4j.Status;

/**
 *
 * @author Brouwer M.R.
 */
public class TweetEntity {
    
    private final String dataSeperator;
    private final long id;
    private final int retweets;
    private final int favourites;
    private final String text;
    private final long time;
    private final String country;
    private final long userId;
    private final String keywords;

    public TweetEntity(String dataSeperator, Status status, String keywords) {
        this(dataSeperator, status, status.getPlace(), keywords);
    }
    
    private TweetEntity(String dataSeperator, Status status, Place place, 
            String keywords) {
        String countrycode = "";
        if (place != null) {
            countrycode = place.getCountryCode();
        }
        this.dataSeperator = dataSeperator;
        this.id = status.getId();
        this.retweets = status.getRetweetCount();
        this.favourites = status.getFavoriteCount();
        this.text = formatText(status.getText());
        this.time = status.getCreatedAt().getTime();
        this.country = countrycode;
        this.userId = status.getUser().getId();
        this.keywords = keywords;
    }
    
    public TweetEntity(String dataSeperator, long id, int retweets, int favourites, 
            String text, long time, String country, long userId, String keywords) {
        this.dataSeperator = dataSeperator;
        this.id = id;
        this.retweets = retweets;
        this.favourites = favourites;
        this.text = formatText(text);
        this.time = time;
        this.country = country;
        this.userId = userId;
        this.keywords = keywords;
    }
    
    private String formatText(String text) {
        text = text.replace("\n", " ");
        text = text.replace("  ", " ");
        
        return text;
    }
    
    public final long getID() {
        return id;
    }
    
    public final long getRetweets() {
        return retweets;
    }
    
    public final long getFavourites() {
        return favourites;
    }
    
    public final String getText() {
        return text;
    }
    
    public final long getTime() {
        return time;
    }
    
    public final String getCountry() {
        return country;
    }
    
    public final long getUserID() {
        return userId;
    }
    
    public final String getKeywords() {
        return keywords;
    }
    
    @Override
    public String toString() {
        final String s = dataSeperator;
        return id + s + retweets + s + favourites + s + text + s + time + s + 
                country + s + userId + s + keywords;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TweetEntity)) {
            return false;
        }
        
        TweetEntity entity = (TweetEntity) obj;
        return this.hashCode() == entity.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 37 * hash + this.retweets;
        hash = 37 * hash + this.favourites;
        hash = 37 * hash + Objects.hashCode(this.text);
        hash = 37 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 37 * hash + Objects.hashCode(this.country);
        hash = 37 * hash + (int) (this.userId ^ (this.userId >>> 32));
        hash = 37 * hash + Objects.hashCode(this.keywords);
        return hash;
    }
}