package danaus;

/** 
 * An instance represents the power of a butterfly. Power is in the range
 * MIN_POWER..MAX_POWER. When a butterfly gains enough power
 * to exceed MAX_POWER, the power is set to MAX_POWER.
 * If power gets below MIN_POWER, the butterfly should be forced to land
 * and the power is changed to MIN_POWER.
 */
public class Power implements Comparable<Power> {
	/** Highest power possible. */
	public static final int MAX_POWER = 100;
	/** Lowest power possible before a butterfly is forced to land. */
	public static final int MIN_POWER = 0;
	/** The power assigned to a Power object instantiated with the default 
	 * constructor. It is >= MIN_POWER. */
	public static final int DEFAULT_POWER = MAX_POWER;
	
	/** Power, in the range MIN_POWER..MAX_POWER. */
	private int power;
	
	/** 
     * Constructor: an object with the default power DEFAULT_POWER. 
     */
	Power() {
		this(DEFAULT_POWER);
	}
	
	/** Constructor: an object with power p, lowered to MAX_POWER, if necessary,
	 * to be in range MIN_POWER..MAX_POWER.
	 * 
	 * Precondition: p >= MIN_POWER. */
	Power(int p) {
	    assert p >= MIN_POWER;
		power= fixPower(p);
	}
	
	/** 
     * Return the power of this object.
     */
	public int getPower() {
		return power;
	}
	
	/** 
     * Add p to the current power (lower the result to MAX_POWER, if necessary,
     * to be in the range MIN_POWER..MAX_POWER).
	 * 
	 * Throw a NoPowerException if the power gets below MIN_POWER. 
     */
	public void addPower(int p) {
		if (Simulator.INFINITE_ENERGY) {
			power = MAX_POWER;
		}
		else {
			power = fixPower(power + p);
		}
	}
	

	/** 
     * Subtract p from the current power (lower the result to MAX_POWER, if
	 * necessary, to be in the range MIN_POWER..MAX_POWER).
     * 
	 * Throw a NoPowerException if the power gets below MIN_POWER.
	 */
	public void subtractPower(int p) {
		if (Simulator.INFINITE_ENERGY) {
			power = MAX_POWER;
		}
		else {
			power = fixPower(power - p);
		}
	}
	
	/**
	 * Return the minimum of p and MAX_POWER. Throw a NoPowerException if 
     * p < MIN_POWER.
	 */
	private static int fixPower(int p) {
		if (p < MIN_POWER) {
			throw new NoPowerException();
		}
		return Math.min(p, MAX_POWER);
	}

	/**
	 * Returns a string representation of the object. 
	 */
	public @Override String toString() {
		return Integer.toString(power);
	}
	
	/**
     * Return true iff obj is of class Power and this object and obj have the
     * same power.
	 */
	public @Override boolean equals(Object obj) {
		if (!(obj instanceof Power)) {
			return false;
		}
		
		return power == ((Power)obj).power;
	}
	
	/**
	 * Returns a hash code value for the object. This method is supported for
	 * the benefit of hashtables such as those provided by java.util.Hashtable.
	 */
	public @Override int hashCode() {
		return power;
	}
	
	/**
     * Return a negative integer, zero, or a positive integer depending on
     * whether this object's power is less than, equal to, or greater than p's
     * power.
	 */
	public @Override int compareTo(Power p) {
		return power - p.power;
	}
}
