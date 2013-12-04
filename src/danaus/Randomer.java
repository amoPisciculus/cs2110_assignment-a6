package danaus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * An instance is a pseudorandom number generator with more to offer than the
 * standard Random class. It's like Random, but Randomer.
 */
@SuppressWarnings("serial")
public class Randomer extends Random {
	/** Constructor: an instance with a random seed. */
	public Randomer() {}

    /** Constructor: an instance with seed seed. */
	public Randomer(long seed) {super(seed);}

    /** Return a random integer in the range low..high. */
	public int nextInt(int low, int high) {	
		return nextInt(high - low + 1) + low;
	}
	
	/**
	 * Return a random double in the range low..high.
	 */
	public double nextDouble(double low, double high) {	
		return (nextDouble() * (high - low)) + low;
	}
	
	/**
	 * Returns true with probability p in the range [0,1].
     * <br>
     * If p is given out of range, it is rounded to the nearest boundary.
	 * For example a probability of -0.5 will returns false.
	 */
	public boolean nextBoolean(double p) {
		if (p <= 0) {
			return false;
		}
		if (p >= 1) {
			return true;
		}
		return nextDouble() <= p;
	}
	
	/**
	 * Returns true with probability p out of 1000.
     * @see danaus.Randomer#nextBoolean
	 */
	public boolean nextBoolean(int probability) {
		return nextInt(1000) <= probability;
	}
	
	/** Returns a random element of array b. Return null if b.length = 0. */
	public <T> T nextElement(T[] b) {
		if (b.length == 0) {
			Debugger.WARNING("zero length array in next element.");
			return null;
		}
		
		return b[nextInt(b.length)];
	}
	
	/** Return a random element of list b. Return null if b.size = 0; */
	public <T> T nextElement(List<T> b) {
		if (b.size() == 0) {
			Debugger.WARNING("zero length list in next element.");
			return null;
		}

		return b.get(nextInt(b.size()));
	}
	
	/**
	 * Return a random sample of n elements from b.
     * <br>
     * Precondition: 0 <= n <= b.size()
	 */
	public <T> List<T> sample(List<T> b, int n) {
		List<T> copy = new ArrayList<T>(b);
		Collections.shuffle(copy, this);
		return new ArrayList<T>(copy.subList(0, n));
	}
}
