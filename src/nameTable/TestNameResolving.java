package nameTable;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import util.Debug;
import util.SourceCodeParser;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;

public class TestNameResolving {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String rootPath = "C:\\";

		
		String[] paths = {"C:\\QualitasPacking\\recent\\eclipse_SDK\\eclipse_SDK-4.3\\", "C:\\QualitasPacking\\recent\\jfreechart\\jfreechart-1.0.13\\", 
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

		String path = paths[2];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}


		testMethodFieldReferences(path, writer);
		
//		testCreateScopeReferences(path, writer);
		
//		Debug.println("Version Path\tFiles\tLines\tBytes\tPackages\tClasses\tFields\tMethods\tParameters\tVariables\tDefinitions");
//		testDefinitionStatistics(path);
/*
		Debug.println("Version Path\tFiles\tLines\tBytes\tPackages\tClasses\tFields\tMethods\tParameters\tVariables\tDefinitions");
		for (int i = 0; i < versionPaths.length; i++) {
			System.out.println("Version: " + systemPath + versionPaths[i]);
			testDefinitionStatistics(systemPath + versionPaths[i] + "\\");		
		}
*/
//		testASTNodeReferences(path, writer);
//		testExecutionPointReferences(path, writer);
//		testBindReferences(path, writer);
//		testBindImports(path);
		
		writer.close();
		output.close();
	}
	
	public static void testMethodFieldReferences(String path, PrintWriter writer) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
