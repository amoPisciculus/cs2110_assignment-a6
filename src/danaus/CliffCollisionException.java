package danaus;

/**
 * This exception is thrown when a butterfly attempts to fly into a cliff.
 */
@SuppressWarnings("serial")
public class CliffCollisionException extends ObstacleCollisionException {
	/** The power cost of colliding with a cliff. */
	public static final int CLIFF_COLLISION_POWER_COST = 5;
	/** The slow down of colliding with a cliff. */
	public static final int CLIFF_COLLISION_SLOW_DOWN = 0;
    
	/** Constructor: an instance with message m*/
    public CliffCollisionException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message */
    public CliffCollisionException() {
        super();
    }
}
