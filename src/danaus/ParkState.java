package danaus;

import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * An instance represents a park's state. A park is aware of the state of the
 * game and collects helpful statistics the player may find helpful. 
 */
public class ParkState {
	/** The current turn of the simulation. */
	long turn;
	/** The number of slow turns. */
	long slowTurns;
	/** The number of flyable tiles. */
	long numTiles;
	/** The number of explored tiles. */
	long exploredTiles;
	/** All the flowers on the map. */
	HashSet<Flower> allFlowers;
	/** The required flowers the player must find. */
	List<Flower> requiredFlowers;
	/** The flowers the player has found. */
	List<Flower> foundFlowers;
	/** The total power spent by the player. */
	long powerSpent;
	/** The total power consumed by the player. */
	long powerConsumed;
	/** The number of cliff collisions. */
	long cliffCollisions;
	/** The number of water collisions. */
	long waterCollisions;
	
	/**
	 * Returns a string representation of the object. 
	 *
	 * @return a string representation of the object.
	 */
	public @Override String toString() {
		StringBuilder builder = new StringBuilder();
		Formatter formatter = new Formatter(builder, Locale.US);
		
		if (foundFlowers == null) {
			formatter.close();
			return "";
		}
		
		formatter.format("%-20s : %6d\n", "Turn Number", turn);
		formatter.format("%-20s : %6d\n", "Slow Turns", slowTurns);
		formatter.format("%-20s : %6d/%-6d\n", "Tiles Explored", exploredTiles, numTiles);
		formatter.format("%-20s : %6d/%-6d\n", "Flower Found", foundFlowers.size(), allFlowers.size());
		formatter.format("%-20s : %6d\n", "Power Spent", powerSpent);
		formatter.format("%-20s : %6d\n", "Power Consumed", powerConsumed);
		formatter.format("%-20s : %6d\n", "Cliff Collisions", cliffCollisions);
		formatter.format("%-20s : %6d", "Water Collisions", waterCollisions);

		String retval = formatter.toString();
		formatter.close();
		return retval;
	}
}
