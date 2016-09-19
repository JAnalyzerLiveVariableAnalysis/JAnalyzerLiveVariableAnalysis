package nameTable;

import graph.cfg.ExecutionPoint;

import java.io.PrintWriter;
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

import util.SourceCodeLocation;
import util.SourceCodeParser;
import nameTable.creator.NameReferenceCreator;
import nameTable.creator.ReferenceASTVisitor;
import nameTable.creator.TypeDeclarationVisitor;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;

/**
 * <p>The class to manage the name table. This class provides methods to access name definitions and name references.
 * We may extend the class to provide more methods to access name definitions and references quickly.
 * <p>Note that we distribute name definitions and name references to scopes, which may slow the access of name definitions
 * and references.
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-30 Zhou Xiaocong
 * 		Add methods for getting definitions, references or creating references for an AST node! 
 * @update 2014-1-1 Zhou Xiaocong
 * 		Add method int getTotalNumaberOfDefinitions(NameDefinitionKind kind) to do some statistics for definition
 * 		Add method int getTotalNumberOfCompilationUnits()
 * @update 2015-6-24 Zhou Xiaocong
 * 		Add some methods, refer to the notes for detailed information.
 */
public class NameTableManager {
	private SourceCodeParser parser = null;
	private String systemPath = null;
	private SystemScope rootScope = null;


	public NameTableManager(SourceCodeParser parser) {
		this.parser = parser;
		this.systemPath = parser.getSystemPath();
	}
	
	/**
	 * @return the rootScope
	 */
	public SystemScope getRootScope() {
		return rootScope;
	}

	/**
	 * @return the rootScope
	 */
	public SourceCodeParser getSouceCodeParser() {
		return parser;
	}
	
	/**
	 * Get all package definitions in the system
	 */
	public List<PackageDefinition> getAllPackageDefinitions() {
		return rootScope.getPackages();
	}
	
	/**
	 * Find the package by a package name
	 */
	public PackageDefinition findPackageByName(String packageName) {
		return rootScope.findPackageByName(packageName);
	}

	/**
	 * Get all compilation unit scopes in the system
	 */
	public List<CompilationUnitScope> getAllCompilationUnitScopes() {
		List<PackageDefinition> packageList = rootScope.getPackages();
		if (packageList == null) return null;
		
		List<CompilationUnitScope> result = new ArrayList<CompilationUnitScope>();
		for (PackageDefinition packageDef : packageList) {
			List<CompilationUnitScope> units = packageDef.getCompilationUnitList();
			for (CompilationUnitScope unit: units) result.add(unit);
		}
		return result;
	}
	
	/**
	 * Get all compilation unit scopes in a package
	 */
	public List<CompilationUnitScope> getCompilationUnitScopesInPackage(String packageName) {
		PackageDefinition packageDef = rootScope.findPackageByName(packageName);
		if (packageDef == null) return null;
		
		return packageDef.getCompilationUnitList();
	}
	
	/**
	 * Find all compilation unit scopes in the system by a file name. 
	 * Possibly the system have the many compilation units with the same file name
	 */
	public List<CompilationUnitScope> findCompilationUnitScopesByName(String fileName) {
		List<PackageDefinition> packageList = rootScope.getPackages();
		if (packageList == null) return null;
		
		List<CompilationUnitScope> result = new ArrayList<CompilationUnitScope>();
		for (PackageDefinition packageDef : packageList) {
			List<CompilationUnitScope> units = packageDef.getCompilationUnitList();
			for (CompilationUnitScope unit: units) 
				if (unit.getUnitFullName().endsWith(fileName)) result.add(unit);
		}
		return result;
	}

	/**
	 * Find a compilation unit scopes in the system by a full unit name (= parser.getCurrentUnitFullName()) 
	 */
	public CompilationUnitScope findCompilationUnitScopeByFullName(String fullUnitName) {
		List<PackageDefinition> packageList = rootScope.getPackages();
		if (packageList == null) return null;
		
		for (PackageDefinition packageDef : packageList) {
			List<CompilationUnitScope> units = packageDef.getCompilationUnitList();
			for (CompilationUnitScope unit: units) 
				if (unit.getUnitFullName().equals(fullUnitName)) return unit;
		}
		return null;
	}
	
