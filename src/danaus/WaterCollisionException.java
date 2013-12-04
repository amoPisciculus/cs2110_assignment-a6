package danaus;

/** 
 * Thrown when a butterfly attempts to fly over water. 
 */
@SuppressWarnings("serial")
public class WaterCollisionException extends ObstacleCollisionException {
	/** The power cost of colliding with water. */
	public static final int WATER_COLLISION_POWER_COST = 10;
	/** The slow down of colliding with water. */
	public static final int WATER_COLLISION_SLOW_DOWN= 0;
	
	/** Constructor: an instance with message m*/
    public WaterCollisionException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message */
    public WaterCollisionException() {
        super();
    }
}
