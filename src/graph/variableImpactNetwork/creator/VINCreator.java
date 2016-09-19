package graph.variableImpactNetwork.creator;

import graph.variableImpactNetwork.FieldImpactNode;
import graph.variableImpactNetwork.ReturnValueImpactNode;
import graph.variableImpactNetwork.VariableImpactGraph;
import graph.variableImpactNetwork.VariableImpactNode;

import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import util.SourceCodeLocation;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/1
 * @version 1.0
 */
public class VINCreator {

	protected NameTableManager table = null;
	protected String systemPath = null;
	protected SourceCodeParser parser = null;
	
	protected String currentSourceFileName = null;
	protected CompilationUnit currentASTRoot = null;
	
	protected StatementVINCreator statementVINCreator = null;
	
	public VINCreator(SourceCodeParser parser, NameTableManager table) {
		this.parser = parser;
		this.table = table;
		systemPath = parser.getSystemPath();
		statementVINCreator = new StatementVINCreator(this.table);
	}
	
	/**
	 * Create the variable impact network for given system in the parser
	 */
	public VariableImpactGraph create(String graphName) {
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		
		parser.toGetFirstParsedFile();
		while (parser.hasParsedFileInfo()) {
			currentASTRoot = parser.getCurrentCompilationUnit();
			if (currentASTRoot != null) {
				currentSourceFileName = parser.getCurrentUnitFullName();
				
//				System.out.println("Creating for file: " + currentSourceFileName);
				scanCurrentCompilationUnit(network);
				
				parser.releaseCurrentCompilatinUnits();
				parser.releaseCurrentFileContents();
			}
			parser.toGetNextParsedFile();
		}
		
		network.normalize();
		return network;
	}
	
	
	public VariableImpactGraph create(String graphName, PackageDefinition definition) {
		List<CompilationUnitScope> unitList = definition.getCompilationUnitList();
		if (unitList == null) return null;
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		for (CompilationUnitScope unit : unitList) {
			String fileFullName = unit.getUnitFullName();
			currentASTRoot = parser.findCompilationUnitByUnitFullName(fileFullName);
			if (currentASTRoot == null) throw new AssertionError("Can not find file " + fileFullName + " in the give parser!");
			currentSourceFileName = parser.getCurrentUnitFullName();
			
			scanCurrentCompilationUnit(network);
			parser.releaseCurrentCompilatinUnits();
			parser.releaseCurrentFileContents();
		}
		
		network.normalize();
		return network;
	}

	public VariableImpactGraph create(String graphName, List<PackageDefinition> packageList) {
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		for (PackageDefinition definition : packageList) {
			List<CompilationUnitScope> unitList = definition.getCompilationUnitList();
			if (unitList == null) continue;
			
			for (CompilationUnitScope unit : unitList) {
				String fileFullName = unit.getUnitFullName();
				currentASTRoot = parser.findCompilationUnitByUnitFullName(fileFullName);
				if (currentASTRoot == null) throw new AssertionError("Can not find file " + fileFullName + " in the give parser!");
				currentSourceFileName = parser.getCurrentUnitFullName();
				
				scanCurrentCompilationUnit(network);
				parser.releaseCurrentCompilatinUnits();
				parser.releaseCurrentFileContents();
			}
		}
		
		network.normalize();
		return network;
	}
	
	public VariableImpactGraph create(String graphName, String[] packages) {
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		
		for (int index = 0; index < packages.length; index++) {
			PackageDefinition definition = table.findPackageByName(packages[index]);
			if (definition == null) continue;
			
			List<CompilationUnitScope> unitList = definition.getCompilationUnitList();
			if (unitList == null) return null;
			
			for (CompilationUnitScope unit : unitList) {
				String fileFullName = unit.getUnitFullName();
				currentASTRoot = parser.findCompilationUnitByUnitFullName(fileFullName);
				if (currentASTRoot == null) throw new AssertionError("Can not find file " + fileFullName + " in the give parser!");
				currentSourceFileName = parser.getCurrentUnitFullName();
				
				scanCurrentCompilationUnit(network);
				parser.releaseCurrentCompilatinUnits();
				parser.releaseCurrentFileContents();
			}
		}
		
		network.normalize();
		return network;
	}
	
	public VariableImpactGraph create(String graphName, CompilationUnitScope unit) {
		String unitFileFullName = unit.getUnitFullName();
		
		currentASTRoot = parser.findCompilationUnitByUnitFullName(unitFileFullName);
		if (currentASTRoot == null) return null;
		currentSourceFileName = parser.getCurrentUnitFullName();
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		
		scanCurrentCompilationUnit(network);
		parser.releaseCurrentCompilatinUnits();
		parser.releaseCurrentFileContents();
		
		network.normalize();
		return network;
	}
	
