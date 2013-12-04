package danaus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* *************************************************************************//**
 * An instance represents the GUI representation of a map.
 * ****************************************************************************/
@SuppressWarnings("serial")
public class GUIMap extends JPanel implements MouseListener, MouseMotionListener, 
MouseWheelListener, ActionListener {
	/** The main painting loop's frames per second. */
	private static int GUI_FPS = 60;
	/** The main painting loop's delay for the timer. */
	private static int GUI_DELAY = 1000 / GUI_FPS;
	/** The minimum frames per move. */
	public static int MIN_FPM = 1;
	/** The minimum frames per move. */
	public static int MAX_FPM = 180;
	/** The minimum frames per move. */
	public static int INIT_FPM = 30;
	/** The number of pixels zoomed in or out for every scroll of the mouse. */
	@SuppressWarnings("unused")
	private static int MOUSE_WHEEL_CONSTANT = 8;
	
	/** The array of GUITiles: the heart of the map. */
	private GUITile[][] tiles;

	/** The gui version of the butterfly */
	private GUIButterfly butterfly;

	/** The total rows in the map. */
	private int rows;
	/** The total columns in the map. */
	private int cols;
	/** The side length of a GUITile. */
	private int sideLength;
	
    /** The top left corner of the abstract camera. */
	private Point cameraTopLeft;
	/** The bottom right corner of the abstract camera. */
	private Point cameraBottomRight;
	
    /** The current frames per move. */
	public int fpm;
	
    /** The ain painting loop timer. */
	Timer timer;
	
    /** True if the camera should center on the butterfly. */
	public boolean lockOn;
	
    /** The most recently pressed mouse location. */
	private Point pressed;
	
    /** The gui that contains this map. Used to send signals upward. */
	GUI gui;
	
    /***************************************************************************
     * Constructor: an instance for gui with butterfly bfly and tiles tiles. 
     **************************************************************************/
    GUIMap(GUI gui, AbstractButterfly bfly, Tile[][] tiles) {
		/* Set basic GUI attributes. */
		setBackground(GUI.MARGIN_COLOR);
		setPreferredSize(new Dimension(600, 400));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		this.gui = gui;
		
		/* Initialize both camera points, though not necesarily to the right
		 * locations. */
		cameraTopLeft = new Point();
		cameraBottomRight = new Point();
		updateCamera();
		
		/* Load the tiles */
		sideLength = 32;
		rows = tiles.length;
		cols = tiles[0].length;
		this.tiles = new GUITile[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				this.tiles[row][col] = new GUITile(tiles[row][col], row, col);
			}
		}
		
		/* Initialize the butterfly and fpm. */
		fpm = INIT_FPM;
		butterfly = new GUIButterfly(this, getMapWidth(), getMapHeight());
		int x = (bfly.location.col) * sideLength;
		int y = ((bfly.location.row) * sideLength) - (sideLength / 2);
		butterfly.xTopLeft = x;
		butterfly.yTopLeft = y;
		
		/* The first tile is visited, even if it is never moved to. */
		GUITile tile = this.tiles[bfly.location.row][bfly.location.col];
		tile.visited = true;
		Common.change_brightness(tile.cachedImage, 0.8f);
		
		/* Begin the main painting loop. */
		timer = new Timer(GUI_DELAY, this);
		timer.start();
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Animation
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Paints the map, tiles, and butterfly;
	 * ************************************************************************/
	protected @Override void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		/* Update the camera */
		updateCamera(); 
		if (lockOn) {
			centerCamera(butterfly.getTopLeft());
		}
		
		/* Draw the tiles. */
		int minRow = cameraTopLeft.y / sideLength;
		int maxRow = cameraBottomRight.y / sideLength + 1;
		int minCol = cameraTopLeft.x / sideLength;
		int maxCol = cameraBottomRight.x / sideLength + 1;
		for (int row = minRow; row < maxRow; row++) {
			for (int col = minCol; col < maxCol ; col++) {
				int modRow = Common.mod(row, rows);
				int modCol = Common.mod(col, cols);
				int xTopLeft = col * sideLength - cameraTopLeft.x;
				int yTopLeft = row * sideLength - cameraTopLeft.y;
				
				tiles[modRow][modCol].draw(g, sideLength, xTopLeft, yTopLeft);
			}
		}
				
		/* Update and draw the butterfly. */
		butterfly.moveAdd(60 / GUI_FPS); // GUI_FPS cannot be higher than 60!
		if (inCamera(butterfly.getTopLeft())) {
			butterfly.draw(g, (int)butterfly.xTopLeft - cameraTopLeft.x,
					          (int)butterfly.yTopLeft - cameraTopLeft.y);
		}
		else {
            Point topLeft = inCameraPhantom(butterfly.getTopLeft());
            if (topLeft != null) {
			butterfly.draw(g,
					topLeft.x - cameraTopLeft.x,
			        topLeft.y - cameraTopLeft.y);
            }
		}
				
		g.dispose();
	}
	

    /***************************************************************************
     * Re-tile the map given by tiles. <br><br>
     * 
     * This usually takes place after the learning phase has been completed, 
     * because tiles may have sprouted flowers, so each GUITile updates
     * its flowers.
     * 
     * @param tiles The map.
     **************************************************************************/
	public void retile(Tile[][] tiles) {
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				this.tiles[row][col].flowers = 
						GUITile.load(tiles[row][col].tileState.flowers);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Camera Movement
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Translate the camera. The top left corner of the camera is always within
	 * the actualy map. The bottom right corner can stray into phantom map 
	 * territory. 
	 * 
	 * @param x The x coordinate of the top left of the camera.
	 * @param y The y coordinate of the top left of the camera.
	 * ************************************************************************/
	private void moveCamera(int x, int y) {
		cameraTopLeft.x = Common.mod(x, getMapWidth());
		cameraTopLeft.y = Common.mod(y, getMapHeight());
		updateCamera();
	}
	
	/* *********************************************************************//**
	 * Translate the camera, but just a nudge: by (dx, dy)
	 * ************************************************************************/
	private void nudgeCamera(int dx, int dy) {
		moveCamera(cameraTopLeft.x + dx, cameraTopLeft.y + dy);
	}
	
	/* *********************************************************************//**
	 * Center the camera at a point center.
	 * ************************************************************************/
	private void centerCamera(Point center) {
		moveCamera(center.x - (getCameraWidth() / 2), 
				   center.y - (getCameraHeight() / 2));
	}
	
	/* *********************************************************************//**
	 * Update the camera's bottom right corner. The top left corner of the 
	 * camera is always adjusted and the bottom right corner is then updated
	 * accordingly.
	 * ************************************************************************/
	private void updateCamera() {
		/* This check is done in case the map is smaller than the screen. */
		int dwidth = Math.min(getMapWidth(), getWidth());
		int dheight = Math.min(getMapHeight(), getHeight());
		cameraBottomRight.x = cameraTopLeft.x + dwidth;
		cameraBottomRight.y = cameraTopLeft.y + dheight;
	}
	
	/* *********************************************************************//**
	 * Returns true iff a map entity is within the real map.
	 * 
	 * @param topLeft The top left corner of the entity.
	 * @return True if the entity is within the real map.
	 * ************************************************************************/
	private boolean inCamera(Point topLeft) {
//		System.out.println("s  " + topLeft.x ); 
//		System.out.println(topLeft.y ); 
//		System.out.println(cameraTopLeft.x - sideLength); 
//		System.out.println( cameraTopLeft.y - sideLength); 
//		System.out.println( cameraBottomRight.x ); 
//		System.out.println( cameraBottomRight.y); 

		return topLeft.x >= cameraTopLeft.x - sideLength &&
			   topLeft.y >= cameraTopLeft.y - sideLength &&  
			   topLeft.x <= cameraBottomRight.x && 
			   topLeft.y <= cameraBottomRight.y; 
	}
	
	/* *********************************************************************//**
	 * Returns the phantom position of the top left corner of an entity if it 
	 * is within the map or phantom map. Returns null otherwise.
	 * 
	 * @param topLeft The top left corner of the entity.
	 * @return The top left corner of the phantom entity or null.
	 * ************************************************************************/
	private Point inCameraPhantom(Point topLeft) {		
		// move down
		topLeft.y += getMapHeight();
		if (inCamera(topLeft)) {
			return topLeft; 
		}
		// move down right
		topLeft.x += getMapWidth();
		if (inCamera(topLeft)) {
			return topLeft;
		}
		// move right
		topLeft.y -= getMapHeight();
		if (inCamera(topLeft)) {
			return topLeft;
		}
	
		return null;
	}
	
	/* *********************************************************************//**
	 * Returns the current width of the camera, in pixels.
	 * ************************************************************************/
	private int getCameraWidth() {
		return cameraBottomRight.x - cameraTopLeft.x;
	}
	
	/* *********************************************************************//**
	 * Returns the current width of the camera, in pixels.
	 * ************************************************************************/
	private int getCameraHeight() {
		return cameraBottomRight.y - cameraTopLeft.y;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Helper Methods 
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Updates the butterfly's fpm.
	 * ************************************************************************/
	public void updateFPM() {
		butterfly.updateFPM(fpm);
	}
	
	/* *********************************************************************//**
	 * Begins the move process for a butterfly.
	 * ************************************************************************/
	public void move(int s, Direction d, int toRow, int toCol) {
		if (!tiles[toRow][toCol].visited) {
			GUITile tile = tiles[toRow][toCol];
			tile.visited = true;
			Common.change_brightness(tile.cachedImage, 0.8f);
		}
		butterfly.move(s, d, toRow, toCol, sideLength);
	}
	
	/* *********************************************************************//**
	 * Returns the current width of the map, in pixels.
	 * 
	 * @return The width of the map.
	 * ************************************************************************/
	private int getMapWidth() {
		return cols * sideLength;
	}
	
	/* *********************************************************************//**
	 * Returns the current height of the map, in pixels.
	 * 
	 * @return The height of the map.
	 * ************************************************************************/
	private int getMapHeight() {
		return rows * sideLength;
	}
	
	/* *********************************************************************//**
	 * Returns the column and row of the tile at relative position (relx, rely)
	 * in the format (col, row). 
     * <br>
     * <br>
     * (relx, rely) are the (x, y) coordinates relative to the top left corner 
     * of the map's pane. When added to the absolute (x,y) of the top left of 
     * the camera, we get the absolute position of (relx, rely). Taking the 
     * result mod the map width and height handles wrap around. Finally, 
     * dividing by sideLength yields the column and row of the tile.
	 * ************************************************************************/
	private Point getPositionFromClick(int relx, int rely) {
		int x = Common.mod(cameraTopLeft.x + relx, getMapWidth()) / sideLength;
		int y = Common.mod(cameraTopLeft.y + rely, getMapHeight()) / sideLength;
		return new Point(x,y);
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Callbacks
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Double clicking the mouse centers the map around the map click.
	 * ************************************************************************/
	public @Override void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Point center = new Point();
			center.x = cameraTopLeft.x + e.getX();
			center.y = cameraTopLeft.y + e.getY();
			centerCamera(center);
		}
		else if (e.getClickCount() == 1) {
			gui.updateTileInfo(getPositionFromClick(e.getX(), e.getY()));
		}
	}

	/* *********************************************************************//**
	 * Anchor the mouse press for dragging.
	 * ************************************************************************/
	public @Override void mousePressed(MouseEvent e) {
		if (!lockOn) {
			pressed = e.getPoint();
		}
	}

	/* *********************************************************************//**
	 * Nudge the map a final time after dragging. Like dragging, this is 
	 * enabled only if locking is enabled.
	 * ************************************************************************/
	public @Override void mouseReleased(MouseEvent e) {
		if (!lockOn) {
			Point released = e.getPoint();
			int dx = released.x - pressed.x;
			int dy = released.y - pressed.y;
			nudgeCamera(-dx, -dy);
		}
		pressed = null;
	}

	public @Override void mouseEntered(MouseEvent e) {}	
	public @Override void mouseExited(MouseEvent e) {}

	/* *********************************************************************//**
	 * As the mouse is dragged, so too is the map. Dragging is enabled only
	 * if locking is not enabled.
	 * ************************************************************************/
	public @Override void mouseDragged(MouseEvent e) {
		if (!lockOn) {
			Point current = e.getPoint();
			int dx = current.x - pressed.x;
			int dy = current.y - pressed.y;
			nudgeCamera(-dx, -dy);
			pressed = current;
		}
	}

	/* *********************************************************************//**
	 * TODO
	 * ************************************************************************/
	public @Override void mouseMoved(MouseEvent e) {
		// TODO: track mouse for smart zoom
	}
	
	/* *********************************************************************//**
	 * TODO
	 * ************************************************************************/
	public @Override void mouseWheelMoved(MouseWheelEvent e) {
		// TODO: zoom; make sure to update the butterfly's map width and 
		// map height!
	}

	/* *********************************************************************//**
	 * This method is invoked by the gui's timer. It controls the main FPS of
	 * the GUI.
	 * ************************************************************************/
	public @Override void actionPerformed(ActionEvent e) {
		repaint();
	}
}
