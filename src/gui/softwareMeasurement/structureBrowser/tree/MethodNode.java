package gui.softwareMeasurement.structureBrowser.tree;

public class MethodNode extends ProjectTreeNode {

	String[] params;

	public MethodNode(NodeKind kind, String label, String[] params, String sizeMetrics) {
		super(kind, label, sizeMetrics);
		this.params = params;
	}

	public String toString() {
		String res = getSimpleName() + "(";
		if (params != null && params.length > 0) {
			res += params[0];
			for (int i = 1; i < params.length; i++) {
				res += (", " + params[i]);
			}
			res += ")";
			return res;
		} else {
			return res + ")";
		}

	}
	
	
	@Override
	public String getItemString(int index) {
		switch(index) {
		case 0:
			return getSimpleName();
		case 1:// 类
			ProjectTreeNode classNode = (ProjectTreeNode)getParent();
			return classNode.getSimpleName();
		case 2:// 包
			ProjectTreeNode packNode = (ProjectTreeNode)getParent().getParent();
			return packNode.getSimpleName();
		case 3:// 版本
			ProjectTreeNode verNode = (ProjectTreeNode)getParent().getParent().getParent();
			return verNode.getSimpleName();
		case 4:// 系统
			ProjectTreeNode sysNode = (ProjectTreeNode)getParent().getParent().getParent().getParent();
			return sysNode.getSimpleName();
		}
		return null;
	}
}
