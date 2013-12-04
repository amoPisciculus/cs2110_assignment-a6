package danaus;

/* @see danaus.DirectionConditions */ 
public class TileConditions {
	/** Tiles that can be flown to. */
	public static class Flyables implements TileCondition {
		public @Override boolean tileCondition(Tile tile) {
			return tile.flyable;
		}
	}
	
	/** Tiles that cannot be flown to. */
	public static class Obstacles implements TileCondition {
		public @Override boolean tileCondition(Tile tile) {
			if (tile == null) {
				return false;
			}
			return !tile.flyable;
		}
	}
	
	/** Null tiles. */
	public static class Nulls implements TileCondition {
		public @Override boolean tileCondition(Tile tile) {
			return tile == null;
		}
	}
	
	/** Land tiles. */
	public static class Lands implements TileCondition {
		public @Override boolean tileCondition(Tile tile) {
			return tile instanceof Land;
		}
	}
}
