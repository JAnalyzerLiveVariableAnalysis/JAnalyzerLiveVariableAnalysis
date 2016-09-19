package ui.mainFrame;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ui.structureBrowser.tree.TitleObserver;

public class ExpandableTitle extends JPanel {

	final Icon collapseIcon = new ImageIcon("res/img/collapse.png");
	final Icon expandIcon = new ImageIcon("res/img/expandBtn.png");
	
	private JButton iconBtn;
	private JLabel label = new JLabel();
	boolean isExpanded = true;
	
	TitleObserver observer;
	
	public ExpandableTitle() {
		setBackground(Color.WHITE);
		iconBtn = new JButton(expandIcon);
		iconBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				handleIconBtn();
			}
		});
		
		
		iconBtn.setBorder(null);
	
		label.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					handleIconBtn();
				}
			}
		});
		
		
		setLayout(null);
		
		add(iconBtn);
		add(label);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		int btnHeight = iconBtn.getIcon().getIconHeight();
		int btnWidth = iconBtn.getIcon().getIconWidth();
		
		iconBtn.setBounds(3, (getHeight()-btnHeight) / 2, btnWidth, btnHeight);
		
		int fontSize = getFont().getSize();
		label.setBounds(iconBtn.getX() + iconBtn.getWidth() + 8, 0, getWidth(), fontSize + 5);
	}
	
	
	private void handleIconBtn() {
		if(!isExpanded) {
			isExpanded = true;
			iconBtn.setIcon(expandIcon);
		} else {
			isExpanded = false;
			iconBtn.setIcon(collapseIcon);
		}
		observer.update(isExpanded);
	}
	
	public void setExpandedObserver(TitleObserver o) {
		observer = o;
	}
	
	public String getText() {
		return label.getText();
	}
	
	public boolean isExpanded() {
		return isExpanded;
	}
	
	public void setText(String title) {
		label.setText(title);
	}
	
//	public static void main(String args[]) {
//		ExpandableTitle title = new ExpandableTitle();
//		JFrame frame = new JFrame();
//		frame.add(title);
//		frame.pack();
//		frame.setVisible(true);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	}
}
