package danaus;

/**
 * An instance represents a map tile. A tile has information to identify 
 * it, information essential to a simulation, and information for a GUI. 
 */
public abstract class Tile implements Comparable<Tile> {
	// IDENTIFYING INFORMATION
	/** The name of the tile (eg tree). */
	protected final String name;
	/** The skin of the tile (eg snow). */
	protected String skin;
	
	// SIMULATION INFORMATION
	/** True iff the tile can be flown to. */
	public final boolean flyable; 
	/** The slow-down number of a tile 
	 * @see danaus.AbstractButterfly#fly(Direction, Speed) fly */
	final int slowDown;
	/** Power cost @see danaus.Map#updateCosts() */
	final int powerCost;
	/** The dynamic state information of a tile. */
	protected TileState tileState;
	
	// GUI INFORMATION
	/** The root directory of all tile images. */
	public static String TILES_DIR = "res/tiles/";
	/** The root directory of all obstacle images. */
	public static String OBSTACLES_DIR = "res/obstacles/";
	/** Image extension. */
	public static String IMAGE_EXT = ".png";
	/** Filename of the tile's image. */
	protected final String tileFilename;
	/** The most recent turn a butterfly entered. This can be used to update
     * the path trail of a butterfly in the GUI. -1 indicates that the tile has
     * not yet been entered. */
    public long turnEntered;
	
	/**
	 * Copies one tile into another. This method is designed to allow subclasses
	 * of a tile be converted between one another. For example, a cliff can
	 * be converted to land. Thus, only non-tile specific information is copied.
	 */
	public static boolean copy(Tile to, Tile from) {
		if (to == null || from == null) {
			return false;
		}
		
		to.skin = from.skin;
		to.tileState = from.tileState;
		to.tileState.type = to.getType();
		return true;
	}
	
	/** 
     * Constructor: a tile instance. <br>
	 * Its name is name. e.g. "forest".<br>
	 * Its skin is skin (e.g. "snow").<br>
	 * It can be entered only if flyable is true.<br>
	 * slowDown is slow-down number -- @see danaus.AbstractButterfly#fly(Direction, Speed).<br>
	 * powerCost is the power cost -- @see danaus.Map#updateCosts().<br>
	 * tileState is the dynamic state information of the tile.<br>
	 * tileFilename is the name of the image file for the tile.<br>
	 * A turn has not yet been entered for this tile.
     */
	public Tile(String name, String skin, boolean flyable, 
			int slowDown, int powerCost, TileState tileState,
			String tileFilename) {
		this.name         = name;
		this.skin         = skin;
		this.flyable      = flyable;
		this.slowDown     = slowDown;
		this.powerCost    = powerCost;
		this.tileState    = tileState;
		this.tileFilename = tileFilename;
		turnEntered = -1;
	}
	
	/** Returns a string representation of the object. */
	public @Override String toString() {
		return name + "_" + skin + " at " + tileState.location + tileState;
	}
	
	/**
	 * Returns a string representation of the object that can be used to print
	 * an ascii representation of the map. A tile's ascii map string reflects
	 * whether the tile has a butterfly or flowers. 
	 *
	 * @return An ascii map string representation of the object.
	 */
	public String toStringMap() {
		String butterflyToken = "db";
		String flowerToken = "*";
		
		if (tileState != null && tileState.butterfly != null) {
			return butterflyToken;
		}
		if (tileState != null && tileState.flowers != null && 
				!tileState.flowers.isEmpty()) {
			return toStringTile() + flowerToken;
		}
		return toStringTile() + toStringTile();
	}
	
	/**
	 * Returns a tile's unique printable character. Each tile has a unique
	 * printable character to print in an ascii map. 
	 *
	 * @return A tile's unique printable character.
	 */
	public abstract String toStringTile();
	
	/** Return tile type */
	public abstract TileType getType();
	
	/** 
     * Return true iff obj is a Tile and this object and obj are at the same
	 * location. <br>
	 * Tiles on different maps in the same location are equal,
	 * even if they appear very different. This is acceptable because tiles
	 * from different maps should not be interacting. All comparisons are
	 * performed within the scope of a map.  
     */
	public @Override boolean equals(Object obj) {
		if (!(obj instanceof Tile)) {
			return false;
		}
		
		return tileState.location.equals(((Tile)obj).tileState.location);
	}
	
	/**
	 * Returns a hash code value for the object. This method is supported for
	 * the benefit of hashtables such as those provided by java.util.Hashtable.
	 * 
	 * @return a hash code value for this object.
	 */
	public @Override int hashCode() {
		return tileState.location.hashCode();
	}
	
	/** 
     * Return a negative integer, zero, or a positive integer depending on
	 *  whether this tile's location is less than equal to, or greater than
	 *  the location of obj. 
     */
	public @Override int compareTo(Tile t) {
		return tileState.location.compareTo(t.tileState.location);
	}
}
