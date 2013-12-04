package danaus;

/*******************************************************************************
 * An instance is one of 8 cardinal directions. Each direction contains
 * information on its relative position to a tile in an array. For example,
 * consider tile tiles[tRow][tCol]. The tile to its north is
 * tile tiles[tRow + NORTH.dRow][tCol + NORTH.dColumn]. <br>
 * 
 * Modular arithmetic fixes instances in which tiles are on the edge of a
 * map.
 ******************************************************************************/
public enum Direction {
	N  (-1, +0),
	NE (-1, +1),
	E  (+0, +1),
	SE (+1, +1),
	S  (+1, +0),
	SW (+1, -1),
	W  (+0, -1),
	NW (-1, -1);
	
	/** The change in the row needed to reach a tile in the given direction.
	 * See the class' description for an example. */
	public final int dRow;
	/** The change in the column needed to reach a tile in the given direction.
     * See the class' description for an example. */
	public final int dCol;
	
	/***************************************************************************
	 * Constructor: an instance with direction (dRow, dCol). 
	 **************************************************************************/
	Direction(int dRow, int dCol) {
		this.dRow = dRow;
		this.dCol = dCol;
	}
	
	/***************************************************************************
	 * Return the Direction corresponding to direction.
	 * Throw a DirectionFormatException if direction is not one of
	 * "n", "ne", "e", "se", "s", "sw", "w", "nw" (upper or lower case).
	 * 
	 * @param direction String representation of a direction. e.g. "N"
	 * @return A direction object corresponding to String direction.
	 * @throws DirectionFormatException
	 * @throws NullPointerException
	 **************************************************************************/
	public static Direction parseDirection(String direction) {
		Debugger.NULL_CHECK(direction, "null direction in parseDirection!");
		
		switch (direction.trim().toLowerCase()) {
			case "n":
				return Direction.N;
			case "ne":
				return Direction.NE;
			case "e":
				return Direction.E;
			case "se":
				return Direction.SE;
			case "s":
				return Direction.S;
			case "sw":
				return Direction.SW;
			case "w":
				return Direction.W;
			case "nw":
				return Direction.NW;
			default:
				throw new DirectionFormatException();
		}
	}
	
	/***************************************************************************
	 * Return the direction opposite direction d. Throw an 
	 * IllegalArgumentException if d is not one of the 8 Direction objects
	 * (which should not happen!).
	 **************************************************************************/
	public static Direction opposite(Direction direction) {
		switch(direction) {
		case N:  return S;
		case NE: return SW;
		case E:  return W;
		case SE: return NW;
		case S:  return N;
		case SW: return NE;
		case W:  return E;
		case NW: return SE;
		default:
			Debugger.ERROR("You've discovered an invalid direction. You're " +
					"rewarded with an error!");
			throw new IllegalArgumentException();
		}
	}
}
