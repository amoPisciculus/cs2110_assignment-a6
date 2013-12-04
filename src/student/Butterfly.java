package student;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import danaus.*;

public class Butterfly extends AbstractButterfly {
	/** An array of TileStates to keep track of tile information which we acquire 
	 *  We arbitrarily set this to 100 by 100 because we cannot access getMapWidth() and
	 *  getMapHeight() before learn() is initialized. The value is chosen because any recursion
	 *  stack larger than 100 will fail, so it is extremely unlikely a map of size > 100 will be used.
	 *  
	 *  We greedily initialize this outside the learn phase to avoid spending additional time on
	 *  a trivial operation
	 */
	private TileState[][] result = new TileState[100][100];
	
	/** An array of booleans to keep track of tiles which have been visited */
	private boolean[][] v = new boolean[100][100];
	private boolean[][] vRun = new boolean[100][100];

	/** Dummy variables to store the current location of the butterfly */
	private int row, col;
	
	/** Dummy variable to do state-checking */
	private TileState tempState;
	
	/** Variables to keep the map's height and width */
	private int height, width;

	/** Set the "default" speed we use to be FAST */
	private final Speed s = Speed.FAST;

	/** A stack (via the Deque interface) to implement the LIFO behavior of the DFS 
	 * Precondition: Direction must be one of the 8 valid cardinal directions in the direction class 
	 */
	private Deque<Direction> dfsStack = new ArrayDeque<Direction>();
	private Deque<Direction> dfsCollectStack = new ArrayDeque<Direction>();

	/** An instance generalizes a pair of integers to cardinal directions 
	 *  
	 * @param r An integer r={-1,0,1} describing the direction in the y-axis
	 * @param c An integer c={-1,0,1} describing the direction in the x-axis
	 * @return A direction value of class Direction that is one of the 8 cardinal directions
	 */
	private Direction dir(int r, int c) {
		if(r==-1 && c==-1) return Direction.NW;
		if(r==-1 && c==0)  return Direction.N;
		if(r==-1 && c==1)  return Direction.NE;
		if(r==1  && c==-1) return Direction.SW;
		if(r==1  && c==0)  return Direction.S;
		if(r==1  && c==1)  return Direction.SE;
		if(r==0  && c==-1) return Direction.W;
		return Direction.E;
	}

	/** An instance runs a stack-based depth-first search (DFS) to traverse a map 
	 * 	
	 *  The algorithm uses the flySafe function to fly a butterfly to an arbitrary location.
	 *  If the action was not possible (i.e. the butterfly hit an obstacle), the tile is still 
	 *  marked as visited, and recorded as "null" in the results array for lookup by the run phase.
	 *  
	 *  Use of the flySafe function, and comparison of the butterfly's position before and after the
	 *  flySafe has been executed, allows us to detect obstacles without incurring the overhead of a trace
	 *  arising from using a try-catch block. This provides a significant speed enhancement
	 *  
	 *  @see danaus.flySafe
	 */
	private void dfs() {
		// The actual bounds are r=[-1 1] but we subtract 1 throughout (and add it back in the loop) to exploit Java's faster checking for 0
		for(int r=-2; r<=0; ++r) {
			// The actual bounds are c=[-1 1] but we subtract 1 throughout (and add it back in the loop) to exploit Java's faster checking for 0
			for(int c=-2; c<=0; ++c) {
				// Initialize temporary variables for easy calculation
				// The fancy math normalizes the coordinates from the contiguous map space to Cartesian space
				// Without this normalization the DFS will fail at edge cases of the map
				int curRow = (state.location.row+r+1+height)%height;
				int curCol = (state.location.col+c+1+width)%width;
				
				// Only fly to tiles which have not been visited (i.e. they are not in the visited array)
				if (!v[curRow][curCol]) {
					// Store the current state in a temporary variable
					tempState = state;
					
					// Fly in the specified, de-enumerated direction
					// Here we intentionally use flySafe to avoid the speed penalty of a try-catch block
					flySafe(dir(r+1,c+1),s);

					// Update the state variable with information about the current tile
					refreshState(); 
					
					// Set the location as visited
					v[curRow][curCol] = true;
					
					// If the butterfly's state remains unchanged even after flying, we're in front of an obstacle
					if (!state.location.equals(tempState.location)) {						
						// Write the state of the current tile to the correct position in the results array
						result[curRow][curCol] = state;

						// Push the direction that was flown onto the stack
						dfsStack.push(dir(r+1,c+1));

						// Call the algorithm again to traverse the stack from the top once more
						dfs();
					}
				}
			}
		}
		
		// Base Case: Algorithm is complete when there are no more tiles to visit
		// i.e. there are no more tiles on the stack
		if(dfsStack.isEmpty()) return;
		
		// If there's nowhere else to go, backtrack to the previous visited location
		fly(Direction.opposite(dfsStack.pop()),s);
		
		// Update the state variable with information about the current tile
		refreshState();
	}
	
