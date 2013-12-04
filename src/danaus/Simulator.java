package danaus;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/* *************************************************************************//**
 * An instance represents the heart of Danaus' simulation engine. The 
 * controller in the Model-View-Controller pattern, a simulator connects the
 * user, the park, and the GUI. Graphically, you can visualize a simulator as
 * follows.
 * 
 *  								USER
 *  								 ||
 *  								 \/
 *  						     SIMULATOR
 *                               /\     /\
 *                               ||     ||
 *                               \/     \/
 *                              PARK    GUI
 * 
 * Simulators parse command line options and arguments from the user to 
 * configure the simulation. It then relays information between the park and
 * the GUI. 
 * ****************************************************************************/
public class Simulator {
   	/** Use a GUI iff HEADLESS is true. */
	public static boolean HEADLESS;
	/** The seed parsed from the command line. */
	public static Integer SEED;
	/** The name of the map file parsed from the command line */
	public static String MAP_FILENAME;
	/** If true, a butterfly has infinite energy. */
	public static boolean INFINITE_ENERGY;
	/** The names of the classes parsed from the command line. */
	public static List<String> CLASS_NAMES = new ArrayList<String>();
	
	/** A simulator's park. */
	public Park park;
	/** A simulator's GUI */
	public GUI gui;

	/** When the Simulator owns the semaphore, the GUI cannot move. When the 
	 * GUI owns the semaphore, the simulator cannot run. */
	public Semaphore GUIMoving;
	
	////////////////////////////////////////////////////////////////////////////
	// Static Methods
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The entry point of a Danaus simulation. Users can provide command line
     * options and arguments to configure the execution of the simulation.
	 * 
	 * @param args Command line arguments
	 * @see danaus.Simulator#handle_arguments(String[])
	 */
	public static void main(String args[]) 
	throws ParserConfigurationException, SAXException, IOException {		
		Debugger.DEBUG("Simulation Enterred...");
		
		Debugger.DEBUG("Parsing Arguments...");
		Simulator.handle_arguments(args);
		Simulator.INFINITE_ENERGY = true;
				
		final Simulator simulator;
		if (null == Simulator.MAP_FILENAME) {
			simulator = new Simulator();
		}
		else {
			simulator = new Simulator(Simulator.MAP_FILENAME);
		}
		
		if (!HEADLESS) {
			Debugger.DEBUG("GUI Initializing...");
			GUI gui = new GUI(simulator);
			simulator.gui = gui;
			gui.updateState(simulator.park.state, simulator.park.map.butterfly);
			gui.setVisible(true);
		}

		Debugger.DEBUG("Simulation Begun...");
		simulator.simulate();
		Debugger.DEBUG("Simulation Complete.");
		
		if (simulator.park.isVictorious()) {
			simulator.victory();
		} 
		else {
			simulator.failure();
		}
	}
	
	/**
	 * Handles command line arguments. Arguments must be formatted as if they
	 * were parsed by the getopt command. That is, options cannot be grouped,
	 * arguments must follow the options separated by whitespace, etc. Class
	 * names must be prefixed with the name of the package. For example,
	 * "student.Butterfly" is valid, but "Butterfly" is not.
	 * 
	 * @param sysv Command line arguments.
	 * @see danaus.Simulator#usage()
	 */
	public static void handle_arguments(String sysv[]) {
		for (int i = 0; i < sysv.length;) {
			switch(sysv[i]) {
			case ("--help"):
				usage();
				System.exit(0);
			case ("-h"):
			case ("--headless"):
				HEADLESS = true;
				i++;
				break;
			case ("-s"):
			case ("--seed"):
				SEED = Integer.parseInt(sysv[i + 1]);
				i += 2;
				break;
			case ("-f"):
			case ("--file"):
				MAP_FILENAME = sysv[i + 1];
				i += 2;
				break;
			case ("-d"):
			case ("--debug"):
				Debugger.DEBUG_ENABLED = true;
				i++;
				break;
			case ("-w"):
			case ("--warning"):
				Debugger.WARNING_ENABLED = true;
				i++;
				break;
			case ("-i"):
			case ("--infinite"):
				INFINITE_ENERGY = true;
				i++;
				break;
			default:
				CLASS_NAMES.add(sysv[i]);
				i++;
				break;
			}
		}
		
		// If no classes are specified, we default to "default.Butterfly."
		if (0 == CLASS_NAMES.size()) {
			CLASS_NAMES.add("student.Butterfly");
		}
	}
	
