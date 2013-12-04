package danaus;

import java.util.*;

import org.xml.sax.SAXException;   
import java.io.IOException;  	   
import javax.xml.parsers.*; 	   
import java.util.Scanner;   	   
import java.util.regex.Matcher;    
import java.util.regex.Pattern;    
import org.w3c.dom.*;			   

/** 
 * An instance represents a map: a two-dimensional array of tiles
 * along with some basic information and settings. A Map can be generated
 * randomly, from map files, or from a combination of the two. 
 */
class Map {
	////////////////////////////////////////////////////////////////////////////
	// Map Essentials
	////////////////////////////////////////////////////////////////////////////
	/** The tile text tokens used in map XML files. */
	private static final String LAND_TOKEN      = "#";
	private static final String WATER_TOKEN     = "~";
	private static final String FOREST_TOKEN    = "|";
	private static final String CLIFF_TOKEN     = "^";
	private static final String BUTTERFLY_TOKEN = "B";
	
	/** When true, an ascii representation of the map is printed to the screen
     * during generation. This process is known as drawing. */
	private static final boolean SKETCH = false;
    /** Time to sleep after every sketch, in milliseconds. */
	private static final int SKETCH_TIME = 10;
	/** When true, an ascii representation of the map is printed to the screen
     * during flight. This process is known as drawing. */
	private static final boolean DRAW = false;
    /** Time to sleep after every draw, in milliseconds. */
	private static final int DRAW_TIME = 100;
	/** If true, drawing and sketching are instantaneous. */
	private boolean instadraw = false;
	
    /** A rectangular grid of tiles. */
	public Tile tiles[][];
	/** The width of the map (in columns). */
	private int width;
	/** The height of the map (in rows). */
	private int height;
	
    /** The butterfly on the map, or null if no butterfly is on the map. */
	AbstractButterfly butterfly;
	/** The butterfly's initial position. This is used for random butterfly
	 * generation. */
	private Position butterflyStart;
	
    /** The park this map belongs to, or null if it does not belong to a park.*/
	public final Park park;
    
    /** All the flower positions on the map during learning. */
    private List<Position> learningFlowerPositions;
    /** All the flower positions added to the map during runtime. */
    private List<Position> runningFlowerPositions;
    /** All the flowers on the map during learning. */
    List<Flower> learningFlowers;
    /** All the flowers added to the map during runtime. */
    List<Flower> runningFlowers;
	
	////////////////////////////////////////////////////////////////////////////
	// Map Settings
	////////////////////////////////////////////////////////////////////////////
	/** The skin of the map tiles (eg snow). */
	private String skin;
	
	/** True if light should be randomized. */
	private boolean randomLight;
	/** User defined default light value. */
	private int default_light;
	/** The user defined minimum random light value. */
	private int min_light;
	/** The user defined maximum random light value. */
	private int max_light;
	
	/** True if wind should be randomized. */
	private boolean randomWind;
	/** User defined default wind value. */
	private Wind default_wind;
	/** The user defined minimum wind value. */
	private int min_wind;
	/** The user defined maximum wind value. */
	private int max_wind;
	
	/** True if flowers should be randomized. */
	private boolean randomFlowers;
	/** The expected number of flowers generated on a map during learning. */
	private int expected_learning_flowers;
	/** The expected number of additional flowers generated on a map during
	 * runtime. */
	private int expected_running_flowers;
	

    /** True if aromas should be randomized. */
    private boolean randomAromaIntensity;
    /** User defined default aroma value. */
    private double default_aroma_intensity;
    /** The user defined minimum aroma value. */
    private double min_aroma_intensity;
    /** The user defined maximum aroma value. */
    private double max_aroma_intensity;

    /** Convenient indices into an XML cell generated from a map file.
     * <br>
     * If a map is generated from a map file, each tile is parsed from an XML
     * cell. The cell contains different fields, each with a different index. 
     * These indices can be used to extract the desired features from the XML
     * cell. For example. cell[TYPE_INDEX] accesses the type of the cell while
     * cell[AROMA_INDEX] accesses the aromas of the cell. */
    private static final int TYPE_INDEX   = 0;
    private static final int LIGHT_INDEX  = 1;
    private static final int WIND_INDEX   = 2;
    private static final int FLOWER_INDEX = 3;
    private static final int AROMA_INDEX  = 4;

    ////////////////////////////////////////////////////////////////////////////
    // Random Generation Constants
    ////////////////////////////////////////////////////////////////////////////
    /** The inclusive lower bound of a randomly generated map height. */
    private static final int MIN_HEIGHT = 80;
    /** The incluse upper bound of a randomly generated map height. */
    private static final int MAX_HEIGHT = 80;
    /** The lower bound on a randomly generated map width. */
    private static final int MIN_WIDTH = 80;
    /** The upper bound on a randomly generated map width. */
    private static final int MAX_WIDTH = 80;
    
    /** The desired fraction of map tiles that are not water (ie Land, Forest,
     * or Cliff.). The actual fraction of non-water tiles is not guaranteed to
     * equal LAND_FRACTION, but is guaranteed to be a close approximation. */
    private static final double LAND_FRACTION = 0.6;

    /** Out of 10, the probability that a frontiersman will be selected from 
	 * the free frontier. For example, a free probability of 8 signifies that
     * 80% of the time, a tile from the free frontier will be grown.
     * <br>
     * The smoothness of a map's border is inversely proportional to
     * FREE_PROBABILITY. That is, a larger FREE_PROBABILITY will produce a more
     * jagged border.
     * 
     * @see danaus.Map#initRandomTiles() */
    private static final int FREE_PROBABILITY = 8;
	
	/** Out of 1000, the probability that a land tile will be seeded as a 
     * cliff. As CLIFF_SEED_PROBABILITY grows larger, the number of cliff
     * ranges on the map increases. The average length of each cliff range also
     * decreases, as the total number of cliffs is held constant and determined
     * by CLIFF_FRACTION. */
    private static final int CLIFF_SEED_PROBABILITY = 20;
	
    /** The desired fraction of map tiles that are cliffs. This is
     * distinct from CLIFF_SEED_PROBABILITY which dealt with mountain seeding,
     * not total cliff percentage. The actual fraction of tiles that are cliffs
     * is not guaranteed to match CLIFF_FRACTION. Usually, CLIFF_FRACTION is
     * larger than the true fraction of cliffs. */ 
    private static final double CLIFF_FRACTION = 0.1;
	
    /** The average length of a cliff range. */
	private static int AVERAGE_CLIFF_LENGTH;
	
    /** The fraction of AVERAGE_CLIFF_LENGTH that the length of a cliff range
     * varies. For example, a DELTA_CLIFF_LENGTH of 0 signifies that all cliff
     * ranges should ideally be the same length. In practice, mountain ranges
     * may be shorter than DELTA_CLIFF_LENGTH. */
    private static double DELTA_CLIFF_LENGTH;
	
    /** The maximum length of a cliff range. This maximum is guaranteed. */
	private static int MIN_CLIFF_LENGTH;
	/** The ideal minimum length a cliff range. */
	private static int MAX_CLIFF_LENGTH;

    /** Out of (1000-CLIFF_SEED_PROBABILITY), the probability that a land tile
     * will be seeded as a forest. For example, a forest probability of
     * 10 + CLIFF_PROBABILITY means that (10/1000) land tiles will be seeded as
     * a forest. */
    private static final int FOREST_SEED_PROBABILITY = 10 +
    CLIFF_SEED_PROBABILITY;
	
    /** The desired fraction of map tiles that are forest. This is
     * different than FOREST_SEED_PROBABILITY because FOREST_SEED_PROBABILITY
     * relates to the probability of a tile being seeded. This relates to the
     * actual forest fraction after random generation is complete. */
    private static final double FOREST_FRACTION = .3; 
	
    /** Out of 1000, the probability that a forest's neighbor will become a 
	 * tree during forest growth. @see danaus.Map#initRandomTiles(). */
	private static int FOREST_GROW_PROBABILITY = 625; // 5/8
	
    /** The maximum number of forests. Once the width and height of a map are 
	 * calculated, this will be equal to width * height * LAND_FRACTION *
	 * FOREST_FRACTION. */
	private static int MAX_FORESTS;
	
    /** The actual number of forest tiles on the map. */
	private int numForests;
	
	/** The total number of flower images available. */
	private static final int NUMBER_FLOWERS = 100;
	
    /** The list of all available flower image number suffixes. flower images
     * are saved in the format "flower_<x>.png" where <x> is a number.*/
	private static final Integer[] FLOWER_NUMBERS;
	
    /** Out of 1000, the probability that a land tile will be seeded with a
     * flower. */
    private static int FLOWER_SEED_PROBABILITY;
	
	/** All possible directions. */
	private static final Direction[] DIRECTIONS = Direction.values();

    /** The seed used by rand. This seed can be printed and reused to replicate
     * random maps. */
    public int seed; 
	
    /** The map's random number generator. A map uses a single randomizer to
     * generate all random values. This ensures that if two seeds are equal,
     * then the maps generated from them are also equal. */
    Randomer rand; 

