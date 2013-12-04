package danaus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/* *************************************************************************//**
 * An instance is a graphical representation of a tile on the map.
 * ****************************************************************************/
public class GUITile {
	/** The original size background tile image (e.g. land or water). */
	public BufferedImage image;
	/** A cached copy of the tile that may be a different size. */
	public BufferedImage cachedImage;
	/** The row of the GUITile. */
	private int row; 
	/** The column of the GUITile. */
	private int col;
	/** True if the tile has been visited by a butterfly. */
	boolean visited;
	/** The images of the flowers at the tile. */
	List<BufferedImage> flowers;
	
	/** Constructor: an instance that represents tile tile  on (row, col).
	 * 
	 * @param tile The tile this GUITile represents.
	 * @param row The row of the tile.
	 * @param col The column of the tile.
	 * ************************************************************************/
	public GUITile(Tile tile, int row, int col) {
		image = Common.load_image(tile.tileFilename);
		cachedImage = image;
		flowers = load(tile.tileState.flowers);
		
		this.row = row; 
		this.col = col;
	}
	
	/** Load list flowers into this object and return a vector of them.
	 * 
	 * @param flowers2 A list of flowers.
	 * @return A list of flower images.
	 * ************************************************************************/
	public static List<BufferedImage> load(List<Flower> flowers2) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (Flower flower : flowers2) {
			BufferedImage flowerImg = Common.load_image(flower.imageFilename);
			images.add(flowerImg);
		}
		return images;
	}
	
	/** Draw this tile on the map, using g, at topleft corner
	 * (x, y). The tile side length is s.
	 * 
	 * @param g The graphics to paint with.
	 * @param s The side length of the tile.
	 * @param x The x coordinate of the top left of the tile.
	 * @param y The y coordinate of the top left of the tile. */
	public void draw(Graphics g, int s, int x, int y) {
		g.drawImage(getTile(s), x, y, null);
		for (BufferedImage flower : flowers) {
			g.drawImage(flower, x, y, null);
		}
	}
	
	/** Return this image with side length s. If the size requested matches the
	 * cached image, it is returned. Otherwise, the image is resized and cached
	 * before return.
	 * 
	 * @param s The side length of the requested image.
	 * @return An appropriately sized image. */
	private Image getTile(int s) {
		// TODO this is needed for zooming.
//		if (tileCache.getHeight(null) != sideLength) {
//			tileCache = tile.getScaledInstance(
//					sideLength, sideLength, Image.SCALE_DEFAULT);
//		}
		return cachedImage;
		
	}
	
	/** Return the top left point of the tile, which has side length s. */
	public Point getTopLeft(int s) {
		return new Point(col*s, row*s);
	}
}
