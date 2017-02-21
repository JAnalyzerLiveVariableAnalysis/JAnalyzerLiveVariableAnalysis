package nameTable.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nameTable.NameTableManager;
import nameTable.nameDefinition.AutoGeneratedConstructor;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.ImportedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeParameterDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.QualifiedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.SystemScope;
import sourceCodeAST.SourceCodeFile;
import sourceCodeAST.SourceCodeLocation;

/**
 * Read and scan messages of imported types. The user can write messages for imported types in a text file for providing 
 * more information to analysis source code files. One can give these imported types as abstract class (or interface), just
 * giving its field declarations and method signatures (do not give method body). The imported declarations are not supported,   
 * so one can use the full qualified names to declaration types of parameters in method signatures. 
 * <p>Each external file can declare a package, and all imported types in this file belong to this packages. For example, the 
 * following lines can consist of an external file:
 * <p>package java.io <br>
 * class PrintStream { <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;void print();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;void print(String);<br>
 * } 
 * <p>The following lines can consist of another external file:
 * <p>package java.lang<br>
 * class System {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;java.io.PrintStream out;<br>
 * } 
 * 
 * @author Zhou Xiaocong
 * @since 2016��11��19��
 * @version 1.0
 *
 */
public class ImportedTypeManager {
	private static TypeASTVisitor typeVisitor = new TypeASTVisitor(null, null);
	
	/**
	 * Bind imports in all compilation units to its type definition
	 */
	static void bindImportsInAllCompilationUnits(NameTableManager table) {
		List<CompilationUnitScope> units = table.getAllCompilationUnitScopes();
		if (units == null) return;
		for (CompilationUnitScope unit : units) unit.bindImportDeclaration();
	}

	/**
	 * Define all primitive type and some class name in java.lang to the system scope
	 */
	static void defineGlobalNames(NameTableManager table) {
		String[][] primitiveTypeName = {
				{NameReferenceLabel.TYPE_BOOLEAN, NameReferenceLabel.TYPE_BOOLEAN},
				{NameReferenceLabel.TYPE_BYTE, NameReferenceLabel.TYPE_BYTE},
				{NameReferenceLabel.TYPE_CHAR, NameReferenceLabel.TYPE_CHAR},
				{NameReferenceLabel.TYPE_DOUBLE, NameReferenceLabel.TYPE_DOUBLE},
				{NameReferenceLabel.TYPE_FLOAT, NameReferenceLabel.TYPE_FLOAT},
				{NameReferenceLabel.TYPE_INT, NameReferenceLabel.TYPE_INT},
				{NameReferenceLabel.TYPE_LONG, NameReferenceLabel.TYPE_LONG},
				{NameReferenceLabel.TYPE_SHORT, NameReferenceLabel.TYPE_SHORT},
				{NameReferenceLabel.TYPE_VOID, NameReferenceLabel.TYPE_VOID},
		};
		String[][] systemName = {
				{NameReferenceLabel.TYPE_STRING, "java.lang." + NameReferenceLabel.TYPE_STRING},
				{NameReferenceLabel.TYPE_CLASS, "java.lang." + NameReferenceLabel.TYPE_CLASS},
				{"Object", "java.lang.Object"},
				{"System", "java.lang.System"},
				{"StringBuffer", "java.lang.StringBuffer"},
				{"StringBuilder", "java.lang.StringBuilder"},
				{"Math", "java.lang.Math"},
				{"Exception", "java.lang.Exception"},
				{"AssertionError", "java.lang.AssertionError"},
				{"Boolean", "java.lang.Boolean"},
				{"Character", "java.lang.Character"},
				{"CharSequence", "java.lang.CharSequence"},
				{"Number", "java.lang.Number"},
				{"Byte", "java.lang.Byte"},
				{"Short", "java.lang.Short"},
				{"Integer", "java.lang.Integer"},
				{"Long", "java.lang.Long"},
				{"Float", "java.lang.Float"},
				{"Double", "java.lang.Double"},
				{"Void", "java.lang.Void"},
				{"StringCoding", "java.lang.StringCoding"},
				{"StringValue", "java.lang.StringValue"},
				{"Runtime", "java.lang.Runtime"},
				{"Thread", "java.lang.Thread"},
				{"ThreadGroup", "java.lang.ThreadGroup"},
				{"ClassLoader", "java.lang.ClassLoader"},
				{"Package", "java.lang.Package"},
				{"Process", "java.lang.Process"},
				{"Comparable", "java.lang.Comparable"},
				{"Runnable", "java.lang.Runnable"},
				{"Throwable", "java.lang.Throwable"},
				{"Cloneable", "java.lang.Cloneable"},
				{"Comparable", "java.lang.Comparable"},
				{"Iterable", "java.lang.Iterable"},
		};
		
		SystemScope currentScope = table.getSystemScope();
		for (int index = 0; index < primitiveTypeName.length; index++) {
			if (!hasDefinedImportedTypeDefinition(currentScope, primitiveTypeName[index][1])) {
				ImportedTypeDefinition name = new ImportedTypeDefinition(primitiveTypeName[index][0], primitiveTypeName[index][1], currentScope);
				currentScope.define(name);
			}
		}
		
		PackageDefinition systemPackage = currentScope.findPackageByName(SystemScope.SYSTEM_PACKAGE_NAME);
		if (systemPackage == null) {
			for (int index = 0; index < systemName.length; index++) {
				if (!hasDefinedImportedTypeDefinition(currentScope, systemName[index][1])) {
					ImportedTypeDefinition name = new ImportedTypeDefinition(systemName[index][0], systemName[index][1], currentScope);
					currentScope.define(name);
				}
			}
		}
	}
	
