package dblis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import twitter4j.Status;

/**
 *
 * @author Brouwer M.R.
 */
public class DBStore {
    
    // Instance declaration 
    
    /**
     * DBStore instance
     */
    private static final DBStore instance = new DBStore();

    /**
     * Don't let anyone else instantiate this class
     */
    private DBStore() {}

    /**
     * Gets the DBStore instance
     *
     * @return DBStore instance
     */
    public synchronized static final DBStore getInstance() {
        return instance;
    }
    
    // Variables
    
    private final Set<Long> ids = new HashSet();
    private final Map<String, Set<Status>> tweets = new ConcurrentHashMap();
    private List<String> sports = new ArrayList<>();
    private int s = 0;
    private boolean done = false;
        
    // Methods
    
    public final void initIDs() {
        final ServerAccess sa = new ServerAccess();
        ids.addAll(sa.getTweetsIDs());
    }
    
    public final void setSports(List<String> sports) {
        this.sports = sports;
    }
    
    public final void setDone() {
        this.done = true;
    }
    
    public final void addData(Map<String, Set<Status>> data) {
        data.entrySet().stream().forEach(elem -> {
            synchronized (this) {
                if (!tweets.containsKey(elem.getKey())) {
                    tweets.put(elem.getKey(), new HashSet());
                }
                elem.getValue().stream()
                        .filter(status -> !ids.contains(status.getId()))
                        .forEach(status -> {
                            tweets.get(elem.getKey()).add(status);
                            ids.add(status.getId());
                        });
            }
        });
    }
    
    public final String getSport() {
        final List<String> keys = new ArrayList(tweets.keySet());
        s = (s + 1) % keys.size();
        final String sport = keys.get(s);
        return sport;
    }
    
    public final Status getElem(String sport) {
        Status status = null;
        
        synchronized (this) {
            if (tweets.containsKey(sport)) {
                if (!tweets.get(sport).isEmpty()) {
                    status = tweets.get(sport).stream().findAny().get();
                    tweets.get(sport).remove(status);
                } else {
                    tweets.remove(sport);
                }
            }
        }
        
        return status;
    }
    
    public final boolean isEmpty() {
        if (tweets.isEmpty()) {
            return true;
        }
        
        boolean hasElem = false;
        synchronized (this) {
            for (String key : tweets.keySet()) {
                if (!tweets.get(key).isEmpty()) {
                    hasElem = true;
                    break;
                }
            }
        }
        
        return !hasElem;
    }
    
    public final boolean isDone() {
        return done;
    }
    
}