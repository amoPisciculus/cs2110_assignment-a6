package student;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import danaus.*;

public class Butterfly extends AbstractButterfly {
	private int algo = 2;
	private TileState[][] result;
    private boolean[][] v;
    private int row, col;
    private Speed s = Speed.FAST;
    private Deque<Direction> x = new ArrayDeque<Direction>();
    private Map<Flower,Location> m = new HashMap<Flower,Location>();
    private Map<Long, Location> auxm = new HashMap<Long, Location>();
    private Map<Long, ArrayList<Location>> possibilities = new HashMap<Long, ArrayList<Location>>();
    private Vertex[][] searcher;
    private List<Long> known = new ArrayList<Long>();
    private List<Long> unknown = new ArrayList<Long>();
    private static final double AROMA_I = Math.pow(10,6);
    
    private void updateState() {
    	refreshState();
        row = state.location.row;
        col = state.location.col;
        result[row][col] = state;
        v[row][col] = true;
        try { for(Flower fl : state.getFlowers()) m.put(fl, state.location); }
        catch (NullPointerException e) {}
    }
    
    protected @Override void fly(Direction heading, Speed s) {
        super.fly(heading, s);
        updateState();
    }
    
    private static Direction dir(int r, int c) {
    	if(r==-1 && c==-1) return Direction.NW;
    	if(r==-1 && c==0)  return Direction.N;
        if(r==-1 && c==1)  return Direction.NE;
        if(r==1  && c==-1) return Direction.SW;
        if(r==1  && c==0)  return Direction.S;
        if(r==1  && c==1)  return Direction.SE;
        if(r==0  && c==-1) return Direction.W;
        return Direction.E;
    }   
    
    private void dfs() {
    	for(int r=-1; r<=1; r++) {
    		for(int c=-1; c<=1; c++) {
    			if (!v[(row+r+v.length)%v.length][(col+c+v[0].length)%v[0].length]) {
    				try {
    					fly(dir(r,c),s);
    					x.push(dir(r,c));
                        dfs();
                    }
                    catch (ObstacleCollisionException e) {
                    	v[(row+r+v.length)%v.length][(col+c+v[0].length)%v[0].length] = true;
                    }
                }
    		}
    	}
        if(x.isEmpty()) return;
        fly(Direction.opposite(x.pop()),s);
    }
    
    private class Vertex {
    	public TileState ts;
    	public List<Vertex> adjacent;
    	public Vertex(TileState t) {
    		ts = t;
    		adjacent = new ArrayList<Vertex>();
    	}
    }
    
    private class Flowerpath {
    	public Long id;
    	public LinkedList<Direction> path;
    	public Flowerpath(Long i, LinkedList<Direction> p) {
    		id = i;
    		path = p;
    	}
    }
    
    private void createVertices() {
    	int height = getMapHeight(), width = getMapWidth();
    	for(int r=0; r<height; r++) {
			for(int c=0; c<width; c++) {
				searcher[r][c] = new Vertex(result[r][c]);
			}
		}
		for(int r=0; r<height; r++) {
			for(int c=0; c<width; c++) {
				if(searcher[r][c].ts==null) continue;
				for(int rd=-1; rd<=1; rd++) {
					for(int cd=-1; cd<=1; cd++) {
						if(searcher[(r+rd+height)%height][(c+cd+width)%width].ts!=null && !(rd==0&&cd==0)) {
							searcher[r][c].adjacent.add(searcher[(r+rd+height)%height][(c+cd+width)%width]);
						}
					}
				}
			}
		}
    }
    
    
   private void createPossibilities() {
	   int height = getMapHeight(), width = getMapWidth();
	   ArrayList<Location> all = new ArrayList<Location>();
	   for(int i=0; i<height; i++) {
		   for(int j=0; j<width; j++) {
			   all.add(new Location(j,i));
		   }
	   }
	   for(Long l : possibilities.keySet()) {
		   possibilities.put(l, all);
	   }
   }
    
    private LinkedList<Direction> findPath(LinkedList<Vertex> vertices) {
    	Vertex[] v = vertices.toArray(new Vertex[vertices.size()]);
    	LinkedList<Direction> path = new LinkedList<Direction>();
    	for(int i=0; i<v.length-1; i++) {
    		int rd = v[i+1].ts.location.row-v[i].ts.location.row;
    		int cd = v[i+1].ts.location.col-v[i].ts.location.col;
    		if(rd<-1) rd = 1;
    		if(rd>1) rd = -1;
    		if(cd<-1) cd = 1;
    		if(cd>1) cd = -1;
    		path.add(dir(rd,cd));
    	}
    	return path;
    }
    
