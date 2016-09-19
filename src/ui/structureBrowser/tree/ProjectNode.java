package ui.structureBrowser.tree;

public class ProjectNode extends ProjectTreeNode {

	// 项目节点的label为项目名+@+项目所在的路径
	public ProjectNode(NodeKind kind, String label, String sizeMetrics) {
		super(kind, label, sizeMetrics);
	}

	public String getProjectDir() {
		return getLocation();
	}
	
	public void setSizeMetrics(String sizeMetrics) {
		this.sizeMetrics = sizeMetrics;
	}
	
	public String getItemString(int index) {
		switch(index) {
		case 0:// 系统
			return getSimpleName();
		case 1:
			return getLocation();
		}
		return null;
	}
}
