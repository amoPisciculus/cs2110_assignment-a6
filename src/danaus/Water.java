package danaus;

/** 
 * An instance represents a square of water. Butterflies are too afraid to fly
 * over water. 
 */
public class Water extends Tile {
	/* Tile specific constant. @see main.Tile */
	private static final String NAME = "water";
	private static final boolean FLYABLE = false; 
	private static final int SLOW_DOWN = Integer.MAX_VALUE;
	private static final int POWER_COST = Integer.MAX_VALUE;
			
	/** 
     * Constructor: an instance with skin skin and tile state tileState.
	 * It is flyable, has name "water", and has the highest possible
	 * slow-down and power-cost properties. 
     */
	Water(String skin, TileState tileState) {
		super(NAME, skin, FLYABLE, SLOW_DOWN, POWER_COST, tileState,
				Tile.TILES_DIR + NAME + Tile.IMAGE_EXT);
		tileState.type = getType();
	}
	
	/** Return "@", a Water tile's unique printable character. */
	public String toStringTile() {
		return "@";
	}
	
	/** Return tile type */
	public TileType getType() {
		return TileType.WATER;
	}
}