    private LinkedList<Direction> findPath(Vertex start, Vertex end) {
    	HashMap<Vertex,Vertex> route = new HashMap<Vertex,Vertex>();
    	HashSet<Vertex> visited = new HashSet<Vertex>();
    	LinkedList<Vertex> path = new LinkedList<Vertex>();
    	ArrayDeque<Vertex> next = new ArrayDeque<Vertex>();
    	next.offer(start);
    	visited.add(start);
    	bfs(next,visited,route,end);
    	Vertex v = end;
    	while(v!=start) {
    		path.addFirst(v);
    		v = route.get(v);
    	}
    	path.addFirst(v);
    	return findPath(path);
    }
    
    private static void bfs(ArrayDeque<Vertex> next, HashSet<Vertex> visited, HashMap<Vertex,Vertex> route, Vertex end) {
    	Vertex v = next.poll();
    	if(v==end) return;
    	for(Vertex w : v.adjacent) {
      		if(visited.contains(w)) continue;
       		visited.add(w);
       		next.offer(w);
       		route.put(w,v);
       	}
    	bfs(next,visited,route,end);
    }
    
    private Flowerpath shortestPath1() {
    	refreshState();
        row = state.location.row;
        col = state.location.col;
    	LinkedList<Direction> spath = new LinkedList<Direction>();
    	LinkedList<Direction> test = new LinkedList<Direction>();
    	Long id = null;
    	int size = Integer.MAX_VALUE;
    	for(Long l : known) {
    		test = findPath(searcher[row][col],searcher[flowerLocation(l).row][flowerLocation(l).col]);
    		if(test.size()<size) {
    			size = spath.size();
    			spath = test;
    			id = l;
    		}
    	}
    	return new Flowerpath(id, spath);
    }
    
    private Flowerpath shortestPath2() {
    	refreshState();
        row = state.location.row;
        col = state.location.col;
        int rowavg = 0, colavg = 0;
        for(Long l : known) {
        	rowavg += flowerLocation(l).row;
        	colavg += flowerLocation(l).col;
        }
        rowavg = Math.round(rowavg/known.size());
        colavg = Math.round(colavg/known.size());
        Long id = null;
        int max = Integer.MIN_VALUE;
        for(Long l : known) {
        	if(Math.abs(Math.max(flowerLocation(l).row-rowavg,flowerLocation(l).col-colavg))>max) {
        		max = Math.abs(Math.max(flowerLocation(l).row-rowavg,flowerLocation(l).col-colavg));
        		id = l;
        	}
        }
        return new Flowerpath(id,findPath(searcher[row][col],searcher[flowerLocation(id).row][flowerLocation(id).col]));
    }
    
    private Flowerpath shortestPath() {
    	if(algo==1) return shortestPath1();
    	return shortestPath2();
    }
    
    private void flyPath(Flowerpath fpath) {
    	for(Direction d : fpath.path) {
    		super.fly(d,s);
    		if(triangulate()) {
    			flyPath(shortestPath());
    			return;
    		}
    	}
    	land();
    	refreshState();
    	for(Flower f : state.getFlowers()) {
    			if(f.getFlowerId()==fpath.id) {
        			collect(f);
        			known.remove(fpath.id);
    			}
    	}
    	if(known.size()==0 && unknown.size()==0) return;
    	if(known.size()==0) {
    		while(known.size()==0) {
    	        Direction[] d = {Direction.E,Direction.SE,Direction.S,Direction.SW,
    	        		Direction.W,Direction.NW,Direction.N,Direction.NE};
    	        for(Direction r : d) {
    	        	try {
    	        		super.fly(r,s);
    	        	}
    	        	catch (ObstacleCollisionException e) {}
    	        	if(triangulate()) break;
    	        }
    		}
    	}
    	flyPath(shortestPath());
    }
    
    private boolean triangulate() {
    	boolean change = false;
    	refreshState();
    	row = state.location.row;
        col = state.location.col;
        for(Aroma a : state.getAromas()) {
        	if(unknown.contains(a.getFlowerId())) {
        		possibilities.put(a.getFlowerId(), 
        				intersect(circle(a.intensity),possibilities.get(a.getFlowerId())));
        		if(possibilities.get(a.getFlowerId()).size()==1) {
        			auxm.put(a.getFlowerId(),possibilities.get(a.getFlowerId()).get(0));
        			known.add(a.getFlowerId());
        			unknown.remove(a.getFlowerId());
        			change = true;
        		}
        	}
        }
        return change;
    }
    
    private ArrayList<Location> intersect(ArrayList<Location> a, ArrayList<Location> b) {
    	ArrayList<Location> c = new ArrayList<Location>();
    	for(Location l : a) {
    		for(Location m : b) {
    			if(l.row==m.row && l.col==m.col) c.add(l);
    		}
    	}
    	return c;
    }
    