//		writeAllInfos(manager, writer);

		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new SpecificDetailedTypeFilter());
		SystemScope rootScope = manager.getRootScope();
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();

		Debug.time("Begin scan....!");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			String className = type.getSimpleName();
			System.out.println("Scan class " + className);
			writer.println("In class : " + className);
			
			scanMethodReferenceInClass(manager, type, writer);				
			
			writer.println();
			writer.flush();
		}
		Debug.time("End scan....!");
	}
	
	public static void scanMethodReferenceInClass(NameTableManager manager, DetailedTypeDefinition type, PrintWriter writer) {
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList == null) return;
		
		for (MethodDefinition method : methodList) {
			String methodName = method.getSimpleName();
			System.out.println("Scan method " + methodName);
			writer.println("Method [" + methodName + "] has following references: ");
			
			List<NameReference> methodReferenceList = manager.createAndReturnReferencesInMethod(method);
			if (methodReferenceList == null) continue;
			
			for (NameReference reference : methodReferenceList) {
				reference.resolveBinding();
				if (reference.isGroupReference()) {
					NameReferenceGroup refGroup = (NameReferenceGroup)reference;
					List<NameReference> leafReferenceList = refGroup.getReferencesAtLeaf();
					writer.println("\t\tIt is a reference group, kind " + refGroup.getGroupKind() + ", and has following leaf references: ");
					boolean shouldWrite = true;
					for (NameReference leaf : leafReferenceList) {
						if (!leaf.isResolved()) {
							shouldWrite = shouldWriteInformation(leaf);
							if (shouldWrite) break;
						}
					}
					if (shouldWrite) {
						writer.println("\tReference " + reference.getName() + ", kind " + reference.getReferenceKind() + ", at " + reference.getLocation().toFullString());
						for (NameReference leaf : leafReferenceList) {
							if (leaf.isResolved()) {
								writer.println("\t\t\tLeaf reference " + leaf.getName() + ", kind " + leaf.getReferenceKind());
								writer.println("\t\t\t\tBind to " + leaf.getDefinition().getUniqueId() + ", kind " + leaf.getDefinition().getDefinitionKind());
							} else {
								if (shouldWriteInformation(leaf)) {
									writer.println("\t\t\tLeaf reference " + leaf.getName() + ", kind " + leaf.getReferenceKind());
									writer.println("\t\t\t\tCan not resolve this leaf reference!");
								}
							}
						}
					}
				} else {
					if (reference.isResolved()) {
						writer.println("\tReference " + reference.getName() + ", kind " + reference.getReferenceKind() + ", at " + reference.getLocation().toFullString());
						NameDefinition definition = reference.getDefinition();
						writer.println("\t\tBind to " + definition.getUniqueId() + ", kind " + definition.getDefinitionKind());
					} else {
						writer.println("\tReference " + reference.getName() + ", kind " + reference.getReferenceKind() + ", at " + reference.getLocation().toFullString());
						writer.println("\t\tCan not resolve this reference!");
					}
				}
			}
			writer.println();
		}
	}

	private static boolean shouldWriteInformation(NameReference reference) {
		if (reference.getName().equals("null")) return false;
		if (reference.getName().equals("equals")) return false;
		if (reference.getName().equals("IllegalArgumentException")) return false;
		return true;
	}
	
	public static void checkMethodReferenceInClass(NameTableManager manager, DetailedTypeDefinition type, PrintWriter writer) {
		List<FieldDefinition> fieldList = type.getAllFieldList();
		if (fieldList == null) return;
		
		List<MethodDefinition> methodList = type.getMethodList();
		if (methodList == null) return;
		
		for (MethodDefinition method : methodList) {
			String methodName = method.getSimpleName();
			System.out.println("Scan method " + methodName);
			writer.println("Method [" + methodName + "] uses the following fields: ");
			
			List<NameReference> methodReferenceList = manager.createAndReturnReferencesInMethod(method);
			if (methodReferenceList == null) continue;
			
			for (NameReference reference : methodReferenceList) {
				NameReferenceKind kind = reference.getReferenceKind(); 
				// We not only consider the kind of NRK_FIELD and also the kind of NRK_VARIABLE, since we can not always 
				// correctly distinguish a field access or a variable (simple name, or qualified name) reference 
				if (kind == NameReferenceKind.NRK_FIELD || kind == NameReferenceKind.NRK_VARIABLE) {
					FieldDefinition field = isReferToField(reference, fieldList);
					if (field != null) writer.print(field.getSimpleName() + "\t");
				} else if (kind == NameReferenceKind.NRK_GROUP) {
					// For the reference group, we must to treat its leaf reference!
					NameReferenceGroup group = (NameReferenceGroup)reference;
					List<NameReference> leafReferenceList = group.getReferencesAtLeaf();
					for (NameReference leaf : leafReferenceList) {
						FieldDefinition field = isReferToField(leaf, fieldList);
						if (field != null) writer.print(field.getSimpleName() + "\t");
					}
				}
			}
			writer.println();
		}
	}
	
	// Check if a reference refers to a field definition in a list 
	public static FieldDefinition isReferToField(NameReference reference, List<FieldDefinition> fieldList) {
		reference.resolveBinding();
		if (reference.isResolved()) {
			NameDefinition definition = reference.getDefinition();
			// Justify if the definition is a field of the class!
			for (FieldDefinition field : fieldList) {
				if (field == definition) {
					// Yes, it is a field of the class
					return field;
				}
			}
		}
		return null;
	}
	
	public static void testCreateScopeReferences(String path, PrintWriter writer) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
