package graph.cfg.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import graph.cfg.CFGNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameDefinitionKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê9ÔÂ3ÈÕ
 * @version 1.0
 *
 */
public class TestCFGCreator {

	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
							rootPath + "ZxcWork\\JAnalyzer\\src\\", rootPath + "ZxcTools\\EclipseSource\\org\\", rootPath + "ZxcTemp\\JAnalyzer\\src\\",
							rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\TestCFGTwo.java", rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\", 
							rootPath + "ZxcProject\\AspectViz\\ZxcWork\\SortAnimator4\\", rootPath + "ZxcTools\\JDKSource\\", 
							rootPath + "ZxcCourse\\JavaProgramming\\JHotDraw5.2\\sources\\", rootPath + "ZxcWork\\FaultLocalization\\src\\", 
							rootPath + "ZxcTools\\ArgoUml\\", rootPath + "ZxcTools\\jEdit_5_1_0\\", 
							rootPath + "ZxcTools\\lucene_2_0_0\\", rootPath + "ZxcTools\\struts_2_0_1\\",
							rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\", rootPath + "ZxcTools\\apache_ant_1_9_3\\src\\main\\org\\apache\\tools\\ant\\",
		};
		
		String path = paths[2];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = new PrintWriter(System.out);
		try {
			output = new PrintWriter(new FileOutputStream(result));
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(-1);
		}

		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			writer = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(writer);
			Debug.setScreenOn();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
