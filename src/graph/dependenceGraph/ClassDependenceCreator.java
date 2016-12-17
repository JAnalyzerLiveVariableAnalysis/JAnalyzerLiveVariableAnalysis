package graph.dependenceGraph;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.CompilationUnitScope;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/4
 * @version 1.0
 */
public class ClassDependenceCreator {
	private SourceCodeFileSet parser = null;
	private NameTableManager nameTableManager = null;
	
	private CompilationUnit currentASTRoot = null;
	private String currentUnitFileName = null;
	private DetailedTypeDefinition currentClass = null;
	private ClassDependenceNode currentClassNode = null;
	private ClassDependenceGraph currentCDG = null;
	
	private CFGCreator cfgCreator = null;
	
	/**
	 * Note: for creating the class dependence graph, the name definition table should have been created  
	 */
	public ClassDependenceCreator(SourceCodeFileSet parser, NameTableManager nameTableManager) {
		this.parser = parser;
		this.nameTableManager = nameTableManager;
	}

	public ClassDependenceGraph create(String[] packageNames) {
		List<PackageDefinition> packages = new ArrayList<PackageDefinition>();
		for (int i = 0; i < packageNames.length; i++) {
			PackageDefinition packageDef = nameTableManager.findPackageByName(packageNames[i]);
			if (packageDef != null) {
				System.out.println("Add package: " + packageDef.getSimpleName());
				packages.add(packageDef);
			} else System.out.println("Do not find package for name " + packageNames[i]);
		}
		return create(packages, true);
	}

	public ClassDependenceGraph create(String packageName) {
		List<PackageDefinition> packages = new ArrayList<PackageDefinition>();
		PackageDefinition packageDef = nameTableManager.findPackageByName(packageName);
		if (packageDef != null) packages.add(packageDef);
		return create(packages, true);
	}

	public ClassDependenceGraph create(List<PackageDefinition> packages) {
		return create(packages, true);
	}
	
	public ClassDependenceGraph create(boolean recreate) {
		return create(null, recreate);
	}
	
	public ClassDependenceGraph create() {
		return create(null, false);
	}
	
	/**
	 * Note: for creating the class dependence graph, the name definition table should have been created  
	 */
	ClassDependenceGraph create(List<PackageDefinition> packages, boolean recreate) {
		if (currentCDG != null && !recreate) return currentCDG;		// we have create the class dependence graph. we do not need to create it twice!

		List<CompilationUnitScope> units = new ArrayList<CompilationUnitScope>();
		if (packages == null) units = nameTableManager.getAllCompilationUnitScopes();
		else {
			for (PackageDefinition packageDef : packages) {
				units.addAll(packageDef.getCompilationUnitScopeList());
			}
		}
		if (units == null || units.size() <= 0) return null;

		String systemPath = nameTableManager.getSystemPath();
		String CDGName = systemPath;
		if (packages != null) {
			if (packages.size() == 1) CDGName = packages.get(0).getFullQualifiedName();
		}
		currentCDG = new ClassDependenceGraph(CDGName);
		
		// At first, we create all node for all detailed types
		createAllNodes();
		
		for (CompilationUnitScope unit : units) {
			currentUnitFileName = unit.getUnitName();

//			if (!unitFileName.contains("AnyImplHelper")) continue;
			System.out.println("Scan file " + currentUnitFileName);

			currentASTRoot = parser.findSourceCodeFileASTRootByFileUnitName(currentUnitFileName);
			if (currentASTRoot == null) throw new AssertionError("Can not find the compilation unit for the file: " + (systemPath + currentUnitFileName));
			
			cfgCreator = new CFGCreator(currentUnitFileName, currentASTRoot);
			
			@SuppressWarnings("unchecked")
			List<AbstractTypeDeclaration> typeList = currentASTRoot.types();
			for (AbstractTypeDeclaration type : typeList) {
				if (type.getNodeType() != ASTNode.TYPE_DECLARATION) continue;
				
				TypeDeclaration classDeclaration = (TypeDeclaration)type;
				if (classDeclaration.isInterface()) continue;
				if (!classDeclaration.isPackageMemberTypeDeclaration()) continue;
				
				NameTableASTBridge bridge = new NameTableASTBridge(nameTableManager);
				currentClass = bridge.findDefinitionForTypeDeclaration(currentUnitFileName, classDeclaration);
				if (currentClass == null) throw new AssertionError("Internal error: can not find detailed type definition for " + classDeclaration.getName().getFullyQualifiedName());
				currentClassNode = currentCDG.findNodeByDefinition(currentClass);
				if (currentClassNode == null) throw new AssertionError("Internal error: can not find dependence node for " + currentClass.toFullString());

				findDependencesInCurrentClass(classDeclaration);
			}
			
			// Set those possibly pointed to AST node to null for release memory of AST by gc.
			cfgCreator = null;
			typeList = null;
			currentASTRoot = null;
			parser.releaseFileContent(currentUnitFileName);
			parser.releaseAST(currentUnitFileName);
		}
		return currentCDG;
	}
	
