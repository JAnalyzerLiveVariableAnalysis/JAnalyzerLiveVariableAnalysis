package gui.softwareMeasurement.structureBrowser.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

	final static ImageIcon proIcon = new ImageIcon("res/img/project_icon.jpg");
	final static ImageIcon verIcon = new ImageIcon("res/img/version_icon.png");
	final static ImageIcon packIcon = new ImageIcon("res/img/package_icon.png");
	final static ImageIcon classIcon = new ImageIcon("res/img/class_icon.png");
	final static ImageIcon fieldIcon = new ImageIcon("res/img/field_icon.png");
	final static ImageIcon methodIcon = new ImageIcon("res/img/method_icon.png");

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getParent() == null) {// 根节点不展示
			setIcon(null);
		} else {
			if (node instanceof ProjectTreeNode) {
				ProjectTreeNode pNode = (ProjectTreeNode) node;
				if (pNode.NODE_KIND == NodeKind.PROJECT_NODE) {
					setIcon(proIcon);
				}
				if (pNode.NODE_KIND == NodeKind.VERSION_NODE) {
					setIcon(verIcon);
				}
				if (pNode.NODE_KIND == NodeKind.PACKAGE_NODE) {
					setIcon(packIcon);
				}
				if (pNode.NODE_KIND == NodeKind.CLASS_NODE) {
					setIcon(classIcon);
				}
				if (pNode.NODE_KIND == NodeKind.FIELD_NODE) {
					setIcon(fieldIcon);
				}
				if (pNode.NODE_KIND == NodeKind.METHOD_NODE) {
					setIcon(methodIcon);
				}
			}
		}
		return this;
	}
}
