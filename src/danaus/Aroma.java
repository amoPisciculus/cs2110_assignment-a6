package danaus;

/**
 * An instance represents an aroma. Aromas have both an intensity and an
 * associated flower that produced them. In order for a flower to appear in a 
 * tile's list of aromas, it must be stronger than the the minimum aroma
 * intensity.  
 */
public class Aroma implements Comparable<Aroma> {
    /** Tunable constants to help construct the minimum aroma intensity. */
    private static int MAXIMUM_STEPS = Integer.MAX_VALUE;

    /** The smallest an aroma can be to appear in a tile's list of aromas. */
    public static final double MIN_AROMA_INTENSITY = 
            Flower.AROMA_INTENSITY / (Math.pow(MAXIMUM_STEPS, 2));

    /** The intensity of a flower. Intensity is on a linear scale. That is, 
	    an intensity 2t is twice as intense as an intensity t. */
    public double intensity;

    /** The flower that produced this aroma. */
    final Flower flower;

    /**
     * Constructor: an instance with intensity intensity that was produced
     * from flower flower.
     * 
     * @param intensity The intensity of the flower.
     * @param flower The flower that produced the aroma. 
     */
    Aroma(double intensity, Flower flower) {
        this.intensity = Math.max(0, intensity);
        this.flower = flower;
    }
    
    /** Returns the flowerId of the flower that produced the aroma. */
    public long getFlowerId() {
    	return flower.getFlowerId();
    }

    /**
     * Return the aroma intensity at s steps away from initial intensity in.
     * 
     * @param in An initial intensity.
     * @param s The number of steps away from the initial intensity.
     * @return The intensity at s steps away from initial intensity in. 
     */
    public static double calculateIntensity(double in, int s) {
        return in / ((s + 1.0) * (s + 1.0));
    }

    /**
     * Return the maximum number of steps that can be taken away from
     * initial intensity in and still be detected.
     *
     * @param in An initial intensity.
     * @return the maximum number of steps that can be taken away from 
     * intensity in and still be detected.
     */
    public static int getMaxSteps(double in) {
        return Aroma.MAXIMUM_STEPS;
    }

    /** Make a negative intensity zero. */
    public void zero() {
        intensity= Math.max(0, intensity);
    }

    /**
     * Return a string representation of the object. 
     *
     * @return a string representation of the object. 
     */
    public @Override String toString() {
        return "{" + flower.getFlowerId() + ", " + String.format("%.2f", intensity) + "}";
    }

    /**
     * Return a string representation of the object, using a short name
     * for the flower. 
     *
     * @return a string representation of the object. 
     */
    public String toStringShort() {
        return flower.getFlowerId() + ":" + String.format("%.1f", intensity);
    }

    /**
     * Return true iff obj is an Aroma and this Aroma and obj have the
     *  same flower and intensity. 
     */
    public @Override boolean equals(Object obj) {
        if (!(obj instanceof Aroma)) {
            return false;
        }

        Aroma a= (Aroma) obj;
        return flower.equals(a.flower)  &&  intensity == a.intensity;
    }

    /**
     * Return a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by java.util.Hashtable.
     * 
     * @return a hash code value for this object.
     */
    public @Override int hashCode() {
        return (int) ((int)(intensity * 31) ^ flower.hashCode());
    }

    /**
     * Return a negative integer, zero, or a positive integer depending on
     * whether this object is less than, equal to, or greater than obj.
     * 
     * Comparison is first made based on their flowers, then on their intensities.
     * 
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than obj.
     */
    public @Override int compareTo(Aroma obj) {
        if (!flower.equals(flower)) {
            return flower.compareTo(obj.flower);
        }
        if (intensity == obj.intensity) {
            return 0;
        }
        if (intensity < obj.intensity) {
            return -1;
        }
        return 1;
    }
}