//		testCreateCFGWithDominateNode(path3, output);
//		testCreateCFGWithReachName(path, output);
//		testCreateCFG(path3, output);
		
		testRootReachName(path, output);
		
		writer.close();
		output.close();
	}

	public static void testCreateCFGWithDominateNode(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			if (!method.getFullQualifiedName().contains("get")) continue;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		
		if (maxMethod == null) return;
		
		Debug.flush();
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("CurrentNodeId\tCurrentNodeLabel\tDominateNodeId\tDominateNodeLabel");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
		if (cfg == null) return;
		
		DominateNodeAnalyzer.printDominateNodeInformation(cfg, output);
		
		output.println();
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	
	public static void testRootReachName(String path, PrintWriter writer) {
		NameTableManager manager = NameTableManager.createNameTableManager(path);
		SourceCodeFileSet sourceCodeFileSet = manager.getSouceCodeFileSet();

		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		manager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		int counter = 0;
		int methodCounter = 0;

		StringBuilder message = new StringBuilder("No\tMethod\tExecutionPointId\tReference\tLocation\tFirstRootValue\tOtherRootValue\tMethodId");
		writer.println(message.toString());
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(manager, true);
		
		for (NameDefinition nameDefinition : methodList) {
			if (!nameDefinition.getFullQualifiedName().contains("getSpearmanCoefficient")) continue;
			
			methodCounter++;
			MethodDefinition method = (MethodDefinition)nameDefinition;
			if (method.isAutoGenerated()) continue; 
			TypeDefinition enclosingType = method.getEnclosingType();
			if (!enclosingType.isDetailedType()) continue;
			DetailedTypeDefinition type = (DetailedTypeDefinition)enclosingType;
			if (type.isAnonymous()) continue;

			Debug.println("Method " + methodCounter + ": " + method.getFullQualifiedName());
			
			CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(method);
			String unitFileName = unitScope.getUnitName();
			CompilationUnit astRoot = manager.getSouceCodeFileSet().findSourceCodeFileASTRootByFileUnitName(unitFileName);
			CompilationUnitRecorder unitRecorder = new CompilationUnitRecorder(unitFileName, astRoot);
			
			// Create a ControFlowGraph object
			ControlFlowGraph currentCFG = CFGCreator.create(manager, unitRecorder, method);
			if (currentCFG == null) {
				sourceCodeFileSet.releaseAST(unitFileName);
				sourceCodeFileSet.releaseFileContent(unitFileName);
				continue;
			}
			
			ReachNameAnalyzer.setReachNameRecorder(currentCFG);
			ReachNameAnalyzer.reachNameAnalysis(manager, unitRecorder, method, currentCFG);
			
			List<GraphNode> nodeList = currentCFG.getAllNodes();
			for (GraphNode graphNode : nodeList) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				if (node.isVirtual()) continue;
				
				ASTNode astNode = node.getAstNode();
				if (astNode == null) continue;
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitFileName, astNode);
				if (referenceList == null) continue;
				
				for (NameReference reference : referenceList) {
					reference.resolveBinding();
					List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
					for (NameReference leafReference : leafReferenceList) {
						if (leafReference.isLiteralReference() || leafReference.isMethodReference() || leafReference.isTypeReference()) continue;
						String firstRootValue = "~~";
						String otherRootValue = "~~";
						Debug.println("In TestCFGCreator: Get root reach name for [" + leafReference.getUniqueId() + "] in node " + node.getId());
						List<ReachNameDefinition> rootDefinedNameList = ReachNameAnalyzer.getRootReachNameDefinitionList(currentCFG, node, leafReference);
						for (ReachNameDefinition rootDefinedName : rootDefinedNameList) {
							NameReference value = rootDefinedName.getValue();
							if (value != null) {
								if (firstRootValue.equals("~~")) {
									firstRootValue = "[" + value.getLocation() + "]" + value.toSimpleString();
								} else if (otherRootValue.equals("~~")) {
									otherRootValue = "[" + value.getLocation() + "]" + value.toSimpleString();
								} else {
									otherRootValue = otherRootValue + ";~" + "[" + value.getLocation() + "]" + value.toSimpleString();
								}
							}
						}
						counter++;
						writer.println(counter + "\t" + method.getSimpleName() + "\t[" + node.getId() + "]\t" + leafReference.toSimpleString() + "\t[" + leafReference.getLocation() + "]" + "\t" + firstRootValue + "\t" + otherRootValue + "\t" + method.getUniqueId());
					}
				}
			}
			sourceCodeFileSet.releaseAST(unitFileName);
			sourceCodeFileSet.releaseFileContent(unitFileName);
		}
		writer.flush();
	}
	
	public static void testCreateCFGWithReachName(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();

		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getFullQualifiedName().contains("main")) continue;
			if (maxMethod == null) {
				maxMethod = method;
				maxLineNumber = maxMethod.getEndLocation().getLineNumber() - maxMethod.getLocation().getLineNumber();
			} else {
				int currentLineNumber = method.getEndLocation().getLineNumber() - method.getLocation().getLineNumber();
				if (currentLineNumber > maxLineNumber) {
					maxMethod = method;
					maxLineNumber = currentLineNumber; 
				}
			}
		}
		
		if (maxMethod == null) return;
		
		Debug.flush();
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		output.println("ExecutionPointId\tDefinedName\tValue\tNameLocation\tValueLocation");
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		for (GraphNode graphNode : nodeList) {
			if (graphNode instanceof ExecutionPoint) {
				ExecutionPoint node = (ExecutionPoint)graphNode;
				ReachNameRecorder recorder = (ReachNameRecorder)node.getFlowInfoRecorder();
				List<ReachNameDefinition> definedNameList = recorder.getReachNameList();
				for (ReachNameDefinition definedName : definedNameList) {
					NameDefinition name = definedName.getName();
					NameReference value = definedName.getValue();
					if (definedName.getValue() != null) {
						output.println("[" + graphNode.getId() + "]\t" + name.getSimpleName() + "\t" + value.toSimpleString() + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]");
					} else {
						output.println("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
					}
				}
			} else {
				output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
				System.out.println("Found none execution point with defined name node!");
			}
		}
		
		output.println();
		output.println();
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
	}
	

	public static void testCreateCFG(String path, PrintWriter output) {
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		Debug.flush();
		int counter = 0;
		Debug.setStart("Begin creating CFG and analysis dominate node...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getSimpleName().equals("enable")) continue;
			
//			System.out.println("Method " + method.getFullQualifiedName());
			ControlFlowGraph cfg1 = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
//			ControlFlowGraph cfg2 = ReachNameAnalyzer.create(tableManager, method);
//			if (compareTwoCFGs(cfg1, cfg2)) {
//				Debug.println("Two CFGs are the same for method " + method.getFullQualifiedName());
//			} else {
//				Debug.println("\tTwo CFGs are different for method " + method.getFullQualifiedName());
//				counter++;
//			}
		}
		Debug.time("After Create " + methodList.size() + " CFGs....., and there are " + counter + " different CFGs....");
		output.println();
		
		
		Debug.setStart("Begin creating CFG and analysis reache name...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
//			if (!method.getSimpleName().equals("compareMethodDefinitionSignature")) continue;

//			System.out.println("Method " + method.getSimpleName());
			ControlFlowGraph cfg = ReachNameAnalyzer.create(tableManager, method);
//			try {
//				cfg.simplyWriteToDotFile(output);
//			} catch (Exception exc) {
//				exc.printStackTrace();
//			}
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
	}
	
	static boolean compareTwoCFGs(ControlFlowGraph cfg1, ControlFlowGraph cfg2) {
		if (cfg1 == null && cfg2 != null) return false;
		if (cfg1 != null && cfg2 == null) return false;
		if (cfg1 == null && cfg2 == null) return true;
		
		List<GraphNode> nodeList1 = cfg1.getAllNodes();
		List<GraphNode> nodeList2 = cfg2.getAllNodes();
		
		for (GraphNode node1 : nodeList1) {
			CFGNode cfgNode1 = (CFGNode)node1;
			boolean found = false;
			for (GraphNode node2 : nodeList2) {
				CFGNode cfgNode2 = (CFGNode)node2;
				
				if (cfgNode1.getId().equals(cfgNode2.getId()) && cfgNode1.isStart() == cfgNode2.isStart() && 
						cfgNode1.isAbnormalEnd() == cfgNode2.isAbnormalEnd() && cfgNode1.isNormalEnd() == cfgNode2.isNormalEnd()) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find CFG1 node " + node1.getId() + " in CFG2");
				return false;
			}
		}
		
		for (GraphNode node2 : nodeList2) {
			CFGNode cfgNode2 = (CFGNode)node2;
			boolean found = false;
			for (GraphNode node1 : nodeList1) {
				CFGNode cfgNode1 = (CFGNode)node1;

				if (cfgNode1.getId().equals(cfgNode2.getId()) && cfgNode1.isStart() == cfgNode2.isStart() && 
						cfgNode1.isAbnormalEnd() == cfgNode2.isAbnormalEnd() && cfgNode1.isNormalEnd() == cfgNode2.isNormalEnd()) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find CFG2 node " + node2.getId() + " in CFG1");
				return false;
			}
		}
		
		List<GraphEdge> edgeList1 = cfg1.getEdges();
		List<GraphEdge> edgeList2 = cfg2.getEdges();
		
		for (GraphEdge edge1 : edgeList1) {
			boolean found = false;
			for (GraphEdge edge2 : edgeList2) {
				if (edge1.getStartNode().getId().equals(edge2.getStartNode().getId()) && 
						edge1.getEndNode().getId().equals(edge2.getEndNode().getId())) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find CFG1 edge in CFG2");
				return false;
			}
		}
		
		for (GraphEdge edge2 : edgeList2) {
			boolean found = false;
			for (GraphEdge edge1 : edgeList1) {
				if (edge1.getStartNode().getId().equals(edge2.getStartNode().getId()) && 
						edge1.getEndNode().getId().equals(edge2.getEndNode().getId())) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find CFG2 edge in CFG1");
				return false;
			}
		}
		
		return true;
	}
}