//		writeAllInfos(manager, writer);

		List<CompilationUnitScope> units = manager.getAllCompilationUnitScopes();
		if (units == null || units.size() <= 0) return;
		
		Debug.setStart("Begin scanning....");
		for (CompilationUnitScope unit : units) {
			String unitFileName = unit.getUnitFullName();
			System.out.println("Scan file " + unitFileName);
			
			CompilationUnit astRoot = parser.findCompilationUnitByUnitFullName(unitFileName);
			if (astRoot == null) throw new AssertionError("Can not find the compilation unit for the file: " + unitFileName);
			
			CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
			
			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> typeList = astRoot.types();
			for (AbstractTypeDeclaration type : typeList) {
				if (type.getNodeType() != ASTNode.TYPE_DECLARATION) continue;
				
				TypeDeclaration classDeclaration = (TypeDeclaration)type;
				if (classDeclaration.isInterface()) continue;
				if (!classDeclaration.isPackageMemberTypeDeclaration()) continue;

				DetailedTypeDefinition detailedType = manager.findDetailedTypeDefinitionByDeclaration(classDeclaration, astRoot, unitFileName);
				if (detailedType == null) {
					Debug.println("Can not find definition for declaration: " + classDeclaration);
					continue;
				}
				List<NameReference> referenceList =  manager.createAndReturnReferencesInDetailedType(detailedType);

				NameDefinitionVisitor definitionVisitor = new NameDefinitionVisitor();
				detailedType.accept(definitionVisitor);
				List<NameDefinition> definitionList = definitionVisitor.getResult();
				if (definitionList == null) throw new AssertionError("Can not get definitions in " + detailedType);
				
				String className = classDeclaration.getName().getIdentifier();

				Debug.println("\r\nIn class: " + className);
				
				// Create and bind the reference in the field declaration
				FieldDeclaration[] fields = classDeclaration.getFields();
				for (int index = 0; index < fields.length; index++) {
					// Print the information of the name definitions in the field
					List<NameDefinition> definitions = manager.getDefinitionsInASTNode(fields[index], astRoot, unitFileName);
					Debug.println("\r\nIn field: " + fields[index]);
					checkDefinitions(definitionList, definitions);
					
					// Print the information of the references in the field
					@SuppressWarnings("unchecked")
					List<VariableDeclarationFragment> variables = fields[index].fragments();
					for (VariableDeclarationFragment variable : variables) {
						Expression initExp = variable.getInitializer();
						if (initExp != null) {
							NameReference reference = manager.createReferenceForASTNode(initExp, astRoot, unitFileName);
							checkReferences(referenceList, reference);
						}
					}
					
				}
				
				// We only create CFG for the methods defined in those class declared in the root of the package!
				MethodDeclaration[] methodDeclarationArray = classDeclaration.getMethods();
				for (MethodDeclaration methodDeclaration : methodDeclarationArray) {
					ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, className);
					
					MethodDefinition methodDefinition = manager.findMethodDefinitionByDeclaration(methodDeclaration, detailedType, astRoot, unitFileName);
					List<NameReference> methodReferences = manager.createAndReturnReferencesInMethod(methodDefinition);
					
					if (cfg == null) continue;
					
					Debug.println("\r\nIn Method: " + cfg.getMethodName());
					
					System.out.println("Scan method: " + cfg.getMethodName());
					
					List<GraphNode> nodes = cfg.getAllNodes();
					if (nodes != null) {
						for (GraphNode node : nodes) {
							ExecutionPoint point = (ExecutionPoint)node;
							if (point.isVirtual()) continue;
							
//							Debug.println("\tIn execution point: " + node.toFullString());
							
							List<NameDefinition> definitions = manager.getDefinitionsInExecutionPoint(point);
							checkDefinitions(definitionList, definitions);
							
							NameReference reference = manager.createReferenceForExecutionPoint(point);
//							checkReferences(referenceList, reference);
							checkReferences(methodReferences, reference);
						}
					}
				}
			}
			Debug.flush();
			
			cfgCreator = null;
			typeList = null;
			parser.releaseCurrentFileContents();
			parser.releaseCurrentCompilatinUnits();
		}
		
		Debug.time("End scan....!");
	}
	
	
	public static void testDefinitionStatistics(String path) {
		
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		NameTableManager manager = creator.createNameTableManager();
		
		int allDefNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_UNKNOWN);
		int allPackageNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_PACKAGE);
		int allTypeNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_TYPE);
		int allFieldNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_FIELD);
		int allMethodNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_METHOD);
		int allParaNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_PARAMETER);
		int allVarNum = manager.getTotalNumaberOfDefinitions(NameDefinitionKind.NDK_VARIABLE);