	/**
	 * Test if the imported type with the given full qualified name has been defined in the system scope!
	 */
	static boolean hasDefinedImportedTypeDefinition(SystemScope scope, String fullQualifiedName) {
		List<ImportedTypeDefinition> importedTypeList = scope.getImportedTypeList();
		if (importedTypeList == null) return false;
		for (ImportedTypeDefinition importedType : importedTypeList) {
			if (fullQualifiedName.equals(importedType.getFullQualifiedName())) return true;
		}
		return false;
	}
	
	/**
	 * Read messages on the imported types from external files! One can provided many external files. 
	 */
	static void readImportedTypesFromExternalFiles(NameTableManager tableManager, String[] fileNameArray) {
		if (fileNameArray == null) return;
		if (fileNameArray.length <= 0) return;
		
		SystemScope systemScope = tableManager.getSystemScope();
		for (String fileName : fileNameArray) {
			try {
				File file = new File(fileName);
				SourceCodeFile codeFile = new SourceCodeFile(file);
				if (codeFile.hasCreatedAST()) {
					CompilationUnit root = codeFile.getASTRoot();
					CompilationUnitFile currentUnitFile = new CompilationUnitFile(fileName, root);
					scanCurrentCompilationUnit(currentUnitFile, systemScope);
					codeFile.releaseAST();
					codeFile.releaseFileContent();
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Scan an external file as a compilation unit. However, all imported types are defined in the system scope directly. 
	 * We do not create any compilation unit scope for the given external file. 
	 */
	static void scanCurrentCompilationUnit(CompilationUnitFile currentUnitFile, SystemScope currentScope) {
		CompilationUnit node = currentUnitFile.root; 
		// 1. Process the package declaration in the file, we only extract the name of the package, do not define any package definition
		//   in the system scope
		PackageDeclaration packageDecl = node.getPackage();
		String packageName = null;
		if (packageDecl != null) {
			packageName = packageDecl.getName().getFullyQualifiedName();
		}
		
		// 2 Process the type declarations in the node, we do not define compilation unit scope for the file, all 
		//   imported type definitions are defined in the system scope!
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> types = node.types();
		
		for (AbstractTypeDeclaration type : types) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				scan(currentUnitFile, packageName, (TypeDeclaration)type, currentScope);
			} else if (type.getNodeType() == ASTNode.ENUM_DECLARATION) {
				scan(currentUnitFile, packageName, (EnumDeclaration)type, currentScope);
			}
		}
	}
	
	/**
	 * Scan an imported type to extract field declarations and method declarations. Note that, all imported types are defined in the 
	 * system scope directly, and its field declarations and method declarations are defined in this imported types. However, we do not
	 * regard imported types as a name scope of the name table of the source code file set even if implements the interface NameScope. 
	 */
	@SuppressWarnings("unchecked")
	static void scan(CompilationUnitFile currentUnitFile, String qualifier, TypeDeclaration node, NameScope currentScope) {
		// Create a type definition for the node
		String name = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? name : qualifier + NameReferenceLabel.NAME_QUALIFIER + name;
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		ImportedTypeDefinition typeDef = new ImportedTypeDefinition(name, fullQualifiedName, location, currentScope, endLocation);
		typeDef.setInterface(node.isInterface());
		typeDef.setPackageMember(node.isPackageMemberTypeDeclaration());
		currentScope.define(typeDef);
		
		// Create type reference for the super class
		if (node.getSuperclassType() != null) {
			Type superClassType = node.getSuperclassType();
			typeVisitor.reset(currentUnitFile, currentScope);
			superClassType.accept(typeVisitor);
			TypeReference superClassRef = typeVisitor.getResult();
			typeDef.addSuperType(superClassRef);
		}
		// Create type reference for the super interfaces
		List<Type> superInterfaces = node.superInterfaceTypes();
		for (Type superInterface : superInterfaces) {
			typeVisitor.reset(currentUnitFile, currentScope);
			superInterface.accept(typeVisitor);
			TypeReference superInterfaceRef = typeVisitor.getResult();
			typeDef.addSuperType(superInterfaceRef);
		}
		
		// Process the type parameter definition of the node
		List<TypeParameter> typeParameterList = node.typeParameters();
		for (TypeParameter typeParameter : typeParameterList) {
			String typeParaName = typeParameter.getName().getFullyQualifiedName();
			SourceCodeLocation typeParaLocation = SourceCodeLocation.getStartLocation(typeParameter, currentUnitFile.root, currentUnitFile.unitName);
			TypeParameterDefinition typeParaDef = new TypeParameterDefinition(typeParaName, typeParaName, typeParaLocation, typeDef);
			typeDef.define(typeParaDef);
		}
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) scan(currentUnitFile, fullQualifiedName, fields[index], typeDef);
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) scan(currentUnitFile, fullQualifiedName, methods[index], typeDef);
		
		// Automatically generated default constructor if this class has not any constructor
		boolean hasConstructor = false;
		List<MethodDefinition> methodList = typeDef.getMethodList();
		if (methodList == null) methodList = new ArrayList<MethodDefinition>();
		for (MethodDefinition method : methodList) {
			if (method.isConstructor()) hasConstructor = true;
		}
		if (!hasConstructor) {
			MethodDefinition defaultConstructor = new AutoGeneratedConstructor(name, fullQualifiedName, location, typeDef, location);
			methodList.add(defaultConstructor);
		}
		
		// Process the type declarations in the node
		AbstractTypeDeclaration[] typeMembers = node.getTypes(); 
		for (int index = 0; index < typeMembers.length; index++) {
			AbstractTypeDeclaration type = typeMembers[index];
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				scan(currentUnitFile, fullQualifiedName, (TypeDeclaration)type, typeDef);
			} else if (type.getNodeType() == ASTNode.ENUM_DECLARATION) {
				scan(currentUnitFile, fullQualifiedName, (EnumDeclaration)type, typeDef);
			}
		}
	}

	/**
	 * Scan an enumeration declaration in the given external file 
	 */
	@SuppressWarnings("unchecked")
	static void scan(CompilationUnitFile currentUnitFile, String qualifier, EnumDeclaration node, NameScope currentScope) {
		// Create a type definition for the node
		String name = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? name : qualifier + NameReferenceLabel.NAME_QUALIFIER + name;
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		EnumTypeDefinition typeDef = new EnumTypeDefinition(name, fullQualifiedName, location, currentScope, endLocation);
		typeDef.setPackageMember(node.isPackageMemberTypeDeclaration());
		typeDef.setModifierFlag(node.getModifiers());
		currentScope.define(typeDef);
		
		// Create type reference for the super interfaces
		List<Type> superInterfaces = node.superInterfaceTypes();
		for (Type superInterface : superInterfaces) {
			typeVisitor.reset(currentUnitFile, currentScope);
			superInterface.accept(typeVisitor);
			TypeReference superInterfaceRef = typeVisitor.getResult();
			typeDef.addSuperType(superInterfaceRef);
			// Add to the reference list in the current scope, which include the type definition
		}
		
		// Process the constant declarations in the node. We regard the enum constant as method 
		List<EnumConstantDeclaration> constants = node.enumConstants();
		for (EnumConstantDeclaration constant : constants) {
			scan(currentUnitFile, fullQualifiedName, constant, typeDef);
		}
	}

	/**
	 * Scan a field declaration of an imported type in the given external file 
	 */
	@SuppressWarnings("unchecked")
	static void scan(CompilationUnitFile currentUnitFile, String qualifier, FieldDeclaration node, NameScope currentScope) {
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentUnitFile, currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add to the reference list in the currentScope, which include the field
		
		int modifierFlag = node.getModifiers();
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			defineField(currentUnitFile, qualifier, varNode, typeRef, currentScope, modifierFlag);
		}
	}

	/**
	 * Scan a method declaration of an imported type in the given external file 
	 */
	@SuppressWarnings("unchecked")
	static void scan(CompilationUnitFile currentUnitFile, String qualifier, MethodDeclaration node, NameScope currentScope) {
		// Create type reference for the return type
		Type returnType = node.getReturnType2();
		TypeReference returnTypeRef = null;
		if (returnType != null) {
			typeVisitor.reset(currentUnitFile, currentScope);
			returnType.accept(typeVisitor);
			returnTypeRef = typeVisitor.getResult();
			int dimension = returnTypeRef.getDimension() + node.getExtraDimensions();
			returnTypeRef.setDimension(dimension);
		} // else is a constructor 
		
		// Create method definition for the node
		String methodName = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? methodName : qualifier + NameReferenceLabel.NAME_QUALIFIER + methodName;
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		MethodDefinition methodDef = new MethodDefinition(methodName, fullQualifiedName, location, currentScope, endLocation);
		methodDef.setReturnType(returnTypeRef);
		methodDef.setModifierFlag(node.getModifiers());
		methodDef.setConstructor(node.isConstructor());
		currentScope.define(methodDef);

		// Process the type parameter definition of the node
		List<TypeParameter> typeParameterList = node.typeParameters();
		for (TypeParameter typeParameter : typeParameterList) {
			String typeParaName = typeParameter.getName().getFullyQualifiedName();
			SourceCodeLocation typeParaLocation = SourceCodeLocation.getStartLocation(typeParameter, currentUnitFile.root, currentUnitFile.unitName);
			TypeParameterDefinition typeParaDef = new TypeParameterDefinition(typeParaName, typeParaName, typeParaLocation, methodDef);
			methodDef.define(typeParaDef);
		}
		
		// Create parameter definition for the node
		List<SingleVariableDeclaration> parameters = node.parameters();
		for (SingleVariableDeclaration parameter : parameters) {
			defineParameber(currentUnitFile, parameter, methodDef);
		}
		
		// Create type reference for throws type in the node
		List<Type> throwTypes = node.thrownExceptionTypes();
		for (Type throwType : throwTypes) {
			typeVisitor.reset(currentUnitFile, currentScope);
			throwType.accept(typeVisitor);
			TypeReference throwTypeRef = typeVisitor.getResult();
			methodDef.addThrowType(throwTypeRef);
		}
	}
	
	/**
	 * Scan a enum constant declaration of an imported type in the given external file 
	 */
	static void scan(CompilationUnitFile unitFile, String qualifier, EnumConstantDeclaration node, NameScope currentScope) {
		String name = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? name : qualifier + NameReferenceLabel.NAME_QUALIFIER + name;
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, unitFile.root, unitFile.unitName);
		
		EnumConstantDefinition constDef = new EnumConstantDefinition(name, fullQualifiedName, location, currentScope);
		currentScope.define(constDef);
	}
	
	/**
	 * Define a parameter in a SingleVariableDeclaration to the scope given by the parameter currentScope
	 */
	static void defineParameber(CompilationUnitFile currentUnitFile, SingleVariableDeclaration node, NameScope currentScope) {
		Type type = node.getType();
		typeVisitor.reset(currentUnitFile, currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Set the correct dimension for the variable 
		int dimension = typeRef.getDimension() + node.getExtraDimensions();
		typeRef.setDimension(dimension);

		// Define the variable to the current scope
		String varName = node.getName().getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		VariableDefinition variableDef = new VariableDefinition(varName, varName, location, currentScope);
		variableDef.setDefinitionKind(NameDefinitionKind.NDK_PARAMETER);
		variableDef.setType(typeRef);
		currentScope.define(variableDef);
	}

	/**
	 * Define a field in a VariableDeclaration to the scope given by the parameter currentScope
	 * The caller must to provide the type reference for the variable, since the the node may be a VariableDeclarationFragment
	 * Because the dimension of the node may be different from the other variables declared in the same variableDeclaration,
	 * we should copy the type reference for the variable definition of the node.
	 */
	static void defineField(CompilationUnitFile currentUnitFile, String qualifier, VariableDeclaration node, TypeReference varTypeRef, NameScope currentScope, int modifierFlag) {
		// And because the dimension of the node may be different from the other variables declared in the same variableDeclaration,
		// we should copy the type reference for the variable definition of the node.
		TypeReference typeRef = null;
		if (varTypeRef.isQualifiedType()) typeRef = new QualifiedTypeReference((QualifiedTypeReference)varTypeRef);
		else if (varTypeRef.isParameterizedType()) typeRef = new ParameterizedTypeReference((ParameterizedTypeReference)varTypeRef);
		else typeRef = new TypeReference(varTypeRef);

		// Set the correct dimension for the variable 
		int dimension = varTypeRef.getDimension() + node.getExtraDimensions();
		typeRef.setDimension(dimension);
		
		// Define the variable to the current scope
		String varName = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? varName : qualifier + NameReferenceLabel.NAME_QUALIFIER + varName;
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, currentUnitFile.root, currentUnitFile.unitName);
		FieldDefinition fieldDef = new FieldDefinition(varName, fullQualifiedName, location, currentScope);
		fieldDef.setType(typeRef);
		fieldDef.setModifierFlag(modifierFlag);
		currentScope.define(fieldDef);
	}
	
}
