package danaus;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/* *************************************************************************//**
 * An instance is the box on the very right of the GUI that displays information
 * about the tile that was most recently clicked. 
 * ****************************************************************************/
@SuppressWarnings("serial")
public class GUITileInfo extends JPanel{
	private JLabel image;
	private InfoLabel location;
	private InfoLabel light;
	private InfoLabel wind;
	private InfoLabel aromas;
	private InfoLabel flowers;
	private InfoLabel turnEntered;
	
	/* *********************************************************************//**
	 * Constructor: an intitialized instance.
	 * ************************************************************************/
	GUITileInfo() {
		initGUI();
	}
	
	/* *********************************************************************//**
	 * Initializes the instance.
	 * ************************************************************************/
	private void initGUI() {
		setLayout(new GridBagLayout());
		setBackground(GUI.BACKGROUND_COLOR);
		setBorder(new TitledBorder("Tile Information") {
			private final int in = 10;
			private Insets borderInsets = new Insets(in, in, in, in);

			public Insets getBorderInsets(Component c) {
				return borderInsets;
		    }
		});
		setToolTipText("Click on tiles for information.");
		
		image = new JLabel();
		location = new InfoLabel("(0, 0)");
		light = new InfoLabel("0");
		wind = new InfoLabel("0/0");
		aromas = new InfoLabel("0/0");
		flowers = new InfoLabel("0");
		turnEntered = new InfoLabel("0");

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4,4,4,4);
		constraints.anchor = GridBagConstraints.LINE_START;
	
		int col = 0;
		int row = 1;
		add(new InfoLabel("Location: "), constraints, col, row++);
		add(new InfoLabel("light: "), constraints, col, row++);
		add(new InfoLabel("Wind: "), constraints, col, row++);
		add(new InfoLabel("Aromas: "), constraints, col, row++);
		add(new InfoLabel("Flowers: "), constraints, col, row++);
		add(new InfoLabel("Turn Entered: "), constraints, col, row++);
		
		row = 1;
		col = 1;
		constraints.anchor = GridBagConstraints.LINE_END;
		add(location, constraints, col, row++);
		add(light, constraints, col, row++);
		add(wind, constraints, col, row++);
		add(aromas, constraints, col, row++);
		add(flowers, constraints, col, row++);
		add(turnEntered, constraints, col, row++);
		
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.CENTER;
		add(image, constraints);
	}
	
	/* *********************************************************************//**
	 * Adds an info label label at (gridx, gridy) of constraints to the JPanel.
	 * ************************************************************************/
	private void add(InfoLabel label, GridBagConstraints constraints, 
			int gridx, int gridy) {
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		add(label, constraints);
	}
	
	/* *********************************************************************//**
	 * Updates the tile info box using tile.
	 * ************************************************************************/
	public void update(Tile tile) {
		location.setText(tile.tileState.location.toString());
		light.setText(String.valueOf(tile.tileState.light));
		wind.setText(tile.tileState.wind.toString());
		aromas.setText(tile.tileState.toStringAromas());
		flowers.setText(tile.tileState.toStringFlowers());
		turnEntered.setText(String.valueOf(tile.turnEntered));
		
		BufferedImage back = Common.load_image(tile.tileFilename);
		if (tile.tileState.flowers.isEmpty()) {
			image.setIcon(new ImageIcon(back));
			return;
		}
		
		Randomer rand = new Randomer();
		String filename = rand.nextElement(tile.tileState.flowers).imageFilename;
		BufferedImage flower = Common.load_image(filename);
		BufferedImage merged = new BufferedImage(back.getWidth(), back.getHeight(), 
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = merged.getGraphics();
		g.drawImage(back, 0, 0, null);
		g.drawImage(flower, 0, 0, null);
		ImageIcon mergedIcon = new ImageIcon(merged);
		image.setIcon(mergedIcon);
	}

	/* *********************************************************************//**
	 * A nicely formatted label to display tile information.
	 * ************************************************************************/
	private class InfoLabel extends JLabel {
		InfoLabel(String text) {
			setText(text);
			setFont(new Font("Monospaced", Font.BOLD, 14));
		}
	}
}
