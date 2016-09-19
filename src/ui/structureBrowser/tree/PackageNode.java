package ui.structureBrowser.tree;

public class PackageNode extends ProjectTreeNode {

	public PackageNode(NodeKind kind, String label, String sizeMetrics) {
		super(kind, label, sizeMetrics);
	}

	@Override
	public String getItemString(int index) {
		switch(index) {
		case 0:// ��
			return getSimpleName();
		case 1:// �汾
			ProjectTreeNode verNode = (ProjectTreeNode)getParent();
			return verNode.getSimpleName();
		case 2:// ϵͳ
			ProjectTreeNode sysNode = (ProjectTreeNode)getParent().getParent();
			return sysNode.getSimpleName();
		case 3:
			return getLocation();
		}
		return null;
	}

}
