package ui.structureBrowser.tree;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dom4j.Element;

import ui.structureBrowser.ProjectTreeManager;

public class LoadTreeWorker extends SwingWorker<Boolean, Void>{

	JLabel infoLabel;
	JProgressBar progressBar;
	ProjectTree tree;
	List<Element> projectEles;
	int row;
	String selectedNodeName;
	
	// 刷新或开启界面的时候载入项目
	public LoadTreeWorker(ProjectTree tree, List<Element> projectEles) {
		this.tree = tree;
		this.progressBar = tree.getStatusBar().getProgressBar();
		this.infoLabel = tree.getStatusBar().getInfoLabel();
		this.projectEles = projectEles;
		progressBar.setVisible(true);
		infoLabel.setVisible(true);
		
		progressBar.setMinimum(0);
		ProjectTree.canFresh = false;
		
		row = tree.getRowForPath(tree.getSelectionPath());
		if(tree.getSelectionPath() != null) {
			selectedNodeName = tree.getSelectionPath().getLastPathComponent().toString();
		}
		((DefaultMutableTreeNode)tree.getModel().getRoot()).removeAllChildren();
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		progressBar.setValue(0);
		for(int i=0; i < projectEles.size(); i++) {
			Element pro = projectEles.get(i);
			String label = pro.attributeValue("label");
			infoLabel.setText("  载入项目"+getSimpleNameByLabel(label));
			progressBar.setValue(i);
			loadProjectEle(pro);
		}
		return Boolean.TRUE;
	}
	
	protected void done() {
		try {
			if(get()) {// 获取到了true值
				progressBar.setVisible(false);
				infoLabel.setVisible(false);
				tree.updateUI();
				ProjectTree.canFresh = true;
				
				TreePath path = tree.getPathForRow(row);
				if(path != null) {// 刷新之后选中之前的节点
					ProjectTreeNode node = (ProjectTreeNode)path.getLastPathComponent();
					if(selectedNodeName.equals(node.getSimpleName()) && node.NODE_KIND == NodeKind.PROJECT_NODE) {
						tree.setSelectionPath(path);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private String getSimpleNameByLabel(String label) {
		int index = label.indexOf('@');
		if (index < 0) {
			throw new AssertionError(
					"Can not find charater @ at the label of node, label = ["
							+ label + "]");
		}
		return label.substring(0, index);
	}
	
	private void loadProjectEle(Element projectEle) {
//		System.out.println("loadProjectEle");
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
				.getRoot();
		ProjectNode proNode = new ProjectNode(NodeKind.PROJECT_NODE,
				projectEle.attributeValue("label"), projectEle.attributeValue("metrics"));
		File file = new File(proNode.getProjectDir());
		File infoFile = new File(".info/" + proNode.getSimpleName());
		if(infoFile.exists() && file.exists()) {// 项目文件和项目所在文件夹必须存在
			root.add(proNode);
			loadVersionEle(projectEle.elements(), proNode);
		} else {
			if(infoFile.exists() && !file.exists()) {
				ProjectTreeManager.projectIds.remove(proNode.getSimpleName());
				infoFile.delete();
			}
		}
	}

	private void loadVersionEle(List<Element> versionEles,
			ProjectTreeNode parent) {
//		System.out.println("loadVersionEle");
		boolean hasVersion = false;
		for (Element e : versionEles) {
			// package element下的标签有可能是version也有可能是package
			if (e.getName().equals("version")) {// 如果是version标签
				hasVersion = true;
				VersionNode vNode = new VersionNode(NodeKind.VERSION_NODE,
						e.attributeValue("label"), e.attributeValue("metrics"));
				File file = new File(vNode.getVersionDir());
				if(file.exists()) {// 存在这个项目文件才能将其显示到树上
					parent.add(vNode);
					loadPackageEle(e.elements(), vNode);
				}
			}
		}
		// 否则直接标签为package
		if (!hasVersion) {
			loadPackageEle(versionEles, parent);
		}
	}

	private void loadPackageEle(List<Element> packageEles,
			ProjectTreeNode parent) {
		progressBar.setMaximum(packageEles.size());
//		System.out.println("loadPackageEle");
		for (Element e : packageEles) {
			progressBar.setValue(progressBar.getValue()+1);
			PackageNode pNode = new PackageNode(NodeKind.PACKAGE_NODE,
					e.attributeValue("label"), e.attributeValue("metrics"));
			parent.add(pNode);
			loadClassEle(e.elements(), pNode);
		}
	}

	private void loadClassEle(List<Element> classEles, ProjectTreeNode parent) {

//		System.out.println("loadClassEle");
		for (Element e : classEles) {
			ClassNode cNode = new ClassNode(NodeKind.CLASS_NODE,
					e.attributeValue("label"), e.attributeValue("path"), e.attributeValue("metrics"));
			if(cNode.getFile() != null) {
				parent.add(cNode);
				loadFieldAndMethodEle(e.elements(), cNode);
			}
		}
	}

	private void loadFieldAndMethodEle(List<Element> fieldAndMethodEles,
			ProjectTreeNode parent) {

//		System.out.println("loadFieldAndMethodEle");
		for (Element e : fieldAndMethodEles) {
			ProjectTreeNode node = null;
			if (e.getName().equals("field")) {
				node = new FieldNode(NodeKind.FIELD_NODE,
						e.attributeValue("label"), e.attributeValue("type"), e.attributeValue("metrics"));
			} else if (e.getName().equals("method")) {
				List<Element> params = e.elements();
				String[] paramStrs = new String[params.size()];
				int i = 0;
				for (Element param : params) {
					paramStrs[i++] = param.attributeValue("type");
				}
				node = new MethodNode(NodeKind.METHOD_NODE,
						e.attributeValue("label"), paramStrs, e.attributeValue("metrics"));
			}
			if (node != null)
				parent.add(node);
		}
	}

}
