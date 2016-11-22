package gui.softwareMeasurement.structureBrowser.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import gui.softwareMeasurement.metricBrowser.Item;

public abstract class ProjectTreeNode extends DefaultMutableTreeNode implements Item{
	private static final long serialVersionUID = 8103840236190149638L;

	NodeKind NODE_KIND;
	String label;
	String sizeMetrics;

	public ProjectTreeNode(NodeKind kind, String label, String sizeMetrics) {
		super();
		NODE_KIND = kind;
		this.label = label;
		this.sizeMetrics = sizeMetrics;
	}

	public ProjectTreeNode(Object userObject, NodeKind kind, String label) {
		super(userObject);
		NODE_KIND = kind;
		this.label = label;
	}

	public NodeKind getNodeKind() {
		return NODE_KIND;
	}

	public String getLabel() {
		return label;
	}
	
	public String getSimpleName() {
		int index = label.indexOf('@');
		if (index < 0) {
			throw new AssertionError(
					"Can not find charater @ at the label of node, label = ["
							+ label + "]");
		}
		return label.substring(0, index);
	}

	public String getLocation() {
		int index = label.indexOf('@');
		if (index < 0) {
			throw new AssertionError(
					"Can not find charater @ at the label of node, label = ["
							+ label + "]");
		}
		return label.substring(index + 1, label.length());
	}

	public String[] getSizeMetricIdentifier() {
		String[] metrics = sizeMetrics.split("@");
		String[] identifier = new String[metrics.length];
		for(int i = 0; i < metrics.length; i++){
			// metrics[i] = "LOC=0.0"
			// split("=")之后取第一个，就是度量的id
			identifier[i] = metrics[i].split("=")[0];
		}
		return identifier;
	}
	
	public String[] getSizeMetricValue() {
		String[] metrics = sizeMetrics.split("@");
		String[] value = new String[metrics.length];
		for(int i = 0; i < metrics.length; i++){
			// metrics[i] = "LOC=0.0"
			// split("=")之后取第一个，就是度量的值
			value[i] = metrics[i].split("=")[1];
		}
		return value;
	}
	
	public String toString() {
		return getSimpleName();
	}
	
	public String getQualifiedName() {
		if(this instanceof ProjectNode) {
			return getSimpleName();
		} else {
			TreeNode parent = getParent();
			if(parent instanceof ProjectTreeNode) {
				ProjectTreeNode node = (ProjectTreeNode)getParent();;
				return node.getQualifiedName() + "\\" + this.getSimpleName();
			} else {
				return "";
			}
		}
	}

	public List<ProjectTreeNode> getChildNodes() {
		List<ProjectTreeNode> res = new ArrayList<ProjectTreeNode>();
		Enumeration<ProjectTreeNode> childNodes = children();
		ProjectTreeNode node;
		while(childNodes.hasMoreElements()) {
			node = childNodes.nextElement();
			res.add(node);
		}
		return res;
	}
	
	@Override
	public abstract String getItemString(int index);
	
//	public void finalize() {
//		try {
//			super.finalize();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		System.out.println("ProjectTreeNode released!!");
//	}
}
