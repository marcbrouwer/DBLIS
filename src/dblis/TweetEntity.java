package dblis;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import twitter4j.Place;
import twitter4j.Status;

/**
 *
 * @author Brouwer M.R.
 */
public class TweetEntity {
    
    private long id;
    private long retweetid;
    private int retweets;
    private int favourites;
    private String text;
    private long creationTime;
    private String countryCode;
    private String language;
    private long userID;
    private String keywords;
    
    public TweetEntity(Status status, String keywords) 
            throws UnsupportedEncodingException {
        this(status, status.getPlace(), 
                status.getRetweetedStatus(), keywords);
        if (status.getLang().equals("nl")) {
            countryCode = "NL";
        }
    }
    
    private TweetEntity(Status status, Place place, Status retweet, 
            String keywords) throws UnsupportedEncodingException {
        String countrycode = "";
        if (place != null) {
            countrycode = place.getCountryCode();
        }
        long retweetStatusID = -1;
        if (retweet != null) {
            retweetStatusID = retweet.getId();
        }
        String lang = "";
        if (status.getLang() != null) {
            lang = status.getLang();
        }
        this.id = status.getId();
        this.retweetid = retweetStatusID;
        this.retweets = status.getRetweetCount();
        this.favourites = status.getFavoriteCount();
        this.text = new String(formatText(status.getText()).getBytes(), "UTF-8");
        this.creationTime = status.getCreatedAt().getTime();
        this.countryCode = countrycode;
        this.language = lang;
        this.userID = status.getUser().getId();
        this.keywords = keywords;
        if (status.getLang().equals("nl")) {
            countryCode = "NL";
        }
    }
    
    public TweetEntity(long id, long retweetid, int retweets, 
            int favourites, String text, long time, String country, 
            String language, long userId, String keywords) {
        this.id = id;
        this.retweetid = retweetid;
        this.retweets = retweets;
        this.favourites = favourites;
        this.text = formatText(text);
        this.creationTime = time;
        this.countryCode = country;
        this.language = language;
        this.userID = userId;
        this.keywords = keywords;
        if (language.equals("nl")) {
            countryCode = "NL";
        }
    }
    
    private String formatText(String text) {
        text = text.replace("\n", " ");
        text = text.replace("  ", " ");
        
        return text;
    }
    
    public final long getID() {
        return id;
    }
    
    public final long getRetweetID() {
        return retweetid;
    }
    
    public final int getRetweets() {
        return retweets;
    }
    
    public final int getFavourites() {
        return favourites;
    }
    
    public final String getText() {
        return text;
    }
    
    public final long getTime() {
        return creationTime;
    }
    
    public final String getCountry() {
        return countryCode;
    }
    
    public final String getLanguage() {
        return language;
    }
    
    public final long getUserID() {
        return userID;
    }
    
    public final String getKeywords() {
        return keywords;
    }
    
    public final TweetEntity getEntity() {
        return this;
    }
    
    @Override
    public String toString() {
        final String s = ";&;";
        return id + s + retweets + s + favourites + s + text + s + creationTime + s + 
                countryCode + s + userID + s + keywords;
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
        hash = 37 * hash + (int) (this.creationTime ^ (this.creationTime >>> 32));
        hash = 37 * hash + Objects.hashCode(this.countryCode);
        hash = 37 * hash + (int) (this.userID ^ (this.userID >>> 32));
        hash = 37 * hash + Objects.hashCode(this.keywords);
        return hash;
    }
    
    public final boolean isRelatedTo(List<String> words) {
        return words.parallelStream().anyMatch((word) -> (isRelatedTo(word)));
    }
    
    public final boolean isRelatedTo(String word) {
        word = word.toLowerCase();
        if (keywords.toLowerCase().equals(word)) {
            return true;
        }
        
        String lowerText = text.toLowerCase();
        return lowerText.contains(" " + word + " ")
                || lowerText.contains(" " + word + ".")
                || lowerText.contains(" " + word + "!")
                || lowerText.contains(" " + word + "?")
                
                || lowerText.contains("#" + word + " ")
                || lowerText.contains("#" + word + ".")
                || lowerText.contains("#" + word + "!")
                || lowerText.contains("#" + word + "?")
                
                || lowerText.contains("@" + word + " ")
                || lowerText.contains("@" + word + ".")
                || lowerText.contains("@" + word + "!")
                || lowerText.contains("@" + word + "?");
    }
    
    public Set<String> getHashtags() {
        final Set<String> hashtags = new HashSet();
        final Set<String> hashwords = new HashSet();
        final Set<String> words = new HashSet();
        
        words.addAll(Arrays.asList(text.toLowerCase().split(" ")));
        words.stream().filter(word -> word.startsWith("#"))
                .forEach(word -> hashwords.add(word));
        
        final Set<String> splits = new HashSet();
        hashwords.stream().forEach(word -> {
            splits.addAll(Arrays.asList(word.split("#")));
            
            splits.stream().filter(tag -> !tag.equals("") && !tag.equals(" "))
                    .forEach(tag -> hashtags.add(tag));
            
            splits.clear();
        });
        
        return hashtags;
    }
    
    public final boolean isInTimeFrame(long starttime, long endtime) {
        return starttime <= creationTime && creationTime <= endtime;
    }
    
}