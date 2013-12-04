package danaus;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;

/* *************************************************************************//**
 * An instance represents the central GUI element for a butterfly simulation.
 * The GUI has four main components: the menu, the northern panel, the eastern
 * panel, and the center panel. For information on each component, refer to 
 * their respective initialization methods.
 * ****************************************************************************/
@SuppressWarnings("serial")
public class GUI extends JFrame implements ChangeListener {
	/** JPanel inset thickness. */
	private static int thickness = 2;
	
	/* Both these colors were taken from http://ethanschoonover.com/solarized */
	/** A nice off-white background color for the GUI background */
	public static Color BACKGROUND_COLOR = new Color(253, 246, 227);
	/** A contrasting color that lies between components. */
	public static Color MARGIN_COLOR = new Color(0, 43, 54);
	
	private Clip audio;

	/** The slider element that controls the animation's fpm. */
	private JSlider fpmSlider;
	/** The spinner element that controls the animation's fpm. */
	private JSpinner fpmSpinner;

	/** The main panel. */
	JPanel mainPanel;
	/** The northern slider panel. */
	JPanel northPanel;
	/** The eastern gui state panel. */
	 GUIState statePanel;
	/** The eastern tile info panel. */
	 GUITileInfo tileInfoPanel;
	/** The center panel. */
	private GUIMap mapPanel;
	/* The northern panel is not stored because it is never referenced. */
	
	Simulator simulator;
	
	/* *********************************************************************//**
	 * Constructor: a GUI for s
	 * ************************************************************************/
	public GUI(Simulator s) {
		mainPanel = new JPanel(new BorderLayout(thickness, thickness));
		this.simulator = s;

		/* The center of the GUI must be initialized before the menu or northern
		 * panel of the GUI, since those two components reference the center 
		 * panel. */
		initGUI(mainPanel, s.park.map.seed);
		initCenter(mainPanel, s.park.map.butterfly, s.park.map.tiles);
		initMenu();
		initNorth(mainPanel);
		initEast(mainPanel);
		
		getContentPane().add(mainPanel);	
		pack();
		setMinimumSize(getSize());
	}
	