	/* Prints the usage of this application. */
	public static void usage() {
		String call    = "java danaus.Simulator ";
		String options = "[--help] ";
		options       += "[-h | --headless] ";
		options       += "[-d | --debug] ";
		options       += "[-w | --warning] ";
		options       += "[-i | --infinite] ";
		options 	  += "[-s | --seed seed] ";
		options 	  += "[-f | --file file] ";
		String params  = "[class]...";
		System.out.println("USAGE " + call + options + params); 
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Class definition
	////////////////////////////////////////////////////////////////////////////
	/* Constructor: an instance operating on a random map. */
	Simulator() {
		park = new Park(this);
		GUIMoving = new Semaphore(1);
		Common.acquire_or_exit(GUIMoving);
	}
	
	/**
	 * Constructor: an instance using the map given by filename. 
     * Throw a ParserConfigurationException, SAXException, or IOEXception if 
     * there is a problem with the map.
	 */
	Simulator(String filename) throws ParserConfigurationException, 
	SAXException, IOException {
		park = new Park(this, filename);
		GUIMoving = new Semaphore(1);
		Common.acquire_or_exit(GUIMoving);
	}
	
	/* Begin the simulation. */
	public void simulate() {
		park.simulate();
	}
	
	/**
	 * Update the GUI if one is being used. 
	 */
	public synchronized void update(int speed, Direction direction, 
			int fromRow, int fromCol, int toRow, int toCol) {
		if (Simulator.HEADLESS) {
			return;
		}
		
		GUIMoving.release();
		gui.updateState(park.state, park.map.butterfly);
		gui.move(speed, direction, toRow, toCol);
		Common.acquire_or_exit(GUIMoving);
	}
	
	/**
	 * Update the GUI if one is being used.
	 */
	public synchronized void update() {
		if (Simulator.HEADLESS) {
			return;
		}
		
		GUIMoving.release();
		gui.updateState(park.state, park.map.butterfly);
		Common.acquire_or_exit(GUIMoving);
	}
	
	/**
	 * Informs the GUI to retile itself. This is executed during the transition
	 * to running mode.
	 */
	public void retile() {
		if (!HEADLESS) {
			gui.retile(park.map.tiles);
		}
	}
	
	/** Congratulates the player on his or her victory. */
	public void victory() {
		if (HEADLESS) {
			headlessVictory();
		}
		else {
			guiVictory();
		}
	}

	/**
	 * Consoles the player in his or her time of defeat.
	 */
	public void failure() {
		if (HEADLESS) {
			headlessFailure();
		}
		else {
			guiFailure();
		}
	}

	/**
	 * Sets the GUI's background green to indicate victory.
	 */
	public void guiVictory() {
		Color victory = new Color(133, 153, 0); // Green
		gui.northPanel.setBackground(victory);
		gui.statePanel.setBackground(victory);
		gui.tileInfoPanel.setBackground(victory);
	}

	/** Prints victory message and game statistics. */
	public void headlessVictory() {
		System.out.println("WIN");
		System.out.println("=================================");
		System.out.println(getPerfStats());
	}

	/** Sets the GUI's background red to indicate failure. */
	public void guiFailure() {
		Color failure = new Color(220, 50, 47); // red
		gui.northPanel.setBackground(failure);
		gui.statePanel.setBackground(failure);
		gui.tileInfoPanel.setBackground(failure);
	}

	/** Prints a defeat message and game statistics. */
	public void headlessFailure() {
		System.out.println("LOSE.");
		System.out.println("==================================");
		System.out.println(getPerfStats());
	}
	
	/** Return a string version of the game's performance statistics. */
	private String getPerfStats() {
		String colOneSize = "15"; // size of first column
		String colTwoSize = "15";  // size of second column
		String halfColTwoSize = String.valueOf((Integer.parseInt(colTwoSize) / 2));
		
		/* Adjust time according to slow turns. */
		double totalTime_ = (park.learningTime + park.runningTime) / 1000000000.0;
		long totalTurns_ = park.state.turn + park.state.slowTurns;
		double timePerTurn_ = totalTime_ / park.state.turn;
		double adjustedTime_ = timePerTurn_ * totalTurns_;
		
		String turns = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + "s\n", 
			"Turns", park.state.turn
		);
		String slowTurns = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + "s\n", 
			"Slow Turns", park.state.slowTurns
		);
		String totalTurns = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + "s\n", 
			"Total Turns", totalTurns_
		);
		String learningTime = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + ".6f\n", 
			"Learning Time", park.learningTime / 1000000000.0
		);
		String runningTime = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + ".6f\n", 
			"Running Time", park.runningTime / 1000000000.0
		);
		String totalTime = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + ".6f\n", 
			"Total Time", totalTime_ 
		);
		
		String adjustedTime = String.format(
			"%-" + colOneSize + "s: %" + colTwoSize + ".6f\n", 
			"Adjusted Time", adjustedTime_
		);
		String learningScore = String.format(
			"%-" + colOneSize + "s: %" + 
		    String.valueOf(Integer.parseInt(colTwoSize) - 1) + ".1f%%\n", 
			"Learning Score", park.learningScore 
		);
		String flowersCorrect = String.format(
			"%-" + colOneSize + "s: %" + halfColTwoSize + "s/%" + halfColTwoSize + "s", 
			"Flowers Found", park.state.foundFlowers.size(), 
			park.state.requiredFlowers.size()
		);

		return turns + 
			   slowTurns +
			   totalTurns +
			   learningTime + 
			   runningTime +
			   totalTime +
			   adjustedTime +
			   learningScore +
			   flowersCorrect;
	}
}
