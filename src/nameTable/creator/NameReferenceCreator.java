package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import util.SourceCodeLocation;

/**
 * A class provided some static methods to create reference for compilation unit, class, method or field
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameReferenceCreator {

	/**
	 * Scan current compilation unit to create references. All the created references are stored in the name table
	 * CompilationUnit:
     *   [ PackageDeclaration ]
     *     { ImportDeclaration }
     *     { TypeDeclaration | EnumDeclaration | AnnotationTypeDeclaration | ; }
     * @pre-condition: currentSourceFileName ! = null &&  currentASTRoot != null
	 */
	@SuppressWarnings("unchecked")
	public static void createReferencesForCompilationUnit(String unitFullName, CompilationUnit root, CompilationUnitScope scope) {
		
		// Process the list declarations in the node
		List<AbstractTypeDeclaration> types = root.types();
		for (AbstractTypeDeclaration type : types) {
			if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
				TypeDeclaration typeDecl = (TypeDeclaration)type;
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(type, root, unitFullName);
				String declFullName = typeDecl.getName().getIdentifier();
				
				List<TypeDefinition> typeList = scope.getTypeList();
				for (TypeDefinition typeDef : typeList) {
					if (typeDef.isDetailedType()) {
						DetailedTypeDefinition detailedType = (DetailedTypeDefinition)typeDef;
						if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
							createReferencesForDetailedType(unitFullName, root, detailedType, typeDecl);
						}
					}
					
				}
			}
		}
	}

	
	/**
	 * Scan a type declaration node to create name references, all the created references are stored in the name table
     * TypeDeclaration:
     *  [ Javadoc ] { Modifier } class Identifier
     *                   [ extends Type]
     *                   [ implements Type { , Type } ]
     *                   { { BodyDeclaration | ; } }
     *  BodyDeclaration: FieldDelcaration MethodDeclaration TypeDeclaration           
	 */
	public static void createReferencesForDetailedType(String unitFullName, CompilationUnit root, DetailedTypeDefinition type, TypeDeclaration node) {
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) createReferencesForField(unitFullName, root, type, fields[index]);
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) {

			List<MethodDefinition> methodList = type.getMethodList();
			for (MethodDefinition methodInType : methodList) {
				String methodSimpleName = methods[index].getName().getIdentifier();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(methods[index], root, unitFullName);
				
				if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) {
					createReferencesForMethod(unitFullName, root, methodInType, methods[index]);
				}
			}
		}
		
		// Process the type declarations in the node
		TypeDeclaration[] typeMembers = node.getTypes();
		for (int index = 0; index < typeMembers.length; index++) {
			String declFullName = typeMembers[index].getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(typeMembers[index], root, unitFullName);
			
			List<DetailedTypeDefinition> typeList = type.getTypeList();
			for (DetailedTypeDefinition detailedType : typeList) {
//				System.out.println("Match detailed type: " + detailedType.getSimpleName() + "[" + detailedType.getLocation() + "] with " + " declaration " + declFullName + "[" + location + "]");
				
				if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
					createReferencesForDetailedType(unitFullName, root, detailedType, typeMembers[index]);
				}
			}
		}
	}


	/**
	 * Scan a type declaration node to create name references, and return the references. All the created references are not 
	 * stored in the name table 
 	 */
	public static List<NameReference> createAndReturnReferencesInDetailedType(String unitFullName, CompilationUnit root, DetailedTypeDefinition type, TypeDeclaration node) {
		List<NameReference> resultList = new ArrayList<NameReference>();
		
		// Process the field declarations in the node
		FieldDeclaration[] fields = node.getFields();
		for (int index = 0; index < fields.length; index++) {
			List<NameReference> result = createAndReturnReferencesInField(unitFullName, root, type, fields[index]);
			resultList.addAll(result);
		}
		
		// Process the method declarations in the node
		MethodDeclaration[] methods = node.getMethods();
		for (int index = 0; index < methods.length; index++) {

			List<MethodDefinition> methodList = type.getMethodList();
			for (MethodDefinition methodInType : methodList) {
				String methodSimpleName = methods[index].getName().getIdentifier();
				SourceCodeLocation location = SourceCodeLocation.getStartLocation(methods[index], root, unitFullName);
				
				if (methodInType.getSimpleName().equals(methodSimpleName) && methodInType.getLocation().equals(location)) {
					List<NameReference> result = createAndReturnReferencesInMethod(unitFullName, root, methodInType, methods[index]);
					resultList.addAll(result);
				}
			}
		}
		
		// Process the type declarations in the node
		TypeDeclaration[] typeMembers = node.getTypes();
		for (int index = 0; index < typeMembers.length; index++) {
			String declFullName = typeMembers[index].getName().getIdentifier();
			SourceCodeLocation location = SourceCodeLocation.getStartLocation(typeMembers[index], root, unitFullName);
			
			List<DetailedTypeDefinition> typeList = type.getTypeList();
			for (DetailedTypeDefinition detailedType : typeList) {
//				System.out.println("Match detailed type: " + detailedType.getSimpleName() + "[" + detailedType.getLocation() + "] with " + " declaration " + declFullName + "[" + location + "]");
				
				if (detailedType.getSimpleName().equals(declFullName) && detailedType.getLocation().equals(location)) {
					List<NameReference> result = createAndReturnReferencesInDetailedType(unitFullName, root, detailedType, typeMembers[index]);
					resultList.addAll(result);
				}
			}
		}
		
		return resultList;
	}

	
	/**
	 * Scan a field declaration node to create references. All the created references are stored in the name table
	 * FieldDelcaration: [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
     *                       { , VariableDeclarationFragment } ;
	 */
	@SuppressWarnings("unchecked")
	public static void createReferencesForField(String unitFullName, CompilationUnit astRoot, DetailedTypeDefinition detailedType, FieldDeclaration node) {
		ReferenceASTVisitor expressionVisitor = new ReferenceASTVisitor(unitFullName, astRoot, detailedType);

		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset(unitFullName, astRoot, detailedType);
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				detailedType.addReference(initExpRef);
			}
		}
	}

	/**
	 * Scan a field declaration node to create references, and return the references. The created references are not stored 
	 * in name table
	 */
	@SuppressWarnings("unchecked")
	public static List<NameReference> createAndReturnReferencesInField(String unitFullName, CompilationUnit astRoot, DetailedTypeDefinition detailedType, FieldDeclaration node) {
		ReferenceASTVisitor expressionVisitor = new ReferenceASTVisitor(unitFullName, astRoot, detailedType);
		List<NameReference> resultList = new ArrayList<NameReference>();

		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset(unitFullName, astRoot, detailedType);
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				resultList.add(initExpRef);
			}
		}
		return resultList;
	}
	
	/**
	 * Scan a method declaration node to create name references. All the created references are stored in the name table
     *  [ Javadoc ] { ExtendedModifier }
     *              [ < TypeParameter { , TypeParameter } > ]
     *    ( Type | void ) Identifier (
     *    [ FormalParameter
     *                 { , FormalParameter } ] ) {[ ] }
     *   [ throws TypeName { , TypeName } ] ( Block | ; )
	 */
	@SuppressWarnings("unchecked")
	public static void createReferencesForMethod(String unitFullName, CompilationUnit astRoot, MethodDefinition method, MethodDeclaration node) {
		// Debug.println("Scan method: " + methodName);
		
		// Scan the body of the method
		Block body = node.getBody();
		LocalScope localScope = method.getBodyScope();
		
		if (body != null) {
			BlockReferenceASTVisitor blockVisitor = new BlockReferenceASTVisitor(unitFullName, astRoot, localScope);
			// Then visit all children of the block
			List<Statement> statementList = body.statements();
			for (Statement statement : statementList) statement.accept(blockVisitor);
		}
	}
	
	/**
	 * Scan a method declaration node to create name references, and return the references. The created references are not stored 
	 * in name table
	 */
	public static List<NameReference> createAndReturnReferencesInMethod(String unitFullName, CompilationUnit astRoot, MethodDefinition method, MethodDeclaration node) {
		// Debug.println("Scan method: " + methodName);
		
		// Scan the body of the method
		Block body = node.getBody();
		LocalScope localScope = method.getBodyScope();
		
		if (body != null) {
			LocalReferenceASTVisitor localVisitor = new LocalReferenceASTVisitor(unitFullName, astRoot, localScope);
			// Then visit the block
			body.accept(localVisitor);
			return localVisitor.getResult();
		} else return new ArrayList<NameReference>();
	}

}
