package ui.metricBrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

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
import ui.mainFrame.Log;
import ui.resultBrowser.ResultFileParser;
import ui.resultBrowser.ResultTable;
import ui.resultBrowser.ResultTableFrame;
import ui.structureBrowser.tree.ClassNode;
import ui.structureBrowser.tree.NodeKind;
import ui.structureBrowser.tree.ProjectTreeNode;
import ui.structureBrowser.tree.VersionNode;
import util.SourceCodeLocation;
import util.SourceCodeParser;

/**
 * 计算度量的类
 * @author Wu zhangsheng
 */
public class MetricCalculator extends Thread {

	String formatDate;
	String resFilePath;
	List<ProjectTreeNode> nodes;
	String[] metrics;
	private boolean isInit = true;
	JTabbedPane srcTabbedPane;
	
	public MetricCalculator(String formatDate,String resFilePath,
			List<ProjectTreeNode> nodes, String[] metrics, JTabbedPane srcTabbedPane) {
		this.formatDate = formatDate;
		this.resFilePath = resFilePath;
		this.nodes = nodes;
		this.metrics = metrics;
		
		this.srcTabbedPane = srcTabbedPane;
	}
	
	public void run() {
		try {
			calculate();
			File file = new File(resFilePath);
			ResultFileParser parser = new ResultFileParser(file);
			ResultTable table = new ResultTable(parser);
			table.setTextArea(srcTabbedPane);

			JFrame frame = new ResultTableFrame(table);
			frame.setTitle(file.getName());
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// 用户在选择的时候就已经确定了计算的NameDefinition的粒度
	// 而一次计算过程中只能计算同种粒度的实体
	public void calculate() throws FileNotFoundException {
		
		File file = new File(resFilePath);
		PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
		
		List<SoftwareMeasure> measures = new ArrayList<SoftwareMeasure>();
		for(String metric : metrics) {
			measures.add(new SoftwareMeasure(metric));
		}

		// lastVersionNode用来记录上次计算时的版本节点，当要被度量的实体是处
		// 在同一个版本下时只需要建立一次名字表
		ProjectTreeNode lastVersionNode = null;
		SourceCodeParser parser = null;
		NameTableCreator creator = null;
		NameTableManager nameTableManager = null;
		SoftwareStructManager structManager = null;
		NodeKind kind = null;
		List<SoftwareMeasure> resultMeasures = null;
		String nodesStr="";
		
		for(int index = 0; index < nodes.size(); index ++) {
			ProjectTreeNode n = nodes.get(index);

			if(index < 2) {
				nodesStr += n.getSimpleName() + ";";
			}
			
			switch(n.getNodeKind()) {
			case PROJECT_NODE:// 选择系统的话，下面的所有版本分别计算
				Log.consoleLog("System metric calculating...");
				Enumeration<ProjectTreeNode> versionNodes = n.children();
				resultMeasures = new ArrayList<SoftwareMeasure>();
				for(SoftwareMeasure m : measures) {
					SoftwareMeasure sMeasure = new SoftwareMeasure(m.getIdentifier());
					resultMeasures.add(sMeasure);
				}
				int versCount = 0;
				while(versionNodes.hasMoreElements()) {
					versCount ++;
					ProjectTreeNode version = versionNodes.nextElement();
					Log.consoleLog("calculating " + n.getSimpleName());
					parser = new SourceCodeParser(version.getLocation());
					creator = new NameDefinitionCreator(parser);
					nameTableManager = creator.createNameTableManager();
					structManager = new SoftwareStructManager(nameTableManager);
					SystemScopeMeasurement sysMeasurement = new SystemScopeMeasurement(nameTableManager.getRootScope(),structManager);
					List<SoftwareMeasure> versMeasures = sysMeasurement.getMeasureList(measures);
					// 传入的是系统节点时，由于以版本为基本路径建立名字节点，因此此处会有多次循环
					for(int i = 0; i < resultMeasures.size(); i++) {
						SoftwareMeasure res = resultMeasures.get(i);
						SoftwareMeasure ver = versMeasures.get(i);
						if(ver.isUsable()) {
							if(res.isUsable()) {
								res.setValue(res.getValue() + ver.getValue());
							} else {
								res.setUnusable();
								res.setValue(ver.getValue());
							}
						}
					}
				}
				for(SoftwareMeasure m : resultMeasures) {
					if(m.isUsable()) {
						m.setValue(m.getValue()/versCount);
					}
				}
				kind = NodeKind.PROJECT_NODE;
				break;
			case VERSION_NODE:// 选择版本，则直接计算
				Log.consoleLog("Version metric calculating...");
				parser = new SourceCodeParser(n.getLocation());
				creator = new NameDefinitionCreator(parser);
				nameTableManager = creator.createNameTableManager();
				structManager = new SoftwareStructManager(nameTableManager);
				SystemScopeMeasurement sysMeasurement = new SystemScopeMeasurement(nameTableManager.getRootScope(),structManager);
				resultMeasures = sysMeasurement.getMeasureList(measures);
				kind = NodeKind.VERSION_NODE;
				break;
			case PACKAGE_NODE:// 选择包
				Log.consoleLog("Package metric calculating...");
				// 以包的父节点(版本)来建立名字表，并获取PackageDefinition
				VersionNode versionNode = (VersionNode)n.getParent();
				if(!versionNode.equals(lastVersionNode)) {
					lastVersionNode = versionNode;
					parser = new SourceCodeParser(versionNode.getLocation());
					creator = new NameDefinitionCreator(parser);
					nameTableManager = creator.createNameTableManager();
					structManager = new SoftwareStructManager(nameTableManager);
				}
				Log.consoleLog("calculating " + n.getSimpleName());
				PackageDefinition pack = nameTableManager.findPackageByName(n.getSimpleName());
				PackageMeasurement packMeasure = new PackageMeasurement(pack, structManager);
				resultMeasures = packMeasure.getMeasureList(measures);
				kind = NodeKind.PACKAGE_NODE;
				break;
			case CLASS_NODE:
				Log.consoleLog("Class metric calculating...");
				// 用版本来建立名字表，并获取DetailedTypeDefinition
				VersionNode vNode = (VersionNode)(n.getParent().getParent());
				if(!vNode.equals(lastVersionNode)) {
					lastVersionNode = vNode;
					parser = new SourceCodeParser(vNode.getLocation());
					creator = new NameDefinitionCreator(parser);
					nameTableManager = creator.createNameTableManager();
					structManager = new SoftwareStructManager(nameTableManager);
				}
				Log.consoleLog("calculating " + n.getSimpleName());
				DetailedTypeDefinition type = nameTableManager.findDetailedTypeDefinitionByLocation(n.getSimpleName(), 
						SourceCodeLocation.getLocation(n.getLocation()));
				ClassMeasurement classMeasure = new ClassMeasurement(type, structManager);
				resultMeasures = classMeasure.getMeasureList(measures);
				kind = NodeKind.CLASS_NODE;
				break;
			case METHOD_NODE:
				Log.consoleLog("Method metric calculating...");
				// 用版本来建立名字表，并查找MethodDefinition
				VersionNode verNode = (VersionNode)(n.getParent().getParent().getParent());
				if(!verNode.equals(lastVersionNode)) {
					lastVersionNode = verNode;
					parser = new SourceCodeParser(verNode.getLocation());
					creator = new NameDefinitionCreator(parser);
					nameTableManager = creator.createNameTableManager();
					structManager = new SoftwareStructManager(nameTableManager);
				}
				Log.consoleLog("calculating " + n.getSimpleName());
				DetailedTypeDefinition typeOfMethod = nameTableManager.findDetailedTypeDefinitionByLocation(n.getSimpleName(), 
						SourceCodeLocation.getLocation(n.getLocation()));
				List<MethodDefinition> methods = typeOfMethod.getMethodList();
				for(MethodDefinition m : methods) {
					if(m.getLocation().equals(n.getLocation())) {
						MethodMeasurement methodMeasure = new MethodMeasurement(m, structManager);
						resultMeasures = methodMeasure.getMeasureList(measures);
						break;
					}
				}
				kind = NodeKind.METHOD_NODE;
				break;
			}
			saveResult(pw, n, resultMeasures);
		}
		if(nodes.size() == 3) {
			ProjectTreeNode n = nodes.get(2);
			nodesStr += n.getSimpleName();
		}
		if(nodes.size() > 3) {
			ProjectTreeNode n = nodes.get(nodes.size() - 1);
			nodesStr += "...";
			nodesStr += n.getSimpleName()+";";
		}
		Recorder r = new Recorder(formatDate, nodesStr, kind, metrics, resFilePath);
		r.save();
		
		pw.close();
	}

	private void saveResult(PrintWriter pw, ProjectTreeNode n,
			List<SoftwareMeasure> resultMeasures) {
		if(isInit) {// 初始写入的内容在之后作为表头的信息展示出来
			isInit = false;
			pw.write("路径\t");
			switch(n.getNodeKind()) {// 写入被度量的实体粒度
			case PROJECT_NODE:
				pw.write("系统\t");
				break;
			case VERSION_NODE:
				pw.write("版本\t");
				break;
			case PACKAGE_NODE:
				pw.write("包\t");
				break;
			case CLASS_NODE:
				pw.write("类\t");
				break;
			case METHOD_NODE:
				pw.write("方法\t");
				break;
			}
			// 写入计算的度量类型
			for(int i = 0; i < metrics.length - 1; i++) {
				pw.write(metrics[i] + "\t");
			}
			pw.write(metrics[metrics.length - 1] + "\r\n");
		}
		
		// 节点对应文件的实际路径
		if(n.getNodeKind() == NodeKind.CLASS_NODE) {
			String path = ((ClassNode)n).getFilePath();
			System.out.println(path);
			pw.write(path+"\t");
		} else {
			pw.write(n.getLocation()+"\t");
		}
		
		// 写入被度量的实体的完整名
		pw.write(n.getQualifiedName()+"\t");
		// 写入计算出来的度量值
		for(int i = 0; i < resultMeasures.size() - 1; i++) {
			SoftwareMeasure m = resultMeasures.get(i);
			pw.write(m.getValue() + "\t");
		}
		pw.write(resultMeasures.get(resultMeasures.size()-1).getValue() + "\r\n");
		pw.flush();
	}
}
