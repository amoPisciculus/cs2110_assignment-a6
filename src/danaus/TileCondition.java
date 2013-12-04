package danaus;

/**
 * A callback interface invoked by a map's neighbor retrieving methods. When a 
 * map is queried for a set of neighbors, an instance of this callback 
 * interface, implemented as an anonymous class (or function pointer for those 
 * more familiar with C/C++), is used to filter the results. Only neighbors -- 
 * whether it be locations, directions, or tile -- that satisfy the user 
 * defined condition are returned. 
 */
public interface TileCondition {
	/**
	 * Returns a user defined truth assessment based on a condition to filter
	 * map neighbor queries. See the classes description or consult the Map 
	 * class for more information.
	 */
	public boolean tileCondition(Tile tile);
}
