package ui.resultBrowser;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class TestResultBrowser {

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ResultFileTree t = new ResultFileTree(null);
		JScrollPane scrollPane = new JScrollPane(t);

		JFrame frame = new JFrame();
		frame.add(scrollPane);
		frame.setSize(300, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
