package danaus;

/**
 * An instance represents a speed a Butterfly can travel. Speed is directly
 * proportional to power cost and inversely proportional to slow down cost.
 */
public enum Speed {
	SLOW  ( 1, -3),
	NORMAL( 0,  0),
	FAST  (-1,  3);
	
	/** The slow down associated with a speed. */
	public final int slowDownNumber;
	/** The power cost associated with a speed. */
	public final int powerCost;
	
    /** Constructor: an instance with slow down number s and power cost p. */
	Speed(int s, int p) {
		this.slowDownNumber = s;
		this.powerCost = p;
	}
}
