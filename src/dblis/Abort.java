package dblis;

/**
 *
 * @author Brouwer M.R.
 */
public class Abort {
    
    // Instance declaration 
    
    /**
     * Abort instance
     */
    private static final Abort instance = new Abort();

    /**
     * Don't let anyone else instantiate this class
     */
    private Abort() {
    }

    /**
     * Gets the Abort instance
     *
     * @return Abort instance
     */
    public synchronized static final Abort getInstance() {
        return instance;
    }
    
    // Variables
    
    private static boolean abort = false;
    
    // Methods
    
    public final boolean abort() {
        return abort;
    }
    
    public final void setAbort(boolean abort) {
        Abort.abort = abort;
    }
    
}