package nameTable.creator;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.WildcardType;

import nameTable.nameScope.NameScope;

/**
 * A block visitor for creating all name definitions, while ignoring name references as far as possible
 *  
 * @author Zhou Xiaocong
 * @since 2013-4-12
 * @version 1.0
 *
 */
public class BlockDefinitionVisitor extends BlockASTVisitor {

	public BlockDefinitionVisitor(NameTableCreator creator, String unitFullName, CompilationUnit root, NameScope currentScope) {
		super(creator, unitFullName, root, currentScope);
		expressionVisitor = new ExpressionDefinitionVisitor(unitFullName, root, currentScope);
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayAccess node) {
		return false;
	}

	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayCreation node) {
		return false;
	}

	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayInitializer node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ArrayType node) {
		return false;
	}

	/**
	 * AssertStatement: assert Expression: Expression
	 * Only need to visit its children
	 */
	public boolean visit(AssertStatement node) {
		return true;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(Assignment node) {
		return false;
	}

	// For block node, use the same implementation in the super class BlockASTVisitor
	/* public boolean visit(Block node) {
		// Check if the block need to create a local scope, if it need to create a local scope, then create a new scope and 
		// push it to the scopeStack
		boolean createScope = creator.needCreateLocalScope(node);
		if (createScope) {
			NameScope currentScope = scopeStack.getTop();
			NameScope newLocalScope = creator.createLocalScope(node, currentScope);
			scopeStack.push(newLocalScope);
		}
		
		// Then visit all children of the block
		List<Statement> statementList = node.statements();
		for (Statement statement : statementList) statement.accept(this);
		
		// Pop the new scope created by the block, since the scope of the following AST node is the original current scope
		if (createScope) {
			scopeStack.pop();
		}
		return false;
	}*/


	/**
	 * CastExpression: (Type) Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(CastExpression node) {
		return false;
	}

	// For catch clause node, use the same implementation in the super class BlockASTVisitor
	/**
	 * CatchClause:  catch ( FormalParameter ) Block
	 * 
	 * Create a new local scope for body block statement of the catch clause, and then define the exception declared in 
	 * the catch clause to the new scope, push the scope to stack, and then visit the block statement of the catch clause, 
	 * after that, pop the scope
	 */
	/*public boolean visit(CatchClause node) {
		if (scopeStack.isEmpty()) throw new AssertionError("Get null scope for node: " + node);
		
		// Create a new local scope for the body of the catch clause
		NameScope currentScope = scopeStack.getTop();
		Block block = node.getBody();
		NameScope newLocalScope = creator.createLocalScope(block, currentScope);
		
		SingleVariableDeclaration exception = node.getException();
		// Get the type reference of the exception
		Type type = exception.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);

		// Define the exception declared in the clause to the new scope
		String exceptionName = exception.getName().getIdentifier();
		SourceCodeLocation location = creator.getStartPosition(exception);
		VariableDefinition exceptionDef = new VariableDefinition(exceptionName, exceptionName, location, newLocalScope);
		exceptionDef.setVariableDefinitionKind(NameDefinitionKind.NDK_VARIABLE);
		exceptionDef.setType(typeRef);
		newLocalScope.define(exceptionDef);
		
		// Push the new scope to the scope stack, and visit the body block statement of the catch clause
		// System.out.println("In catch cluase " + creator.getStartPosition(node).getLineNumber() + ", Push scope: " + newLocalScope.getScopeName());
		scopeStack.push(newLocalScope);

		// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
		// may create a local scope for the block more than once. 
		List<Statement> statementList = block.statements();
		for (Statement statement : statementList) statement.accept(this);

		scopeStack.pop();
		
		return false;
	}*/


	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ClassInstanceCreation node) {
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ConditionalExpression node) {
		return false;
	}

	// For enhanced for statement node, use the same implementation in the super class BlockASTVisitor
	/**
	 * EnhancedForStatement: for ( FormalParameter : Expression ) Statement
	 * 
	 * Create a new local scope for body block statement of the statement, and then define the exception declared in 
	 * the catch clause to the new scope, push the scope to stack, and then visit the block statement of the catch clause, 
	 * after that, pop the scope
	 */
	/* public boolean visit(EnhancedForStatement node) {
		NameScope currentScope = scopeStack.getTop();
		boolean createNewScope = false;
		Statement body = node.getBody();
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			// Only when the body of the enhanced for statement is a block statement, we create a new scope for this block
			createNewScope = true;
			// Create a new local scope for the body of the catch clause
			NameScope newLocalScope = creator.createLocalScope((Block)body, currentScope);
			// Push the new scope to the scope stack, and visit the body block statement of the catch clause
			// System.out.println("In enhanced for " + creator.getStartPosition(node).getLineNumber() + ", Push scope: " + newLocalScope.getScopeName());
			scopeStack.push(newLocalScope);
			// The parameter declared in the enhanced for statement should be defined in the new scope
			currentScope = newLocalScope; 
		} // else we define the parameter in the currentScope
		
		SingleVariableDeclaration variable = node.getParameter();
		// Get the type reference of the parameter
		Type type = variable.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);

		// Define the parameter declared in the enhanced for statement to the current scope. Note that the current scope 
		// will be the new scope if the new scope have been created!
		String variableName = variable.getName().getIdentifier();
		SourceCodeLocation location = creator.getStartPosition(variable);
		VariableDefinition variableDef = new VariableDefinition(variableName, variableName, location, currentScope);
		variableDef.setVariableDefinitionKind(NameDefinitionKind.NDK_VARIABLE);
		variableDef.setType(typeRef);
		currentScope.define(variableDef);
		
		// Visit the expression of the statement
		Expression expression = node.getExpression();
		expressionVisitor.reset(currentScope);
		expression.accept(expressionVisitor);
		NameReference result = expressionVisitor.getResult();
		currentScope.addReference(result);

		// Visit the body of the statement
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block)body;
			// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
			// may create a local scope for the block more than once. 
			List<Statement> statementList = block.statements();
			for (Statement statement : statementList) statement.accept(this);
		} else if (body != null) {
			// We use the current visitor to visit the body directly!
			body.accept(this);
		}

		if (createNewScope) {
			// System.out.println("In enhanced for " + creator.getStartPosition(node).getLineNumber() + ", pop scope: " + scopeStack.getTop().getScopeName());
			scopeStack.pop();
		}
		
		return false;
	}*/


	/**
	 * ExpressionStatement Expression ;
	 * There is no name definition in this kind of AST node, ignore the node! 
	 */
	public boolean visit(ExpressionStatement node) {
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(FieldAccess node) {
		return false;
	}

	/**
	 * Ignore this kind of AST node so far, since it can not occur in a block
	 */
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	
	// For for statement node, use the same implementation in the super class BlockASTVisitor
	/**
	 * If there are variable declaration in the initialize expression of the statement, and the body is 
	 * a block, then create a local scope for the body, and visit the children of the statement in the appropriate 
	 * name scope
	 */
	/* public boolean visit(ForStatement node) {
		NameScope currentScope = scopeStack.getTop();
		boolean createNewScope = createScopeForForStatement(node);
		Statement body = node.getBody();
		if (createNewScope) {
			// When createNewScope == true, the body of the for statement must be a block statement, 

			// Create a new local scope for the body of the catch clause
			NameScope newLocalScope = creator.createLocalScope((Block)body, currentScope);
			// Push the new scope to the scope stack, and visit the body block statement of the catch clause
			// System.out.println("In for " + creator.getStartPosition(node).getLineNumber() + ", Push scope: " + newLocalScope.getScopeName());
			scopeStack.push(newLocalScope);
			// The parameter declared in the enhanced for statement should be defined in the new scope
			currentScope = newLocalScope; 
		}

		// Visit the initializers of the for statement
		List<Expression> initializers = node.initializers();
		for (Expression expression : initializers) {
			expressionVisitor.reset(currentScope);
			expression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the condition expression of the for statement
		Expression condExpression = node.getExpression();
		if (condExpression != null) {
			expressionVisitor.reset(currentScope);
			condExpression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the updaters of the for statement
		List<Expression> updaters = node.updaters();
		for (Expression expression : updaters) {
			expressionVisitor.reset(currentScope);
			expression.accept(expressionVisitor);
			currentScope.addReference(expressionVisitor.getResult());
		}
		
		// Visit the body of the statement
		if (body != null && body.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block)body;
			// Then visit all statements of the block, we can not visit the block directly, because if do so then we 
			// may create a local scope for the block more than once. 
			List<Statement> statementList = block.statements();
			for (Statement statement : statementList) statement.accept(this);
		} else if (body != null) {
			// We use the current visitor to visit the body directly!
			body.accept(this);
		}
		
		if (createNewScope) {
			// System.out.println("In for " + creator.getStartPosition(node).getLineNumber() + ", pop scope: " + scopeStack.getTop().getScopeName());
			scopeStack.pop();
		}
		
		return false;
	}*/

	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(InfixExpression node) {
		return false;
	}

	/**
	 * InstanceofExpression: Expression instanceof Type
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(InstanceofExpression node) {
		return false;
	}

	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(MethodInvocation node) {
		return false;
	}

	
	/**
	 * ParameterizedType: Type < Type { , Type } >
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ParameterizedType node) {
		return false;
	}

	/**
	 * Use the expression visitor to visit the expression in the node directly
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ParenthesizedExpression node) {
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PostfixExpression node) {
		return false;
	}

	/**
	 * Use the expression visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrefixExpression node) {
		return false;
	}

	/**
	 * Use the type visitor to visit the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrimitiveType node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedName node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedType node) {
		return false;
	}

	/**
	 * ReturnStatement: return [Expression]
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ReturnStatement node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleName node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleType node) {
		return false;
	}


	// For single variable declaration node, use the same implementation in the super class BlockASTVisitor
	/**
	 * SingleVariableDeclaration: { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
	 * Define the variable to the scope, and visit the initializer of the declaration
	 */
	/* public boolean visit(SingleVariableDeclaration node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Define the variable to the current scope
		creator.defineVariable(node, typeRef, currentScope);
		
		// Visit the initializer in the variable declaration
		Expression initializer = node.getInitializer();
		expressionVisitor.reset(currentScope);
		initializer.accept(expressionVisitor);
		NameReference initExpRef = expressionVisitor.getResult();
		currentScope.addReference(initExpRef);
		
		return false;
	}*/


	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SuperFieldAccess node) {
		return false;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SuperMethodInvocation node) {
		return false;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ThisExpression node) {
		return false;
	}

	// For type declaration statement node, use the same implementation in the super class BlockASTVisitor
	/**
	 * Use creator.scan() to create the name definitions and references in the local type
	 */
	/* public boolean visit(TypeDeclarationStatement node) {
		NameScope currentScope = scopeStack.getTop();
		if (node.getDeclaration().getNodeType() == ASTNode.TYPE_DECLARATION) {
			TypeDeclaration type = (TypeDeclaration)node.getDeclaration();
			creator.scan(null, type, currentScope);
		}
		return false;
	}*/

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}

	// For variable declaration expression node, use the same implementation in the super class BlockASTVisitor
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	/* public boolean visit(VariableDeclarationExpression node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(varNode, typeRef, currentScope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			expressionVisitor.reset(currentScope);
			initializer.accept(expressionVisitor);
			NameReference initExpRef = expressionVisitor.getResult();
			currentScope.addReference(initExpRef);
		}
		return false;
	}*/

	// For variable declaration statement node, use the same implementation in the super class BlockASTVisitor
	/**
	 * VariableDeclarationStatement: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	/* public boolean visit(VariableDeclarationStatement node) {
		NameScope currentScope = scopeStack.getTop();
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(currentScope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		currentScope.addReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			creator.defineVariable(varNode, typeRef, currentScope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				expressionVisitor.reset(currentScope);
				initializer.accept(expressionVisitor);
				NameReference initExpRef = expressionVisitor.getResult();
				currentScope.addReference(initExpRef);
			}
		}
		return false;
	}*/


	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(WildcardType node) {
		return false;
	}
}
