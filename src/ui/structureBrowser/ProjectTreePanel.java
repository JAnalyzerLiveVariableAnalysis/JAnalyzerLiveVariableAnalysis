package ui.structureBrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import ui.mainFrame.StatusBar;
import ui.structureBrowser.tree.ProjectTree;

public class ProjectTreePanel extends JPanel {

	ProjectTree tree;
	ProjectTreeManager manager;

	public ProjectTreePanel(JTabbedPane textPane, StatusBar bar, JScrollPane infoScroll) {
		setBorder(null);
		this.setBackground(Color.white);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		scrollPane.setBackground(Color.white);
		manager = new ProjectTreeManager();
		manager.setTextTabPane(textPane);
		tree = (ProjectTree) manager.getTree();
		tree.setStatusBar(bar);
		tree.setMetricArea(infoScroll);
		tree.load();
		scrollPane.setViewportView(tree);
		
		JPanel searchPane = new JPanel();
		searchPane.setLayout(new BorderLayout());
		searchPane.setBackground(Color.white);
		final JTextField searchField = new JTextField();
		searchField.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					String text = searchField.getText().trim();
					if(!text.equals("")) {
						tree.findAndSelectNodeByNodeName(text);
					}
				}
			}
		});
		
		JButton button = new JButton("ËÑË÷");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = searchField.getText().trim();
				if(!text.equals("")) {
					tree.findAndSelectNodeByNodeName(text);
				}
			}
		});
		
		searchPane.add(searchField, BorderLayout.CENTER);
		searchPane.add(button, BorderLayout.EAST);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(searchPane, BorderLayout.NORTH);
	}

	public ProjectTreeManager getManager() {
		return manager;
	}
	
}
