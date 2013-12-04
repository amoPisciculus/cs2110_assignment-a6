package danaus;

/*******************************************************************************
 * An instance represents a flower.
 ******************************************************************************/
public class Flower extends Entity {
    /** A unique index is assigned to every flower to differentiate flowers 
     * with the same name at the same tile. Integers 0..UNIQUE_FLOWER_ID-1 have
     * already been assigned to flowers. */
    private static long UNIQUE_FLOWER_ID;
    
    static final double AROMA_INTENSITY = 1e6;
    
    /** The aroma of the flower at the location of the flower. (The aroma is 
     * then spread throughout a map.) <br>
     * @see danaus.Map Map */
    public final double aromaIntensity;

    /** A unique flower id assigned to each flower */
    long flowerId;
	
    /***************************************************************************
     * Constructor: an instance named name at location loc with aroma
     * intensity ai. If ai is negative, 0 is used. 
     * 
     * ai is not used. It should be removed in future revisions.
     **************************************************************************/
    Flower(String name, Location loc, double ai) {
        super("res/flowers/" + name, loc);
        aromaIntensity= AROMA_INTENSITY;
        flowerId= UNIQUE_FLOWER_ID++;
    }
    
    /***************************************************************************
     * Return a string representation of the object. 
     **************************************************************************/
    public @Override String toString() {
        return String.format("%s(%.3f)[%d] @ %s", 
                name, aromaIntensity, flowerId,  location);
    }
    
	/* *********************************************************************//**
	 * Return a shortened string representation of the object. All non-digit
     * characters are removed from the name.
	 * ************************************************************************/
	public String toStringShort() {
		return name.replaceAll("\\D+","");
	}
	
	/* *********************************************************************//**
	 * Return a flower's id.
	 * 
	 * @return A flowers id.
	 * ************************************************************************/
	public long getFlowerId() {
		return flowerId;
	}
	
    /***************************************************************************
     * Return true iff obj is a Flower with the same name, location, image name,
     * and flower id.
     * 
     * @param obj the object with which to compare.
     * @return true iff this object is the same as obj.
     **************************************************************************/
    public @Override boolean equals(Object obj) {
        if (!(obj instanceof Flower)) {
            return false;
        }

        return flowerId == ((Flower) obj).flowerId;
    }

    /***************************************************************************
     * Return a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by java.util.Hashtable.
     * 
     * @return a hash code value for this object.
     * ************************************************************************/
    public @Override int hashCode() {
        return (int) flowerId;
    }

    /***************************************************************************
     * Return a negative integer, 0, or a positive integer depending on 
     * whether this object is less than, equal to, or greater than e.
     * 
     * Comparison is made on name, then location, then image filename, and
     * finally IFF obj is a flower, the flower id.
     * 
     * @return a negative integer, zero, or a positive integer depending on
     * whether this object is less than, equal to, or greater than e.
     * ************************************************************************/
    public int compareTo(Flower e) {
        return (int) (flowerId - e.flowerId);
    }
}
