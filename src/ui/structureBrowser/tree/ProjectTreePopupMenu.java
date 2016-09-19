package ui.structureBrowser.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import softwareMeasurement.ClassMeasurement;
import softwareMeasurement.MethodMeasurement;
import softwareMeasurement.PackageMeasurement;
import softwareMeasurement.SystemScopeMeasurement;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareStructure.SoftwareStructManager;
import ui.mainFrame.Checker;
import ui.mainFrame.ConfirmDialog;
import ui.mainFrame.Log;
import ui.metricBrowser.MetricDescriptionDlg;
import ui.structureBrowser.CloseVersionDialog;
import ui.structureBrowser.LoadVersionDialog;
import ui.structureBrowser.ProjectBuildDialog;
import ui.structureBrowser.ProjectTreeManager;
import util.SourceCodeLocation;
import util.SourceCodeParser;

public class ProjectTreePopupMenu extends JPopupMenu {

	public final static String MENU_NEW_PROJECT_STR = "新建项目";
	public final static String MENU_DEL_PROJECT_STR = "删除项目";
	public final static String MENU_SYNTAX_TREE_STR = "抽象语法树";
	public final static String MENU_LOAD_VERSION_STR = "装载版本...";
	public final static String MENU_CLOSE_VERSION_STR = "关闭版本...";
	public final static String MENU_FRESH_STR = "刷新";

	private ProjectTreeManager manager;
	JMenuItem mntmDel;
	JMenuItem mntmFresh;
	
	public ProjectTreePopupMenu(ProjectTreeManager manager) {
		super();
		this.manager = manager;
		init();
	}

	public ProjectTreePopupMenu(String label, ProjectTreeManager manager) {
		super(label);
		this.manager = manager;
		init();
	}

	public void init() {
		JMenuItem mntmNew = new JMenuItem(MENU_NEW_PROJECT_STR);
		mntmDel = new JMenuItem(MENU_DEL_PROJECT_STR);
		mntmDel.setEnabled(false);
		if(manager.getSelectedNode() != null && 
				manager.getSelectedNode().getNodeKind() == NodeKind.PROJECT_NODE) {
			mntmDel.setEnabled(true);
		}
		JMenuItem mntmLoad = new JMenuItem(MENU_LOAD_VERSION_STR);
		if(ProjectTree.isBuilding) {
			mntmNew.setEnabled(false);
			mntmLoad.setEnabled(false);
		} else {
			mntmNew.setEnabled(true);
			mntmLoad.setEnabled(true);
		}
		
		
		JMenuItem mntmClose = new JMenuItem(MENU_CLOSE_VERSION_STR);
		mntmFresh = new JMenuItem(MENU_FRESH_STR);
		mntmFresh.setAccelerator(KeyStroke.getKeyStroke("F5"));

		
		this.add(mntmNew);
		this.add(mntmDel);
		this.addSeparator();
		this.add(mntmLoad);
		this.add(mntmClose);
		
		Handler handler = new Handler();
		mntmNew.addActionListener(handler);
		mntmDel.addActionListener(handler);
		mntmFresh.addActionListener(handler);
		mntmLoad.addActionListener(handler);
		mntmClose.addActionListener(handler);

		ProjectTreeNode selectedNode = manager.getSelectedNode();
		if(selectedNode != null) {
			JMenuItem mntmCalculate = new JMenuItem("计算"+ selectedNode.getSimpleName()+
				"的度量");
			this.addSeparator();
			this.add(mntmCalculate);
			mntmCalculate.addActionListener(handler);
		}
		
		if(selectedNode != null 
				&& selectedNode.getNodeKind()==NodeKind.CLASS_NODE) {
			JMenuItem mntmSyntaxTree = new JMenuItem(selectedNode.getSimpleName() + "的" + MENU_SYNTAX_TREE_STR);
			mntmSyntaxTree.addActionListener(handler);
			this.add(mntmSyntaxTree);
		}
		
		this.add(mntmFresh);
		
		
		if (manager.getSelectedNode() != null
				&& manager.getSelectedNode().NODE_KIND == NodeKind.PROJECT_NODE) {
			mntmDel.setEnabled(true);
		} else {
			mntmDel.setEnabled(false);
		}
		if(manager.projectIds == null || manager.projectIds.isEmpty()) {
			mntmLoad.setEnabled(false);
			mntmClose.setEnabled(false);
		}
	}

