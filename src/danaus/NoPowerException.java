package danaus;

/** 
 * Thrown when a Butterfly runs out of power. Try to land to get your power
 * back. 
 */
@SuppressWarnings("serial")
public class NoPowerException extends RuntimeException {
    /** Constructor: an instance with message m*/
    public NoPowerException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message */
    public NoPowerException() {
        super();
    }
}