    private ArrayList<Location> circle(double d) {
    	ArrayList<Location> loc = new ArrayList<Location>();
    	int dist = (int)Math.round(Math.sqrt(AROMA_I/d)-1);
    	HashSet<Vertex> visited = new HashSet<Vertex>();
    	ArrayDeque<Vcount> next = new ArrayDeque<Vcount>();
    	next.offer(new Vcount(searcher[row][col],0));
    	visited.add(searcher[row][col]);
    	bfsCircle(next,visited,loc,dist);
    	return loc;
    }
    
    private void bfsCircle(ArrayDeque<Vcount> next, HashSet<Vertex> visited, ArrayList<Location> loc, int dist) {
    	Vcount a = next.poll();
    	if(a.c==dist) {
    		loc.add(a.v.ts.location);
    	}
    	else {
    		for(Vertex w : a.v.adjacent) {
          		if(visited.contains(w)) continue;
           		visited.add(w);
           		next.offer(new Vcount(w,a.c+1));
           	}
    	}
    	if(next.size()!=0) bfsCircle(next,visited,loc,dist);
    }
    
    private class Vcount {
    	public Vertex v;
    	public int c;
    	public Vcount(Vertex vertex, int count) {
    		v = vertex; c = count;
    	}  	
    }
    
	/**
	 * Returns a two-dimensional array of TileStates that represents the map the
	 * butterfly is on.
	 * 
	 * During the learning phase of a simulation, butterflies are tasked with
	 * learning the map in preparation for the running phase of a simulation. 
	 * A butterfly should traverse the entire map and generate a two-dimensional
	 * array of TileStates in which each TileState corresponds to the
	 * appropriate in the map.
	 *
	 * The returned array is graded based on the percentage of correctly 
	 * identified TileStates. It is recommended that a butterfly save the 
	 * TileState array to use during the running phase of a simulation.
	 *
	 * For more information, refer to Danaus' documentation.
	 * 
	 * @return A two-dimensional array of TileStates that represents the map 
	 * 		    the butterfly is on.
	 */
	public @Override TileState[][] learn() {
		result = new TileState[getMapHeight()][getMapWidth()];
        v = new boolean  [getMapHeight()][getMapWidth()];
        updateState();
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
     * @param flowers A vector of flowers which the butterfly has to collect
     * @see danaus.AbstractButterfly#collect(Flower)
     */

	public @Override void run(List<Long> flowerIds) {
		int height = getMapHeight(), width = getMapWidth();
		searcher = new Vertex[height][width];
		createVertices();
		refreshState();
		row = state.location.row;
        col = state.location.col;
        for(Long l : flowerIds) {
        	if(flowerLocation(l)==null) {
        		unknown.add(l);
        		possibilities.put(l, new ArrayList<Location>());
        	}
        	else known.add(l);
        }
        createPossibilities();
		flyPath(shortestPath());
	}
	
	/**
	 * Return a list of all the flowers seen by this butterfly. More formally,
	 * return the union of the sets of flowers of all visited tiles. If no
	 * flowers have been found, the empty list should be returned.
	 * 
	 * @return A list of the discovered flowers, or empty if no flowers have
	 * 		been discovered.
	 */
	public @Override List<Flower> flowerList() {
		return new ArrayList<Flower>(m.keySet());
	}
	
	/**
	 * If f is in the list produced by flowerList(), return the location of f.
	 * Otherwise, return null. If f is null, return null. Note that null will be
	 * returned if the flower is not present on the map or if the flower is
	 * present on the map but has not yet been discovered.
	 * 
	 * Note that flowers are equal if and only if their flowerId's are equal. 
	 * Thus, we could pass you a Flower instance with a null location field. 
	 * Therefore, returning f.getLocation() is not always guaranteed to work. 
	 * 
	 * @param f A flower.
	 * @return The Location of f if f has been discovered. null 
	 * 		otherwise.
	 */
	public @Override Location flowerLocation(Flower f) {
		return m.get(f);
	}
	
	/**
	 * If there exists a flower <f> with the flower id <flowerId> in the list of
	 * flowers returned by flowerList(), then return the location of <f>. If 
	 * there does not exist a flower <f>, then return null. 
	 * 
	 * @param flowerId A flower id.
	 * @return The location of the flower with flower id <flowerId>. null
	 * 		otherwise.  
	 */
	public @Override Location flowerLocation(long flowerId) {
		for(Flower fl : m.keySet())
			if (fl.getFlowerId()==flowerId) return m.get(fl);
		if(auxm.containsKey(flowerId)) return auxm.get(flowerId);
		return null;
	}

}
