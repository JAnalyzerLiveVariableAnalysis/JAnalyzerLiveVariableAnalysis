package gui.softwareMeasurement.mainFrame;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.Icon;

public class TabbedPaneIcon implements Icon {

	private int x_pos;
	private int y_pos;
	private int width;
	private int height;
	private Image image;

	public TabbedPaneIcon(String filename) {
		width = 10;
		height = 10;
		image = Toolkit.getDefaultToolkit().getImage(filename);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.x_pos = x;
		this.y_pos = y;
		g.drawImage(image, x, y, c);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	public Rectangle getBounds() {
		return new Rectangle(x_pos, y_pos, width, height);
	}

}
