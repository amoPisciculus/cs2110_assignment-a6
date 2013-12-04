package danaus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An instance represents one unit of wind, which has a non-negative
 * intensity and a cardinal direction. 
 */
public class Wind implements Comparable<Wind> {
	/** A coefficient used to calculate the power costs associated with wind. 
	 * @see danaus.Map#updateCosts(Position, Speed) */
	public static final int WIND_POWER_COEFFICIENT = 1;
	/** A coefficient used to calculate the slow down associated with wind. 
	 * @see danaus.Map#updateCosts(Position, Speed) */
	public static final int WIND_SLOW_DOWN_COEFFICIENT = 1;

	/** The non-negative intensity of the wind. */
	public final int intensity;
	/** The cardinal direction of the wind. */
	public final Direction direction;
	
	/** 
     * Constructor: an instance with intensity in and direction d.
	 * if in < 0, use 0 instead for in.
	 * 
	 * @param in An intensity. Negative intensities are rounded to zero.
	 * @param d A cardinal direction. 
     */
	public Wind(int in, Direction d) {
		intensity= Math.max(0, in);
		direction= d;
	}
	
	/** Constructor: copy constructor. */
	public Wind(Wind other) {
		intensity = other.intensity;
		direction = other.direction;
	}
	
	/** 
     * Parse s into a wind object and return the object.
	 * Precondition: s has one of the following forms:
	 *       30 N, N 30, 30N, N30.
	 * where 30 can be replaced by any nonnegative integer and N can
	 * be replaced by any of the directions N, NE, E, SE, S, SW, W, NW.
	 * 
	 * Throw a WindFormatException if s is not well-formed. 
     */
	public static Wind parseWind(String s) {		
		Debugger.NULL_CHECK(s, "null rawWind in parseWind()");
		
		/* Regular expressions for the intensity and direction allow the wind
		 * strings to be very flexible. */
		Pattern intensityPattern = Pattern.compile("-?\\p{Digit}++");
		Matcher intensityMatcher = intensityPattern.matcher(s);
		Pattern directionPattern = Pattern.compile("[neswNESW]++");
		Matcher directionMatcher = directionPattern.matcher(s);
		
		/* If the regular expressions are not found within the wind string, the
		 * wind string is considered invalid. */
		if (!intensityMatcher.find() || !directionMatcher.find()) {
			throw new WindFormatException();
		}
						
		/* Try and parse intensity and direction strings, but be aware that 
		 * both strings could still potentially be invalid. If any parsing
		 * fails, throw an exception. */
		try {
			String directionString= directionMatcher.group();
			String intensityString= intensityMatcher.group();

			int intensity= Math.max(0, Integer.parseInt(intensityString));
			Direction direction= Direction.parseDirection(directionString);
			return new Wind(intensity, direction);
		}
		catch (NumberFormatException e) {
			throw new WindFormatException();
		}
		catch (DirectionFormatException e) {
            throw new WindFormatException();
        }
	}
	
	/** Return a string representation of the object. */
	public @Override String toString() {
		return intensity + " " + direction;
	}
	
	/** 
     * Return true iff obj is a Wind and has the same intensity and direction
	 * of this Wind.Two wind. 
     */
	public @Override boolean equals(Object obj) {
		if (!(obj instanceof Wind)) {
			return false;
		}
		
		Wind w = (Wind) obj;
		return intensity == w.intensity  &&  direction == w.direction;
	}
	
	/** 
     * Return a hash code value for the object. This method is supported for
	 * the benefit of hashtables such as those provided by java.util.Hashtable. 
     */
	public @Override int hashCode() {
		return (intensity * 31) ^ direction.hashCode();
	}
	
	/** 
     * Return a negative integer, zero, or a positive integer depending on
	 * whether this object is less than, equal to, or greater than obj. <br><br>
	 *  
	 * Comparisons are made first on the direction's row change, then on the
	 * direction's column change, and then on the intensity. 
     */
	public @Override int compareTo(Wind w) {
		if (direction.dRow !=  w.direction.dRow) {
			return direction.dRow - w.direction.dRow;
		}
		if (direction.dCol != w.direction.dCol) {
			return direction.dCol - w.direction.dCol;
		}
		return intensity - w.intensity;
	}
}
