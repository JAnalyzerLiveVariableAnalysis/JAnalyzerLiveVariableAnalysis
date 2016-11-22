package graph.dependenceGraph;

import graph.basic.AbstractGraph;
import graph.basic.GraphNode;
import graph.basic.GraphUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/4
 * @version 1.0
 */
public class TestClassDependence {


	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String rootPath = "C:\\";
		
		String[] paths = {rootPath + "ZxcTools\\debug\\package\\print_tokens2\\", rootPath + "ZxcTools\\debug\\package\\replace\\", 
				rootPath + "ZxcWork\\ProgramAnalysis\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", 
				rootPath + "ZxcWork\\ToolKit\\src\\", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
				rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
				rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
				rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
				rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
				rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};

		String systemPath = "E:\\ZxcTools\\jEdit\\"; 
		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.1)", "jEdit(3.2)", "jEdit(4.0)", "jEdit(4.0.2)", "jEdit(4.0.3)", 
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.3.3)", "jEdit(4.4.1)",  "jEdit(4.4.2)", "jEdit(4.5.0)", 
				"jEdit(4.5.1)", "jEdit(4.5.2)", "jEdit(5.0.0)", "jEdit(5.1.0)",
		};

		String netFilePath = rootPath + "ZxcWork\\ProgramAnalysis\\data\\";
		String netFilePostfix =  "CDG.net";
		
		String netFile = netFilePath + "PA" + netFilePostfix;
		String resultDotFile = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.dot";
		String resultNetFile = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.net";
		String resultTxtFile = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";
		
//		String path  = systemPath + versionPaths[16] + "\\";
	
		generatePackageDependenceGraph(paths[2], netFile, resultNetFile, resultDotFile);
/*		
		try {
			PackageDependenceGraph pdg = PackageDependenceGraph.readFromNetFile(resultNetFile);
			Debug.setStart("Before calculate....");
			ValuedNodeManager manager = ValuedNodeGraphUtil.calculateNodeImportanceByPageRanking(pdg);
			Debug.time("After calculate....");
//			manager.sortNodeByValue(false);
			String title = "Package Id\tPackage Label\tPageRank Value";
			manager.write(resultTxtFile, title, false);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
*/
//		String centerLabel = "GraphNode@3:0@basic\\GraphNode.java";
//		int distance = 1;
//		testSubClassDependenceGraph(netFile, centerLabel, resultDotFile, distance);
	}

	public static void generatePackageDependenceGraph(String path, String cdgNetFile, String pdgNetFile, String dotFile) {
		
		PrintWriter cdgNetWriter = new PrintWriter(System.out);
		PrintWriter pdgNetWriter = new PrintWriter(System.out);
		PrintWriter dotWriter = new PrintWriter(System.out);
		
		try {
			cdgNetWriter = new PrintWriter(new FileOutputStream(new File(cdgNetFile)));
			pdgNetWriter = new PrintWriter(new FileOutputStream(new File(pdgNetFile)));
			dotWriter = new PrintWriter(new FileOutputStream(new File(dotFile)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");

		ClassDependenceCreator CDGCreator = new ClassDependenceCreator(parser, manager);
		
		Debug.setStart("Begin create CDG....");
		ClassDependenceGraph CDG = CDGCreator.create();
		Debug.time("End creating CDG....");

		parser.releaseAllASTs();
		parser.releaseAllFileContents();
		manager = null;
		parser = null;
		creator = null;
		CDGCreator = null;
		
		if (CDG != null) {
			PackageDependenceCreator PDGCreator = new PackageDependenceCreator(CDG);
			Debug.setStart("Begin create PDG....");
			PackageDependenceGraph PDG = PDGCreator.create();
			Debug.time("End creating PDG....");
			
			try {
				GraphUtil.simplyWriteToNetFile(CDG, cdgNetWriter);
				GraphUtil.simplyWriteToNetFile(PDG, pdgNetWriter);
				GraphUtil.simplyWriteToDotFile(PDG, dotWriter);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} else System.out.println("Class dependence graph is a null graph!");
		
		cdgNetWriter.close();
		pdgNetWriter.close();
		dotWriter.close();
	}
	
	public static void testSubClassDependenceGraph(String netFile, String centerLabel, String resultDotFile, int distance) {
		
		try {
			ClassDependenceGraph graph = ClassDependenceGraph.readFromNetFile(netFile);
			GraphNode centerNode = graph.findByLabel(centerLabel);
			int degree = graph.getDegree(centerNode);
			int inDegree = graph.getInDegree(centerNode);
			int outDegree = graph.getOutDegree(centerNode);
			System.out.println("The degree of " + centerLabel + " is " + degree + ", in degree is " + inDegree + ", out degree is " + outDegree);
			
			AbstractGraph subgraph = GraphUtil.extractSubGraphToCenter(graph, centerNode, distance);
			
			PrintWriter output = new PrintWriter(new FileWriter(new File(resultDotFile)));
			GraphUtil.simplyWriteToDotFile(subgraph, output);
			output.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public static void generateAllCDGs(String systemPath, String[] versionPaths, String netFilePath, String netFilePostfix) {
		int versionIndex = 0;
		while (versionIndex < versionPaths.length) {
			String path = systemPath + versionPaths[versionIndex] + "\\";
			String resultNet = netFilePath + versionPaths[versionIndex] + netFilePostfix;

			PrintWriter netWriter = new PrintWriter(System.out);
			
			try {
				netWriter = new PrintWriter(new FileOutputStream(new File(resultNet)));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			
			SourceCodeFileSet parser = new SourceCodeFileSet(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.setStart("Begin creating system, path = " + path);
			NameTableManager manager = creator.createNameTableManager();
			Debug.time("End creating.....");

			ClassDependenceCreator CDGCreator = new ClassDependenceCreator(parser, manager);
			
			Debug.setStart("Begin create CDG....");
			ClassDependenceGraph CDG = CDGCreator.create();
			Debug.time("End creating CDG....");

			if (CDG != null) {
				try {
					GraphUtil.simplyWriteToNetFile(CDG, netWriter);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			} else System.out.println("Class dependence graph is a null graph!");
			
			parser.releaseAllASTs();
			parser.releaseAllFileContents();
			manager = null;
			parser = null;
			creator = null;
			CDGCreator = null;
			
			netWriter.close();
			versionIndex = versionIndex + 1;
		}
		
	}

}
