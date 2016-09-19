package ui.structureBrowser.tree;

import java.io.File;

public class ClassNode extends ProjectTreeNode {

	String filePath;

	public ClassNode(NodeKind kind, String label, String filePath, String sizeMetrics) {
		super(kind, label, sizeMetrics);
		this.filePath = filePath;
	}

	/**
	 * 获取类节点对应的文件，如没有对应的文件则返回空值
	 * @return
	 */
	public File getFile() {
		File file = new File(filePath);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public String getFilePath() {
		return filePath;
	}

	@Override
	public String getItemString(int index) {
		switch(index) {
		case 0:// 类
			return getSimpleName();
		case 1:// 包
			ProjectTreeNode packNode = (ProjectTreeNode)getParent();
			return packNode.getSimpleName();
		case 2:// 版本
			ProjectTreeNode verNode = (ProjectTreeNode)getParent().getParent();
			return verNode.getSimpleName();
		case 3:// 系统
			ProjectTreeNode sysNode = (ProjectTreeNode)getParent().getParent().getParent();
			return sysNode.getSimpleName();
		case 4:// 路径
			return filePath;
		}
		return null;
	}
}
