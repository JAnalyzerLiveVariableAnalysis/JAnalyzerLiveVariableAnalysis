package gui.softwareMeasurement.structureBrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

import gui.softwareMeasurement.structureBrowser.tree.ClassNode;
import gui.softwareMeasurement.structureBrowser.tree.FieldNode;
import gui.softwareMeasurement.structureBrowser.tree.MethodNode;
import gui.softwareMeasurement.structureBrowser.tree.NodeKind;
import gui.softwareMeasurement.structureBrowser.tree.PackageNode;
import gui.softwareMeasurement.structureBrowser.tree.ProjectNode;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTree;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTreeNode;
import gui.softwareMeasurement.structureBrowser.tree.VersionNode;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.SystemScope;
import softwareMeasurement.ClassMeasurement;
import softwareMeasurement.MethodMeasurement;
import softwareMeasurement.PackageMeasurement;
import softwareMeasurement.SystemScopeMeasurement;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;

public class BuildWorker extends SwingWorker<Boolean, Void> implements ButtonActionListener {

	ProjectTreeManager manager;
	SoftwareStructManager structManager;
	List<File> versions;
	ProjectInformation project;
	int currentNum = 0;
	int packProgress = 0;
	ProgressDialog dlg;
	boolean cancel = false;
	
	/* 用来新建项目或者加载版本的时候重新载入项目 */
	// 传过来的versions不可能为空，且当中必然会有至少一个版本文件夹
	public BuildWorker(ProjectTreeManager manager, List<File> versions, ProjectInformation proInfo) {
		this.manager = manager;
		this.versions = versions;
		this.project = proInfo;
		
		ProjectTree.canFresh = false;
		ProjectTree.isBuilding = true;
		dlg = new ProgressDialog(this);
	}
	
	
	@Override
	protected Boolean doInBackground() throws Exception {
//		progressBar.setValue(0);
		dlg.setVerMinimum(0);
		dlg.setVerMaximum(versions.size());
		dlg.setPackMinimum(0);
		dlg.setVisible(true);
		loadProject(project, versions);
		return Boolean.TRUE;
	}
	
	
	protected void done() {
		try {
			if(get()) {
//				progressBar.setVisible(false);
//				infoLabel.setVisible(false);
				manager.getTree().updateUI();
				ProjectTree.canFresh = true;
				ProjectTree.isBuilding = false;
				dlg.setVisible(false);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 用来建立项目时候加载版本或者在装载版本的时候加载新的版本，versions中是建立的项目中包含的版本
	 * 在加载版本时，如果版本所在的项目已经存在，则直接加载到那个项目下
	 * @param proInfo
	 * @param versions
	 */
	public void loadProject(ProjectInformation proInfo, List<File> versions) {
		// 如果项目已经存在，则将版本加载到该项目之下
		ProjectTreeNode n = manager.getProjectNodeByProjectName(proInfo.getProjectName());
		if( n != null) {
			dlg.setTitle("载入中，请稍候...");
			loadVersions(versions, n);
		} else {// 项目不存在，新建项目并加载版本
			dlg.setTitle("创建项目" + proInfo.getProjectName() +"，请稍候...");
			// 初始的Metrics中的内容都为N.A
			n = new ProjectNode(NodeKind.PROJECT_NODE,
					proInfo.getProjectName() + "@" + proInfo.getLinkedDirPath(), 
					getMetricsString(getAvailableSoftwareSizeMeasureList()));
			
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) manager.getTree().getModel()
					.getRoot();
			root.add(n);
			loadVersions(versions, n);
			if(!cancel) {
				manager.projectIds.add(proInfo.getProjectName());
			} else {// 取消的时候，将节点移除
				n.removeFromParent();
				System.gc();
			}
		}
		if(!cancel) {
			((ProjectTree) manager.getTree()).save(proInfo.getProjectName());// 加载完之后将树的信息保存到文件中
		}
	}
	
	
	private void loadVersions(List<File> versions, ProjectTreeNode parentNode) {
		if (versions != null && !versions.isEmpty()) {
			List<ProjectTreeNode> addedNodes = new ArrayList<ProjectTreeNode>();
			int max = dlg.getVerMaximum();
			dlg.setVerProgressString("0/" + max);
			for (File f : versions) {
				if(!cancel) {// 记录下来新加入的版本，以便用户取消加载时移除这些节点
					ProjectTreeNode vNode = loadVersion(f, parentNode);
					addedNodes.add(vNode);
					dlg.setVerProgress(++ currentNum);
					dlg.setVerProgressString(currentNum + "/" + max);
				}
			}
			if(cancel) {
				for(ProjectTreeNode n : addedNodes) {
					n.removeFromParent();
				}
				System.gc();
			}
		}
	}
	
	
	private ProjectTreeNode loadVersion(File versionDir, ProjectTreeNode parent) {
		dlg.setVerNote("载入版本" + versionDir.getName());
		dlg.setIndeterminate(true);
		dlg.setPackProgressString("");
		dlg.setPackNote("扫描文件中，请稍候...");
		SourceCodeFileSet parser = new SourceCodeFileSet(versionDir.getAbsolutePath());
		NameTableCreator creator = new NameDefinitionCreator(parser);
		NameTableManager nameTableManager = creator.createNameTableManager();
		
		// 整个版本的规模度量计算
		structManager = new SoftwareStructManager(nameTableManager);
		SystemScope rootScope = nameTableManager.getSystemScope();
		SystemScopeMeasurement sysMeasurement = new SystemScopeMeasurement(rootScope, structManager);
		
		List<SoftwareMeasure> sizes = getAvailableSoftwareSizeMeasureList();
		
		List<SoftwareMeasure> sizeMeasurements = sysMeasurement.getMeasureList(sizes);
		String metrics = getMetricsString(sizeMeasurements);
		
		dlg.setIndeterminate(false);
		// 获取类文件的数量以供进度条使用
		int progress = nameTableManager.getTotalNumberOfDefinitions(NameDefinitionKind.NDK_TYPE);
		dlg.setPackMaximum(progress);
		
		
		String label = versionDir.getName() + "@"
				+ versionDir.getAbsolutePath();
		VersionNode vNode = new VersionNode(NodeKind.VERSION_NODE, label, metrics);
		parent.add(vNode);
		modifyProjectMetricString(parent, vNode);
		
		
		List<PackageDefinition> packs = nameTableManager.getAllPackageDefinitions();
		loadPackages(packs, vNode);
		
		// 版本加载完毕之后清零
		packProgress = 0;
		dlg.setPackProgress(0);
		return vNode;
	}

	private void loadPackages(List<PackageDefinition> packs,
			ProjectTreeNode parent) {
		if (packs != null) {
			ProjectTreeNode packNode;
			for (PackageDefinition pack : packs) {
				if(!cancel) {
	//				infoLabel.setText("  载入包"+pack.getSimpleName());
					// 计算包的规模度量值
					dlg.setPackNote("载入包"+pack.getSimpleName());
					PackageMeasurement packageMeasure = new PackageMeasurement(pack, structManager);
					List<SoftwareMeasure> sizeMeasures = packageMeasure.getMeasureList(getAvailableSoftwareSizeMeasureList());
					String metrics = getMetricsString(sizeMeasures);
					
					packNode = new PackageNode(NodeKind.PACKAGE_NODE,
							pack.getSimpleName() + "@", metrics);
					parent.add(packNode);
					List<DetailedTypeDefinition> types = pack.getAllDetailedTypeDefinitions();
					loadClasses(types, packNode);
				}
			}
		}
	}


	private void loadClasses(List<DetailedTypeDefinition> types, ProjectTreeNode parent) {
		if (types != null) {
			String classFilePath = null;
			ProjectTreeNode classNode = null;
			List<FieldDefinition> fields = null;
			List<MethodDefinition> methods = null;
			int max = dlg.getPackMaximum();
			for (DetailedTypeDefinition type : types) {
				if(!cancel) { 
	//				infoLabel.setText("  载入类"+type.getSimpleName());
	//				progressBar.setValue(progressBar.getValue()+1);
					dlg.setPackNote("载入类"+type.getSimpleName());
					dlg.setPackProgressString(packProgress + "/" + max);
					dlg.setPackProgress(++ packProgress);
					NameTableManager nameTableManager = structManager.getNameTableManager();
					classFilePath = nameTableManager.getCorrespondingFilePath(type);
					if (new File(classFilePath).exists()) {
						//计算类的规模度量值
						ClassMeasurement classMeasure = new ClassMeasurement(type, structManager);
						List<SoftwareMeasure> sizeMeasure = classMeasure.getMeasureList(getAvailableSoftwareSizeMeasureList());
						String metrics = getMetricsString(sizeMeasure);
						
						classNode = new ClassNode(NodeKind.CLASS_NODE,
								type.getSimpleName() + "@"
										+ type.getLocation().getUniqueId(),
								classFilePath, metrics);
						parent.add(classNode);
						fields = type.getFieldList();
						loadFields(fields, classNode);
	
						methods = type.getMethodList();
						loadMethods(methods, classNode);
					}
				}
			}

		}
	}

	private void loadFields(List<FieldDefinition> fields, ProjectTreeNode parent) {
		if (fields != null) {
			ProjectTreeNode fieldNode = null;
			for (FieldDefinition field : fields) {
				String type = field.getType().getName();
				fieldNode = new FieldNode(NodeKind.FIELD_NODE,
						field.getSimpleName() + "@"
								+ field.getLocation().getUniqueId(), type, 
								getMetricsString(getAvailableSoftwareSizeMeasureList()));
				parent.add(fieldNode);
			}
		}
	}

	private void loadMethods(List<MethodDefinition> methods,
			ProjectTreeNode parent) {
		if (methods != null) {
			ProjectTreeNode methodNode = null;
			for (MethodDefinition method : methods) {
				List<VariableDefinition> vars = method.getParameterList();
				// 计算方法的规模度量值
				MethodMeasurement methodMeasure = new MethodMeasurement(method, structManager);
				List<SoftwareMeasure> sizeMeasures = methodMeasure.getMeasureList(getAvailableSoftwareSizeMeasureList());
				String metrics = getMetricsString(sizeMeasures);
				
				if (vars != null && vars.size() > 0) {
					String[] paramsType = new String[vars.size()];
					for (int i = 0; i < paramsType.length; i++) {
						paramsType[i] = vars.get(i).getType().getName();
					}
					
					methodNode = new MethodNode(NodeKind.METHOD_NODE,
							method.getSimpleName() + "@"
									+ method.getLocation().getUniqueId(),
							paramsType, metrics);
				} else {
					methodNode = new MethodNode(NodeKind.METHOD_NODE,
							method.getSimpleName() + "@"
									+ method.getLocation().getUniqueId(), null, metrics);
				}
				parent.add(methodNode);
			}
		}
	}
	
	// 将size Metric信息保存
	private void modifyProjectMetricString(ProjectTreeNode n, ProjectTreeNode versionNode) {
		Enumeration<ProjectNode> childNodes = n.children();
		String[] proValues = n.getSizeMetricValue();// 存储Metric值
		String[] values = versionNode.getSizeMetricValue();
		for(int i = 0; i < values.length; i++){
			if(proValues[i].equals("N.A")) {// 初始化ProjectNode里的Metric值
				proValues[i] = values[i];
			} else {
				if(!values[i].equals("N.A")) {// 两者都有初始值
					double proValue = Double.valueOf(proValues[i]);
					double value = Double.valueOf(values[i]);
					proValues[i] = proValue + value + "";
				}
			}
		}
		String[] proMetricIds = n.getSizeMetricIdentifier();
		String[] proMetrics = new String[proMetricIds.length];
		for(int i = 0; i<proValues.length; i++) {
			proMetrics[i] = proMetricIds[i] + "=" + proValues[i];
		}
		String result="";
		int index = 0;
		for(; index < proMetrics.length - 1; index++) {
			result += proMetrics[index] + "@";
		}
		result += proMetrics[index];
		((ProjectNode)n).setSizeMetrics(result);
	}
	
	private String getMetricsString(List<SoftwareMeasure> sizeMeasurements) {
		String metrics = "";
		int i=0;
		SoftwareMeasure measurement;
		for(;i < sizeMeasurements.size() - 1; i++) {
			measurement = sizeMeasurements.get(i);
			if(measurement.isUsable()) {
				metrics += measurement.getIdentifier() + "=" + measurement.getValue()+"@";
			} else {
				metrics +=  measurement.getIdentifier() + "=N.A@";
			}
		}
		measurement = sizeMeasurements.get(i);
		if(measurement.isUsable()) {
			metrics  += measurement.getIdentifier()+"="+measurement.getValue();
		} else {
			metrics +=  measurement.getIdentifier() + "=N.A";
		}
		return metrics;
	}
	
	private List<SoftwareMeasure> getAvailableSoftwareSizeMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PKG),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OVMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IMPMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLFLD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHFLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}


	@Override
	public void cancelButtonIsDown() {
		ProjectTree.canFresh = true;
		ProjectTree.isBuilding = false;
		cancel = true;
	}
}
