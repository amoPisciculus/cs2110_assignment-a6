package danaus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;

/* *************************************************************************//**
 * An instance represents an animated butterfly. Inspiration and information
 * for writing this class was taken heavily from 
 * http://docs.oracle.com/javase/tutorial/uiswing/components/slider.html
 * ****************************************************************************/
public class GUIButterfly implements ActionListener {
	/** The number of unique frames in a flap sequence. */
	private static int UNIQUE_FRAMES = 3;
	/** The sequence of frame indices that constitute a flap animation. */
	private static int[] FRAME_SEQUENCE = new int[] {0, 1, 2, 2, 1, 0};
	/** The number of frames in a flap animation. */
	private static int FRAMES_PER_FLAP = FRAME_SEQUENCE.length;
	/** The frames per second of the flapping animation. */
	private static int FLAP_FPS = 10;
	/** The timer delay of the flapping animation. */
	private static int FLAP_DELAY = 1000 / FLAP_FPS;
	
    /** The current frame index. */
	private int frame;
	
    /** The flapping timer. */
	Timer flapTimer;
	
	/** indices into the array of flapping frames. */
	public static int NORTH_INDEX = 0;
	public static int EAST_INDEX = 1;
	public static int SOUTH_INDEX = 2;
	public static int WEST_INDEX = 3;
	
    private int directionIndex;

    /** An array of frame arrays. Each array of frames is a flapping animation
     * in a given direction. */
    @SuppressWarnings("unused")
	private BufferedImage[][] directions;
    private BufferedImage[] north;
    private BufferedImage[] east;
    private BufferedImage[] south;
    private BufferedImage[] west;

    private BufferedImage[][] directionsCache;
    private BufferedImage[] northCache;
    private BufferedImage[] eastCache;
    private BufferedImage[] southCache;
    private BufferedImage[] westCache;

    /** The x coordinate of the Butterfly's top left point. */
    public float xTopLeft;
    /** The y coordinate of the Butterfly's top left point. */
    public float yTopLeft;
    /** The width of the map the butterfly is on. */
    int mapWidth;
    /** The height of the map the butterfly is on. */
    int mapHeight;

    /** The current move count. When this reaches the fpm, a move is done. */ 
    int moveCount;

    /** x-coordinate of the top left corner of the current move destination. */ 
    int destXTopLeft;
    /** y-coordinate of the top left corner of the current move destination. */ 
    int destYTopLeft;

    /** The change in x required for the current move. */ 
    int dx;
    /** The change in y required for the current move. */ 
    int dy;

    /** The number of frames required for a given move. */ 
    int fpm;

    /** The fpm adjusted for slow or fast moves. */
    int slowfpm;

    /** True iff the butterfly should stop moving. Flapping still occurs when
     * frozen. This is useful to see if the GUI has died or if the butterfly 
     * is simply landing repeatedly. */
    boolean frozen;

    /** The amount to slow down a move. */
	int slowDownFactor;
	
    /** The GUIMap on which this GUIButterfly is drawn. */
	GUIMap map;
	
    /***************************************************************************
     * Constructor: an instance on map map with width width and height height. 
     **************************************************************************/
    public GUIButterfly(GUIMap map, int width, int height) {
		initFrames();
		flapTimer = new Timer(FLAP_DELAY, this);
		flapTimer.start();
		
		moveCount = -1;
		fpm = GUIMap.INIT_FPM;
		slowDownFactor = 1;
		
		this.map = map;
		mapWidth = width;
		mapHeight = height;
	}
	
	/* *********************************************************************//**
	 * Initializes the flapping animation frames.
	 * ************************************************************************/
	private void initFrames() {
		north = initFrames("north");
		east = initFrames("east");
		south = initFrames("south");
		west = initFrames("west");
		directions = new BufferedImage[][] {north, east, south, west};
		
		northCache = initFrames("north");
		eastCache = initFrames("east");
		southCache = initFrames("south");
		westCache = initFrames("west");
		directionsCache = new BufferedImage[][] {
				northCache, eastCache, southCache, westCache
		};
	}
	
