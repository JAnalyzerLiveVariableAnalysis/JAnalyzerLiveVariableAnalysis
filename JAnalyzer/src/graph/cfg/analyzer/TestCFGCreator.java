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
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.visitor.NameDefinitionVisitor;
import sourceCodeAST.CompilationUnitRecorder;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2017��9��3��
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
	
	public static String testCreateCFGWithReachName(String path, PrintWriter output) {
		StringBuffer buffer = new StringBuffer();
		
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		MethodDefinition maxMethod = null;
		int maxLineNumber = 0;
		//Debug.setStart("s"+methodList.size());
		for (NameDefinition definition : methodList) {
			/*MethodDefinition method = new MethodDefinition(definition.getSimpleName(), definition.getFullQualifiedName(),
					definition.getLocation() ,definition.getScope(), d);*/
			
			MethodDefinition method = (MethodDefinition)definition;
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
		
		if (maxMethod == null) return buffer.toString();
		
		Debug.setStart("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines...");
		buffer.append("Begin creating CFG for method " + maxMethod.getUniqueId() + ", " + maxLineNumber + " lines..."+"\n");
		
		output.println("ExecutionPointId\tDefinedName\tValue\tNameLocation\tValueLocation");
		buffer.append("ExecutionPointId\tDefinedName\tValue\tNameLocation\tValueLocation"+"\n");
		
		MethodDefinition method = maxMethod;
		
		ControlFlowGraph cfg = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
		
		List<GraphNode> nodeList = cfg.getAllNodes();
		System.out.println("Before write execution point " + nodeList.size() + " nodes!");
		buffer.append("Before write execution point " + nodeList.size() + " nodes!"+"\n");
		
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
						buffer.append("[" + graphNode.getId() + "]\t" + name.getSimpleName() + "\t" + value.toSimpleString() + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]"+"\n");
					} else {
						output.println("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
						buffer.append("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n");
					}
				}
			} else {
				output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
				System.out.println("Found none execution point with defined name node!");
				buffer.append("Found none execution point with defined name node!"+"\n");
			}
		}
		
		output.println();
		output.println();
		buffer.append("\n");
		
		try {
			cfg.simplyWriteToDotFile(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		output.println();
		return buffer.toString();
	}
	
	public static String testCreateCFGWithFileName(String path, PrintWriter output) {
		int totalCFGS = 0;
		StringBuffer buffer = new StringBuffer();
        NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			ControlFlowGraph cfg = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
			
			List<GraphNode> nodeList = cfg.getAllNodes();
			
			if(nodeList.isEmpty()) continue;
			
			System.out.println("Before write execution point " + nodeList.size() + " nodes!");
			buffer.append("Before write execution point " + nodeList.size() + " nodes!"+"\n");
			
			totalCFGS += nodeList.size();
			
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
							buffer.append("[" + graphNode.getId() + "]\t" + name.getSimpleName() + "\t" + value.toSimpleString() + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]"+"\n");
						} else {
							output.println("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
							buffer.append("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n");
						}
					}
				} else {
					output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
					System.out.println("Found none execution point with defined name node!");
					buffer.append("Found none execution point with defined name node!"+"\n");
				}
			}
			//nodeList.clear(); //��� �Ա���һ��ѭ�����¼���nodeList
			
			output.println();
			output.println();
			
			buffer.append("\n");
			try {
				cfg.simplyWriteToDotFile(output);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		
		Debug.time("After Create " + totalCFGS + " CFGs.....");
		output.println();
		return buffer.toString();
	}
	

	public static String testCreateCFG(String path, PrintWriter output) {
		StringBuffer buffer = new StringBuffer();
		NameTableManager tableManager = NameTableManager.createNameTableManager(path);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor(new NameDefinitionKindFilter(NameDefinitionKind.NDK_METHOD));
		tableManager.accept(visitor);
		List<NameDefinition> methodList = visitor.getResult();
		
		Debug.flush();
		int counter = 0;
		Debug.setStart("Begin creating CFG and analysis dominate node...");
		buffer.append("Begin creating CFG and analysis dominate node..."+"\n");
		
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			//if (!method.getSimpleName().equals("enable")) continue;
			
			System.out.println("Method " + method.getFullQualifiedName());
			buffer.append("Method " + method.getFullQualifiedName() + "\n" + "\n");
			ControlFlowGraph cfg1 = ReachNameAndDominateNodeAnalyzer.create(tableManager, method);
			ControlFlowGraph cfg2 = ReachNameAnalyzer.create(tableManager, method);
			List<GraphNode> nodeList = cfg1.getAllNodes();
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
							buffer.append("[" + graphNode.getId() + "]\t" + name.getSimpleName() + "\t" + value.toSimpleString() + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]"+"\n");
						} else {
							output.println("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
							buffer.append("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n");
						}
					}
				} else {
					output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
					System.out.println("Found none execution point with defined name node!");
					buffer.append("Found none execution point with defined name node!"+"\n");
				}
			}
			
			/*if (compareTwoCFGs(cfg1, cfg2)) {
				Debug.println("Two CFGs are the same for method " + method.getFullQualifiedName());
			} else {
				Debug.println("\tTwo CFGs are different for method " + method.getFullQualifiedName());
				counter++;
			}*/
		}
		Debug.time("After Create " + methodList.size() + " CFGs....., and there are " + counter + " different CFGs....");
		buffer.append("After Create " + methodList.size() + " CFGs....., and there are " + counter + " different CFGs...." + "\n");
		output.println();
		
		
		Debug.setStart("Begin creating CFG and analysis reache name...");
		for (NameDefinition definition : methodList) {
			MethodDefinition method = (MethodDefinition)definition;
			//if (!method.getSimpleName().equals("compareMethodDefinitionSignature")) continue;

			System.out.println("Method " + method.getSimpleName());
			buffer.append("Method " + method.getSimpleName() + "\n");
			
			ControlFlowGraph cfg = ReachNameAnalyzer.create(tableManager, method);
	
			try {
				cfg.simplyWriteToDotFile(output);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			/*List<GraphNode> nodeList = cfg.getAllNodes();
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
							buffer.append("[" + graphNode.getId() + "]\t" + name.getSimpleName() + "\t" + value.toSimpleString() + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]"+"\n");
						} else {
							output.println("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
							buffer.append("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n");
						}
					}
				} else {
					output.println(graphNode.getId() + "\t~~\t~~\t~~\t~~");
					System.out.println("Found none execution point with defined name node!");
					buffer.append("Found none execution point with defined name node!"+"\n");
				}
			}*/
			
		}
		Debug.time("After Create " + methodList.size() + " CFGs.....");
		buffer.append("After Create " + methodList.size() + " CFGs....." + "\n");
		return buffer.toString();
	}
	
}
