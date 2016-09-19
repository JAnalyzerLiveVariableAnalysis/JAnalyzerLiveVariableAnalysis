package nameTable.creator;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import util.SourceCodeLocation;
import util.SourceCodeParser;

/**
 * The class creates all name definitions for all Java source code files under a given path. Moreover, 
 * some name references related to name definitions will be created at the same time, including super type
 * references in type definitions, return type references in method definitions, type references in 
 * parameters of methods, type references in variable definitions and field definitions.
 * 
 * @author Zhou Xiaocong
 * @since 2013-4-12
 * @version 1.0
 * 
 * @update 2013-9-28
 * Add a constructor (i.e. NameDefinitionCreator(SourceCodeParser)) to use the parser provide by the client
 *
 */
public class NameDefinitionCreator extends NameTableCreator {

	public NameDefinitionCreator(SourceCodeParser parser) {
		super(parser);
		expressionVisitor = new ExpressionDefinitionVisitor(currentUnitFullName, currentASTRoot, null);
	}

	public NameDefinitionCreator(String systemPath) {
		super(systemPath);
		expressionVisitor = new ExpressionDefinitionVisitor(currentUnitFullName, currentASTRoot, null);
	}


	/**
	 * Scan a method declaration node to create name definitions, and ignore name references as far 
	 * as possible. 
     * <pre>   [ Javadoc ] { ExtendedModifier }
     *              [ < TypeParameter { , TypeParameter } > ]
     *    ( Type | void ) Identifier (
     *    [ FormalParameter
     *                 { , FormalParameter } ] ) {[ ] }
     *   [ throws TypeName { , TypeName } ] ( Block | ; )</pre>
	 */
	@Override
	@SuppressWarnings("unchecked")
	void scan(String qualifier, MethodDeclaration node, NameScope currentScope) {
		// Create type reference for the return type
		Type returnType = node.getReturnType2();
		TypeReference returnTypeRef = null;
		if (returnType != null) {
			typeVisitor.reset(currentUnitFullName, currentASTRoot, currentScope);
			returnType.accept(typeVisitor);
			returnTypeRef = typeVisitor.getResult();
			int dimension = returnTypeRef.getDimension() + node.getExtraDimensions();
			returnTypeRef.setDimension(dimension);
			
			// Add the reference to the reference list in the current scope, which include the method
			currentScope.addReference(returnTypeRef);
		} // else is a constructor 
		
		// Create method definition for the node
		String methodName = node.getName().getFullyQualifiedName();
		String fullQualifiedName = (qualifier == null) ? methodName : qualifier + NameReferenceLabel.NAME_QUALIFIER + methodName;
		SourceCodeLocation location = getStartPosition(node);
		SourceCodeLocation endLocation = getEndPosition(node);
		MethodDefinition methodDef = new MethodDefinition(methodName, fullQualifiedName, location, currentScope, endLocation);
		methodDef.setReturnType(returnTypeRef);
		methodDef.setModifierFlag(node.getModifiers());
		methodDef.setConstructor(node.isConstructor());
		currentScope.define(methodDef);

		// Debug.println("Scan method: " + methodName);
		
		// Create parameter definition for the node
		List<SingleVariableDeclaration> parameters = node.parameters();
		for (SingleVariableDeclaration parameter : parameters) {
			defineParameber(parameter, methodDef);
		}
		
		// Create type reference for throws type in the node
		@SuppressWarnings("deprecation")
		List<Name> throwTypes = node.thrownExceptions(); 
		for (Name throwType : throwTypes) {
			location = getStartPosition(throwType);
			TypeReference throwTypeRef = new TypeReference(throwType.getFullyQualifiedName(), location, currentScope);
			methodDef.addThrowTypes(throwTypeRef);

			// Add the reference to the reference list in the current scope, which include the method
			currentScope.addReference(returnTypeRef);
		}
		
		// Scan the body of the method
		Block body = node.getBody();
		if (body != null) {
			SourceCodeLocation start = getStartPosition(body);
			SourceCodeLocation end = getEndPosition(body);
			LocalScope localScope = createLocalScope(start, end, methodDef);
			methodDef.setBodyScope(localScope);
			// We can not reuse the block visitor for all methods, because when the method defines 
			// local types, and there are methods in the local types, we can not reuse the scope stack in 
			// the same block visitor! 
			BlockDefinitionVisitor blockVisitor = new BlockDefinitionVisitor(this, currentUnitFullName, currentASTRoot, localScope);
			// Then visit all children of the block
			List<Statement> statementList = body.statements();
			for (Statement statement : statementList) statement.accept(blockVisitor);
		}
	}
}
