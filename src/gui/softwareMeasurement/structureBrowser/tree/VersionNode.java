package gui.softwareMeasurement.structureBrowser.tree;

public class VersionNode extends ProjectTreeNode {

	public VersionNode(NodeKind kind, String label, String sizeMetrics) {
		super(kind, label, sizeMetrics);
	}

	public String getVersionDir() {
		return getLocation();
	}

	@Override
	public String getItemString(int index) {
		switch(index) {
		case 0:// �汾
			return getSimpleName();
		case 1:// ϵͳ
			ProjectTreeNode sysNode = (ProjectTreeNode)getParent();
			return sysNode.getSimpleName();
		case 2:
			return getLocation();
		}
		return null;
	}
	
	
}