	/* *********************************************************************//**
	 * Initializes the non component specific GUI attributes on panel panel
     * using random number seed seed.
	 * ************************************************************************/
	private void initGUI(JPanel panel, long seed) {
		panel.setBackground(MARGIN_COLOR);
		panel.setBorder(BorderFactory.createLineBorder(MARGIN_COLOR, thickness));
		setTitle("Map : " + String.valueOf(seed));
		initAudio();
		setSize(800, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/** 
	 * Initialize the audio, using a playing sound found on the web.
     * Borrowed heavily from
     * // http://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html
     */
	private void initAudio() {
		try {
			Path p = Paths.get(Common.absolute_path() + "/../res/audio/zelda.wav");
			File music = new File(p.toUri());
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(music);
			audio = AudioSystem.getClip();
			audio.open(audioStream);
			long startMicrosecond = 
                (long) new Randomer().nextInt((int) audio.getMicrosecondLength());
			audio.setMicrosecondPosition(startMicrosecond);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
	}
	
	/* *********************************************************************//**
	 * Initializes the menu at the top of the GUI's frame. The menu contains 
	 * multiple options and settings to control the flow of a simulation.
	 * ************************************************************************/
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* File */
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		file.setToolTipText("Alt-F");
				
		JMenuItem _exit = makeJMenuItem("Exit", KeyEvent.VK_Q, "Ctrl-Q", 
			new ActionListener() {
				public @Override void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			}
		);
		
		/* Settings */
		JMenu settings = new JMenu("Settings");
		settings.setMnemonic(KeyEvent.VK_S);
		settings.setToolTipText("Alt-S");
		
		audio.stop();
		JCheckBoxMenuItem _mute = makeJCheckBoxMenuItem("Mute", 
			KeyEvent.VK_M, "Ctrl-M", true,
			new ActionListener() {
				public @Override void actionPerformed(ActionEvent e) {
					if (audio.isRunning()) {
						audio.stop();
					}
					else {
						audio.start();
						audio.loop(Clip.LOOP_CONTINUOUSLY);
					}
				}
			}
		);
		
		mapPanel.lockOn = true;
		JCheckBoxMenuItem _lock = makeJCheckBoxMenuItem("Lock",
				KeyEvent.VK_L, "Lock the butterfly's position to the center " +
						"of the screen", true, 
				new ActionListener() {
					public @Override void actionPerformed(ActionEvent e) {
						mapPanel.lockOn = !mapPanel.lockOn;
					}
				}
			);

		file.addSeparator();
		file.add(_exit);
		file.add(settings);
		
		settings.add(_mute);
		settings.add(_lock);
		
		menuBar.add(file);
		menuBar.add(settings);
		
		setJMenuBar(menuBar);
	}
	
	/* *********************************************************************//**
	 * Constructs a menu item with various features.
	 * 
	 * @param name The name of the menu item.
	 * @param keystroke The keystroke that, when paired with control, will invoke
	 * the menu item's callback. Use "KeyEvent.VK_*"
	 * @param toolTipText The menu item's tool tip text.
	 * @param listener The callback interface.
	 * @return A menu item with various features
	 * ************************************************************************/
	private JMenuItem makeJMenuItem(String name, int keystroke, 
			String toolTipText, ActionListener listener) {
		JMenuItem item = new JMenuItem(name);
		item.setAccelerator(KeyStroke.getKeyStroke(keystroke, ActionEvent.CTRL_MASK));
		item.setToolTipText(toolTipText);
		item.addActionListener(listener);

		return item;
	}
	
	/* *********************************************************************//**
	 * Constructs a menu item with various features.
	 * 
	 * @param initial True if the checkbox should be checked. False, otherwise.
	 * @return A check box menu item with various features.
	 * @see danaus.GUI#makeJMenuItem(String, int, String, ActionListener)
	 * ************************************************************************/
	private JCheckBoxMenuItem makeJCheckBoxMenuItem(String name, int keystroke,
			String toolTipText, boolean initial, ActionListener listener) {
		JCheckBoxMenuItem check = new JCheckBoxMenuItem(name);
		check.setAccelerator(KeyStroke.getKeyStroke(keystroke, ActionEvent.CTRL_MASK));
		check.setToolTipText(toolTipText);
		check.setState(initial);
		check.addActionListener(listener);
		
		return check;
	}
	
	/***************************************************************************
     * Initialize the northern panel of the GUI. It contains a synchronized
	 * slider and spinner that control the speed of the simulation.
     **************************************************************************/
	private void initNorth(JPanel panel) {
		northPanel = new JPanel(new BorderLayout(thickness, thickness));
		northPanel.setBackground(BACKGROUND_COLOR);
		northPanel.setBorder(BorderFactory.createTitledBorder("Frames Per Move"));
		
		/* Build fpm slider. */
		fpmSlider = new JSlider(JSlider.HORIZONTAL, 
				GUIMap.MIN_FPM, GUIMap.MAX_FPM, GUIMap.INIT_FPM);
		fpmSlider.setMajorTickSpacing(10);
		fpmSlider.setMinorTickSpacing(1);
		fpmSlider.setPaintTicks(true);
		fpmSlider.setPaintLabels(true);
		fpmSlider.setSnapToTicks(true);
		fpmSlider.setToolTipText("The duration, in frames, of a butterfly move.");
		fpmSlider.setOpaque(false);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(GUIMap.MAX_FPM, new JLabel(String.valueOf('\u221e')));
		for (int i = 0; i < GUIMap.MAX_FPM; i += 10) {
			labelTable.put(i, new JLabel(String.valueOf(i)));
		}
		fpmSlider.setLabelTable(labelTable);		
		
		/* Build fpm spinner */
		SpinnerNumberModel model = new SpinnerNumberModel(
				GUIMap.INIT_FPM, GUIMap.MIN_FPM, GUIMap.MAX_FPM, 1);
		fpmSpinner = new JSpinner(model);

		/* Synchronize the two */
		fpmSlider.addChangeListener(this);
		fpmSpinner.addChangeListener(this);
		
		/* Add them to the GUI */
		northPanel.add(fpmSlider, BorderLayout.CENTER);
		northPanel.add(fpmSpinner, BorderLayout.WEST);
		panel.add(northPanel, BorderLayout.NORTH);
	}
	
	/***************************************************************************
     * Place on the east of p the east panel of the GUI, which displays
	 * the state of the simulation. The information is taken from a park
	 * state object.
     *
	 * @param p The GUI's main panel which should have a BorderLayout
	 * ************************************************************************/
	private void initEast(JPanel p) {
		JPanel east = new JPanel(new BorderLayout());
		statePanel = new GUIState();
		tileInfoPanel = new GUITileInfo();
		
		east.add(statePanel, BorderLayout.CENTER);
		east.add(tileInfoPanel, BorderLayout.EAST);
		
		p.add(east, BorderLayout.EAST);
	}
	
	/* *********************************************************************//**
	 * @see danaus.GUIState#updateState(ParkState, AbstractButterfly)
	 * ************************************************************************/
	public void updateState(ParkState parkState, AbstractButterfly bfly) {
		statePanel.updateState(parkState, bfly);
	}
	
	/***************************************************************************
     * Place on the north of p the northern panel of the GUI, which contains
	 * a synchronized slider and spinner that control the speed of the
	 * simulation. Initialize field map as well.
	 * 
	 * @param p The GUI's main panel which should have a BorderLayout.
	 * @param bfly The butterfly on the map.
	 * @param tiles The map's tiles.
	 **************************************************************************/
	private void initCenter(JPanel p, AbstractButterfly bfly, Tile[][] tiles) {
		mapPanel = new GUIMap(this, bfly, tiles);	
		p.add(mapPanel, BorderLayout.CENTER);
	}
	
	/* *********************************************************************//**
	 * Triggered by moving the fpm slider, this ensures the butterfly, the
	 * slider, and the spinner all agree on the fpm value.
	 * ************************************************************************/
	public @Override void stateChanged(ChangeEvent e) {
		Object changed = e.getSource();
		if (changed.equals(fpmSlider)) {
			mapPanel.fpm = fpmSlider.getValue();
			fpmSpinner.setValue(mapPanel.fpm);
		}
		else if (changed == fpmSpinner) {
			mapPanel.fpm = (int) fpmSpinner.getValue();
			fpmSlider.setValue(mapPanel.fpm);
		}
		
		mapPanel.updateFPM();
	}
	
	/* *********************************************************************//**
	 * @see danaus.GUIButterfly#move(int, Direction, int, int, int)
	 * ************************************************************************/
	public void move(int s, Direction d, int toRow, int toCol) {
		try {
			simulator.GUIMoving.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mapPanel.move(s, d, toRow, toCol);
	}
	
	/* *********************************************************************//**
	 * Informs the simulator that the move is complete. 
	 * 
	 * @see danaus.Simulator#update(int, Direction, int, int, int, int)
	 * ************************************************************************/
	public void wakeupSimulator() {
		simulator.GUIMoving.release();
	}
	
	/***************************************************************************
     * Update the tile information box to be at point position. This is
     * triggered by a mouse click on the map. 
     **************************************************************************/
	public void updateTileInfo(Point position) {
		Tile t = simulator.park.map.tiles[position.y][position.x];
		tileInfoPanel.update(t);
		
		/** ADD PRINT STATEMENTS HERE. */
		//
		//
	}
	
    /***************************************************************************
     * Re-tile the map to use tiles.
     **************************************************************************/
	public void retile(Tile[][] tiles) {
		mapPanel.retile(tiles);
	}
}
