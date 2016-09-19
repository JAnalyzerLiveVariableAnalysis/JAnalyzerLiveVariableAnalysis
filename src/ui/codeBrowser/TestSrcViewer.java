package ui.codeBrowser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class TestSrcViewer {
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		JFrame frame = new JFrame();

		/*
		 * 只添加垂直滚动条的话把JTextPane放到JScrollPane里, 会有水平滚动条,但是这样会使得JTextPane自动换行;
		 * 
		 * 添加水平和垂直滚动条需要把JTextPane放到JPanel里，再把JPanel放到JScrollPane里。
		 * JTextPane如果没有水平滚动条的话JTextPane会自动换行，否则就不会。
		 */
		JSrcViewer viewer = new JSrcViewer(
				"D:\\Program Files\\workspace2\\ProgramAnalysisUI_20150925\\src\\softwareStructure\\SoftwareStructManager.java");
		viewer = new JSrcViewer(
				"E:\\文档\\lucene-3.5\\src\\java\\org\\apache\\lucene\\analysis\\standard\\StandardAnalyzer.java");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(viewer);
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setRowHeaderView(new LineNumberHeaderView(viewer.lineCount));
		frame.add(scrollPane);
		frame.setSize(500, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