	class Handler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals(ProjectTreePopupMenu.MENU_NEW_PROJECT_STR)) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						ProjectBuildDialog dialog = new ProjectBuildDialog(manager);
						dialog.setVisible(true);
					}
				});
			}
			if (command.equals(ProjectTreePopupMenu.MENU_DEL_PROJECT_STR)) {
				if(Checker.isDeleteDlgAlert()) {
					int res = ConfirmDialog.showDlg("提示", "删除项目不会删除磁盘上的文件，是否删除项目\"" +
							manager.getSelectedNode().getSimpleName() + "\"？");
					if(res == 1) {
						new Thread() {
							public void run() {
								manager.removeSelectedProject();
								mntmDel.setEnabled(false);
							}
						}.start();
					}
				} else {
					new Thread() {
						public void run() {
							manager.removeSelectedProject();
							mntmDel.setEnabled(false);
						}
					}.start();
				}
			}
			if (command.equals(ProjectTreePopupMenu.MENU_LOAD_VERSION_STR)) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						LoadVersionDialog dlg = new LoadVersionDialog(manager);
						dlg.setVisible(true);
					}
				});
			}
			if (command.equals(ProjectTreePopupMenu.MENU_CLOSE_VERSION_STR)) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						CloseVersionDialog dlg = new CloseVersionDialog(manager);
						dlg.setVisible(true);
					}
				});
			}
			if (command.equals(ProjectTreePopupMenu.MENU_FRESH_STR)) {
				if(ProjectTree.canFresh) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							manager.updateTree();
						}
					});
				}
			}
			if (command.contains("计算")) {
				Thread t = new Thread() {
					public void run() {
						MetricDescriptionDlg dialog = new MetricDescriptionDlg(null);
						dialog.setTitle("请选择要计算的度量");
						dialog.setVisible(true);
						
						String metrics = dialog.getSelectedMetrics();
						if(metrics != null) {
							calculate(metrics.split(";"));
						} else {
							JOptionPane.showMessageDialog(null, "没有选择任何数据！");
						}
					}
				};
				t.start();
			}
			if(command.endsWith(MENU_SYNTAX_TREE_STR)) {
				ClassNode selectedNdoe = (ClassNode)manager.getSelectedNode();
				File file = selectedNdoe.getFile();
				manager.notifyTabbedPane(file);
			}
		}
	}
	
	private void calculate(String[] metrics) {
		List<SoftwareMeasure> measures = new ArrayList<SoftwareMeasure>();
		for(String metric : metrics) {
			measures.add(new SoftwareMeasure(metric));
		}
		
		ProjectTreeNode n = manager.getSelectedNode();
		SourceCodeParser parser = null;
		NameTableCreator creator = null;
		NameTableManager nameTableManager = null;
		SoftwareStructManager structManager = null;
		switch(n.getNodeKind()) {
		case PROJECT_NODE:// 选择系统的话，下面的所有版本分别计算
			Log.consoleLog("System calculating...");
			Enumeration<ProjectTreeNode> versionNodes = n.children();
			while(versionNodes.hasMoreElements()) {
				ProjectTreeNode version = versionNodes.nextElement();
				Log.consoleLog("calculating " + n.getSimpleName());
				parser = new SourceCodeParser(version.getLocation());
				creator = new NameDefinitionCreator(parser);
				nameTableManager = creator.createNameTableManager();
				structManager = new SoftwareStructManager(nameTableManager);
				SystemScopeMeasurement sysMeasurement = new SystemScopeMeasurement(nameTableManager.getRootScope(),structManager);
				List<SoftwareMeasure> resultMeasures = sysMeasurement.getMeasureList(measures);
				String dlgMsg = "     ";
				for(SoftwareMeasure m : resultMeasures) {
					dlgMsg += m.getIdentifier() + " : " + m.getValue() + "\r\n     ";
				}
				JOptionPane.showMessageDialog(null, dlgMsg);
				Log.consoleLog(""+resultMeasures);
				
			}
			break;
		case VERSION_NODE:// 选择版本，则直接计算
			Log.consoleLog("Version calculating...");
			parser = new SourceCodeParser(n.getLocation());
			creator = new NameDefinitionCreator(parser);
			nameTableManager = creator.createNameTableManager();
			structManager = new SoftwareStructManager(nameTableManager);
			SystemScopeMeasurement sysMeasurement = new SystemScopeMeasurement(nameTableManager.getRootScope(),structManager);
			List<SoftwareMeasure> resultMeasures = sysMeasurement.getMeasureList(measures);
			String dlgMsg = "     ";
			for(SoftwareMeasure m : resultMeasures) {
				dlgMsg += m.getIdentifier() + " : " + m.getValue() + "\r\n     ";
			}
			JOptionPane.showMessageDialog(null, dlgMsg);
			Log.consoleLog(""+resultMeasures);
			break;
		case PACKAGE_NODE:// 选择包
			Log.consoleLog("Package calculating...");
			// 以包的父节点(版本)来建立名字表，并获取PackageDefinition
			VersionNode versionNode = (VersionNode)n.getParent();
			parser = new SourceCodeParser(versionNode.getLocation());
			creator = new NameDefinitionCreator(parser);
			nameTableManager = creator.createNameTableManager();
			structManager = new SoftwareStructManager(nameTableManager);
			Log.consoleLog("calculating " + n.getSimpleName());
			PackageDefinition pack = nameTableManager.findPackageByName(n.getSimpleName());
			PackageMeasurement packMeasure = new PackageMeasurement(pack, structManager);
			List<SoftwareMeasure> packResMeasures = packMeasure.getMeasureList(measures);
			String packMsg = "     ";
			for(SoftwareMeasure m : packResMeasures) {
				packMsg += m.getIdentifier() + " : " + m.getValue() + "\r\n     ";
			}
			JOptionPane.showMessageDialog(null, packMsg);
			Log.consoleLog(""+packResMeasures);
			break;
		case CLASS_NODE:
			Log.consoleLog("Class calculating...");
			// 用版本来建立名字表，并获取DetailedTypeDefinition
			VersionNode vNode = (VersionNode)(n.getParent().getParent());
			parser = new SourceCodeParser(vNode.getLocation());
			creator = new NameDefinitionCreator(parser);
			nameTableManager = creator.createNameTableManager();
			structManager = new SoftwareStructManager(nameTableManager);
			Log.consoleLog("calculating " + n.getSimpleName());
			DetailedTypeDefinition type = nameTableManager.findDetailedTypeDefinitionByLocation(n.getSimpleName(), 
					SourceCodeLocation.getLocation(n.getLocation()));
			ClassMeasurement classMeasure = new ClassMeasurement(type, structManager);
			List<SoftwareMeasure> classResMeasures = classMeasure.getMeasureList(measures);
			String classMsg = "     ";
			for(SoftwareMeasure m : classResMeasures) {
				classMsg += m.getIdentifier() + " : " + m.getValue() + "\r\n     ";
			}
			JOptionPane.showMessageDialog(null, classMsg);
			Log.consoleLog(""+classResMeasures);
			break;
		case METHOD_NODE:
			Log.consoleLog("Method calculating...");
			// 用版本来建立名字表，并查找MethodDefinition
			VersionNode verNode = (VersionNode)(n.getParent().getParent().getParent());
			parser = new SourceCodeParser(verNode.getLocation());
			creator = new NameDefinitionCreator(parser);
			nameTableManager = creator.createNameTableManager();
			structManager = new SoftwareStructManager(nameTableManager);
			Log.consoleLog("calculating " + n.getSimpleName());
			DetailedTypeDefinition typeOfMethod = nameTableManager.findDetailedTypeDefinitionByLocation(n.getSimpleName(), 
					SourceCodeLocation.getLocation(n.getLocation()));
			List<MethodDefinition> methods = typeOfMethod.getMethodList();
			for(MethodDefinition m : methods) {
				if(m.getLocation().equals(n.getLocation())) {
					MethodMeasurement methodMeasure = new MethodMeasurement(m, structManager);
					List<SoftwareMeasure> methodResMeasures = methodMeasure.getMeasureList(measures);
					String methodMsg = "     ";
					for(SoftwareMeasure measure : methodResMeasures) {
						methodMsg += measure.getIdentifier() + " : " + measure.getValue() + "\r\n     ";
					}
					JOptionPane.showMessageDialog(null, methodMsg);
					Log.consoleLog(""+methodResMeasures);
					break;
				}
			}
			break;
		}
	}
}