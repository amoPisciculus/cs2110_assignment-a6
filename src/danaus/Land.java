package danaus;

/** An instance represents a plain piece of ground. */
public class Land extends Tile {
	/* Tile specific constants. @see main.Tile */
	private static final String NAME = "land";
	private static final boolean FLYABLE = true; 
	private static final int SLOW_DOWN = 0;
	private static final int POWER_COST = 0;
				
	/** Constructor: a flyable land instance with skin skin,
	 * slow-down 0, power-cost 0, tile state tileState.
	 * Precondition: skin is one of "bog", "dirt", "ice", "land",
	 * "lava", "mud", "sand", "snow", "stone". */
	Land(String skin, TileState tileState) {
		super(NAME, skin, FLYABLE, SLOW_DOWN, POWER_COST, tileState,
				Tile.TILES_DIR + skin + Tile.IMAGE_EXT);
		tileState.type = getType();
	}
	
	/** Return " ", the unique printable character for Land. Each tile has a unique
	 * printable character to print in an ascii map. */
	public String toStringTile() {
		return " ";
	}
	
	/** Return tile type */
	public TileType getType() {
		return TileType.LAND;
	}
}
