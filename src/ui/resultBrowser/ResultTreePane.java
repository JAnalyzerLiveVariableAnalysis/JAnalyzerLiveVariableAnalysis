package ui.resultBrowser;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ui.mainFrame.StatusBar;

public class ResultTreePane extends JScrollPane {

	ResultFileTree tree;
	
	public ResultTreePane(JTabbedPane tabbedPane, StatusBar bar) {
		tree = new ResultFileTree(bar);
		tree.registerTextArea(tabbedPane);
		setViewportView(tree);
	}

}
