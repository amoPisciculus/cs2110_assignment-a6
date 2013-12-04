package danaus;

/** 
 * Thrown when Wind.parseWind(String) is given an illegal argument.
 */
@SuppressWarnings("serial")
public class WindFormatException extends IllegalArgumentException {
    /** Constructor: an instance with message m. */
    public WindFormatException(String m) {
        super(m);
    }

    /** Constructor: an instance with no message. */
    public WindFormatException() {
        super();
    }
}