	/** An instance runs an assisted DFS to collect flowers on the map.
	 * 
	 *  The algorithm goes through the map using normal DFS behavior, but performs an additional check
	 *  for obstacles using information derived from the learn phase. This prevents it from flying into an
	 *  obstacle and throwing an exception. At every legal tile, we check for flowers and collect them if
	 *  they are on the list provided as input. The DFS terminates when all flowers have been collected.
	 *  
	 *  Precondition: The learn phase has been executed and a map array of obstacles is available
	 *  
	 *  @param flowerIdMap A hash-set of flower IDs that represent flowers to be collected by the butterfly. 
	 */
	private void dfsCollect(Set<Long>flowerIdMap) {
		// The actual bounds are r=[-1 1] but we subtract 1 throughout (and add it back in the loop) to exploit Java's faster checking for 0
		for(int r=-2; r<=0; ++r) {
			// The actual bounds are c=[-1 1] but we subtract 1 throughout (and add it back in the loop) to exploit Java's faster checking for 0
			for(int c=-2; c<=0; ++c) {
				// The fancy math normalizes the coordinates from the contiguous map space to Cartesian space
				// Without this normalization the DFS will fail at edge cases of the map
				int curRow = (state.location.row+r+1+height)%height;
				int curCol = (state.location.col+c+1+width)%width;

				// Only fly to tiles which have not been visited (i.e. they are not in the visited array)
				if (!vRun[curRow][curCol]) {
					vRun[curRow][curCol] = true;
					// Lookup the map from the learn phase to check if the tile we're trying to fly to is an obstacle
					if (result[curRow][curCol]!=null) {
						// Fly in the specified, de-enumerated direction
						// Unlike in the learn phase we do not need flySafe here because we know where the obstacles are
						fly(dir(r+1,c+1),s);

						// Update the state variable with information about the current tile
						refreshState();
						
						// Collect the flowers on the tile that match those in the list
						for (Flower f : state.getFlowers()) {
							if(flowerIdMap.remove(f.getFlowerId())) { collect(f); }
						}

						// Push the direction that was flown onto the stack
						dfsCollectStack.push(dir(r+1,c+1));

						// Call the algorithm again to traverse the stack from the top once more
						dfsCollect(flowerIdMap);
					}
				}
			}
		}

		// Base Case: Algorithm is complete when there are no more tiles to visit
		// i.e. there are no more tiles on the stack
		if(flowerIdMap.isEmpty()) return;

		// If there's nowhere else to go, backtrack to the previous visited location
		fly(Direction.opposite(dfsCollectStack.pop()),s);

		// Update the state variable with information about the current tile
		refreshState();
	}
	
	/**
	 * Returns a two-dimensional array of TileStates that represents the map the
	 * butterfly is on.
	 * 
	 * During the learning phase of a simulation, butterflies are tasked with
	 * learning the map in preparation for the running phase of a simulation. 
	 * A butterfly should traverse the entire map and generate a two-dimensional
	 * array of TileStates in which each TileState corresponds to the
	 * appropriate in the map. For example, consider the map with the following
	 * TileStates.
	 * 
	 * <code>
	 * 					 			 -----
	 * 								|a|b|c|
	 *                   			 -----
	 *                  			|d|e|f|
	 *                   			 -----
	 * </code>
	 * A butterfly should return an identical array. The following arrays are
	 * all incorrect.
	 * 
	 * <code>
	 *                               -----
	 * 								|f|e| |
	 *                   			 -----
	 *                  			|a|b|d|
	 *                   			 -----
	 *                                ---
	 * 								 |a|b|
	 * 								  ---
	 *                  			 |d|e|
	 *                         	      ---
	 * </code>
	 *
	 * The returned array is graded based on the percentage of correctly 
	 * identified TileStates. It is recommended that a butterfly save the 
	 * TileState array to use during the running phase of a simulation.
	 *
	 * For more information, refer to Danaus' documentation.
	 * 
	 * @return A two-dimensional array of TileStates that represents the map the
	 * butterfly is on.
	 */
	public @Override TileState[][] learn() {
		height = getMapHeight();
		width = getMapWidth();

		// Record the information for the starting tile
		refreshState();

		// Get the world coordinates of the butterfly's current position
		row = state.location.row;
		col = state.location.col;

		// Write the state of the current tile to the correct position in the results array
		result[row][col] = state;

		// Mark the starting tile as visited
		v[row][col] = true;

		// Call a depth-first-search to traverse the map
		dfs();

		return result;
	}
	
	/**
     * Simulates the butterfly's flight.
     * <br>
     * During the transition from the learning phase to the running phase, new 
     * flowers are planted on the map. Everything else remains the same. A
     * butterfly must navigate to and collect all the flowers in 
     * <em>flowerIds<em> in any order to successfully complete a simulation. 
     * You must also not collect extraneous flowers. That is, only collect the 
     * flowers represented by flowerIds.
     * 
     * @param flowerIds A vector of flowers which the butterfly has to collect
     * @see danaus.AbstractButterfly#collect(Flower)
     */
	public @Override void run(List<Long> flowerIds) {
		// Convert the list to a hash-set so we have constant time lookup
		// This incurs a one-off execution time penalty but it's better than 
		// iterating over a list for O(n) complexity every time we need to check
		final Set<Long> flowerIdMap = new HashSet<Long>(flowerIds);
		
		// Record the information for the starting tile
		refreshState();
		
		// Collect the flowers on the starting tile that match those in the list
		for (Flower f : state.getFlowers()) {
			if(flowerIdMap.remove(f.getFlowerId())) { collect(f); }
		}
		
		// Mark the starting tile as visited
		vRun[state.location.row][state.location.col] = true;
		
		// Call a depth-first-search to traverse the map and collect the flowers
		dfsCollect(flowerIdMap);
	}
	
	// OMITTED METHODS //
	// All methods below are omitted because they are neither tested nor required by our algorithm.
	// In addition, providing functionality to populate these methods slows down the code.
	@Override public List<Flower> flowerList() { return null; }
	@Override public Location flowerLocation(Flower f) { return null; }
	@Override public Location flowerLocation(long flowerId) { return null; }
}
