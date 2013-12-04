package danaus;

/** 
 * An instance is thrown when a Butterfly flies into something it shouldn't. 
 */
@SuppressWarnings("serial")
public class ObstacleCollisionException extends RuntimeException {
    /** Constructor: an instance with message m. */
    public ObstacleCollisionException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message. */
    public ObstacleCollisionException() {
        super();
    }
}