	static {
		/* Flower files range from flower_1 to flower_100 */
		FLOWER_NUMBERS = new Integer[NUMBER_FLOWERS];
		for (int i = 0; i < NUMBER_FLOWERS; i++) {
			FLOWER_NUMBERS[i] = i+1;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Parsers and Generators
	////////////////////////////////////////////////////////////////////////////
	/** Constructor: a fully randomized map instance in park. No values are 
     * read from the user. */
	Map(Park park) {
		Debugger.DEBUG("Constructing randomly generated map...");
		
		this.park = park;
		initInitialSettings();
		initRandom();
		initNormalize();
		updateParkStateFly();
		Debugger.DEBUG("Randomly generated map constructed.");
	}
	
	/**
     * Constructor: a map instance in <em>park</em> constructed from a map file
     * <em>filename</em>.
	 * 
	 * Precondition: The filename is not null.
	 *  
     * @param filename The filename of the map file from which the map is
     * constructed.  
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	Map(Park park, String filename) 
            throws ParserConfigurationException, SAXException, IOException {
		Debugger.DEBUG("Parsing XML file...");
		
		this.park = park;
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(filename);
		Element map = document.getDocumentElement();
		map.normalize();
		
		initInitialSettings();		
		initSettings(map);
		initTiles(map);
		initRandom();
		initNormalize();
		updateParkStateFly();

		Debugger.DEBUG("Map generated.");
	}
	
	/**
	 * Initializes the map's initial settings. The settings of a map are initially
	 * set to predefined initial defaults. If a user defines any of these values, 
     * the default values are overridden. Otherwise, the defaults are left
     * unmodified.
	 */
	private void initInitialSettings() {
		Debugger.DEBUG("Initializing Initial Settings...");
		
		if (null != Simulator.SEED) {
			seed = Simulator.SEED;
		}
		else {
			seed = new Randomer().nextInt();
		}
		rand = new Randomer(seed);
		Debugger.DEBUG("Seed is " + seed);
		
		skin = "land";
		 
		randomLight   = true;
		default_light = 0;
		min_light     = 0;
		max_light     = 0;
	
		randomWind   = true;
		default_wind = new Wind(0, Direction.N);
		min_wind     = 0;
		max_wind     = 0;
		
		randomFlowers    = true;
		expected_learning_flowers = 50;
		expected_running_flowers = 10;
		
		randomAromaIntensity    = true;
		default_aroma_intensity = Flower.AROMA_INTENSITY;
		min_aroma_intensity     = Flower.AROMA_INTENSITY;
		max_aroma_intensity     = Flower.AROMA_INTENSITY;
		
		learningFlowerPositions = new ArrayList<Position>();
		runningFlowerPositions = new ArrayList<Position>();
		learningFlowers = new ArrayList<Flower>();
		runningFlowers = new ArrayList<Flower>();
	}
	
	/**
	 * Parses the main map XML element from a map file and initializes a map's
	 * settings. Each individual setting (e.g. wind, light) is parsed and passed
	 * to a helper initialization method. If at any point during the
	 * initialization, bad user data is detected, default values are used; the
	 * initialization should not throw any errors.
	 * 
	 * @param map Map XML element parsed from a map file.
	 */
	private void initSettings(Element map) {
		Debugger.DEBUG("Initializing Settings...");
		Debugger.NULL_CHECK(map, "null map in initSettings()!");
		
		// Parse out individual setting elements
		NodeList skinElements    = map.getElementsByTagName("skin");
		NodeList lightElements   = map.getElementsByTagName("light");
		NodeList windElements    = map.getElementsByTagName("wind");
		NodeList flowersElements = map.getElementsByTagName("flowers");
		NodeList aromaElements   = map.getElementsByTagName("aroma");
		
		// Initialize each setting
		initSkinSettings(skinElements);
		initLightSettings(lightElements);
		initWindSettings(windElements);
		initFlowersSettings(flowersElements);
		initAromaSettings(aromaElements);
	}
	
	/**
	 * Initializes skin settings parsed from a map XML element.
	 * 
	 * @param skinElements A nodelist of skin elements. Should contain either
	 * none or one element.
	 */
	private void initSkinSettings(NodeList skinElements) {
		Debugger.DEBUG("Initializing Skin Settings...");
	
		if (skinElements == null || skinElements.getLength() == 0) {
			return;
		}
		
		skin = skinElements.item(0).getTextContent().trim();
	}
	
	/** @see danaus.Map#initSkinSettings(NodeList) */
	private void initLightSettings(NodeList lightElements) {
		Debugger.DEBUG("Initializing Light Settings...");

		if (lightElements == null || lightElements.getLength() == 0) {
			return;
		}
		
		Element light = (Element) lightElements.item(0);
		
		String random = Common.get_text_by_tag_name(light, "random");
		String def    = Common.get_text_by_tag_name(light, "default");
		String min    = Common.get_text_by_tag_name(light, "min");
		String max    = Common.get_text_by_tag_name(light, "max");
		
		randomLight = Common.isNotNo(random);
		default_light = Common.intValueOf(def, default_light, "invalid def light!");
		min_light = Common.intValueOf(min, min_light, "invalid minimum light!");
		max_light = Common.intValueOf(max, max_light, "invalid maximum light!");
	}
	
	/** @see danaus.Map#initSkinSettings(NodeList) */
	private void initWindSettings(NodeList windElements) {
		Debugger.DEBUG("Initializing Wind Settings...");
		
		if (windElements == null || windElements.getLength() == 0) {
			return;
		}
		
		Element wind = (Element) windElements.item(0);
		
		String random = Common.get_text_by_tag_name(wind, "random");
		String def    = Common.get_text_by_tag_name(wind, "default");
		String min    = Common.get_text_by_tag_name(wind, "min");
		String max    = Common.get_text_by_tag_name(wind, "max");
		
		randomWind = Common.isNotNo(random);
		default_wind = Common.windValueOf(def, default_wind, "invalid def wind!");
		min_wind = Common.intValueOf(min, min_wind, "invalid minimum wind!");
		max_wind = Common.intValueOf(max, max_wind, "invalid maximum wind!");
	}
	
	/** @see danaus.Map#initSkinSettings(NodeList) */
	private void initFlowersSettings(NodeList flowersElements) {
		Debugger.DEBUG("Initializing Flowers Settings...");

		if (flowersElements == null || flowersElements.getLength() == 0) {
			return;
		}
		
		Element flowers = (Element) flowersElements.item(0);
		
		String random   = Common.get_text_by_tag_name(flowers, "random");
		String expected_learning = 
            Common.get_text_by_tag_name(flowers, "expected_learning");
		String expected_running = 
            Common.get_text_by_tag_name(flowers, "expected_running");
		
		randomFlowers = Common.isNotNo(random);
		expected_learning_flowers = Common.intValueOf(expected_learning, 
				expected_learning_flowers, "invalid expected flowers!");
		expected_running_flowers = Common.intValueOf(expected_running, 
				expected_running_flowers, "invalid expected flowers!");
	}
	
	/** @see danaus.Map#initSkinSettings(NodeList) */
	private void initAromaSettings(NodeList aromaElements) {
		Debugger.DEBUG("Initializing Aroma Settings...");
		
		if (aromaElements == null || aromaElements.getLength() == 0) {
			return;
		}
		
		Element aroma = (Element) aromaElements.item(0);
		
		String random = Common.get_text_by_tag_name(aroma, "random");
		String def    = Common.get_text_by_tag_name(aroma, "default");
		String min    = Common.get_text_by_tag_name(aroma, "min");
		String max    = Common.get_text_by_tag_name(aroma, "max");
		
		randomAromaIntensity = Common.isNotNo(random);
		default_aroma_intensity = 
            Common.doubleValueOf(def, default_aroma_intensity,"invalid def aroma!");
		min_aroma_intensity = 
            Common.doubleValueOf(min, min_aroma_intensity,"invalid min aroma!");
		max_aroma_intensity = 
            Common.doubleValueOf(max, max_aroma_intensity,"invalid max aroma!");
	}
	
	/**
	 * Extracts and formats the text content of a map file, then passes it on to
	 * be parsed. The main text element of the map file contains all the 
	 * information about the tiles in the map.
	 * 
	 * @param map Map XML element.
	 */
	private void initTiles(Element map) {
		Debugger.DEBUG("Initializing Tiles...");
		Debugger.NULL_CHECK(map, "null map in initTiles!");
		
		/* Extracting the text we're interested in can be tricky. Simply 
		 * requesting the text content of the root element returns the text of
		 * the entire XML file. Instead, we must iterate through the children
		 * elements and extract only the text of text elements. */
		String text = "";
		NodeList children = map.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				text += child.getTextContent().trim(); 
			}
		}
		
		/* Map files without text create randomly generated maps. We leave the
		 * tiles array null; it will be randomly generated later. */
		if (!text.isEmpty()) {
			parseTiles(text);
		}
	}
	