	/* *********************************************************************//**
	 * Initializes a deck of images for a flapping animation.
	 * ************************************************************************/
	private BufferedImage[] initFrames(String direction) {
		BufferedImage[] frames = new BufferedImage[UNIQUE_FRAMES];
		for (int i = 0; i < UNIQUE_FRAMES; i++) {
			String filename = "res/butterfly/butterfly_" + direction;
			filename += "_" + i + ".png";
			frames[i] = Common.load_image(filename);
		}
		return frames;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Movement
	////////////////////////////////////////////////////////////////////////////
    /***************************************************************************
     * Move the butterfly to row-column (toRow, toCol) with speed s and in
     * direction d. sideLength is the length of a side of the map's tiles. 
     **************************************************************************/
	public void move(int s, Direction d, int toRow, int toCol, int sideLength) {
		/* Setting the move count to a non negative integer allows it to be
		 * incremented. */
		moveCount = 0;
		slowDownFactor = s + 2; // -1 if full fast
		slowfpm = fpm * slowDownFactor;
		updateDirection(d);
		
		/* These destinations can be in the phantom map. */
		destXTopLeft = toCol * sideLength;
		destYTopLeft = toRow * sideLength - (sideLength / 2);
		dx = (int) (destXTopLeft - xTopLeft);
		dy = (int) (destYTopLeft - yTopLeft);
		
		if (Math.abs(dx) > sideLength) {
			dx = -1 * Common.unit_scalar(dx) * (mapWidth - Math.abs(dx));
		}
		if (Math.abs(dy) > sideLength) {
			dy = -1 * Common.unit_scalar(dy) * (mapHeight - Math.abs(dy));
		}
	}
	
	/* *********************************************************************//**
	 * Ends a move.
	 * ************************************************************************/
	private void endMove() {
		moveCount = -1;
		map.gui.wakeupSimulator();
	}
	
	/* *********************************************************************//**
	 * True if the butterfly is moving.
	 * ************************************************************************/
	public boolean isMoving() {
		return 0 <= moveCount;
	}
	
	/* *********************************************************************//**
	 * If a move is occurring, add addend to the move count.
	 * ************************************************************************/
	public void moveAdd(int addend) {
		if (frozen) {
			return;
		}
		if (isMoving()) {
			moveCount += addend;
			updatePosition();
		}
	}
	
	/* *********************************************************************//**
	 * Updates the butterfly's position based on the move count and fpm.
	 * ************************************************************************/
	private void updatePosition() {
		/* If the move is complete, force yourself to the destination. This 
		 * fixes bugs if the user is sliding the fpm during movement. */
		if (slowfpm<= moveCount) {
			xTopLeft = destXTopLeft;
			yTopLeft = destYTopLeft;
			endMove();
		}
		else {
//			System.out.println((int) (dx / (float)moveCount));
//			System.out.println((int) (dy / (float)moveCount));
//			System.out.println("");
			nudge(getNudgeX(), getNudgeY());
		}
	}
	
	/* *********************************************************************//**
	 * Nudges the butterfly's position along by (dx, dy)
	 * ************************************************************************/
	public void nudge(float dx, float dy) {
		xTopLeft = Common.mod(xTopLeft + dx, mapWidth);
		yTopLeft = Common.mod(yTopLeft + dy, mapHeight);
	}
	
	/* *********************************************************************//**
	 * Returns the small amount to nudge the butterfly each turn in the x 
     * direction.
	 * ************************************************************************/
	private float getNudgeX() {
		return dx / (float)slowfpm;
	}
	
	/* *********************************************************************//**
	 * Returns the small amount to nudge the butterfly each turn in the y
     * direction.
	 * ************************************************************************/
	private float getNudgeY() {
		return dy / (float)slowfpm;
	}
	
	/* *********************************************************************//**
	 * Change the butterfly's fpm to fpm. This is triggered by the fps slider at the
	 * top of the GUI.
	 * ************************************************************************/
	public void updateFPM(int fpm) {
		if (GUIMap.MAX_FPM == fpm) {
			frozen = true;
		}
		else {
			if (true == frozen) {
				frozen = false;
			}
			this.fpm = fpm;
			this.slowfpm = fpm * slowDownFactor;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Returns the top left corner of the butterfly.
	 * ************************************************************************/
	public Point getTopLeft() {
		return new Point((int)xTopLeft, (int)yTopLeft);
	}
	
	/* *********************************************************************//**
	 * If d is not null, change the butterfly's direction to d.
	 * ************************************************************************/
	private void updateDirection(Direction d) {
		if (d == null) {
			return;
		}
		
		if (Direction.N == d) {
			directionIndex = NORTH_INDEX;
		}
		else if (Direction.S == d) {
			directionIndex = SOUTH_INDEX;
		}
		else if (Direction.W == d || Direction.NW == d || Direction.SW == d) {
			directionIndex = WEST_INDEX;
		}
		else {
			directionIndex = EAST_INDEX;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Drawing
	////////////////////////////////////////////////////////////////////////////
	/* *********************************************************************//**
	 * Draws the butterfly at top-left position (x, y) using graphics g. The 
     * butterfly is told where to be drawn because it doesn't know if it's in 
     * the phantom zone or not.
	 * ************************************************************************/
	public void draw(Graphics g, int xTopLeft, int yTopLeft) {
		g.drawImage(getFrame(), xTopLeft, yTopLeft, null);
	}
	
	/* *********************************************************************//**
	 * Returns the current frame in the flap animation.
	 * ************************************************************************/
	public Image getFrame() {
		return directionsCache[directionIndex][FRAME_SEQUENCE[frame]];
	}
	
	/* *********************************************************************//**
	 * Increment the frame. Triggered by the flap timer.
	 * ************************************************************************/
	public @Override void actionPerformed(ActionEvent e) {
		frame = Common.mod(frame + 1, FRAMES_PER_FLAP);
	}
}
