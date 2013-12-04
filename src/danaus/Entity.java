package danaus;

/*******************************************************************************
 * An instance represents a physical entity that can be situated on, yet exist
 * independently of, a tile. For example, flowers and butterflies are entities,
 * and cliffs and water are not entities since they define a type of tile
 * rather than exist on one.
 ******************************************************************************/
public abstract class Entity implements Comparable<Entity> {
	/** The name of the entity (eg "butterfly", "daisy", "rose"). */
	protected final String name;
	
	/** The location of the entity (eg (1,2), (-3,400)). */
	protected Location location;
	
	/** The filename of the image corresponding to this entity (eg
	 * "daisy.gif", "butterfly.png"). This is automatically assigned to be
	 * name + ".png". */
	protected final String imageFilename;
	
    /**************************************************************************
     * Constructor: an Entity with name n, initial location loc, and image
     * with name n + <.png>. <br><br>
     * The default constructor is omitted because an Entity needs both a
     * name and a location to exist.
     * 
     * @param n The name of the entity.
     * @param loc The initial location of the entity. 
     **************************************************************************/
    Entity(String n, Location loc) {
        name = n;
        location = loc;
        imageFilename = name + ".png";
    }
    
    /***************************************************************************
     * Return the name of the entity. 
     **************************************************************************/
    public String getName() {
        return name;
    }

    /***************************************************************************
     * Return the location of the entity. 
     **************************************************************************/
    public Location getLocation() {
        return location;
    }

    /***************************************************************************
     * Set the location of this entity to (x, y).
     * 
     * @param x The x coordinate of the new location.
     * @param y The y coordinate of the new location.
     **************************************************************************/
    void setLocation(int x, int y) {
        setLocation(new Location(x, y));
    }

    /***************************************************************************
     * Set the location of this entity to loc. 
     **************************************************************************/
    void setLocation(Location loc) {
        location= loc; 
    }

    /***************************************************************************
     * Return the file name of the image for this entity. 
     **************************************************************************/
    public String getImageFilename() {
        return imageFilename;
    }

    /***************************************************************************
     * Return a string representation of this entity. 
     **************************************************************************/
    public @Override String toString() {
        return name + " at " + location;
    }
	
    /***************************************************************************
     * Return true iff obj is an Entity and this entity and 
     * obj have the same name, location, and image name.
     **************************************************************************/
    public @Override boolean equals(Object obj) {
        if (! (obj instanceof Entity)) {
            return false;
        }

        Entity e= (Entity) obj;
        return name.equals(e.name) && 
                location.equals(e.location) &&
                imageFilename.equals(e.imageFilename);
    }

    /***************************************************************************
     * Return a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by java.util.Hashtable.
     **************************************************************************/
    public @Override int hashCode() {
        int result= name.hashCode() ^ (name.hashCode() >>> 16);
        result= 31 * result + (location.hashCode() ^ 
                (location.hashCode() >>> 16));
        return result;
    }

    /***************************************************************************
     * Return a negative integer, zero, or positive integer depending on
     * whether this object is less than, equal to, or greater than e. <br><br>
     * 
     * The comparison is made first on the entities' names, then their
     * locations, and finally on their image filenames.
     **************************************************************************/
    public @Override int compareTo(Entity e) {
        if (!name.equals(e.name)) {
            return name.compareTo(e.name);
        }

        if (!location.equals(e.location)) {
            return location.compareTo(e.getLocation());
        }

        return imageFilename.compareTo(e.imageFilename);
    }
}
