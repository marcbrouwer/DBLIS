package dblis;

/**
 *
 * @author Brouwer M.R.
 */
public class ChartData {
    
    private final String name;
    private final int value;
    
    public ChartData(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public final String getName() {
        return name;
    }
    
    public final int getValue() {
        return value;
    }
    
}