	/**
	 * Find a name definition by its unqiue Id
	 */
	public NameDefinition findDefinitionById(String nameDefinitionId) {
		String locationString = NameDefinition.getDefinitionLocationStringFromId(nameDefinitionId);
		
		if (locationString != null) {
			String fullUnitName = SourceCodeLocation.getFullFileNameFromId(locationString);
			if (fullUnitName != null) {
				List<PackageDefinition> packageList = rootScope.getPackages();
				if (packageList == null) return null;
	
				for (PackageDefinition packageDef : packageList) {
					List<CompilationUnitScope> units = packageDef.getCompilationUnitList();
					for (CompilationUnitScope unit: units) {
						if (unit.getUnitFullName().equals(fullUnitName)) {
//							if (nameDefinitionId.contains("events@151:17")) {
//								System.out.println("before find in unit file "  + fullUnitName + " for " + nameDefinitionId);
//							}
							return unit.findDefinitionById(nameDefinitionId, true);
						}
					}
				}
			}
			if (nameDefinitionId.contains("events@151:17")) {
				System.out.println("Can not find in unit file "  + fullUnitName + " for " + nameDefinitionId);
			}
			return null;
		} else {
			return rootScope.findDefinitionById(nameDefinitionId, false);
		}
	}
	
	/**
	 * Get all definitions in an execution point
	 */
	public List<NameDefinition> getDefinitionsInExecutionPoint(ExecutionPoint point) {
		SourceCodeLocation start = point.getStartPosition();
		SourceCodeLocation end = point.getEndPosition();
		List<CompilationUnitScope> units = findCompilationUnitScopesByName(start.getFullFileName());
		if (units == null || units.size() <= 0) return null;
		CompilationUnitScope unit = units.get(0);
		if (unit == null) return null;
		return unit.findAllDefinitionsByPosition(start, end);
	}
	
	/**
	 * Get all references in an execution point
	 */
	public List<NameReference> getReferencesInExecutionPoint(ExecutionPoint point) {
		SourceCodeLocation start = point.getStartPosition();
		SourceCodeLocation end = point.getEndPosition();
		List<CompilationUnitScope> units = findCompilationUnitScopesByName(start.getFullFileName());
		if (units == null || units.size() <= 0) return null;
		CompilationUnitScope unit = units.get(0);
		if (unit == null) return null;
		return unit.findAllReferencesByPosition(start, end);
	}
	
