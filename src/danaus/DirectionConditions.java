package danaus;

/**
 * Anonymous classes can get annoying to instantiate inside a function call. 
 * This class offers a set of predefined DirectionConditions that are commonly
 * used.
 */
public class DirectionConditions {
	/** East or West tiles. */
	static class EastOrWest implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.E ||
				   direction == Direction.W;
		}
	}
	
	/** North or South tiles. */
	static class NorthOrSouth implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.N ||
				   direction == Direction.S;
		}
	}

	/** Tiles that form a cross, or plus sign. */
	static class Cross implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.N ||
				   direction == Direction.E ||
				   direction == Direction.S ||
				   direction == Direction.W;
		}
	}

	/** Tiles with an equal or greater y-coordinate. */
	static class Up implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.W ||
				   direction == Direction.NW ||
				   direction == Direction.N ||
				   direction == Direction.NE ||
				   direction == Direction.E;
		}
	}

	/** Tiles with an equal or greater x-coordinate. */
	static class Right implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.N ||
				   direction == Direction.NE ||
				   direction == Direction.E ||
				   direction == Direction.SE ||
				   direction == Direction.S;
		}
	}

	/** Tiles with an equal or smaller y-coordinate. */
	static class Down implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.W ||
				   direction == Direction.SW ||
				   direction == Direction.S ||
				   direction == Direction.SE ||
				   direction == Direction.E;
		}
	}

	/** Tiles with an equal or lesser x-coordinate. */
	static class Left implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.N ||
				   direction == Direction.NW ||
				   direction == Direction.W ||
				   direction == Direction.SW ||
				   direction == Direction.S;
		}
	}
	
	/** Tiles in the corners of the 3x3 square around a tile. */
	static class Corners implements DirectionCondition {
		public @Override boolean directionCondition(Direction direction) {
			return direction == Direction.NW ||
				   direction == Direction.NE ||
				   direction == Direction.SW ||
				   direction == Direction.SE;
		}
	}
}
