package graph.callGraph;

import graph.basic.AbstractGraph;
import graph.basic.GraphUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;


public class TestCallGraph 
{
	@SuppressWarnings("unused")
	public static void main(String[] args) 
	{
		String rootPath = "C:\\";

		String path1 = rootPath + "ZxcTools\\debug\\package\\print_tokens2\\";
		String path2 = rootPath + "ZxcTools\\debug\\package\\replace\\";
		String path3 = rootPath + "ZxcWork\\ProgramAnalysis\\src\\";
		String path4 = rootPath + "ZxcTools\\EclipseSource\\org\\";
		String path5 = rootPath + "ZxcWork\\ToolKit\\src\\";
		String path6 = rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\";
		String path7 = rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\";
		String path8 = rootPath + "ZxcTools\\JDKSource\\";
		String path9 = rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\";
		String pathA = rootPath + "ZxcWork\\FaultLocalization\\src\\";
		String pathB = rootPath + "ZxcTools\\ArgoUml\\";
		String pathC = rootPath + "ZxcTools\\jEdit_5_1_0\\";
		String pathD = rootPath + "ZxcTools\\lucene_2_0_0\\";
		String pathE = rootPath + "ZxcTools\\struts_2_0_1\\";

		String path = path3;
		String resultDot = rootPath + "ZxcWork\\ProgramAnalysis\\data\\resultCG.dot";
		String resultNet = rootPath + "ZxcWork\\ProgramAnalysis\\data\\resultCG.net";
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator nameTableCreatorreator = new NameDefinitionCreator(parser);

		Debug.setWriter(new PrintWriter(System.out));
		Debug.setStart("Begin creating system, path = " + path);

		NameTableManager nameTableManager = nameTableCreatorreator.createNameTableManager();
		Debug.time("After creating name table!");
		
		Debug.time("Begin creating structure....");
		Debug.disable();
		SoftwareStructManager structManager = new SoftwareStructManager(nameTableManager);
		if (path.contains("ProgramAnalysis")) structManager.createSoftwareStructure();
		else structManager.readOrCreateSoftwareStructure();
		Debug.enable();
		Debug.time("End creating.....");

		class LargeTypeFilter extends NameTableFilter {

			@Override
			public boolean accept(NameDefinition definition) {
				if (!definition.isDetailedType()) return false;
//				return true;
				if (definition.getFullQualifiedName().equals("ui.mainFrame.JsMetricFrame")) return true;
//				if (definition.getSimpleName().equals("Axis") || definition.getSimpleName().equals("JFreeChart") || definition.getSimpleName().equals("Plot")) return true;
				else return false;
			}

		}
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new LargeTypeFilter());
		SystemScope rootScope = nameTableManager.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		DetailedTypeDefinition type = (DetailedTypeDefinition)definitionList.get(0);
		
		// 创建调用图
		AbstractGraph callGraph = CallGraphCreator.create("JsMetricFrame", structManager, type, false, 0);
		
		try {
			System.out.println("Number of Nodes: " + callGraph.getAllNodes().size());
			System.out.println("Number of Edges: " + callGraph.getEdges().size());
			PrintWriter writer = null;
			
			// 生成dot文件输出给GraphViz画图
			writer = new PrintWriter(new FileOutputStream(new File(resultDot)));
			GraphUtil.simplyWriteToDotFile(callGraph, writer);
			writer.flush();
			writer.close();
//			// 生成net文件输出给Pajek分析
//			writer = new PrintWriter(new FileOutputStream(new File(resultNet)));
//			GraphUtil.simplyWriteToNetFile(callGraph, writer);
//			writer.flush();
//			writer.close();
		} catch (IOException e)	{
			e.printStackTrace();
		}
		Debug.time("End creating.....");
		Debug.flush();
	}
}