	public VariableImpactGraph create(String graphName, DetailedTypeDefinition type) {
		NameScope parentScope = type.getEnclosingScope();
		while (parentScope.getScopeKind() != NameScopeKind.NSK_COMPILATION_UNIT) parentScope = parentScope.getEnclosingScope();
		if (parentScope.getScopeKind() != NameScopeKind.NSK_COMPILATION_UNIT) {
			throw new AssertionError("Can not find unit scope for type " + type.getFullQualifiedName());
		}
		
		CompilationUnitScope unit = (CompilationUnitScope)parentScope;
		String unitFileFullName = unit.getUnitFullName();
		
		currentASTRoot = parser.findCompilationUnitByUnitFullName(unitFileFullName);
		if (currentASTRoot == null) throw new AssertionError("Can not find unit " + unitFileFullName + " in the given parser!");
		currentSourceFileName = parser.getCurrentUnitFullName();
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		
		scan(network, type);
		parser.releaseCurrentCompilatinUnits();
		parser.releaseCurrentFileContents();
		
		network.normalize();
		return network;
	}

	public VariableImpactGraph create(String graphName, String unitFileFullName) {
		currentASTRoot = parser.findCompilationUnitByUnitFullName(unitFileFullName);
		if (currentASTRoot == null) {
			System.out.println("Can not find AST root for " + unitFileFullName);
		}
		if (currentASTRoot == null) return null;
		currentSourceFileName = parser.getCurrentUnitFullName();
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		
		scanCurrentCompilationUnit(network);
		parser.releaseCurrentCompilatinUnits();
		parser.releaseCurrentFileContents();
		
		network.normalize();
		return network;
	}
	