	/**
	 * Create a reference (group) for an execution point and return the reference
	 */
	public NameReference createReferenceForExecutionPoint(ExecutionPoint point) {
		SourceCodeLocation start = point.getStartPosition();
		if (start == null) return null;

		NameScope scope = getNameScopeOfLocation(start);
		if (scope == null) return null;
		
		ASTNode node = point.getAstNode();
		if (node == null) return null;
		
		String unitFullName = start.getFullFileName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return null;
		
		ReferenceASTVisitor visitor = new ReferenceASTVisitor(unitFullName, root, scope);
		node.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Get all definitions in an AST node. For this, the caller should provide the root (a CompilationUnit node of the node) and the full 
	 * name of this compilation unit. The caller should use an object of class SourceCodeParser (unitFullName = SourceCodeParser.getCurrentUnitFullName()) to get such information! 
	 */
	public List<NameDefinition> getDefinitionsInASTNode(ASTNode node, CompilationUnit root, String unitFullName) {
		int position = node.getStartPosition();
		int lineNo = root.getLineNumber(position);
		int colNo = root.getColumnNumber(position);
		SourceCodeLocation start = new SourceCodeLocation(lineNo, colNo, unitFullName);
		
		position = position + node.getLength();
		lineNo = root.getLineNumber(position);
		colNo = root.getColumnNumber(position);
		SourceCodeLocation end = new SourceCodeLocation(lineNo, colNo, unitFullName);
		
		List<CompilationUnitScope> units = findCompilationUnitScopesByName(unitFullName);
		if (units == null || units.size() <= 0) return null;
		CompilationUnitScope unit = units.get(0);
		if (unit == null) return null;
		return unit.findAllDefinitionsByPosition(start, end);
	}

	/**
	 * Get all definitions in an AST node. For this, the caller only provide the full name of this compilation unit (= SourceCodeParser.getCurrentUnitFullName())  
	 * (the method find the AST root for the unit. The caller should use the same object of class SourceCodeParser to get such information! 
	 */
	public List<NameDefinition> getDefinitionsInASTNode(ASTNode node, String unitFullName) {
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		return getDefinitionsInASTNode(node, root, unitFullName);
	}
	
	/**
	 * Get all references in an AST node. For this, the caller should provide the root (a CompilationUnit node of the node) and the full 
	 * (source) file name of this compilation unit. The caller should use an object of class SourceCodeParser to get such information! 
	 */
	public List<NameReference> getReferencesInASTNode(ASTNode node, CompilationUnit root, String compilationUnitFullFileName) {
		int position = node.getStartPosition();
		int lineNo = root.getLineNumber(position);
		int colNo = root.getColumnNumber(position);
		SourceCodeLocation start = new SourceCodeLocation(lineNo, colNo, compilationUnitFullFileName);
		
		position = position + node.getLength();
		lineNo = root.getLineNumber(position);
		colNo = root.getColumnNumber(position);
		SourceCodeLocation end = new SourceCodeLocation(lineNo, colNo, compilationUnitFullFileName);
		
		List<CompilationUnitScope> units = findCompilationUnitScopesByName(compilationUnitFullFileName);
		if (units == null || units.size() <= 0) return null;
		CompilationUnitScope unit = units.get(0);
		if (unit == null) return null;
		return unit.findAllReferencesByPosition(start, end);
	}

	/**
	 * Get all references in an AST node. For this, the caller only provide the full (source) file name of this compilation unit 
	 * (the method find the root AST node for the unit). The caller should use the same object of class SourceCodeParser to get such information! 
	 */
	public List<NameReference> getReferencesInASTNode(ASTNode node, String compilationUnitFullFileName) {
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(compilationUnitFullFileName);
		return getReferencesInASTNode(node, root, compilationUnitFullFileName);
	}
	
	/**
	 * Create a reference (group) for an expression AST node and return the reference. For this, the caller should provide the root (a CompilationUnit node of 
	 * the node) and the full (source) file name of this compilation unit. The caller should use an object of class SourceCodeParser to get such information! 
	 */
	public NameReference createReferenceForASTNode(Expression node, CompilationUnit root, String unitFullName) {
		int position = node.getStartPosition();
		int lineNo = root.getLineNumber(position);
		int colNo = root.getColumnNumber(position);
		SourceCodeLocation start = new SourceCodeLocation(lineNo, colNo, unitFullName);

		NameScope scope = getNameScopeOfLocation(start);
		if (scope == null) return null;
		
		ReferenceASTVisitor visitor = new ReferenceASTVisitor(unitFullName, root, scope);
		
		node.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Create a reference (group) for an expression AST node and return the reference. For this, the caller only provide the full (source) 
	 * file name of this compilation unit (the method find the root AST Node of the unit). The caller should use the same object of class 
	 * SourceCodeParser to get such information! 
	 */
	public NameReference createReferenceForASTNode(Expression node, String compilationUnitFullFileName) {
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(compilationUnitFullFileName);
		return createReferenceForASTNode(node, root, compilationUnitFullFileName);
	}
	
	/**
	 * Get a name scope enclosing a source code location exactly, i.e. the location is in the scope, and 
	 * there is no other scope enclosing the location and is enclosed in the returned scope!
	 */
	public NameScope getNameScopeOfLocation(SourceCodeLocation location) {
		NameScope result = rootScope;
		List<NameScope> subscopes = result.getSubScopeList();
		while (subscopes != null) {
			boolean findScope = false;
			// Test if there is a sub-scope enclosing the given location
			for (NameScope scope : subscopes) {
				if (scope.containsLocation(location)) {
					findScope = true; 
					result = scope;
					
					subscopes = result.getSubScopeList();
					
					break;
				} else {
					// Check the next scope!
				}
			}
			if (!findScope) {
				// There is no sub-scope enclosing the given location. If the result is the system scope, we 
				// return null, because a real location for name reference or name definition can not be in 
				// the system scope!
				if (result == rootScope) return null;  
				else return result;
			} // else we continue to test if there is a sub-scope enclosing the given location
		}
		
		// The result scope enclosing the given location and it has not sub-scopes, so return it!
		return result;
	}
	

	/**
	 * Use a type declaration AST node to find the corresponding detailed type definition in the name table!
	 * We match its simple name and its location!
	 */
	public DetailedTypeDefinition findDetailedTypeDefinitionByDeclaration(TypeDeclaration type, CompilationUnit root, String compilationUnitFileName) {
		String declFullName = type.getName().getIdentifier();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, compilationUnitFileName);
		
		CompilationUnitScope unit = findCompilationUnitScopeByFullName(compilationUnitFileName);
		if (unit == null) return null;
		List<DetailedTypeDefinition> typeList = unit.getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition detailedType : typeList) {
//			System.out.println("Match detailed type: " + detailedType.getSimpleName() + "[" + detailedType.getLocation() + "] with " + " declaration " + declFullName + "[" + location + "]");
			
			if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) return detailedType;
		}
		return null;
	}
	
	/**
	 * Use a simple name and a location to find the corresponding detailed type definition in the name table!
	 */
	public DetailedTypeDefinition findDetailedTypeDefinitionByLocation(String simpleName, SourceCodeLocation location) {
		String compilationUnitFileName = location.getFullFileName();
		
		CompilationUnitScope unit = findCompilationUnitScopeByFullName(compilationUnitFileName);
		if (unit == null) return null;
		List<DetailedTypeDefinition> typeList = unit.getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition detailedType : typeList) {
//			System.out.println("Match detailed type: " + detailedType.getSimpleName() + "[" + detailedType.getLocation() + "] with " + " declaration " + declFullName + "[" + location + "]");
			
			if (detailedType.getSimpleName().equals(simpleName) && detailedType.getLocation().equals(location)) return detailedType;
		}
		return null;
	}
	
	/**
	 * Use a method declaration AST node in a detailed type definition to find the corresponding detailed type definition in the name table!
	 * We match its simple name and its location!
	 */
	public MethodDefinition findMethodDefinitionByDeclaration(MethodDeclaration method, DetailedTypeDefinition type, CompilationUnit root, String compilationUnitFileName) {
		List<MethodDefinition> methodList = type.getMethodList();
		for (MethodDefinition methodInType : methodList) {
			String methodSimpleName = method.getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(method, root, compilationUnitFileName);
			if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) return methodInType;
		}
		return null;
	}

	/**
	 * Use a field declaration AST node in a type definition to find the corresponding detailed type definition in the name table!
	 * We match its simple name and its location!
	 * Note that a field declaration may declare more than one field!
	 */
	public List<FieldDefinition> findFieldDefinitionsByDeclaration(FieldDeclaration field, DetailedTypeDefinition type, CompilationUnit root, String compilationUnitFileName) {
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();
		
		List<FieldDefinition> fieldList = type.getFieldList();
		SourceCodeLocation start = SourceCodeLocation.getStartLocation(field, root, compilationUnitFileName);
		SourceCodeLocation end = SourceCodeLocation.getEndLocation(field, root, compilationUnitFileName);
		
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = field.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			String varName = varNode.getName().getIdentifier();
			for (FieldDefinition fieldInType : fieldList) {
				if (fieldInType.getSimpleName().equals(varName) && fieldInType.getLocation().isBetween(start, end)) {
					result.add(fieldInType);
				}
			}
		}
		return result;
	}
	
	/**
	 * @param rootScope the rootScope to set
	 */
	public void setRootScope(SystemScope rootScope) {
		this.rootScope = rootScope;
	}
	
	public String getSystemPath() {
		return systemPath;
	}
	
	/**
	 * Calculate total number of definitions with given kind! 
	 * <p>If kind == NDK_UNKNOWN, calculate all kinds of definitions! 
	 * <p>If kind == NDK_PACKAGE, calculate all packages
	 * <p>If kind == NDK_TYPE, calculate all detailed types!
	 * <p>If kind == NDK_METHOD, calculate all methods declared in all classes!
	 * <p>If kind == NDK_FIELD, calculate all fields declared in all classes!
	 * <p>If kind == NDK_VARIABLE, calculate all (local) variables declared in all files!
	 * <p>If kind == NDK_PARAMETER, calculate all parameters declared in all methods!
	 */
	public int getTotalNumaberOfDefinitions(NameDefinitionKind kind) {
		return rootScope.getTotalNumberOfDefinitions(kind);
	}
	
	
	/**
	 * Get all compilation unit scopes in the system
	 */
	public int getTotalNumberOfCompilationUnits() {
		int result = 0;
		List<PackageDefinition> packageList = rootScope.getPackages();
		if (packageList == null) return 0;
		
		for (PackageDefinition packageDef : packageList) {
			result += packageDef.getCompilationUnitList().size();
		}
		return result;
	}

	/**
	 * Find a type declaration for a type definition. The caller provide the unit full name and AST root.   
	 */
	public TypeDeclaration findDeclarationInAST(String unitFullName, CompilationUnit root, DetailedTypeDefinition type) {
		if (!type.isPackageMember()) return findTypeDeclarationInAST(unitFullName, root, type);
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> typeDeclList = root.types(); 
		for (AbstractTypeDeclaration abstractType : typeDeclList) {
			if (abstractType.getNodeType() == ASTNode.TYPE_DECLARATION) {
				TypeDeclaration typeDecl = (TypeDeclaration)abstractType;
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(abstractType, root, unitFullName);
				String declName = typeDecl.getName().getIdentifier(); 
				if (declName.equals(type.getSimpleName()) && location.equals(type.getLocation())) {
					return typeDecl;
				}
			}
		}
		return null;
	}
	
	/**
	 * Use ASTVisitor to find type declaration in the compilation unit  
	 */
	protected TypeDeclaration findTypeDeclarationInAST(String unitFullName, CompilationUnit root, DetailedTypeDefinition type) {
		TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		
		root.accept(visitor);
		List<TypeDeclaration> resultList = visitor.getResultList();
		if (resultList == null) return null;
		for (TypeDeclaration resultType : resultList) {
			TypeDeclaration typeDecl = (TypeDeclaration)resultType;
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(resultType, root, unitFullName);
			String declName = typeDecl.getName().getIdentifier(); 
			if (declName.equals(type.getSimpleName()) && location.equals(type.getLocation())) {
				return typeDecl;
			}
		}
		return null;
	}
	
	
	/**
	 * Find a type declaration for a type definition.    
	 */
	public TypeDeclaration findDeclarationForDetailedType(DetailedTypeDefinition type) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(type);
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		
		return findDeclarationInAST(unitFullName, root, type);
	}
	
	/**
	 * Find a compilation unit (AST root) for a compilation unit scope.    
	 */
	public CompilationUnit findDeclarationForCompilatinoUnitScope(CompilationUnitScope unitScope) {
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		
		return root;
	}
	
	/**
	 * Find a method declaration for a type definition. The caller provide the unit full name and AST root.   
	 */
	public MethodDeclaration findDeclarationInAST(String unitFileName, CompilationUnit root, DetailedTypeDefinition type, MethodDefinition method) {
		TypeDeclaration typeDeclaration = findDeclarationInAST(unitFileName, root, type);
		if (typeDeclaration == null) return null;

		MethodDeclaration[] methodDeclArray = typeDeclaration.getMethods();
		for (int index = 0; index < methodDeclArray.length; index++) {
			MethodDeclaration methodDecl = methodDeclArray[index];
			String methodDeclName = methodDecl.getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(methodDecl, root, unitFileName);
			if (methodDeclName.equals(method.getSimpleName()) && location.equals(method.getLocation())) {
				return methodDecl; 
			}
		}
		return null;
	}
	
	/**
	 * Find a method declaration for a type definition.    
	 */
	public MethodDeclaration findDeclarationForMethodDefinition(MethodDefinition method) {
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(method);
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(type);
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		
		return findDeclarationInAST(unitFullName, root, type, method);
	}

	/**
	 * Find a field declaration for a type definition. The caller provide the unit full name and AST root.
	 * Note that a field declaration may declare many fields at the same time.     
	 */
	public FieldDeclaration findDeclarationInAST(String unitFileName, CompilationUnit root, DetailedTypeDefinition type, FieldDefinition field) {
		TypeDeclaration typeDeclaration = findDeclarationInAST(unitFileName, root, type);
		if (typeDeclaration == null) return null;
		FieldDeclaration[] fieldDeclArray = typeDeclaration.getFields();
		for (int index = 0; index < fieldDeclArray.length; index++) {
			FieldDeclaration fieldDecl = fieldDeclArray[index];
			
			// Visit the variable list defined in the node
			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
			for (VariableDeclarationFragment varNode : fragments) {
				String fieldDeclName = varNode.getName().getFullyQualifiedName();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(varNode, root, unitFileName);
				if (fieldDeclName.equals(field.getSimpleName()) && location.equals(field.getLocation())) {
					return fieldDecl; 
				}
			}
		}
		return null;
	}
	
	/**
	 * Find a field declaration for a type definition. Note that a field declaration may declare many fields at the same time.     
	 */
	public FieldDeclaration findDeclarationForFieldDefinition(FieldDefinition field) {
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(field);
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(type);
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		
		return findDeclarationInAST(unitFullName, root, type, field);
	}
	
	/**
	 * Print all definitions into a given PrintWriter
	 */
	public void printDefinitions(PrintWriter writer) {
		rootScope.printDefinitions(writer, 0);
	}
	
	/**
	 * Print all references into a given PrintWriter
	 */
	public void printReferences(PrintWriter writer) {
		rootScope.printReferences(writer, false);
	}
	
	/**
	 * Given a name definition, return the compilation unit scope enclosing the name. 
	 */
	public CompilationUnitScope getEnclosingCompilationUnitScope(NameDefinition name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_COMPILATION_UNIT) return (CompilationUnitScope)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name reference, return the compilation unit scope enclosing the name. 
	 */
	public CompilationUnitScope getEnclosingCompilationUnitScope(NameReference name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_COMPILATION_UNIT) return (CompilationUnitScope)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name definition, return the detailed type enclosing the name. 
	 */
	public DetailedTypeDefinition getEnclosingDetailedTypeDefinition(NameDefinition name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_TYPE) return (DetailedTypeDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}

	/**
	 * Given a name reference, return the detailed type enclosing the name. 
	 */
	public DetailedTypeDefinition getEnclosingDetailedTypeDefinition(NameReference name) {
		NameScope currentScope = name.getScope();
		
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_TYPE) return (DetailedTypeDefinition)currentScope;
			currentScope = currentScope.getEnclosingScope();
		}
		
		return null;
	}
	
	/**
	 * Create references for a compilation unit. All created references are stored in the name table
	 */
	public void createReferencesForCompilationUnit(String unitFullName) {
		CompilationUnitScope unitScope = findCompilationUnitScopeByFullName(unitFullName);
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		
		if (unitScope == null || root == null) return;
		NameReferenceCreator.createReferencesForCompilationUnit(unitFullName, root, unitScope);
	}

	/**
	 * Get all references (stored in the name table) in a compilation unit
	 */
	public List<NameReference> getAllReferencesInCompilationUnit(String unitFullName) {
		CompilationUnitScope unitScope = findCompilationUnitScopeByFullName(unitFullName);
		if (unitScope == null) return null;
		
		NameReferenceVisitor visitor = new NameReferenceVisitor();
		unitScope.accept(visitor);
		return visitor.getResult();
	}
	
	/**
	 * Create references for a detailed type. All created references are stored in the name table
	 */
	public void createReferencesForDetailedType(DetailedTypeDefinition type) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(type);
		if (unitScope == null) return;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return;
		
		TypeDeclaration typeDecl = findDeclarationForDetailedType(type);
		if (typeDecl == null) return;
		NameReferenceCreator.createReferencesForDetailedType(unitFullName, root, type, typeDecl);
	}

	/**
	 * Get all references (stored in the name table) in a detailed type
	 */
	public List<NameReference> getAllReferencesInDetailedType(DetailedTypeDefinition type) {
		NameReferenceVisitor visitor = new NameReferenceVisitor();
		type.accept(visitor);
		return visitor.getResult();
	}
	
	/**
	 * Create and return references of a detailed type. All created references are not stored in the name table
	 */
	public List<NameReference> createAndReturnReferencesInDetailedType(DetailedTypeDefinition type) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(type);
		if (unitScope == null) return null;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return null;
		
		TypeDeclaration typeDecl = findDeclarationInAST(unitFullName, root, type);
		if (typeDecl == null) return null;
		return NameReferenceCreator.createAndReturnReferencesInDetailedType(unitFullName, root, type, typeDecl);
	}
	
	/**
	 * Create references for a field. All created references are stored in the name table. 
	 * We can not get those references by given a field definition, since a field definition is not a scope.
	 * We can get those references by visitor the detailed type of enclosing field definition. 
	 */
	public void createReferencesForField(FieldDefinition field) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(field);
		if (unitScope == null) return;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return;
		
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(field);
		FieldDeclaration fieldDecl = findDeclarationInAST(unitFullName, root, type, field);
		if (fieldDecl == null || type == null) return;
		NameReferenceCreator.createReferencesForField(unitFullName, root, type, fieldDecl);
	}

	
	/**
	 * Create and return references of a field. All created references are not stored in the name table
	 */
	public List<NameReference> createAndReturnReferencesInField(FieldDefinition field) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(field);
		if (unitScope == null) return null;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return null;
		
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(field);
		FieldDeclaration fieldDecl = findDeclarationInAST(unitFullName, root, type, field);
		if (fieldDecl == null || type == null) return null;
		return NameReferenceCreator.createAndReturnReferencesInField(unitFullName, root, type, fieldDecl);
	}

	/**
	 * Create references for a method. All created references are stored in the name table
	 */
	public void createReferencesForMethod(MethodDefinition method) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return;
		
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(method);
		MethodDeclaration methodDecl = findDeclarationInAST(unitFullName, root, type, method);
		if (methodDecl == null) return;
		NameReferenceCreator.createReferencesForMethod(unitFullName, root, method, methodDecl);
	}
	
	/**
	 * Get all references (stored in the name table) in a method definition.
	 * Note that the return references do not include the references in the parameters of the method. 
	 * But createReferencesForMethod() will create the references for the parameters of the method.
	 */
	public List<NameReference> getAllReferencesInMethod(MethodDefinition method) {
		NameReferenceVisitor visitor = new NameReferenceVisitor();
		method.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Create and references of a method. All created references are not stored in the name table
	 */
	public List<NameReference> createAndReturnReferencesInMethod(MethodDefinition method) {
		CompilationUnitScope unitScope = getEnclosingCompilationUnitScope(method);
		if (unitScope == null) return null;
		
		String unitFullName = unitScope.getUnitFullName();
		CompilationUnit root = parser.findCompilationUnitByUnitFullName(unitFullName);
		if (root == null) return null;
		
		DetailedTypeDefinition type = getEnclosingDetailedTypeDefinition(method);
		MethodDeclaration methodDecl = findDeclarationInAST(unitFullName, root, type, method);
		if (methodDecl == null) return null;
		return NameReferenceCreator.createAndReturnReferencesInMethod(unitFullName, root, method, methodDecl);
	}
	
	/**
	 * Get all references in a scope
	 */
	public List<NameReference> getAllReferencesInScope(NameScope scope) {
		NameReferenceVisitor visitor = new NameReferenceVisitor();
		scope.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Get all definitions in a scope
	 */
	public List<NameDefinition> getAllDefinitionsInScope(NameScope scope) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		scope.accept(visitor);
		return visitor.getResult();
	}
	
}