//		int allUnitNum = manager.getTotalNumberOfCompilationUnits();
		int fileNum = parser.getFileNumber();
		long lineNum = parser.getTotalLineNumbersOfAllFiles();
		long fileBytes = parser.getTotalSpacesOfAllFiles(); 
		
		Debug.println(path + "\t" + fileNum + "\t" + lineNum + "\t" + fileBytes + "\t" + allPackageNum + "\t" + allTypeNum + "\t" + allFieldNum + "\t" + allMethodNum + "\t" + allParaNum + "\t" + allVarNum + "\t" + allDefNum);
	}
	
	public static void testASTNodeReferences(String path, PrintWriter writer) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		writeAllInfos(manager, writer);

		List<CompilationUnitScope> units = manager.getAllCompilationUnitScopes();
		if (units == null || units.size() <= 0) return;
		
		String systemPath = manager.getSystemPath();
//		SystemScope systemScope = manager.getRootScope();
		
		Debug.setStart("Begin scanning....");
		for (CompilationUnitScope unit : units) {
			String unitFileName = unit.getUnitFullName();

//			if (!unitFileName.contains("AnyImplHelper")) continue;
			
			System.out.println("Scan file " + unitFileName);

			CompilationUnit astRoot = parser.findCompilationUnitByUnitFullName(unitFileName);
			if (astRoot == null) throw new AssertionError("Can not find the compilation unit for the file: " + (systemPath + unitFileName));
			
			CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
			
			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> typeList = astRoot.types();
			for (AbstractTypeDeclaration type : typeList) {
				if (type.getNodeType() != ASTNode.TYPE_DECLARATION) continue;
				
				TypeDeclaration classDeclaration = (TypeDeclaration)type;
				if (classDeclaration.isInterface()) continue;
				if (!classDeclaration.isPackageMemberTypeDeclaration()) continue;
				
				String className = classDeclaration.getName().getIdentifier();

				Debug.println("\r\nIn class: " + className);
				
				// Create and bind the reference in the field declaration
				FieldDeclaration[] fields = classDeclaration.getFields();
				for (int index = 0; index < fields.length; index++) {
					// Print the information of the name definitions in the field
					List<NameDefinition> definitions = manager.getDefinitionsInASTNode(fields[index], astRoot, unitFileName);
					Debug.println("\r\nIn field: " + fields[index]);
					for (NameDefinition definition : definitions) printDefinitionToDebugger(definition);
					
					// Print the information of the references in the field
					@SuppressWarnings("unchecked")
					List<VariableDeclarationFragment> variables = fields[index].fragments();
					for (VariableDeclarationFragment variable : variables) {
						Expression initExp = variable.getInitializer();
						if (initExp != null) {
							NameReference reference = manager.createReferenceForASTNode(initExp, astRoot, unitFileName);
							if (reference != null) {
								reference.resolveBinding();
								printReferenceToDebugger(reference);
//								if (checkUnresolvedReference(systemScope, reference)) {
//									Debug.println("\t\t\tThis reference in expression: " + initExp.toString());
//								}
							}
						}
					}
					
				}
				
				// We only create CFG for the methods defined in those class declared in the root of the package!
				MethodDeclaration[] methodDeclarationArray = classDeclaration.getMethods();
				for (MethodDeclaration methodDeclaration : methodDeclarationArray) {
					ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, className);
					
					if (cfg == null) continue;
//					if (!cfg.getMethodName().equals("ServerManagerImpl")) continue;
					
					Debug.println("\r\nIn Method: " + cfg.getMethodName());
					
					System.out.println("Scan method: " + cfg.getMethodName());
					
					List<GraphNode> nodes = cfg.getAllNodes();
					if (nodes != null) {
						for (GraphNode node : nodes) {
							ExecutionPoint point = (ExecutionPoint)node;
							if (point.isVirtual()) continue;
							
							Debug.println("\tIn execution point: " + node.toFullString());
							
							List<NameDefinition> definitions = manager.getDefinitionsInExecutionPoint(point);
							NameReference reference = manager.createReferenceForExecutionPoint(point);
							
							for (NameDefinition definition : definitions) printDefinitionToDebugger(definition);

							if (reference != null) {
								reference.resolveBinding();
//								if (checkUnresolvedReference(systemScope, reference)) {
//									Debug.println("\t\t\tThis reference in execution point: " + node.toFullString());
//								}
//								if (!reference.isResolved() && !reference.isLiteralReference()) Debug.println("Reference has not been resolved: " + reference.getName() + "[" + reference.getLocation().toFullString() + "]!");
//								printUnresolvedReferenceToDebugger(reference);
								printReferenceToDebugger(reference);
							}
						}
					}
				}
			}
			Debug.flush();
			
			cfgCreator = null;
			typeList = null;
			parser.releaseCurrentFileContents();
			parser.releaseCurrentCompilatinUnits();
		}
		
		Debug.time("End scan....!");
	}
	
	public static void testExecutionPointReferences(String path, PrintWriter writer) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		writeAllInfos(manager, writer);

		List<CompilationUnitScope> units = manager.getAllCompilationUnitScopes();
		if (units == null || units.size() <= 0) return;
		
		String systemPath = manager.getSystemPath();
		
		Debug.setStart("Begin scanning.....");
		for (CompilationUnitScope unit : units) {
			String unitFileName = unit.getUnitFullName();
			System.out.println("Scan file " + unitFileName);

			CompilationUnit astRoot = parser.findCompilationUnitByUnitFullName(unitFileName);
			if (astRoot == null) throw new AssertionError("Can not find the compilation unit for the file: " + (systemPath + unitFileName));
			
			CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
			List<ControlFlowGraph> cfgs = cfgCreator.create();		
			if (cfgs == null) return;
			
			Debug.println("In compilation unit: " + unitFileName);
			for (ControlFlowGraph cfg : cfgs) {
				Debug.println("\r\nIn control flow graph: " + cfg.getMethodName());
				List<GraphNode> nodes = cfg.getAllNodes();
				if (nodes != null) {
					for (GraphNode node : nodes) {
						ExecutionPoint point = (ExecutionPoint)node;
						if (point.isVirtual()) continue;
						
						Debug.println("\tIn execution point: " + node.toFullString());
						
						List<NameDefinition> definitions = manager.getDefinitionsInExecutionPoint(point);
						NameReference reference = manager.createReferenceForExecutionPoint(point);
						
						for (NameDefinition definition : definitions) printDefinitionToDebugger(definition);

						if (reference != null) {
							reference.resolveBinding();
							printReferenceToDebugger(reference);
//							if (!reference.isResolved()) printUnresolvedReferenceToDebugger(reference);
						}
					}
				}
			}
			
			parser.releaseCurrentFileContents();
			parser.releaseCurrentCompilatinUnits();
			Debug.flush();
		}
		
		Debug.time("End scan....!");
	}
	

	public static void testBindReferences(String path, PrintWriter writer) {
		
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameTableCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		writeAllInfos(manager, writer);
		
		SystemScope root = manager.getRootScope();
		
		List<NameScope> mainScopeList = root.findAllSubScopesByName("main");
		for (NameScope mainScope : mainScopeList) {
			List<NameScope> subscope = mainScope.getSubScopeList();
			for (NameScope scope : subscope) {
				List<NameReference> refList = scope.getReferences();
				for (NameReference ref : refList) {
					ref.resolveBinding();
//					if (!ref.isResolved() && !ref.getName().contains("out")) {
//						Debug.println(ref.bindedDefinitionToString());
//					}
//					Debug.println("Reference : " + ref.toString());
					if (!ref.getName().contains("System.out")) 
						Debug.println(ref.bindedDefinitionToString());
				}
			}
		}
		
	}

	/**
	 * 检查不能解析的引用是否有解析的可能。我们通过系统作用域，找所有以该引用的名字为后缀的名字定义，如果找到了，我们将该名字引用，以及可能的名字定义打印到调试文件(类Debug指定的输出设备)，以便进行人工核对！
	 */
	public static boolean checkUnresolvedReference(SystemScope scope, NameReference reference) {
		if (reference.isResolved()) return false;		// We do not check those resolved name!

		String[] needNotPrintReferences = {"length", "equals", "System.out", "toString", "accept"};
		for (int i = 0; i < needNotPrintReferences.length; i++) 
			if (reference.getName().contains(needNotPrintReferences[i])) return false;
		
		if (reference.isGroupReference()) {
			List<NameReference> referencesInGroup = ((NameReferenceGroup)reference).getReferencesAtLeaf();
			if (referencesInGroup == null) {
				System.out.println("Reference group " + reference.getName() + " has not leaf reference!");
				return false;
			}
			for (NameReference ref : referencesInGroup) {
				if (ref.isResolved()) continue;
				return checkUnresolvedLeafReference(scope, ref);
			}
			return false;
		} else return checkUnresolvedLeafReference(scope, reference);
		
	}
	
	private static boolean checkUnresolvedLeafReference(SystemScope scope, NameReference reference) {
		List<NameDefinition> possibleDefinitions = scope.findAllDefinitionsByName(reference.getName());
		if (possibleDefinitions.size() > 0) {
			Debug.println("\tHas not been resolved: " + reference.referenceToString(0, false));
//			for (NameDefinition definition : possibleDefinitions) {
//				Debug.println("\t\tMay match: " + definition.toFullString());
//			}
			return true;
		} else return false;
	}

	public static void printDefinitionToDebugger(NameDefinition definition) {
		Debug.println("\t\t" + definition.toString());
//		NameScope scope = definition.getScope();
//		if (scope.getScopeKind() == NameScopeKind.NSK_LOCAL) {
//			LocalScope localScope = (LocalScope)scope;
//			Debug.println("\t\t\tThis definition in scope: " + scope.getScopeName() + "[" + localScope.getScopeStart() + ", " + localScope.getScopeEnd() + "]");
//		} else Debug.println("\t\t\tThis definition in scope: " + scope.getScopeName());
	}

	public static void printUnresolvedReferenceToDebugger(NameReference reference) {
		String[] needNotPrintReferences = {"null", "length", "equals"};
		
		if (reference.isGroupReference()) {
			List<NameReference> referencesInGroup = ((NameReferenceGroup)reference).getReferencesAtLeaf();
			if (referencesInGroup == null) {
				System.out.println("Reference group " + reference.getName() + " has not leaf reference!");
				return;
			}
			for (NameReference ref : referencesInGroup) {
				for (int i = 0; i < needNotPrintReferences.length; i++) {
					if (ref.getName().equals(needNotPrintReferences[i])) return;
				}

				if (!ref.isResolved()) {
					Debug.println("\t\t" + ref.referenceToString(0, false));
//					NameScope scope = ref.getScope();
//					if (scope.getScopeKind() == NameScopeKind.NSK_LOCAL) {
//						LocalScope localScope = (LocalScope)scope;
//						Debug.println("\t\t\tThis reference in scope: " + scope.getScopeName() + "[" + localScope.getScopeStart() + ", " + localScope.getScopeEnd() + "]");
//					} else Debug.println("\t\t\tThis reference in scope: " + scope.getScopeName());
					Debug.println("\t\t\t" + ref.bindedDefinitionToString());
					if (ref.isMethodReference()) {
						MethodReference methodRef = (MethodReference)ref;
						List<MethodDefinition> methodList = methodRef.getAlternatives();
						if (methodList != null && methodList.size() > 1) {
							for (MethodDefinition method : methodList) {
								Debug.println("\t\t\t\tAlternative method(s) include: " + method.toString());
							}
						}
					}
				}
			}
		} else if (!reference.isResolved()) {
			for (int i = 0; i < needNotPrintReferences.length; i++) {
				if (reference.getName().equals(needNotPrintReferences[i])) return;
			}
			
			Debug.println("\t\t" + reference.referenceToString(0, false));
			Debug.println("\t\t\t" + reference.bindedDefinitionToString());
		}
	}
	
	public static void printReferenceToDebugger(NameReference reference) {
		if (reference.isGroupReference()) {
			List<NameReference> referencesInGroup = ((NameReferenceGroup)reference).getReferencesAtLeaf();
			if (referencesInGroup == null) {
				System.out.println("Reference group " + reference.getName() + " has not leaf reference!");
				return;
			}
			for (NameReference ref : referencesInGroup) {
				if (!ref.isTypeReference() && !ref.isLiteralReference()) {
					Debug.println("\t\t" + ref.referenceToString(0, false));
//					NameScope scope = ref.getScope();
//					if (scope.getScopeKind() == NameScopeKind.NSK_LOCAL) {
//						LocalScope localScope = (LocalScope)scope;
//						Debug.println("\t\t\tThis reference in scope: " + scope.getScopeName() + "[" + localScope.getScopeStart() + ", " + localScope.getScopeEnd() + "]");
//					} else Debug.println("\t\t\tThis reference in scope: " + scope.getScopeName());
					Debug.println("\t\t\t" + ref.bindedDefinitionToString());
					if (ref.isMethodReference()) {
						MethodReference methodRef = (MethodReference)ref;
						List<MethodDefinition> methodList = methodRef.getAlternatives();
						if (methodList != null && methodList.size() > 1) {
							for (MethodDefinition method : methodList) {
								Debug.println("\t\t\t\tAlternative method(s) include: " + method.toString());
							}
						}
					}
				}
			}
		} else if (!reference.isTypeReference() && !reference.isLiteralReference()) {
			Debug.println("\t\t" + reference.referenceToString(0, false));
			Debug.println("\t\t\t" + reference.bindedDefinitionToString());
		}
	}

	
	public static void writeAllInfos(NameTableManager manager, PrintWriter writer) {
		manager.printDefinitions(writer);
		manager.printReferences(writer);
	}
	
	public static void checkDefinitions(List<NameDefinition> unitList, List<NameDefinition> subList) {
		if (subList == null) return;
		
		for (NameDefinition definition : subList) {
			boolean found = false;
			for (NameDefinition defInList : unitList) {
				if (defInList == definition) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find definition " + definition.toString() + " in the list!");
			}
		}
	}

	public static void checkReferences(List<NameReference> unitList, List<NameReference> subList) {
		if (subList == null) return;
		
		for (NameReference reference : subList) {
			boolean found = false;
			for (NameReference refInList : unitList) {
				if (refInList.equals(reference)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				Debug.println("Can not find reference " + reference.toString() + " in the list!");
			}
		}
	}

	public static void checkReferences(List<NameReference> unitList, NameReference reference) {
		if (reference == null) return;
		if (reference.isLiteralReference()) return;
		
		boolean found = false;
		for (NameReference refInList : unitList) {
			if (refInList.equals(reference)) {
				found = true;
				break;
			}
		}
		if (found == false) {
			Debug.println("Can not find reference " + reference.toString() + " in the list!");
		}
	}

	public static void printReferenceList(String unitFullName, List<NameReference> list, PrintWriter writer) {
		writer.println("In compilation unit file: " + unitFullName);
		
		for (NameReference reference : list) writer.println(reference);
		writer.flush();
	}
}

class SpecificDetailedTypeFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isDetailedType()) return false;
//		return true;
		if (definition.getSimpleName().equals("NameTableCreator")) return true;
//		if (definition.getSimpleName().equals("Axis") || definition.getSimpleName().equals("JFreeChart") || definition.getSimpleName().equals("Plot")) return true;
		else return false;
	}

}