	void createAllNodes() {
		List<DetailedTypeDefinition> types = nameTableManager.getSystemScope().getAllDetailedTypeDefinitions();
		
		for (DetailedTypeDefinition type : types) {
			currentCDG.addNode(new ClassDependenceNode(type));
		}
	}

	/**
	 * <p>Find possible dependence relations in the current class. The class is dependent on its super class and super interfaces, and 
	 * on all detailed type definitions which are bind to any references occurs in the type declaration. 
	 * 
	 * @pre-condition 
	 * 		<p>Have set currentClass to the detailed type definition corresponding to the given classDelcaration
	 * 		<p>Have set currentClassNode to the dependence graph node corresponding to the given classDeclaration
	 * 		<p>Have set currentASTNode to the root of the AST include the given classDeclaration
	 * 		<p>Have set currentUnitFileName to the unit file name declared the given classDeclaration
	 * 		<p>Have create cfgCreator using currentASTNode and currentUnitFileName for create CFG for the methods defined in the given classDeclaration
	 */
	void findDependencesInCurrentClass(TypeDeclaration classDeclaration) {
		// Find possible dependence relations in the super class and super interfaces, this class is dependent on its super class and 
		// super interfaces!
		List<TypeReference> superTypeList = currentClass.getSuperList();
		if (superTypeList != null) {
			for (TypeReference superTypeRef : superTypeList) {
				if (superTypeRef.resolveBinding()) addPossibleDependenceInReference(superTypeRef);
			}
		}
		
		FieldDeclaration[] fields = classDeclaration.getFields();
		for (int i = 0; i < fields.length; i++) findDependencesInField(fields[i]);

		MethodDeclaration[] methods = classDeclaration.getMethods();
		for (int i = 0; i < methods.length; i++) findDependencesInMethod(methods[i]);
	}
	
