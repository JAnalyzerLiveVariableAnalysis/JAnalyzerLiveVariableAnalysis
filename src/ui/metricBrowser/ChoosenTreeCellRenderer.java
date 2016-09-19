package ui.metricBrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class ChoosenTreeCellRenderer extends JPanel implements TreeCellRenderer {

	JCheckBox box = new JCheckBox();
	JLabel label = new JLabel();
	boolean isSelceted = false;

	DefaultTreeCellRenderer dftRenderer = new DefaultTreeCellRenderer();
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		box.setSelected(selected);
		isSelceted = selected;
		label.setText(value.toString());
		Color bgColor = dftRenderer.getBackgroundNonSelectionColor();
		if(expanded) {
			selectChildren(tree, value, selected);
		}
		setBackground(bgColor);
		box.setBackground(bgColor);
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(box);
		this.add(label);
		return this;
	}
	
	
	private void selectChildren(JTree tree, Object value, boolean selected) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)value;
		Enumeration<DefaultMutableTreeNode> children = n.children();
		while(children.hasMoreElements()) {
			DefaultMutableTreeNode child = children.nextElement();
			TreePath path = new TreePath(child.getPath());
			if(n.isLeaf()) {
				if(selected) {
					tree.addSelectionPath(path);
				} else {
					tree.removeSelectionPath(path);
				}
			} else {
				if(selected) {
					tree.addSelectionPath(path);
				} else {
					tree.removeSelectionPath(path);
				}
				selectChildren(tree, child, selected);
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		box.setSelected(isSelceted);
	}

	public static void main(String args[]) {
		final JTree t = new JTree();
		DefaultTreeSelectionModel mode = new DefaultTreeSelectionModel();
		mode.setSelectionMode(DefaultTreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		t.setSelectionModel(mode);
		ChoosenTreeCellRenderer renderer = new ChoosenTreeCellRenderer();
		t.setCellRenderer(renderer);
		t.setShowsRootHandles(true);
		
		t.addMouseListener(new MouseAdapter() {
			
			HashSet<TreePath> flagSet = new HashSet<TreePath>();
			public void mousePressed(MouseEvent e) {
				TreePath path = t.getSelectionPath();
				System.out.println(path + " - " + flagSet);
				if(path != null) {
					if(flagSet.contains(path)) {
						t.removeSelectionPath(path);
						flagSet.remove(path);
					} else {
						t.addSelectionPath(path);
						flagSet.add(path);
					}
				}
			}
			
		});
		
		JFrame f = new JFrame();
		f.add(t);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
