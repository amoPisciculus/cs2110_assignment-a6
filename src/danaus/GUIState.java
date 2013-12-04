package danaus;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/* *************************************************************************//**
 * An instance represents the box on the right side of the GUI, which displays
 * information about the game. For more detail into the fields of this class,
 * refer to ParkState. 
 * ****************************************************************************/
@SuppressWarnings("serial")
public class GUIState extends JPanel {
	private StateLabel turn;
	private StateLabel slowTurns;
	private StateLabel location;
	private StateLabel power;
	private StateLabel exploredTiles;
	private StateLabel foundFlowers;
	private StateLabel powerSpent;
	private StateLabel powerConsumed;
	private StateLabel cliffCollisions;
	private StateLabel waterCollisions;
	
	/* *********************************************************************//**
	 * Constructor: an intialized GUIState. 
	 * ************************************************************************/
	GUIState() {
		initGUI();
	}
	
	/* *********************************************************************//**
	 * Initialize the GUI.
	 * ************************************************************************/
	private void initGUI() {
		setLayout(new GridBagLayout());
		setBackground(GUI.BACKGROUND_COLOR);
		setBorder(new TitledBorder("Game Statistics") {
			private final int in = 10;
			private Insets borderInsets = new Insets(in, in, in, in);

			public Insets getBorderInsets(Component c) {
				return borderInsets;
		    }
		});
		
		turn = new StateLabel("0");
		slowTurns = new StateLabel("0");
		location = new StateLabel("(0, 0)");
		power = new StateLabel("0");
		exploredTiles = new StateLabel("0/0");
		foundFlowers = new StateLabel("0");
		powerSpent = new StateLabel("0");
		powerConsumed = new StateLabel("0");
		cliffCollisions = new StateLabel("0");
		waterCollisions = new StateLabel("0");

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4,4,4,4);
		constraints.anchor = GridBagConstraints.LINE_START;
	
		int col = 0;
		int row = 0;
		add(new StateLabel("Turn Number: "), constraints, col, row++);
		add(new StateLabel("Slow Turns: "), constraints, col, row++);
		add(new StateLabel("Location: "), constraints, col, row++);
		add(new StateLabel("Power: "), constraints, col, row++);
		add(new StateLabel("Explored Tiles: "), constraints, col, row++);
		add(new StateLabel("Discovered Flowers: "), constraints, col, row++);
		add(new StateLabel("Power Spent: "), constraints, col, row++);
		add(new StateLabel("Power Consumed: "), constraints, col, row++);
		add(new StateLabel("Cliff Collisions: "), constraints, col, row++);
		add(new StateLabel("Water Collisions: "), constraints, col, row++);
		
		row = 0;
		col = 1;
		constraints.anchor = GridBagConstraints.LINE_END;
		add(turn, constraints, col, row++);
		add(slowTurns, constraints, col, row++);
		add(location, constraints, col, row++);
		add(power, constraints, col, row++);
		add(exploredTiles, constraints, col, row++);
		add(foundFlowers, constraints, col, row++);
		add(powerSpent, constraints, col, row++);
		add(powerConsumed, constraints, col, row++);
		add(cliffCollisions, constraints, col, row++);
		add(waterCollisions, constraints, col, row++);
	}
	
	/* *********************************************************************//**
	 * Add label to the box at (gridx, gridy) using constraints.
	 * ************************************************************************/
	private void add(StateLabel label, GridBagConstraints constraints, 
	int gridx, int gridy) {
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		add(label, constraints);
	}
	
	/* *********************************************************************//**
	 * Update the simulation's statistics in state, using butterfly bfly.
	 * ************************************************************************/
	public void updateState(ParkState state, AbstractButterfly bfly) {
		String reqFlowerNum = state.requiredFlowers == null ?
				"0" : String.valueOf(state.requiredFlowers.size());
		
		turn.setText(String.valueOf(state.turn));
		slowTurns.setText(String.valueOf(state.slowTurns));
		location.setText(bfly.location.toString());
		power.setText(String.valueOf(bfly.getPower().getPower()));
		exploredTiles.setText(String.valueOf("" + state.exploredTiles + "/" + state.numTiles));
		foundFlowers.setText("" + state.foundFlowers.size() + "/" + reqFlowerNum);
		powerSpent.setText(String.valueOf(state.powerSpent));
		powerConsumed.setText(String.valueOf(state.powerConsumed));
		cliffCollisions.setText(String.valueOf(state.cliffCollisions));
		waterCollisions.setText(String.valueOf(state.waterCollisions));
	}
	
	/* *********************************************************************//**
	 * A nicely formatted monospace label to display state statistics. 
	 * ************************************************************************/
	private class StateLabel extends JLabel {
		StateLabel(String text) {
			setText(text);
			setFont(new Font("Monospaced", Font.BOLD, 14));
		}
	}
}