	public VariableImpactGraph create(String graphName, String unitFileFullName, String className) {
		currentASTRoot = parser.findCompilationUnitByUnitFullName(unitFileFullName);
		if (currentASTRoot == null) return null;
		currentSourceFileName = parser.getCurrentUnitFullName();
		
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> typeList = currentASTRoot.types();
		TypeDeclaration typeDeclaration = null;
		for (AbstractTypeDeclaration type : typeList) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION && type.getName().getIdentifier().equals(className)) {
				typeDeclaration = (TypeDeclaration)type;
				break;
			}
		}
		
		if (typeDeclaration == null) return null;
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		scan(network, typeDeclaration);
		
		parser.releaseCurrentCompilatinUnits();
		parser.releaseCurrentFileContents();
		
		network.normalize();
		return network;
	}

	
	public VariableImpactGraph create(String graphName, String unitFileFullName, String className, String methodName) {
		currentASTRoot = parser.findCompilationUnitByUnitFullName(unitFileFullName);
		if (currentASTRoot == null) return null;
		currentSourceFileName = parser.getCurrentUnitFullName();
		
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> typeList = currentASTRoot.types();
		TypeDeclaration typeDeclaration = null;
		for (AbstractTypeDeclaration type : typeList) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION && type.getName().getIdentifier().equals(className)) {
				typeDeclaration = (TypeDeclaration)type;
				break;
			}
		}
		
		if (typeDeclaration == null) return null;
		DetailedTypeDefinition typeDefinition = table.findDetailedTypeDefinitionByDeclaration(typeDeclaration, currentASTRoot, currentSourceFileName);
		if (typeDefinition == null) {
			throw new AssertionError("Can not find type definition for type declaration " + typeDeclaration.getName().getIdentifier());
		}
		MethodDeclaration methodDeclaration = null;
		MethodDeclaration[] methodArray = typeDeclaration.getMethods();
		for (int index = 0; index < methodArray.length; index++) {
			if (methodArray[index].getName().getIdentifier().equals(methodName)) {
				methodDeclaration = methodArray[index];
				break;
			}
		}
		if (methodDeclaration == null) return null;
		
		VariableImpactGraph network = new VariableImpactGraph(graphName);
		scan(network, typeDefinition, methodDeclaration);
		
		parser.releaseCurrentCompilatinUnits();
		parser.releaseCurrentFileContents();
		
		network.normalize();
		return network;
	}

	/**
	 * Scan current compilation unit to create variable impact network
     * @pre-condition: currentSourceFileName ! = null &&  currentASTRoot != null
	 */
	@SuppressWarnings("unchecked")
	void scanCurrentCompilationUnit(VariableImpactGraph network) {
		CompilationUnit node = currentASTRoot; 
		
		// Process the type declarations in the node
		List<AbstractTypeDeclaration> types = node.types();
		for (AbstractTypeDeclaration type : types) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				scan(network, (TypeDeclaration)type);
			}
		}
	}

	
	/**
	 * Scan a type declaration node to create variable impact network
     *  BodyDeclaration: FieldDelcaration MethodDeclaration TypeDeclaration           
	 */
	void scan(VariableImpactGraph network, TypeDeclaration node) {
		DetailedTypeDefinition typeDefinition = table.findDetailedTypeDefinitionByDeclaration(node, currentASTRoot, currentSourceFileName);
		System.out.println("Scan type " + typeDefinition.getFullQualifiedName());
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) scan(network, typeDefinition, fields[index]);
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) scan(network, typeDefinition, methods[index]);
		
		// Process the type declarations in the node
		TypeDeclaration[] typeMembers = node.getTypes();
		for (int index = 0; index < typeMembers.length; index++) scan(network, typeMembers[index]);
		
	}
	
	/**
	 * Scan a type definition node to create variable impact network
     *  BodyDeclaration: FieldDelcaration MethodDeclaration TypeDeclaration           
	 */
	void scan(VariableImpactGraph network, DetailedTypeDefinition typeDefinition) {
		System.out.println("Scan type " + typeDefinition.getFullQualifiedName());

		TypeDeclaration node = table.findDeclarationInAST(currentSourceFileName, currentASTRoot, typeDefinition);
		if (node == null) {
			throw new AssertionError("Can not find delcaration in AST for type definition " + typeDefinition.toFullString());
		}
		
		scan(network, node);
	}
	

	/**
	 * Scan a field declaration node to create variable impact network
	 * FieldDelcaration: [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
     *                       { , VariableDeclarationFragment } ;
	 */
	@SuppressWarnings("unchecked")
	void scan(VariableImpactGraph network, DetailedTypeDefinition typeDefinition, FieldDeclaration node) {
//		System.out.println("Scan field " + node.toString());
		
		List<FieldDefinition> fieldList = typeDefinition.getFieldList();
		// Visit the variable list defined in the node, and create nodes for the network. If the field has an initialize expression, 
		// find the references in the expression, and bind them to the corresponding definition, and create edges for the network.
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Find the field definition corresponding the field declaration
			String varName = varNode.getName().getIdentifier();
			FieldDefinition field = null;
			for (FieldDefinition fieldInType : fieldList) {
				if (fieldInType.getSimpleName().equals(varName)) {
					field = fieldInType;
					break;
				}
			}
			if (field == null) throw new AssertionError("Internal error, can not find definition for field declaration: " + varName);
			FieldImpactNode fiNode = network.createAndAddNodeForDefinition(typeDefinition, field);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				NameReference initExpRef = table.createReferenceForASTNode(initializer, currentASTRoot, currentSourceFileName);
				if (initExpRef != null) {
					initExpRef.resolveBinding();
					List<VariableImpactNode> startNodeList = ReferenceVINCreator.findImpactsInExpressionReference(network, initExpRef, false);
					for (VariableImpactNode startNode : startNodeList) {
						network.checkAndAddAssignImpact(startNode, fiNode);
					}
				}
			}
		}
	}
	
	void scan(VariableImpactGraph network, DetailedTypeDefinition typeDefinition, MethodDeclaration node) {
//		System.out.println("Scan method " + node.getName().getIdentifier());

		List<MethodDefinition> methodList = typeDefinition.getMethodList();
		MethodDefinition methodDefinition = null;
		for (MethodDefinition definition : methodList) {
			String methodSimpleName = node.getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentASTRoot, currentSourceFileName);
			if (definition.getSimpleName().equals(methodSimpleName) && definition.getLocation().equals(location)) {
				methodDefinition = definition;
				break;
			}
		}
		if (methodDefinition == null) throw new AssertionError("Can not find definition for method declaration: " + node.toString());
		
		// Create return value node for the method
		ReturnValueImpactNode returnValueNode = network.createAndAddNodeForDefinition(methodDefinition);
		
		// Create node for parameters of the nodes
		List<VariableDefinition> parameters = methodDefinition.getParameters();
		if (parameters != null) {
			for (VariableDefinition parameter : parameters) {
				network.createAndAddNodeForDefinition(parameter);
			}
		}
		
		// Scan the body of the method
		Block body = node.getBody();
		if (body != null) {
			statementVINCreator.setCurrentCompilationUnit(currentASTRoot, currentSourceFileName);
			statementVINCreator.setCurrentNetwork(network);
			statementVINCreator.setCurrentMethod(methodDefinition, returnValueNode);
			List<VariableImpactNode> conditionExpressionNodeList = new ArrayList<VariableImpactNode>();  
			statementVINCreator.findImpactsInStatement(body, conditionExpressionNodeList);
			
			List<TypeDeclaration> localTypeList = statementVINCreator.getLocalTypeDeclarationList();
			if (localTypeList != null) {
				for (TypeDeclaration localType : localTypeList) scan(network, localType);
			}
		}
	}

	void scan(VariableImpactGraph network, DetailedTypeDefinition typeDefinition, MethodDefinition methodDefinition) {
		MethodDeclaration node = table.findDeclarationInAST(currentSourceFileName, currentASTRoot, typeDefinition, methodDefinition);
		if (node == null) {
			throw new AssertionError("Can not find declaration in AST for method definition " + methodDefinition.toFullString());
		}
		
		scan(network, typeDefinition, node);
	}

}
