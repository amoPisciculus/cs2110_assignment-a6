package danaus;

/**
 * A callback interface invoked by a map's neighbor retrieving methods. When a 
 * map is queried for a set of neighbors, an instance of this callback 
 * interface, implemented as an anonymous class, is used to filter the results. 
 * Only neighbors --  whether they be locations, directions, or tiles -- that
 * satisfy the user defined condition are returned. 
 */
public interface DirectionCondition {
	/**
	 * Return a user defined truth assessment based on a condition to filter
	 * map neighbor queries. See the classes description or consult the Map 
	 * class for more information. 
	 */
	public boolean directionCondition(Direction direction);
}
