package ui.structureBrowser.tree;

public class ProjectNode extends ProjectTreeNode {

	// ��Ŀ�ڵ��labelΪ��Ŀ��+@+��Ŀ���ڵ�·��
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
		case 0:// ϵͳ
			return getSimpleName();
		case 1:
			return getLocation();
		}
		return null;
	}
}