	/**
	 * <p>Find possible dependence relations in a method declaration AST node. The current class is dependent on all detailed type 
	 * definitions which are binded to any type reference occurs in the method (include its return type, parameters and method body)!  
	 */
	void findDependencesInMethod(MethodDeclaration methodDeclaration) {
		NameTableASTBridge bridge = new NameTableASTBridge(nameTableManager);
		SourceCodeLocation methodLocation = SourceCodeLocation.getStartLocation(methodDeclaration, currentASTRoot, currentUnitFileName);
		MethodDefinition methodDef = bridge.findDefinitionForMethodDeclaration(currentClass, methodLocation, methodDeclaration);
		if (methodDef == null) throw new AssertionError("Internal error: can not find method definition for a method declaration " + methodDeclaration.toString());

		// Find possible dependence relation in return type of the method
		TypeReference returnTypeRef = methodDef.getReturnType();
		if (returnTypeRef != null) {
			if (returnTypeRef.resolveBinding()) addPossibleDependenceInReference(returnTypeRef); 
		}
		
		// Find possible dependence relations in parameters
		List<VariableDefinition> paraList = methodDef.getParameterList();
		if (paraList != null) {
			for (VariableDefinition parameter : paraList) {
				TypeReference paraTypeRef = parameter.getType();
				if (paraTypeRef != null) {
					if (paraTypeRef.resolveBinding()) addPossibleDependenceInReference(paraTypeRef); 
				}
			}
		}
		
		// Find possible dependence relations in the body of the method. For this, we check all execution points of the CFG of the method body.
		ControlFlowGraph cfg = cfgCreator.create(methodDeclaration, currentClass.getSimpleName());
		if (cfg == null) return;
		
		List<GraphNode> nodes = cfg.getAllNodes();
		if (nodes != null) {
			for (GraphNode node : nodes) {
				ExecutionPoint point = (ExecutionPoint)node;
				if (point.isVirtual()) continue;
				
				// Find possible dependence relations in local variable declaration in an execution point
				List<NameDefinition> definitions = bridge.getAllDefinitionsInASTNode(currentUnitFileName, point.getAstNode());
				if (definitions != null) {
					for (NameDefinition definition : definitions) {
						if (definition.isVariableDefinition()) {
							VariableDefinition varDef = (VariableDefinition)definition;
							TypeReference varTypeRef = varDef.getType();
							if (varTypeRef.resolveBinding()) addPossibleDependenceInReference(varTypeRef); 
						}
					}
				}
				
				// Find possible dependence relations in the reference of an execution point, which may be a reference group.
				NameReferenceCreator referenceCreator = new NameReferenceCreator(nameTableManager);
				List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(currentUnitFileName, point.getAstNode());
				for (NameReference reference : referenceList) {
					if (reference.resolveBinding()) addPossibleDependenceInReference(reference);
				}
			}
		}
	}
		
	
	/**
	 * <p>Find possible dependence relations in a field declaration AST node. The current class is dependent on the type of the field 
	 * declaration, and on all result types of the initialize expressions (and its sub-expressions) of the fields defined in this declaration.
	 * <p>Note a field declaration may define many fields. 
	 */
	void findDependencesInField(FieldDeclaration field) {
		NameTableASTBridge bridge = new NameTableASTBridge(nameTableManager);
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(field, currentASTRoot, currentUnitFileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(field, currentASTRoot, currentUnitFileName);
		List<FieldDefinition> fieldsInType = bridge.findDefinitionsForFieldDeclaration(currentClass, startLocation, endLocation, field);
		if (fieldsInType == null) return;
		// Find possible dependence relationship from the type of the fields, the current class is dependent on the type of the fields!
		for (FieldDefinition fieldDef : fieldsInType) {
			TypeReference fieldTypeRef = fieldDef.getType();
			if (fieldTypeRef.resolveBinding()) addPossibleDependenceInReference(fieldTypeRef);
		}
		
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> variables = field.fragments();
		// Find possible dependence relationship from the initialize expressions of the fields
		for (VariableDeclarationFragment variable : variables) {
			Expression initExp = variable.getInitializer();
			if (initExp != null) {
				NameReferenceCreator referenceCreator = new NameReferenceCreator(nameTableManager);
				NameReference reference = referenceCreator.createReferenceForExpressionASTNode(currentUnitFileName, initExp);
				if (reference != null) findDependencesInReferences(reference);
			}
		}
	}
	
	/**
	 * <p>Find possible dependence relations in a reference.
	 *  
	 * <p>Note that a resolved group reference records the type of the result of its sub-expression, so we need not to check its type
	 * when the reference is a method invocation, a field or a variable. We just check all detailed type definitions binded to any 
	 * reference in the group! 
	 */
	void findDependencesInReferences(NameReference reference) {
		if (!reference.isResolved()) return;
		
		if (reference.isGroupReference()) {
			NameReferenceGroup refGroup = (NameReferenceGroup)reference;
			List<NameReference> subreferences = refGroup.getSubReferenceList();
			if (subreferences == null) return;
			for (NameReference subref : subreferences) findDependencesInReferences(subref);
		}
	}
	
	/**
	 * If the reference is bind to a detailed type definition, then add an edge from the current class node to the node corresponding
	 * to this detailed type definition
	 * @pre-condition: the reference must have been resolved!
	 */
	void addPossibleDependenceInReference(NameReference reference) {
		NameDefinition nameDef = reference.getDefinition();
		if (nameDef.isDetailedType()) {
			DetailedTypeDefinition detailedType = (DetailedTypeDefinition)nameDef;
			
			ClassDependenceNode node = currentCDG.findNodeByDefinition(detailedType);
			if (node == null) throw new AssertionError("Internal error: can not find CDG node for " + detailedType.toFullString());
			
			currentCDG.addEdge(new ClassDependenceEdge(currentClassNode, node, reference));
		}
	}
}


