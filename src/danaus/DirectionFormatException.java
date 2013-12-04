package danaus;

/**
 * Thrown when a direction is parsed from a string but the string's format
 * is invalid.
 */
@SuppressWarnings("serial")
public class DirectionFormatException extends IllegalArgumentException {
    /** Constructor: an instance with message m. */
    public DirectionFormatException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message. */
    public DirectionFormatException() {
        super();
    }
}
