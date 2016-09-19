package nameTable.creator;

import java.util.List;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;

/**
 * The expression visitor for creating all name definitions, while ignoring name references as far as possible
 * @author Zhou Xiaocong
 * @since 2013-4-12
 * @version 1.0
 *
 */
public class ExpressionDefinitionVisitor extends ExpressionASTVisitor {

	public ExpressionDefinitionVisitor(String unitFullName, CompilationUnit root, NameScope scope) {
		super(unitFullName, root, scope);
	}

	public NameReference getResult() {
		return null;
	}
	

	/**
	 * ArrayAccess: Expression[Expression]
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		return false;
	}
	
	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ArrayCreation node) {
		return false;
	}
	
	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ArrayInitializer node) {
		return false;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(Assignment node) {
		return false;
	}
	
	/**
	 * CastExpression: (Type) Expression
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(CastExpression node) {
		return false;
	}
	
	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ClassInstanceCreation node) {
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(ConditionalExpression node) {
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
	 * PostfixExpression: Expression PostfixOperator
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PostfixExpression node) {
		return false;
	}

	/**
	 * PrefixExpression: PrefixOperator Expression 
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(PrefixExpression node) {
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(QualifiedName node) {
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(SimpleName node) {
		return false;
	}
	
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
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * <p>VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// Define the variable to the current scope
			NameTableCreator.defineVariable(varNode, typeRef, scope, unitFullName, root);
		}

		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	
	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(NullLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(NumberLiteral node) {
		return false;
	}

	/**
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(StringLiteral node) {
		return false;
	}

	/**
	 * TypeLiteral: ( Type | void ) . class
	 * There is no name definition in this kind of AST expression node, ignore the node! 
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}
}
