package danaus;

import java.util.ArrayList;
import java.util.List;

/** 
 * An instance represents the state of a tile. Not all the information about
 * a tile lives in a TileState object, but the interesting information that
 * may be passed to other objects does. 
 */
public class TileState implements Comparable<TileState> {
	/** An abstraction of a Nil TileState. */
	public static TileState nil = new TileState(new Location(-1, -1));  
	
	/** The location of a tile. */
	public Location location;
	/** The light at a tile. A butterfly earns as much power at a tile
	 * as there is light at a tile. */
	public int light;
	/** The wind at a tile.*/
	public Wind wind;
	/** A list of the aromas at a tile. Only aromas with an intensity greater
	 * the minimum aroma intensity are included. */
	List<Aroma> aromas;
	/** A list of flowers at a tile. */
	List<Flower> flowers;
	/** The butterfly on a tile or null if no butterfly is on the tile. */
	protected AbstractButterfly butterfly;
	/** The type of Tile this TileState belongs to. */
	public TileType type;
	
	/** Constructs a naked tile state. Used often in random map generation. */
	TileState(Location loc) {
		this(loc, 0, new Wind(0, Direction.N), new ArrayList<Flower>());
	}
	
	/** 
     * Constructor a instance with location loc, light li, wind w, and flowers f. <br><br>
	 * Only properties that are likely to have an initial value given in a map file
	 * are provided. Other properties can be changed after creation. 
	 */
	TileState(Location loc, int li, Wind w, List<Flower> f) {
		Debugger.NULL_CHECK(loc, "null location in TileState(...)");
		Debugger.NULL_CHECK(w, "null wind in TileState(...)");
		Debugger.NULL_CHECK(f, "null flowers in TileState(...)");
		
		location = loc;
		light    = li;
		wind     = w;
		aromas   = new ArrayList<Aroma>();
		flowers  = f;
	}
	
	/** Constructor: copy constructor. */
	TileState(TileState other) {
		location = new Location(other.location);
		light = other.light;
		wind = new Wind(other.wind);
		aromas = new ArrayList<Aroma>(other.aromas);
		flowers = new ArrayList<Flower>(other.flowers);
		butterfly = other.butterfly;
		type = other.type;
	}
	
	/** Return flowers at this tile. */
	public List<Flower> getFlowers() {
		return flowers;
	}
	
	/** Return aromas at this tile. */
	public List<Aroma> getAromas() {
		return aromas;
	}
	
	/** 
     * Add non-null flower f to the tilestate's list of flowers and return true.
	 * If f is null, don't add it, and return false. 
     */
	boolean addFlower(Flower f) {
		if (f == null) {
			return false;
		}
		
		flowers.add(f);
		return true;
	}
	
	/** 
     * Add non-null ar to the tilestate's aroma list and return true.
	 * If ar is null, don't add it, and return false. 
     */
	boolean addAroma(Aroma ar) {
		if (ar == null) {
			return false;
		}
		
		aromas.add(ar);
		return false;
	}
	
	/** 
     * For each aroma in this tile state's list of aromas, increase its
	 * intensity by v if flowers contains the aroma's flower. 
     */
	void addToAromas(List<Flower> flowers, double v) {
		for (Aroma aroma : aromas) {
			if (flowers.contains(aroma.flower)) {
				aroma.intensity += v;
			}
		}
	}
	
	/** Subtract v from every aroma in the tile state's list of aromas. */
	void subtractFromAromas(List<Flower> flowers, double v) {
		addToAromas(flowers, -v);
	}
	
	/** 
     * Zero out all the aromas in the tile state's list of aromas.
	 * @see danaus.Aroma#zero().
     */
	void zeroAromas() {
		for (Aroma aroma : aromas) {
			aroma.zero();
		}
	}
	
	/** 
     * Return "yes" if this tile state has at least one aroma and "no" 
     * otherwise. 
     */
	public String toStringAromas() {
		if (aromas.isEmpty()) {
			return "no";
		}
		return "yes";
	}
	
	/** 
     * If this tile state has no flowers, return "no";
	 * if it has flowers, return a list of their short names. 
     */
	public String toStringFlowers() {
		if (flowers.isEmpty()) {
			return "no";
		}
		String flowerString = "";
		for (int i = 0; i < flowers.size(); i++) {
			flowerString += flowers.get(i).toStringShort();
			if (flowers.size() - 1 != i) {
				flowerString += ",";
			}
		}
		return flowerString;
	}
	
	/** Return a string representation of the object. */
	public @Override String toString() {
		String string = "{l:" + light + ", ";
	    
		string += "w:" + wind + ", ";
	    
	    string += "a:";
	    if (aromas != null) {
	    	for (Aroma aroma : aromas) {
	    		string += "(" + String.format("%.2f", aroma.intensity) + ",";  
	    		// Add the last character of the flowers the aromas belong to.
	    		// Ideally, it is a unique number.
	    		String flowerName = aroma.flower.getName();
	    		string += flowerName.substring(flowerName.length() - 2) + ")";
	    	}
	    }
	    
	    string += ", f:";
	    if (flowers != null) {
	    	for (Flower flower : flowers) {
	    		string += flower.flowerId + "-";
	    	}
	    }
	    string += ", " + "b:" + (butterfly != null) + "}";
	    
	    return string;
	}
	
	/** 
     * Return true iff obj is a TileState and their properties are the same,
	 * i.e. their their locations, light, wind, lists of aromas, and lists
	 * of flowers are the same.
     */
	public @Override boolean equals(Object obj) {
		// Check for class equality. instanceof is used over getClass because of
		// the hierarchical nature of TileStates.
		if (!(obj instanceof TileState)) {
			return false;
		}
		
		return compareTo(((TileState)obj)) == 0;
	}
	
	/** 
     * Return a hash code value for the object. This method is supported for
	 * the benefit of hashtables such as those provided by java.util.Hashtable. 
     */
	public @Override int hashCode() {
		return location.hashCode();
	}
	
	/**
     * Return a negative integer, zero, or a positive integer depending on
	 * whether this object is less than, equal to, or greater than obj. <br><br>
	 * 
	 * The comparison is made on the properties in this order:<br>
	 * (0) location, (1) light, (2) wind, (3) number of aromas,
	 * (4) the individual aromas in the lists of aromas,
	 * (5) the number of flowers, (6) the individual flowers in the lists of
	 * flowers. 
     */
	public @Override int compareTo(TileState t) {
		if (! location.equals(t.location)) {
			return location.compareTo(t.location);
		}
		if (light != t.light) {
			return light - t.light;
		}
		if (! wind.equals(t.wind)) {
			return wind.compareTo(t.wind);
		}
		if (aromas.size() != t.aromas.size()) {
			return aromas.size() - t.aromas.size();
		}
		for (int i = 0; i < aromas.size(); ++i) {
			if (! aromas.get(i).equals(t.aromas.get(i))) {
				return aromas.get(i).compareTo(t.aromas.get(i));
			}
		}
		if (flowers.size() != t.flowers.size()) {
			return flowers.size() - t.flowers.size();
		}
		for (int i = 0; i < flowers.size(); ++i) {
			if (! flowers.get(i).equals(t.flowers.get(i))) {
				return flowers.get(i).compareTo(t.flowers.get(i));
			}
		}
		return 0;
	}
}