	/**
	 * Parses the text of a map XML element and initializes map tiles. The text
	 * is parsed into cells. The cell is then broken up into fields and each
	 * field is passed to its corresponding parse function. Then, everything
	 * is put together and a tile is created.
	 * 
	 * @param mapText The text of a map XML element.
	 */
	private void parseTiles(String mapText) {
		Debugger.DEBUG("Initializing Tiles from text...");
		Debugger.NULL_CHECK(mapText, "null mapText in parseTiles!");
	
		/* The information to be parsed from a single tile. */
		int light;
		Wind wind;
		List<Double> aromaIntensities;
		List<Flower> flowers;
		TileState tileState;
	
		/* The map's text must first be parsed to find the width and height of
		 * the map so that the proper sized array can be allocated. */
		tiles = allocateTiles(mapText);
		FLOWER_SEED_PROBABILITY = (int) ((1000.0 * expected_learning_flowers) / 
				(height * width));
		
		/* Parse the text with a combination of Scanners and regular
		 * expressions. Scanners are used to extract rows and regular 
		 * expressions are used to extract tiles. The row and column of each
		 * tile is tracked so that it can be accurately inserted into the tiles
		 * array. The Scanners are initialized with a semicolon delimiter: the 
		 * row delimiter in a map file. The regular expression matches any text
		 * beginning with a tile token followed by anything that's not a tile
		 * token or whitespace. */
		Scanner mapScanner = new Scanner(mapText);    
		mapScanner.useDelimiter("\\s*;\\s*");
		String tokens = String.valueOf(LAND_TOKEN) + WATER_TOKEN + FOREST_TOKEN + CLIFF_TOKEN;
		Pattern tilePattern = Pattern.compile("[" + tokens + "][^\\s," + tokens + "]*");

		/* Iterate through the rows of the map */
		for (int row = 0; mapScanner.hasNext(); row++) {
			String rowText = mapScanner.next();
			Matcher tileMatcher = tilePattern.matcher(rowText);
			
			/* Iterate through the tiles of a row. */
			for (int col = 0; tileMatcher.find(); col++) {
				/* group() extracts the previously found text: a tile. Tile
				 * fields are separated by '.' characters. */
				String tileText = tileMatcher.group();
				String fields[] = tileText.split("\\s*\\.\\s*");
						
				light = parseLight(fields);
				wind  = parseWind(fields);
				aromaIntensities = parseAromaIntensities(fields);
				flowers   = parseFlowers(fields, row, col, aromaIntensities, 
						FLOWER_SEED_PROBABILITY);
				tileState = new TileState(new Position(row, col).toLocation(), light, wind, flowers);
				tiles[row][col] = parseTile(fields, tileState);
				if (tiles[row][col].flyable) {
					park.state.numTiles++;
				}
			}
		}

		nullToWater();
		mapScanner.close();
	}

	/**
	 * Parses the text from a map file and allocates a two dimensional array
	 * of tiles with the correct width and height. The map must be scanned in 
	 * full to find the number of rows and the width of the widest row. The
	 * width of the widest row determines the width of the board. All non-
	 * specified tiles are turned to water.
	 * 
	 * @param mapText The text from a map file.
	 * @return A tile array with correct dimensions.
	 */
	private Tile[][] allocateTiles(String mapText) {
		Debugger.DEBUG("Allocating Tiles...");
		Debugger.NULL_CHECK(mapText, "null mapText in allocateTiles!");
		
		/* mapScanner scans a map text and outputs rows using the row delimiter:
		 * ';'. */
		Scanner mapScanner = new Scanner(mapText);
		mapScanner.useDelimiter("\\s*;\\s*");
		height = 0;
		width  = 0; 
		
		/* Each iteration of the loop is another row in the map, so we increment
		 * the height. The number of columns in each row is the number of 
		 * unique cells, or tokens. */
		while (mapScanner.hasNext()) {
			height++;
			
			String row = mapScanner.next();
			int landCount   = Common.numberOfOccurrences(row, LAND_TOKEN);
			int waterCount  = Common.numberOfOccurrences(row, WATER_TOKEN);
			int forestCount = Common.numberOfOccurrences(row, FOREST_TOKEN);
			int cliffCount  = Common.numberOfOccurrences(row, CLIFF_TOKEN);
			int count = landCount + waterCount + forestCount + cliffCount;
			width = Math.max(width, count);
		}
		
		mapScanner.close();
		return new Tile[height][width];
	}
	
	/**
	 * Parses the light field from a tile's fields. If the field is not present,
	 * the value returned is either unspecified, if a random value is to be
	 * generated later, or the default value. 
	 * 
	 * @param fields The fields of a map tile.
	 * @return The light of a map tile.
	 * @see danaus.Map#initTiles
	 */
	private int parseLight(String fields[]) {
		Debugger.NULL_CHECK(fields, "null fields in parseLight()!");
	
		/* If the light index is larger than the size of fields, a light value
		 * was not specified. */
		if (LIGHT_INDEX > fields.length - 1) {
			return getLight();
		}
		
		Integer light = getLight();
		light = Common.intValueOf(fields[LIGHT_INDEX].trim(), light, 
				"invalid light entry in parseLight!");
		return light;
	}
	
	/**
	 * Parses the wind field from a tile's fields. If the field is not present,
	 * the value returned is either unspecified, if a random value is to be
	 * generated later, or the default value. 
	 * 
	 * @param fields The fields of a map tile.
	 * @return The wind of a map tile.
	 * @see danaus.Map#initTiles
	 */
	private Wind parseWind(String fields[]) {		
		Debugger.NULL_CHECK(fields, "null fields in parseWind()");

		/* If the wind index is larger than the size of fields, a wind value
		 * was not specified.*/
		if (WIND_INDEX > fields.length - 1) {
			return getWind();
		}
		
		/* Parse the wind and catch any exceptions. */
		Wind wind = getWind();
		wind = Common.windValueOf(fields[WIND_INDEX], wind, "invalid wind entry in parseWind!");
		return wind;
	}
	
	/**
	 * Parses the aromas field from a tile's fields. If the field is not present,
	 * the value returned is unspecified.
	 * 
	 * @param fields A fields array.
	 * @return Aromas
	 * @see danaus.Map#initTiles
	 */
	private List<Double> parseAromaIntensities(String fields[]) {		
		Debugger.NULL_CHECK(fields, "null fields in parseAromaIntensities()");
		
		/* If the aromas index is larger than the size of fields, an aromas
		 * value was not specified. Unlike the other parse methods, a default
		 * value cannot be returned. We don't know the number of aromas
		 * to generated. Instead, we return UNSPECIFIED_AROMAS and have
		 * another method determine what to do. */
		if (AROMA_INDEX > fields.length - 1) {
			return null;
		}
		
		// untreated aroma string (e.g. 20-20-21)
		String aromasString = fields[AROMA_INDEX];
		// list of individual aroma strings
		String aromasStrings[] = aromasString.split("\\s*-\\s*");
		// list of actual aroma intensities
		List<Double> aromasDoubles = new ArrayList<Double>();
		
		// attempt to parse each aroma
		for (String aroma : aromasStrings) {
			try {
				Double aromaIntensity = Math.max(0, Double.parseDouble(aroma));
				aromasDoubles.add(aromaIntensity);
			}
			catch (NumberFormatException e) {}
		}
		
		return aromasDoubles;
	}
	
	/**
	 * Parses the flowers field from a tile's fields. If the field is not 
	 * present, the value returned is unspecified. The values in the flowers
	 * field are the suffixes of the flowers' names. They are prepended with
	 * the word "flower_".
	 * 
	 * @param fields A fields array. 
	 * @param row The row of the flower.
	 * @param column The column of the flower.
	 * @param aromaIntensities A list of associated aroma intensities. 
	 * @return Flowers
	 * @see danaus.Map#initTiles
	 */
	private List<Flower> parseFlowers(String fields[], int row, int column, 
			List<Double> aromaIntensities, int probability) {		
		Debugger.NULL_CHECK(fields, "null fields in parseFlowers!");
		

		if (FLOWER_INDEX > fields.length - 1) {
			if (randomFlowers && rand.nextBoolean(probability)) {
				List<Flower> flowers = new ArrayList<Flower>();
				Position flowerPos = new Position(row, column);
				Flower flower = randomFlower(flowerPos);
				flowers.add(flower);
				return flowers;
			}
			return new ArrayList<Flower>();
		}
		
		/* If a flowers field does exist, attempt to match each with it's
		 * corresponding aroma. */
		// raw flower string (e.g. 1-2-34)
		String flowersString = fields[FLOWER_INDEX];
		String flowersStrings[] = flowersString.split("\\s*-\\s*");
		List<Flower> flowers = new ArrayList<Flower>();
		Position position = new Position(row, column);
		
		/* Iterate through each suffix and construct a flower. */
		int i = 0;
		for (String nameSuffix : flowersStrings) {
			String name = "flower_" + nameSuffix;
			double intensity;
			
			/* If an aroma is specified in the aromas field, use it. Otherwise,
			 * choose a random or default value. */
			if (aromaIntensities == null || i > aromaIntensities.size() - 1) {
				intensity = getAromaIntensity();
			}
			else {
				intensity = aromaIntensities.get(i);
			}
			
			Flower flower = new Flower(name, position.toLocation(), intensity);
			flowers.add(flower);
			i++;
		}
		
		return flowers;
	}
	 
