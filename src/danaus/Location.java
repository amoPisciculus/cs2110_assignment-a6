package danaus;

/** An instance represents a pair of integer Cartesian coordinates. */
public class Location implements Comparable<Location> {
    /** x coordinate. */
    public final int col;
    /** y coordinate. */
    public final int row; 

    /**Constructor: an instance with coordinates (x, y). */
	public Location(int col, int row) {
		this.col = col;
		this.row = row;
	}
	
	/**Constructor: copy constructor */
	public Location(Location l) {
		col = l.col;
		row = l.row;
	}

    /** Return a string representation of the object. */
    public @Override String toString() {
        return "(" + row + ", " + col + ")";  
    }

    /** Return true iff obj is a Location and its coordinates are
     * the same as this object's coordinates. */
	public @Override boolean equals(Object obj) {
		if (!(obj instanceof Location)) {
			return false;
		}
		
		Location c = (Location) obj;
		return col == c.col && row == c.row;
	}
	
	 /** Return a hash code value for the object. This method is supported for
	 * the benefit of hashtables such as those provided by java.util.Hashtable.
	 * */
	public @Override int hashCode() {
		// Taken from Joshua Bloch's Effective Java
		int hash = 7;
		hash = 71 * hash + col;
		hash = 71 * hash + row;
		return hash;
	}
	
    /** Return a negative integer, zero, or a positive integer depending on
     * whether this Location comes before location loc. The comparison
     * is made first on the x coordinate and then on the y coordinate. */
	public @Override int compareTo(Location loc) {
		if (col != loc.col) {
			return col - loc.col;
		}
		return row - loc.row;
	}
}
