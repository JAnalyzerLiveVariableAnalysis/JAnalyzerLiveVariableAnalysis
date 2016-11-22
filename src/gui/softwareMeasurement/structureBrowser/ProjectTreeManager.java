package gui.softwareMeasurement.structureBrowser;

import gui.astViewer.SimpleASTViewer;
import gui.softwareMeasurement.structureBrowser.tree.NodeKind;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTree;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTreeCellRenderer;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTreeModel;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTreeNode;
import gui.toolkit.MainFrame;

import java.awt.CardLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class ProjectTreeManager {

	private ProjectTree tree;
	public static List<String> projectIds = new ArrayList<String>();
	private JTabbedPane pane;
	static int progress = 0;

	public ProjectTreeManager() {
		init();
		tree.updateUI();
	}

	/**
	 * 进入工具之前使用ProjectTree的load方法加载之前的项目信息，如果新建了项目，则在之后重新修改项目树的信息
	 */
	private void init() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		tree = new ProjectTree(this);
		tree.setModel(new ProjectTreeModel(root));
		tree.setCellRenderer(new ProjectTreeCellRenderer());
	}


	public JTree getTree() {
		return tree;
	}

	public void setTextTabPane(JTabbedPane tabPane) {
		this.pane = tabPane;
	}

	public JTabbedPane getTextTabPane() {
		return pane;
	}

	public ProjectTreeNode getSelectedNode() {
		TreePath selectedPath = tree.getSelectionPath();
		if (selectedPath != null) {
			return (ProjectTreeNode) selectedPath.getLastPathComponent();
		}
		return null;
	}

	public void removeSelectedProject() {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				ProjectTreeNode node = getSelectedNode();
				TreePath selectedPath = tree.getSelectionPath();
				if (node.getNodeKind() == NodeKind.PROJECT_NODE) {
					tree.deleteNode(node);
					tree.removeSelectionPath(selectedPath);
					projectIds.remove(node.getSimpleName());
				}
//				// 没有项目存在则删除信息展示面板的度量表格的所有内容
//				if(ProjectTreeManager.projectIds.size() == 0) {
//					DefaultTableModel model = (DefaultTableModel)tree.getMetricTable().getModel();
//					model.getDataVector().clear();
//					model.fireTableDataChanged();
//					tree.getMetricArea().removeAll();
//					tree.getMetricArea().repaint();
//				}
				tree.updateUI();
			}
		});
	}

	public void updateTree() {
		tree.load();
	}
	
	public void saveAllProject() {
		for(String projectId: projectIds) {
			tree.save(projectId);
		}
	}
	
	public void saveProject(String proName) {
		tree.save(proName);
	}
	
	public List getAllProjectNodes() {
		List<ProjectTreeNode> res = new ArrayList<ProjectTreeNode>();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Enumeration<ProjectTreeNode> children = root.children();
		while(children.hasMoreElements()) {
			ProjectTreeNode node = (ProjectTreeNode)children.nextElement();
			res.add(node);
		}
		return res;
	}
	
	public List getAllVersionNodes() {
		List<ProjectTreeNode> sysNodes = getAllProjectNodes();
		List<ProjectTreeNode> res = new ArrayList<ProjectTreeNode>();
		for(ProjectTreeNode sysNode : sysNodes) {
			// 系统节点的子节点为版本节点
			Enumeration<ProjectTreeNode> children = sysNode.children();
			while(children.hasMoreElements()) {
				ProjectTreeNode node = (ProjectTreeNode)children.nextElement();
				res.add(node);
			}
		}
		return res;
	}
	
	public List getAllPackageNodes() {
		List<ProjectTreeNode> verNodes = getAllVersionNodes();
		List<ProjectTreeNode> res = new ArrayList<ProjectTreeNode>();
		for(ProjectTreeNode verNode : verNodes) {
			// 版本节点的子节点
			Enumeration<ProjectTreeNode> children = verNode.children();
			while(children.hasMoreElements()) {
				ProjectTreeNode node = (ProjectTreeNode)children.nextElement();
				res.add(node);
			}
		}
		return res;
	}
	
	public List getAllClassNodes() {
		List<ProjectTreeNode> packNodes = getAllPackageNodes();
		List<ProjectTreeNode> res = new ArrayList<ProjectTreeNode>();
		for(ProjectTreeNode packNode : packNodes) {
			// 包节点的子节点
			Enumeration<ProjectTreeNode> children = packNode.children();
			while(children.hasMoreElements()) {
				ProjectTreeNode node = (ProjectTreeNode)children.nextElement();
				res.add(node);
			}
		}
		return res;
	}
	
	/**
	 * 根据名字查找项目节点，如果没有该名字对应的项目，则返回null
	 * @param name
	 * @return
	 */
	public ProjectTreeNode getProjectNodeByProjectName(String name) {
		if(name == null) return null;
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Enumeration<ProjectTreeNode> children = root.children();
		while(children.hasMoreElements()) {
			ProjectTreeNode node = (ProjectTreeNode)children.nextElement();
			if(name.equals(node.getSimpleName())) {
				return node;
			}
		}
		return null;
	}

	public void fireMetricTable(JTable mTable) {
		if(ProjectTreeManager.projectIds.size() == 0) {
			System.out.println("null");
			DefaultTableModel model = (DefaultTableModel)mTable.getModel();
			model.getDataVector().clear();
			model.fireTableDataChanged();
			mTable.updateUI();
		}
	}

	public void notifyTabbedPane(File file) {
		try {
			String fileContents;
			FileInputStream fileIn = new FileInputStream(file);
			ProgressMonitorInputStream progressIn = new ProgressMonitorInputStream(null, "正在读取文件 [" + file.getName() + "]", fileIn);
			
			final Scanner in = new Scanner(progressIn);
			StringBuffer buffer = new StringBuffer(); 
			StringBuffer bufferWithLine = new StringBuffer();
			int lineCounter = 0;
			while (in.hasNextLine()) {
				lineCounter++;
				String line = in.nextLine();
				buffer.append(line + "\n");
				bufferWithLine.append(lineCounter + " " + line + "\n");
			}
			fileContents = buffer.toString();
			SimpleASTViewer viewer = new SimpleASTViewer(null, fileContents);
			viewer.parseSourceCode();
			String errorMessage = viewer.getParseErrorMessage();
			if (errorMessage != null) {
				JOptionPane.showMessageDialog(MainFrame.getMainFrame(), 
						"编译出现错误：\n" + errorMessage, "警示", JOptionPane.WARNING_MESSAGE);	
			} 
			
			JPanel panel = new JPanel();
			panel.setLayout(new CardLayout());
			panel.setBackground(Color.WHITE);
			JTextArea area = new JTextArea();
			area.setEditable(false);
			panel.add(area);
			JScrollPane scrollPane = new JScrollPane(panel);
			JScrollBar bar = scrollPane.getVerticalScrollBar();
			if(bar != null) {
				bar.setUnitIncrement(50);
			}
			pane.addTab(file.getName() + "语法树", scrollPane);
			pane.setSelectedComponent(scrollPane);
			area.setText(viewer.getASTViewerText());
			area.setCaretPosition(0);
			in.close();
		} catch (IOException exc) {
		}
	}
	
}