	/**
	 * Creates a tile instance from the fields parsed from a map file.
	 * 
	 * @param fields A fields array. 
	 * @param tileState The tile's state
	 * @return A tile
	 * @see danaus.Map#initTiles
	 */
	private Tile parseTile(String[] fields, TileState tileState) {		
		Debugger.NULL_CHECK(fields, "null fields in parseTile!");
		/* Tile type is mandatory, unlike other fields. */
		if (TYPE_INDEX > fields.length - 1) {
			Debugger.ERROR("no type field in fields");
			throw new IllegalArgumentException();
		}
		
		String type = fields[TYPE_INDEX].trim();

		/* A butterfly may be specified within the tile's type. An index that
		 * is not -1 signifies that a butterfly was specified. */
		if (type.indexOf(BUTTERFLY_TOKEN) != -1) {
			butterflyStart = new Position(tileState.location);
			type = type.replace(BUTTERFLY_TOKEN, "");
		}
		
		/* Dispatch on the tile's type and construct the tile. */
		switch (type) {
			case LAND_TOKEN:
				learningFlowers.addAll(tileState.flowers);
				for (Flower f : tileState.flowers) {
					learningFlowerPositions.add(new Position(f.location));
				}
				return new Land(skin, tileState);
			case WATER_TOKEN:
				tileState.flowers = new ArrayList<Flower>();
				return new Water(skin, tileState);
			case FOREST_TOKEN:
				learningFlowers.addAll(tileState.flowers);
				for (Flower f : tileState.flowers) {
					learningFlowerPositions.add(new Position(f.location));
				}
				return new Forest(skin, tileState);
			case CLIFF_TOKEN:
				tileState.flowers = new ArrayList<Flower>();
				return new Cliff(skin, tileState);
			default:
				Debugger.WARNING("Invalid tile token in parseTile: " + type);
				return new Land(skin, tileState);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Random
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes all random elements of a map specified by the map settings.
	 */
	private void initRandom() {
		Debugger.DEBUG("Initializing Random...");
		
		if (tiles == null) {
			initRandomTiles();
		}
		
		if (butterflyStart == null) {
			butterflyStart = randomButterflyStart();
		}
	}
		
	/**
	 * Initializes a random set of tiles.
	 */
	private void initRandomTiles() {
		Debugger.DEBUG("Initializing Random Tiles...");
		
		/* Randomly size the map. */
		height = rand.nextInt(MIN_HEIGHT, MAX_HEIGHT);
		width  = rand.nextInt(MIN_WIDTH, MAX_WIDTH);
		tiles = new Tile[height][width];
		
		/* The likelihood of choosing one of EAST or WEST to infect instead of
		   NORTH or SOUTH. This is adjusted according to the width and height of
		   the map so that wider maps generate wider islands and narrower maps
		   generate narrower islands. It is calculated from the following.
		 		widthProb + heightProb = 1
		      	widthProb + k*heightProb
				widthProb + (height/width)*widthProb = 1
				(height/width + 1)*widthProb = 1
			 	widthProb = 1/((height/width) + 1)
		   Consider a map that is twice as wide as it is tall, and the math
		   becomes clear. We multiply by 1000 because this probability will
		   be passed to nextBoolean(int). */
		int horizontalProbability = (int) (1000 * (1.0 / (((float)height/width) + 1.0)));
		int maxLand = (int) ((height * width) * LAND_FRACTION);
		MAX_FORESTS = (int) (maxLand * FOREST_FRACTION);
		
		AVERAGE_CLIFF_LENGTH = (int) (CLIFF_FRACTION / (CLIFF_SEED_PROBABILITY / 1000.0));
		DELTA_CLIFF_LENGTH = 0.25;
		MIN_CLIFF_LENGTH = (int) (AVERAGE_CLIFF_LENGTH - (AVERAGE_CLIFF_LENGTH * DELTA_CLIFF_LENGTH));
		MAX_CLIFF_LENGTH = (int) (AVERAGE_CLIFF_LENGTH + (AVERAGE_CLIFF_LENGTH * DELTA_CLIFF_LENGTH));
		
		FLOWER_SEED_PROBABILITY = (int) ((1000.0 * expected_learning_flowers) / maxLand);
		/* Create lists for cliffs and forests. We keep track of the 
		 * locations of cliffs and forests so that we don't have to iterate
		 * through the entire list to find a few locations. Also track the 
		 * land on a map for flowering. */
		LinkedList<Position> cliffs = new LinkedList<Position>();
		LinkedList<Position> forests = new LinkedList<Position>();
		/* Threshold the frontiers. We separate tiles into those with few
		 * spots to grow and those with many spots to grow. The threshold
		 * sets the number of null neighbors needed to be considered free.
		 * The lower the threshold, the rounder the map. The higher the 
		 * threshold, the stringier the map. */
		final int nullNeighborThreshold = 3;
		List<Position> freeFrontier    = new ArrayList<Position>();
		List<Position> crampedFrontier = new ArrayList<Position>();
		// The tile on the frontier from which another tile will be branched
		Position frontiersman;
		// True if the voyager was from the free frontier.
		boolean fromFreeFrontier;
		// The number of land tiles
		int numLand = 0;
		/* Populate the neighbor lists. This is an optimization trick. Two 
		 * arrays are allocated once. The new Position information, then, 
		 * doesn't have to allocate any new point instances. */
		// east and west neighbors
		Position horizontal[];
		// north and south neighbors
		Position vertical[];
		
		/* Root the linked lists. */
		Position root = new Position(height/2, width/2);
		freeFrontier.add(root);
		set(root, new Land(skin, randomTileState(root.toLocation())));
		numLand++;
		park.state.numTiles++;
				
		while (!(freeFrontier.isEmpty() && crampedFrontier.isEmpty()) && numLand < maxLand) {
			/* Select either from free frontier or the cramped frontier. */
			if (crampedFrontier.isEmpty() ||
			   (!freeFrontier.isEmpty() && numLand % 10 + 1 <= FREE_PROBABILITY)) {
				frontiersman = rand.nextElement(freeFrontier);
				fromFreeFrontier = true;
			}
			else {
				frontiersman = rand.nextElement(crampedFrontier);
				fromFreeFrontier = false;
			}
						
			/* Extract the neighbors. */
			horizontal = getPositions(frontiersman, new TileConditions.Nulls(), 
					new DirectionConditions.EastOrWest());
			vertical = getPositions(frontiersman, new TileConditions.Nulls(), 
					new DirectionConditions.NorthOrSouth());
			
			/* If the voyager doesn't have any null neighbors, remove him 
			 * from the list and start the process again. This can happen
			 * because a neighbor can be infected by another position. 
			 * 
			 * Or, if he has only one neighbor, remove him as well because
			 * we're about to fill his last neighbor. But this time, don't
			 * move on yet. */
			if (horizontal.length + vertical.length <= 1) {
				if (fromFreeFrontier) {
					freeFrontier.remove(frontiersman);
				}
				else {
					crampedFrontier.remove(frontiersman);
				}
				
				// Move on to the next iteration of the loop
				if (horizontal.length + vertical.length == 0) {
					continue;
				}
			}
			
			/* Choose a null tile to infect. */
			Position infected;
			if (vertical.length == 0 || (horizontal.length != 0 && 
					rand.nextBoolean(horizontalProbability))) {
				infected = new Position(horizontal[rand.nextInt(horizontal.length)]);
			}
			else {
				infected = new Position(vertical[rand.nextInt(vertical.length)]);
			}	
						
			/* Choose the type of tile to infect */
			int type = rand.nextInt(1, 1000);
			
			if (type <= CLIFF_SEED_PROBABILITY) {
				set(infected, new Cliff(skin, randomTileState(infected.toLocation())));
				cliffs.add(infected);
     			
			}
			else if (type <= FOREST_SEED_PROBABILITY) {
				set(infected, new Forest(skin, randomTileState(infected.toLocation())));
				forests.add(infected);
				park.state.numTiles++;
			}
			else {
				set(infected, new Land(skin, randomTileState(infected.toLocation())));
				park.state.numTiles++;
			}
			
			/* Flower the tile if it's not a cliff*/
			if (at(infected).flyable && rand.nextBoolean(FLOWER_SEED_PROBABILITY)) {
				Flower flower = randomFlower(infected);
				at(infected).tileState.addFlower(flower);
				learningFlowers.add(flower);
				learningFlowerPositions.add(infected);
			}
						
			/* Add the infected to the frontier */
			if (at(infected).flyable) {
				int numNeighbors = getPositions(infected, 
						new TileConditions.Nulls(), 
						new DirectionConditions.Cross()).length;
				// Yoda conditions; google that :)
				if (nullNeighborThreshold <= numNeighbors) { 
					freeFrontier.add(infected);
				}
				else {
					crampedFrontier.add(infected);
				}
			}			
			numLand++;
			sketch();
		}	
				
		/* Populate the map with appropriate tiles **/
		nullToWater();
		growForests(forests);
		growCliffs(cliffs);
	}
	
	
	/**
	 * Simulates the expansion of mountainous terrain. Each initial mountain
	 * generated from the initial pass is expanded. Each mountain selects
	 * a direction to grow. It then grows a random distance in that general
	 * direction. 
	 * 
	 * Special care must be taken to ensure that the erection of mountain ranges
	 * does not destroy the connectivity of the map. We can do so by adding
	 * only to tiles that we know will not disconnect the graph. Imagine you
	 * are a mountain tile. You want to make one of your neighbors a mountain as
	 * well. You look at each neighbor and find the list of their neighbors that
	 * are obstacles. Then you make a list of all your neighbors who are 
	 * obstacles. If the guy you are looking at has obstacles in his list, 
	 * besides you, that aren't in your list, then he is not a candidate to 
	 * become a mountain. If he doesn't, then you may select him to be a 
	 * mountain.
	 * 
	 * @param cliffs A list of all initial mountain tiles.
	 */
	private void growCliffs(LinkedList<Position> cliffs) {
		Debugger.DEBUG("Growing Cliffs...");
		
		for (Position seed : cliffs) {
			growCliff(seed);
		}
	}
	
	/**
	 * Grows a mountain range.
	 *  
	 * @param cliffPos A cliff location
	 * @see danaus.Map#growCliffs(LinkedList)
	 */
	private void growCliff(Position cliffPos) {
		// the maximum number of tiles that will become cliffs
		int maxCliff = rand.nextInt(MIN_CLIFF_LENGTH, MAX_CLIFF_LENGTH);
		// Possible cliff directions
		DirectionCondition cliffDirections[] = {
				new DirectionConditions.Up(),
				new DirectionConditions.Right(),
				new DirectionConditions.Down(),
				new DirectionConditions.Left()
		};
		// Actual cliff direction
		DirectionCondition direction = rand.nextElement(cliffDirections);
		// the cliff candidate neighbors at a location
		Position neighbors[] = getCliffCandidates(cliffPos, direction);
		
		for (int numCliff = 1; neighbors.length != 0 && numCliff <= maxCliff; numCliff++) {
			cliffPos = rand.nextElement(neighbors);
			Cliff cliff = new Cliff(skin, new TileState(cliffPos.toLocation()));
			Tile.copy(cliff, at(cliffPos));
			set(cliffPos, cliff);
			neighbors = getCliffCandidates(cliffPos, direction);
			park.state.numTiles--;
			sketch();
		} 
	}
	
	/**
	 * Returns a list of cliff neighbor candidates. A location is a candidate
	 * to become a cliff if converting it to a cliff can not disconnect the 
	 * map.
	 * 
	 * @param source An initial cliff position.
	 * @param dc The DirectionCondition the cliffs must pass in order to be 
	 * considered candidates.
	 * @return A list of neighboring cliff candidates.
	 * @see danaus.Map#growCliffs(LinkedList)
	 */
	private Position[] getCliffCandidates(Position source, DirectionCondition dc) {
		// The obstacle neighbors of the initial location
		Position obstacles[] = getPositions(source, 
				new TileConditions.Obstacles(), null);
		final HashSet<Position> sourceObstacles = 
				new HashSet<Position>(Arrays.asList(obstacles));
		
		return getPositions(source,
			/* Get only tiles that cannot disconnect graph. */
			new TileCondition() {
				public boolean tileCondition(Tile tile) {
					// the obstacle neighbors of source's neighbor
					Position obstacles[] = 
							getPositions(new Position(tile.tileState.location), 
							new TileConditions.Obstacles(), null); 
					HashSet<Position> neighborObstacles = new 
							HashSet<Position>(Arrays.asList(obstacles));
					
					/* This is similar to the intersection of the two sets. If
					 * the tile is a candidate the only obstacle neighbor he has
					 * that source does not will be source itself.*/
					neighborObstacles.removeAll(sourceObstacles);
				
					return (tile.flyable) &&
						tile.tileState.flowers.isEmpty() &&
						neighborObstacles.size() == 1;
				}
			}, 
			dc
		);
	}
			
	/**
	 * Simulates the growth of forests in a map. For each forest tile, a random
	 * number of neighbors are selected be turned to forests. These new forests
	 * are appended to the lists of forests and the process continues. This 
	 * is similar to a depth-first search, and it creates a growing out effect
	 * that is similar to the growth of a forest.
	 * 
	 * @param forests A list of all the initial forests in a map.
	 */
	private void growForests(LinkedList<Position> forests) {
		Debugger.DEBUG("Growing Forests...");
		
		numForests = forests.size();
		while (!forests.isEmpty() && numForests < MAX_FORESTS) {
			growForest(forests.pollFirst(), forests);			
		}
	}
	
	/**
	 * Grows the neighbors around a forest location and appends any new forests
	 * to the global list of forests.
	 * 
	 * @param seed An initial forest location.
	 * @param forests The global list of forests.
	 * @see danaus.Map#growForests(LinkedList)
	 */
	private void growForest(Position seed, LinkedList<Position> forests) {
		/* Get the neighbors that are land. */
		Position neighbors[] = getPositions(seed, new TileConditions.Lands(), null);
				
		/* Randomly select a fraction of the neighbors to become forests. */
		for (Position neighbor : neighbors) {
			if (rand.nextBoolean(FOREST_GROW_PROBABILITY)) {
				Forest shrub = new Forest(skin, new TileState(neighbor.toLocation()));
				Tile.copy(shrub, at(neighbor));
				set(neighbor, shrub);
				forests.add(neighbor);
				numForests++;
				sketch();
			}
		}
	}
	
	
	/**
	 * Initiates the map renovations needed to begin the running phase of a 
	 * simulation. Such renovations include reflowering a map, re-spreading 
	 * aromas, and re-applying wind.
	 */
	public void beginRunning() {
		reflower();
		spreadAromas(runningFlowerPositions);
		spreadWind(runningFlowers);
	}
	
	/**
	 * Randomly adds flowers to a map. When a simulation transitions from
	 * the learning to the running phase, additional flowers are randomly
	 * added to the map.
	 */
	public void reflower() {
		if (!randomFlowers) {return;}
		
		for (int i = 0; i < expected_running_flowers; ++i) {
			Position flowerPos = randomPosition();
			Tile tile = at(flowerPos);
			if (tile.flyable) {
				runningFlowerPositions.add(flowerPos);
				Flower flower = randomFlower(flowerPos);
				runningFlowers.add(flower);
				tile.tileState.addFlower(flower);
			}
		}
		
		spreadAromas(runningFlowerPositions);
	}
	
	/**
	 * Returns a random or default light, depending on the map's settings.
	 * @return The light value that satisfies the map's settings.
	 */
	private int getLight() {
		return (randomLight) ? randomLight() : default_light; 
	}
	
	/**
	 * Returns a random or default wind, depending on the map's settings.
	 * @return The wind value that satisfies the map's settings.
	 */
	private Wind getWind() {
		return (randomWind) ? randomWind() : default_wind;
	}
	
	/**
	 * Returns a random or default aroma, depending on the map's settings.
	 * @return The aroma intensity value that satisfies the map's settings.
	 */
	private double getAromaIntensity() {
		return (randomAromaIntensity) ? 
				randomAromaIntensity() : default_aroma_intensity;
	}
	
	/**
	 * Returns a random light. 
	 * @return A random light.
	 */
	private int randomLight() {
		return 0;
	}
	
	/**
	 * Returns a random wind.
	 * @return A random wind.
	 */
	private Wind randomWind() {
		int intensity = 0;
		Direction direction = Direction.N;
		return new Wind(intensity, direction);
	}
	
	/**
	 * Returns a random aroma intensity.
	 * @return A random aroma intensity.
	 */
	private double randomAromaIntensity() {
		return rand.nextDouble(min_aroma_intensity, max_aroma_intensity);
	}
	
	/**
	 * Returns a random flower.
	 * @return A random flower.
	 */
	private Flower randomFlower(Position position) {
		int flowerNum = rand.nextElement(FLOWER_NUMBERS);
		double aromaIntensity = (randomAromaIntensity) ? 
				randomAromaIntensity() : default_aroma_intensity;
		return new Flower("flower_" + flowerNum, position.toLocation(), 
				aromaIntensity);
	}
	
	private Position randomPosition() {
		int row = rand.nextInt(height);
		int col = rand.nextInt(width);
		return new Position(row, col);
	}
	
	/**
	 * Returns a random tile state.
	 * @return A random tile state.
	 */
	private TileState randomTileState(Location location) {
		return new TileState(location, randomLight(), randomWind(), 
				new ArrayList<Flower>());
	}
	
	/**
	 * Returns a random starting position for a map's butterfly. Uniformly 
	 * randomly, positions are chosen from the map. If the tile at the selected
	 * position is a forest or land, it is returned. Otherwise, another position
	 * is selected. If after a specified number of probes, a position is not 
	 * found, the most recently chosen position is converted to land and used
	 * as the butterfly's starting position. Though, the odds of this occurring
	 * are very low on any decently sized map.
	 */
	private Position randomButterflyStart() {
		Debugger.DEBUG("Initializing Random Butterfly Starting Position...");
		/* Probe positions until a suitable candidate is found. */
		Position position = new Position(0, 0);
		for (int i = 0; i < 100; i++) {
			position = new Position(rand.nextInt(height), rand.nextInt(width));
			if (at(position).flyable) {
				return position;
			}
		}
		
		/* When life doesn't give you lemons, make lemons. That is, convert
		 * the most recently selected position to a land position. */
		Land land = new Land(skin, new TileState(position.toLocation()));
		Tile.copy(land, at(position));
		set(position, land);
		return position;
	}
	
	/**
	 * Normalizes the map with post-generation effects. Aroma is radiated, wind
	 * is spread, and a butterfly is initialized.
	 */
	private void initNormalize() {
		Debugger.DEBUG("Initializing Aroma, Wind, and Butterfly...");
		
		spreadAromas(learningFlowerPositions);
		spreadWind(learningFlowers);
		initButterfly();
	}
		
	/**
	 * For each flower on the map, the initial aroma is spread across the map. 
	 * The aroma falls off with the square of the distance plus one. That is,
	 * at a distance of 0 from the flower, the aroma is 1/((0+1)^2) the strength
	 * of the initial aroma. 
	 */
	private void spreadAromas(List<Position> runningFlowerPositions2) {
		Debugger.DEBUG("Spreading Aroma...");
		
		for (Position flowerPos : runningFlowerPositions2) {
			for (Flower flower : at(flowerPos).tileState.flowers) {
				spreadAroma(flower, flowerPos);
			}
		}
	}
	
	/**
	 * For each flower on the map, it's aroma is radiated across the map. An
	 * aroma decays with the square of the distance to the flower that created
	 * it. The minimum distance to a flower is used. Aromas are spread 
	 * travelling in non-diagonal directions.
	 */
	private void spreadAroma(Flower flower, Position flowerPos) {
		// A hashmap of nodes that have been visited mapped to the number of
		// steps away from the flower they are.
		HashMap<Position, Integer> visited = new HashMap<Position, Integer>();
		// The locations with neighbors who have not yet been visited
		LinkedList<Position> frontier = new LinkedList<Position>();
		// The current number of steps away from the flower 
		int steps = 0;
		// The largest number of steps that can be taken while an aroma is still
		// detectable
		int maxSteps = Aroma.getMaxSteps(flower.aromaIntensity);
					
		/* Seed the search */
		visited.put(flowerPos, 0);
		frontier.add(flowerPos);
		
		/* Calculate the steps for each tile. */
		 while (!(frontier.isEmpty()) && steps < maxSteps) {
			 Position frontiersman = frontier.pollFirst();
			 steps = visited.get(frontiersman);
			 			 
			 if (steps < maxSteps) {
				 Position neighbors[] = getPositions(frontiersman,
						 new TileConditions.Flyables(),
						 null);
				 for (Position neighbor : neighbors) {
					 if (!visited.containsKey(neighbor)) {
						 visited.put(neighbor, steps + 1);
						 frontier.addLast(neighbor);
					 }
				 }
			 }
		 } 
		 		 
		 /* Populate each tile with an aroma. */
		 for (Position location : visited.keySet()) {
			 double intensity = Aroma.calculateIntensity(flower.aromaIntensity,
					 visited.get(location));
			 at(location).tileState.addAroma(new Aroma(intensity, flower));
		 }
	}
	
	/**
	 * Initializes the map's butterfly at it's beginning location.
	 * 
	 * Precondition: The butterfly's starting position has already been
	 * determined and initialized. This is true for maps generated from map
	 * files and maps generated randomly.
	 */
	private void initButterfly() {
		Debugger.DEBUG("Initializing Butterfly...");
		try {
			Class<?> butterflyClass = Class.forName(Simulator.CLASS_NAMES.get(0));
			butterfly = (AbstractButterfly) butterflyClass.newInstance();
			butterfly.location = butterflyStart.toLocation();
			butterfly.setMap(this);
			at(butterflyStart).tileState.butterfly = butterfly;
			at(butterflyStart).turnEntered = 0;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			Debugger.ERROR(Simulator.CLASS_NAMES.get(0) + " could not be instantiated!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Spreads the effect of wind around the map. Specifically, aromas are 
	 * distributed. Any given tile has a wind intensity and wind direction. The 
	 * intensity of the wind is the amount of aroma moved. Clearly, it moves 
	 * in the direction of the wind. All tiles iterated through initially as
	 * aromas are transfered. In this initial spread, aromas can become negative.
	 * The map is then iterated through again to zero out any negative aromas.
	 */
	private void spreadWind(List<Flower> runningFlowers2) {
		Debugger.DEBUG("Spreading Wind...");
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				/* Gather information about the current position */
				Position sourcePos = new Position(row, col);
				Tile source = at(sourcePos);
				int intensity = source.tileState.wind.intensity;
				Direction direction = source.tileState.wind.direction;
				
				/* Gather information about the position in the wind's 
				 * direction. A null position indicates wrap around. */
				Position toPos = getPosition(sourcePos, direction);
				if (null != toPos) {
					Tile to = at(toPos);
				
					/* Transfer aroma. */
					source.tileState.subtractFromAromas(runningFlowers2, intensity);
					to.tileState.addToAromas(runningFlowers2, intensity);
				}
			}
		}
		
		/* Transferring aromas can result in negative aromas, which is an 
		 * impossible event. Zero all the negative aromas. */
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				Position position = new Position(row, col);
				at(position).tileState.zeroAromas();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Flying
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Flies the butterfly in the given direction with the given speed. As for
	 * any butterfly, the world is a dangerous place, so an exception may be
	 * thrown if the butterfly attempts to fly into a cliff or over water. 
	 * 
	 * @param heading The direction to fly.
	 * @param speed The speed to fly
	 * @throws CliffCollisionException
	 * @throws WaterCollisionException
	 */
	public void fly(Direction heading, Speed speed) {
		Position toPosition = getPosition(new Position(butterfly.location), heading);
		fly(toPosition, speed, heading, false);
	}
	
	/**
	 * Flies the butterfly in the given direction with the given speed. This
	 * butterfly is fearless and does not throw an exception if it attempts to
	 * fly into a cliff or over water.
	 * 
	 * @param heading The direction to fly.
	 * @param speed The speed to fly
	 */
	public void flySafe(Direction heading, Speed speed) {
		Position toPosition = getPosition(new Position(butterfly.location), heading);
		fly(toPosition, speed, heading, true);
	}
	
	/**
	 * Lands a butterfly. This can accomplish two things. First, a butterfly
	 * absorbs the light of the tile it's on. When a butterfly lands, it cannot
	 * incur any power penalties, and landing is a normal, not slow, operation.
	 * Second, a butterfly must land on a tile to collect its flowers.
	 */
	public void land() {
		park.state.turn++;

		Position position = new Position(butterfly.location);
		butterfly.addPower(at(position).tileState.light);
		moveButterfly(position); // does nothing, but makes timing consistent
		park.update(0, null, position.row, position.col, position.row, position.col);
	}
	
	
	/** Don't use this yet. */
	public void collect(Flower flower) {
		if (park.phase == SimulationPhase.LEARNING) {
			throw new PrematureCollectionException();
		}
		
		Tile tile = at(new Position(butterfly.location));
		if (null != flower && tile.tileState.flowers.contains(flower)) {
			park.state.foundFlowers.add(flower);
		}
		else {
			butterfly.subtractPower(AbstractButterfly.WRONG_COLLECT_POWER_COST);
		}
		
		park.update();
	}
	
	/**
	 * Updates the butterfly's state with the TileState of the tile it is 
	 * currently on. Whenever a butterfly updates its state, it loses power. 
	 */
	public void refreshState() {
		butterfly.subtractPower(AbstractButterfly.REFRESH_STATE_POWER_COST);
		butterfly.state = new TileState(at(new Position(butterfly.location)).tileState);
	}

	/**
	 * Flies a butterfly. First, the game's turn is incremented. Then, a 
	 * collision is detected. If no collision occurs, the butterfly is first
	 * drained of the power required to move. If a butterfly does not run out
	 * of power, the butterfly gains the power associated with a move and moves.
	 * 
	 * @param toPos The position to move to.
	 * @param speed The speed to travel at.
	 * @param heading The direction to travel.
	 * @param safe True if no exception can be thrown, false otherwise. 
	 */
	private void fly(Position toPos, Speed speed, Direction heading, boolean safe) {
		/* No matter how perilous a move the butterfly attempt to make, the 
		 * turn is unconditionally incremented! */
		park.state.turn++;
		
		/* If a collision is detected, prematurely end the turn. */
		if (handleCollisions(at(toPos), safe)) {
			Position p = new Position(butterfly.location);
			park.update(0, null, p.row, p.col, p.row, p.col);
			return;
		}
		int slowDown = updateCosts(toPos, speed);
		
		/* At this point, if an exception has not been thrown, the move will be
		 * a success. Thus, we can update all information with the knowledge 
		 * that the butterfly will move to a new position. */		
		butterfly.addPower(at(toPos).tileState.light);
		updateParkStateFly(toPos);
		moveButterfly(toPos);
		
		Position butterflyPos = new Position(butterfly.location);
		park.update(slowDown, heading, butterflyPos.row, butterflyPos.col,
				toPos.row, toPos.col);
	}
	
	/**
	 * Checks for and handles any possible collisions. If a movement is safe,
	 * no exceptions can be thrown. If the move is unsafe, a collision will
	 * be thrown.
	 * 
	 * @param destination The destination tile of a move.
	 * @param safe If true, exceptions cannot be thrown; false, and they can be.
	 * @return If the operation is safe, true if a collision occurred or false
	 * if one did not occur. 
	 */
	private boolean handleCollisions(Tile destination, boolean safe) {
		if (destination instanceof Cliff) {
			park.state.cliffCollisions++;
			park.state.slowTurns += 
					CliffCollisionException.CLIFF_COLLISION_SLOW_DOWN;
			butterfly.subtractPower(
					CliffCollisionException.CLIFF_COLLISION_POWER_COST);
			
			if (safe) {
				return true;
			}
			else {
				throw new CliffCollisionException();
			}
		}
		if (destination instanceof Water) {
			park.state.waterCollisions++;
			park.state.slowTurns +=
					WaterCollisionException.WATER_COLLISION_SLOW_DOWN;
			butterfly.subtractPower(
					WaterCollisionException.WATER_COLLISION_POWER_COST);
			
			if (safe) {
				return true;
			}
			else {
				throw new WaterCollisionException();
			}
		}
			
		return false;
	}
	
	/**
	 * Updates the costs associated with a move. Tiles, speed, and wind all 
	 * contribute to both power costs and slow down costs. 
	 * 
	 * Precondition: The movement has already been checked for collisions. That,
	 * is the to position cannot be that of a cliff or of water. 
	 * 
	 * @return The slow down cost NOT associated with the wind. This information
	 * is necessary for the GUI. It determines the speed to animate the 
	 * butterfly.
	 */
	private int updateCosts(Position toPos, Speed speed) {
		long initSlowTurns = park.state.slowTurns;
		
		/* Tile Costs. */
		Tile to = at(toPos);
		butterfly.subtractPower(to.powerCost);
		park.state.slowTurns += to.slowDown;		

		/* Speed Costs. */
		butterfly.subtractPower(speed.powerCost);
		park.state.slowTurns += speed.slowDownNumber;
		
		/* Wind Costs.
		Wind costs have been removed for fall 2013.
		
		Position fromPos = new Position(butterfly.location);
		Wind toWind = to.tileState.wind;
		int dCol = toPos.col - fromPos.col;
		int dRow = toPos.row - fromPos.row;
		int unitCol = -(Common.unit_scalar(dCol) * toWind.direction.dCol);
		int unitRow = -(Common.unit_scalar(dRow) * toWind.direction.dRow);
		int powerIntensity = Wind.WIND_POWER_COEFFICIENT * toWind.intensity;
		int slowDownIntensity = Wind.WIND_SLOW_DOWN_COEFFICIENT * toWind.intensity;
		int windPowerCost = (unitCol + unitRow) * powerIntensity;
		int windSlowDown = (unitCol + unitRow) * slowDownIntensity;
		butterfly.subtractPower(windPowerCost);
		park.state.slowTurns += windSlowDown;
		*/
		
		return (int) (park.state.slowTurns - initSlowTurns /*- windSlowDown */);
	}
		
	void updateParkStateFly() {
		if (at(new Position(butterfly.location)).turnEntered <= 0) {
			park.state.exploredTiles++;
		}
	}
	
	/**
	 * Updates the park state associated with a move.
	 */
	private void updateParkStateFly(Position toPos) {
		if (at(toPos).turnEntered == -1) {
			park.state.exploredTiles++;
		}
	}
	
	/**
	 * Moves a butterfly from one position to another and tidies up all the 
	 * loose ends. This movement also invokes a draw. 
	 */
	private void moveButterfly(Position to) {
		Position source = new Position(butterfly.location);
		at(source).tileState.butterfly = null;
		at(to).tileState.butterfly = butterfly;
		at(to).turnEntered = park.state.turn;
		butterfly.location = to.toLocation();
		
		draw();
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// Retrieving Neighbors
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	private Direction[] getDirections(Location l, TileCondition tc, DirectionCondition dc) {
		return getDirections(new Position(l), tc, dc);
	}

	/**
	 * Returns the directions that satisfy the following conditions and unary
	 * predicates:
	 * 	1) Beginning at the source position and traveling in the direction 
	 *  leads to a different position. 
	 *  2) The direction satisfies the unary predicate defined in the direction
	 *  condition.
	 *  3) The tile reached by traveling from the source position in the 
	 *  direction satisfies the unary predicate defined in the tile condition
	 * 
	 * Null conditions are interpreted as unconditionally true predicates. For 
	 * example, a null tile condition indicates that tile need not satisfy
	 * any tile predicate to be returned. Invoking this method with two
	 * null conditions returns all the directions which lead to a unique 
	 * neighbor.
	 * 
	 * Because Java doesn't treat functions as first class citizens - that is,
	 * Java doesn't support function pointers - and relies on anonymous classes
	 * (for anyone who is more familiar with C++, these are similar to functors)
	 * it can get tedious to define conditions. To alleviate this tedium, the 
	 * classes in TileConditions and DirectionConditions implement a variety
	 * of common predicates. Thus, instead of instantiating an anonymous class
	 * for every invocation of this method, look first to see if the predicate
	 * you are defining has already been defined.
	 * 
	 * @param source The source position described in the conditions above.
	 * @param tileFilter The tile filter described in the conditions above.
	 * @param directionFilter The direction filter described in the conditions
	 * above.
	 * @return The directions which satisfy the conditions outlined above.
	 */
	private Direction[] getDirections(Position source, 
			TileCondition tileFilter, DirectionCondition directionFilter) {
		Debugger.NULL_CHECK(source, "null position in getDirections()");

		// All directions that satisfy the direction condition and that point
		// to a tile that is unique and satisfies the tile condition
		List<Direction> goodDirections = new ArrayList<Direction>();
		// True if the tile in the specified direction passes all tile conditions
		boolean tilePass;
		// True if the direction passes all direction conditions
		boolean directionPass;

		/* Iterate through all directions, capturing all good directions. */
		for (Direction direction : DIRECTIONS) {
			Position neighbor = getPosition(source, direction);
			
			// neighbor may be null if neighbor and source are identical
			if (neighbor != null) {
				tilePass = (tileFilter == null) || 
						tileFilter.tileCondition(at(neighbor));
				directionPass = (directionFilter == null) || 
						directionFilter.directionCondition(direction);
			
				if (tilePass && directionPass) {
					goodDirections.add(direction);
				}
			}
		}

		return goodDirections.toArray(new Direction[goodDirections.size()]);
	}
		
	/**
	 * A similar function to getDirections, this function returns positions
	 * instead of directions but is otherwise identical.
	 * 
	 * @see danaus.Map#getDirections(Position, TileCondition, DirectionCondition)
	 */
	private Position[] getPositions(Position source,
			TileCondition tileFilter, DirectionCondition directionFilter) {
		Direction directions[] = getDirections(source, tileFilter, directionFilter);
		Position positions[] = new Position[directions.length];
		
		for (int i = 0; i < directions.length; i++) {
			positions[i] = getPosition(source, directions[i]);
		}
		
		return positions;
	}
	
	/**
	 * Returns the position reached by beginning at the source position and
	 * moving exactly one tile in the specified direction. Due to the continuous
	 * nature of the map, the tile reached may be the same as the source tile.
	 * In this event, a null position is returned.
	 * 
	 * @param source The initial position from which to travel.
	 * @param direction The direction to travel.
	 * @return The position reached from the source position traveling in the
	 * specified direction, or null if these two positions are the same.
	 */
	private Position getPosition(Position source, Direction direction) {
		Debugger.NULL_CHECK(source, "null position in getLocation()");
		Debugger.NULL_CHECK(direction, "null direction in getLocation()");
		
		/* The row and column of the tile in the specified direction. */
		int newRow = wrapRow(source.row + direction.dRow);
		int newCol = wrapCol(source.col + direction.dCol);
		
		/* If the new row and column are the same as the old row and column, 
		 * we've wrapped around the map and come back to ourselves. In this 
		 * event, return null. */
		if (newRow == source.row && newCol == source.col) {
			return null;
		}
		
		return new Position(newRow, newCol);
	}
		
	////////////////////////////////////////////////////////////////////////////
	// Accessing and Setting Tiles
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the tile at the specified position. The tile itself has no 
	 * guarantee of being initialized. That is, this function may return null.
	 * 
	 * @param position The position of the returned tile.
	 * @return The tile at the specified position
	 */
	private Tile at(Position position) {
		Debugger.NULL_CHECK(position, "null position in at(Position)!");
		return tiles[position.row][position.col];
	}
	
	/**
	 * Sets the tile at the specified position. The previous tile at the
	 * specified tile is overriden and the new tile is inserted. Both tiles,
	 * the one being replaced and the one being inserted, may be null.
	 * 
	 * @param position The position of the tile to set.
	 * @param tile The tile to set at the specified position.
	 */
	private void set(Position position, Tile tile) {
		Debugger.NULL_CHECK(position, "null position in set(Position, Tile)!");
		tiles[position.row][position.col] = tile;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	////////////////////////////////////////////////////////////////////////////
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	/**
	 * Converts all the null tiles in a map to water tiles with non-random
	 * tile states.
	 */
	private void nullToWater() {
		Debugger.DEBUG("Converting Null to Water...");
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (tiles[row][col] == null) {
					Position p = new Position(row, col);
					set(p, new Water(skin, new TileState(p.toLocation())));
				}
			}
		}
	}

	/** 
	 * A hybrid of Columbus' view of a flattened Earth and the reality of a 
	 * continuous spherical surface, a map has no edges. Instead, opposing 
	 * edges are connected despite their visual discontinuity. These functions
	 * wrap a row or column around the edge of the map and ensure they fall 
	 * within a valid position.
	 */
	private int wrapRow(int row) {
		return Common.mod(row, height);
	}
	private int wrapCol(int col) {
		return Common.mod(col, width);
	}
	
	/** 
	 * Converts between x-y coordinates and row/column indices.
	 *  
	 * A map's tile array is indexed by row and column. The tiles themselves,
	 * however, each have locations that correspond to an x-y coordinate in a 
	 * Euclidean plane. The lower left tile is at location (1,1). 
	 * 
	 * Note that these functions do not test the validity of the inputs. Tile
	 * validity is left to the caller.
	 */
	private int rowToY(int row) {
		return row;
	}
	
	private int colToX(int col) {
		return col;
	}
	
	private int yToRow(int y) {
		return y;
	}
	
	private int xToCol(int x) {
		return x;
	}
	
	/**
	 * Returns a set of all the flowers in the map.
	 */
	HashSet<Flower> getFlowers() {
		HashSet<Flower> flowers = new HashSet<Flower>();
		for (Position flowerPos : learningFlowerPositions) {
			flowers.addAll(at(flowerPos).tileState.flowers);
		}
		
		return flowers;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Strings and Printing
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prints the map's settings. 
	 */
	public void printSettings() {
		// title
		System.out.println("MAP SETTINGS");
		System.out.println("========================");
		
		// skin
		System.out.printf("%-20s : %10s\n", "skin", skin);
		
		// light
		System.out.printf("%-20s : %10b\n", "randomLight", randomLight);
		System.out.printf("%-20s : %10d\n", "default_light", default_light);
		System.out.printf("%-20s : %10d\n", "min_light", min_light);
		System.out.printf("%-20s : %10d\n", "max_light", max_light);
		
		// wind
		System.out.printf("%-20s : %10b\n", "randomWind", randomWind);
		String windPrint = default_wind == null ? "null" : default_wind.toString();
		System.out.printf("%-20s : %10s\n", "default_wind", windPrint);
		System.out.printf("%-20s : %10d\n", "min_wind", min_wind);
		System.out.printf("%-20s : %10d\n", "max_wind", max_wind);
		
		// flowers
		System.out.printf("%-20s : %10b\n", "randomFlowers", randomFlowers);
		System.out.printf("%-20s : %10d\n", "expected_flowers", expected_learning_flowers);
		
		// aroma
		System.out.printf("%-20s : %10b\n", "randomAroma", randomAromaIntensity);
		System.out.printf("%-20s : %10.2f\n", "default_aroma", default_aroma_intensity);
		System.out.printf("%-20s : %10.2f\n", "min_aroma", min_aroma_intensity);
		System.out.printf("%-20s : %10.2f\n", "max_aroma", max_aroma_intensity);
	}
	
	/**
	 * Returns a string representation of the object printed to show a map. It's
	 * supposed to look like a treasure map :D
	 *
	 * @return a string representation of the object.
	 */
	public @Override String toString() {
		// The string representation of the map
		String map = "";
		// The symbol for the top edge of the map
		String top = ",,";
		// The symbol for the bottom edge of the map
		String bottom = "''";
		// The symbols for the sides of the map. The edge symbol alternates
		// every row
		String sides[] = new String[]{"\\", "/"};
		// The string representation of a null tile
		String nullTile = "00";
		// The optional spaces on the left edge of the map
		String leftSpace = "";
		// The optional spaces on the right edge of the map
		String rightSpace = "";
		// The size of the left and right spaces
		int spaceSize = leftSpace.length() + rightSpace.length();
		// The width of spaces reserved for left edge numbers
		int numberWidth = 3;
		// The spaces after the left numbers.
		String numberSpace = " ";

		// Add the top margin, accounting for numbers and margins.
		for (int i = 0; i < numberWidth + numberSpace.length(); i++) {
			map += " ";
		}
		// Every symbol is two wide, except the edges, so +1
		for (int i = 0; i < width + spaceSize + 1; i++) {
			map += top;
		}
		map += "\n";
		
		/* Add the body of the map. */
		for (int row = 0; row < height; row++) {
			/* Add numbers to the left edge of the map. */
			int y = rowToY(row);
			// The number of digits in the y value
			int digits = String.valueOf(y).length();
			for (int i = 0; i < numberWidth - digits; i++) {
				map += " ";
			}
			map += y + " " + sides[row % 2] + leftSpace;
			
			/* Add the actual map content. */
			for (int column = 0; column < width; column++) {
				if (tiles[row][column] != null) {
					map += tiles[row][column].toStringMap();
				}
				else {
					map += nullTile;
				}
			}
			
			/* Add the right margin. */
			map += rightSpace + sides[row % 2] + "\n";
		}
		
		/* Add the bottom edge, accounting for numbers and margins.*/
		for (int i = 0; i < numberWidth + numberSpace.length(); i++) {
			map += " ";
		}
		// Every symbol is two wide, except the edges, so +1
		for (int i = 0; i < width + spaceSize + 1; i++) {
			map += bottom;
		}
		map += "\n    ";
		
		/* Add the bottom numbers. */
		for (int column = 0; column < width; column++) {
			int x = colToX(column);
			/* Every multiple of 10, add the first digit. */
			if (x % 10 == 0) {
				map += String.valueOf(x).charAt(0) + " ";
			}
			/* Every multiple of five add a five. */
			else if (x % 5 == 0) {
				map += 5 + " ";
			}
			else {
				map += "  ";
			}
		}
		
		return map;
	}
	
	/**
	 * Returns a detailed string representation of the map. Each tile's 
	 * information is fully enumerated.
	 * 
	 * @return A detailed string representation of the map.
	 */
	public String toStringDetailed() {
		String string = "";
		for (int row = height - 1; row >= 0; row--) {
			for (int column = 0; column < width; column++) {
				string += tiles[row][column].toString() + "\n";
			}
		}
		
		return string;
	}
	
	
	
	/**
	 * Draws the map on the screen and pauses for a brief duration to allow a 
	 * user to observe the map. When invoked after every tile placement, this
	 * method has the effect of animating map generation.
	 */
	private void draw() {
		if (DRAW) {
			try {
				System.out.println(park.state);
				System.out.println(toString());
				System.out.println("");
				if (!instadraw) {
					Thread.sleep(DRAW_TIME);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sketch() {
		if (SKETCH) {
			try {
				System.out.println(park.state);
				System.out.println(toString());
				System.out.println("");
				if (!instadraw) {
					Thread.sleep(SKETCH_TIME);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Position Class
	////////////////////////////////////////////////////////////////////////////
	/**
	 * An instance represents a position within an array of tiles. 
	 * 
	 * Maps are designed to be user friendly, so the coordinate system mimics
	 * that of a Euclidean Plane: a coordinate system most people are familiar
	 * with. Manipulating arrays, however, is better dealt with in terms of rows
	 * and column. This also allows for the following useful precondition.
	 * 
	 * Precondition: A position's row and column are valid in the map they are
	 * constructed in. This preconditions proves very useful because no method
	 * that deals with Positions have to check for tile validity.  
	 */
	private class Position implements Comparable <Position> {
		public final int row; // row within a tile array
		public final int col; // column within a tile array

		/**
		 * Constructs a Position instance with a given row and column.
		 * 
		 * Precondition: row and col are valid row and column indices into the
		 * tiles arrray
		 * 
		 * @param row A Position's row.
		 * @param col A Position's column.
		 */
		Position(int row, int col) {
			assert row >= 0 && row < height && col >= 0 && col < width;
			
			this.row = row;
			this.col = col;
		}
		
		/**
		 * Position copy constructor.
		 *  
		 * @param other A position to copy.
		 */
		Position(Position other) {
			this.row = other.row;
			this.col = other.col;
		}
		
		/**
		 * Position constructor from Location.
		 * 
		 * Precondition: The location's x and y coordinates translate to valid
		 * row and column indices.
		 * 
		 * @param location A location to convert to a position.
		 */
		Position(Location location) {
			int _row = yToRow(location.row);
			int _col = xToCol(location.col);
			
			assert _row >= 0 && _row < height && _col >= 0 && _col < width;

			this.row = _row;
			this.col = _col;
		}
		
		/**
		 * Converts a position to a location. 
		 * 
		 * @return A location instance translated from a position.
		 */
		public Location toLocation() {
			return new Location(colToX(col), rowToY(row));
		}
		
		/**
		 * Returns a string representation of a position. 
		 *
		 * @return a string representation of a position.
		 */
		public @Override String toString() { 
			return "(" + rowToY(row) + ", "  + colToX(col) + ")";
		}
		
		/**
		 * Indicates whether some other object is "equal to" this one.
		 * 
		 * @param obj the reference object with which to compare.
		 * @return true if this object is the same as the obj argument; false
		 *         otherwise.
		 */
		public @Override boolean equals(Object obj) {
			// Check for class equality. getClass is used over instanceof to
			// preserve symmetry. Also, it is unlikely a subclass of Position 
			// will exist.
			if (getClass() != obj.getClass()) {
				return false;
			}
			
			Position other = (Position) obj;
			return (row == other.row && col == other.col);
		}
		
		/**
		 * Returns a hash code value for the object. This method is supported
		 * for the benefit of hashtables such as those provided by
		 * java.util.Hashtable.
		 * 
		 * @return a hash code value for this object.
		 */
		public @Override int hashCode() {
			// Taken from the internet. 
			// (http://stackoverflow.com/questions/9135759/java-hashcode-for-a-point-class)
			int hash = 7;
			hash = 71 * hash + row;
			hash = 71 * hash + col;
			return hash;
		}
		
		/**
		 * Compares this object with the specified object for order. Returns a 
		 * negative integer, zero, or a positive integer as this object is less 
		 * than, equal to, or greater than the specified object.
		 * 
		 * @return a negative integer, zero, or a positive integer as this 
		 * object is less than, equal to, or greater than the specified object.
		 */
		public @Override int compareTo(Position other) {
			if (row != other.row) {
				return row - other.row;
			}
			return col - other.col;
		}
	}
}
