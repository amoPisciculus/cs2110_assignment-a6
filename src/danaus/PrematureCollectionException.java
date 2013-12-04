package danaus;
/**
 * Thrown when a Butterfly tries to collect a flower in learning mode.
 */
@SuppressWarnings("serial")
public class PrematureCollectionException extends RuntimeException {
    /** Constructor: an instance with message m*/
    public PrematureCollectionException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message */
    public PrematureCollectionException() {
        super();
    }
}
