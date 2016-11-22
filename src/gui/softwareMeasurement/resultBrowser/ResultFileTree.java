package gui.softwareMeasurement.resultBrowser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import gui.softwareMeasurement.mainFrame.Checker;
import gui.softwareMeasurement.mainFrame.JsMetricFrame;
import gui.softwareMeasurement.mainFrame.StatusBar;

public class ResultFileTree extends JTree {

	private JTabbedPane tabbedPane;
	private StatusBar bar;

	public ResultFileTree(StatusBar bar) {
		this.bar = bar;
		load();
		addMouseListener(new MouseAdapter() {
			
			
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TreePath path = getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						Object o = path.getLastPathComponent();
						if (o instanceof Node) {
							Node node = (Node) o;
							if (node.file.isFile()) {
								showResult(node.file);
							}
						}
					}
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					TreePath path = getPathForLocation(e.getX(), e.getY());
					setSelectionPath(path);
					if(path != null) {
						JPopupMenu popup = new JPopupMenu();
						JMenuItem openResFile = new JMenuItem("打开文件");
						JMenuItem delFile = new JMenuItem("删除");
						Object o = path.getLastPathComponent();
						Node node = null;
						if (o instanceof Node) {
							node = (Node) o;
							if(node.file.isDirectory()) {
								openResFile.setEnabled(false);
							}
						}
						popup.add(openResFile);
						popup.add(delFile);
						
						ResultFileTreePopupMenuAcListener listener = new ResultFileTreePopupMenuAcListener(ResultFileTree.this, node);
						openResFile.addActionListener(listener);
						delFile.addActionListener(listener);
						
						popup.show(ResultFileTree.this, e.getX(), e.getY());
					}
				}
			}
		});
	}

	public void load() {
		final File file = Checker.getResultDir();
		if(!file.isDirectory()) {
			file.mkdir();
		}
		final Node rootNode = new Node(file);
		setModel(new Model(rootNode));
		setCellRenderer(new TreeRenderer());
		new Thread() {
			public void run() {
				if(bar != null) {
					bar.getInfoLabel().setText("载入结果中，请稍候...");
					bar.getInfoLabel().setVisible(true);
				}
				loadResultFiles(file, rootNode);
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						updateUI();
					}
				});
				if(bar != null) {
					bar.getInfoLabel().setVisible(false);
				}
			}
		}.start();
		setShowsRootHandles(true);
	}

	private void loadResultFiles(File file, DefaultMutableTreeNode parent) {
		File[] lists = file.listFiles();
		if (lists != null) {
			for (File f : lists) {
				if (f.isDirectory()) {
					Node node = new Node(f);
					parent.add(node);
					loadResultFiles(f, node);
				}
				if (f.isFile() && f.getName().endsWith(".jsm")) {
					Node n = new Node(f);
					parent.add(n);
				}
			}
		}
	}

	public void showResult(File file) {
		ResultFileParser p = new ResultFileParser(file);
		ResultTable table = new ResultTable(p);
		table.setTextArea(tabbedPane);
		

		JFrame frame = new ResultTableFrame(table);
		frame.setTitle(file.getName());
		frame.setVisible(true);
	}
	

	public void registerTextArea(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

}

class TreeRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof Node) {
			Node node = (Node) value;
			setIcon(FileSystemView.getFileSystemView().getSystemIcon(node.file));
		}
		return this;
	}
}

class Model extends DefaultTreeModel {

	public Model(TreeNode root) {
		super(root);
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof Node) {
			return ((Node) node).file.isFile();
		}
		return false;
	}
}

class Node extends DefaultMutableTreeNode {
	File file;

	public Node(File file) {
		super();
		this.file = file;
	}

	public String toString() {
		return file.getName();
	}
}
