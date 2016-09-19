package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

public class StatusBar extends JToolBar {
	
	JProgressBar progressBar;
	JLabel infoLabel = new JLabel();
	JLabel row_col_Label = new JLabel();
	
	public StatusBar() {
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(150,20));
		setLayout(new BorderLayout());
		add(row_col_Label, BorderLayout.EAST);
		add(infoLabel, BorderLayout.CENTER);
		add(progressBar, BorderLayout.WEST);
		setFloatable(false);
		progressBar.setVisible(false);
		infoLabel.setVisible(false);
	}
	
	public void setRowAndCol(int row, int col) {
		if(row > 0 && col > 0) {
			row_col_Label.setVisible(true);
			row_col_Label.setText("ÐÐ:" + row + " ÁÐ:" + col + "  ");
		}
	}

	public void hideRowAndCol() {
		row_col_Label.setVisible(false);
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public JLabel getInfoLabel() {
		return infoLabel;
	}
	
}
