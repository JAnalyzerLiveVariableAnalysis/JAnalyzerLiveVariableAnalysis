package graph.cfg.creator;

import graph.cfg.ExecutionPoint;
import graph.cfg.ExecutionPointLabel;
import graph.cfg.ExecutionPointType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import util.SourceCodeLocation;

/**
 * The factory class for creating execution point.
 * @author Zhou Xiaocong
 * @since 2012/12/28
 * @version 1.0
 *
 */
public class ExecutionPointFactory {
	private static CompilationUnit root = null;
	private static String fileName = null;

	/**
	 * Set the compilation unit for this factory class. Before calling any other methods of this class, this method should be call first! 
	 * @param fileName : The name of the file to create this AST tree root (i.e. the compilation unit node root)
	 * @param root : The compilation unit node for creating execution points of current CFG.
	 */
	public static void setCompilationUnit(String fileName, CompilationUnit root) {
		ExecutionPointFactory.fileName = fileName;
		ExecutionPointFactory.root = root;
	}
	
	/**
	 * Call different methods according the type of statement to create an execution point for an AssertStatement
	 */
	public static ExecutionPoint create(Statement astNode) {
		if (astNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT) return create((ExpressionStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) return create((VariableDeclarationStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION) return create((ConstructorInvocation)astNode);
		else if (astNode.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) return create((SuperConstructorInvocation)astNode);
		else if (astNode.getNodeType() == ASTNode.ASSERT_STATEMENT) return create((AssertStatement)astNode);
		else if (astNode.getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT) return create((TypeDeclarationStatement)astNode);
		else {
			// The statement type should be the above types. So the following assertion should never be executed. 
			throw new AssertionError("Meet unexpected statement type when creating execution point for statement: " + astNode);
		}
	}

	/**
	 * Create an execution point for an AssertStatement
	 */
	public static ExecutionPoint create(AssertStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.ASSERTION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a ConstructorInvocation
	 */
	public static ExecutionPoint create(ConstructorInvocation astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.CONSTRUCTOR_INVOCATION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Call create(Expression) to create an execution point for a ExpressionStatement
	 */
	public static ExecutionPoint create(ExpressionStatement astNode) {
		Expression expression = astNode.getExpression();
		return create(expression);
	}
	
	/**
	 * Create an execution point for a SuperConstructorInvocation
	 */
	public static ExecutionPoint create(SuperConstructorInvocation astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.SUPER_CONSTRUCTOR_INVOCATION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a TypeDeclarationStatement. 
	 * We DO NOT create CFG for those methods defined in a TypeDeclarationStatement which is in a method body.
	 */
	public static ExecutionPoint create(TypeDeclarationStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.TYPE_DECLARATION;
		String description = astNode.getDeclaration().getName().getIdentifier(); // The description is the type name declared by this statement
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for a VariableDeclarationStatement
	 */
	public static ExecutionPoint create(VariableDeclarationStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.VARIABLE_DECLARATION;
		String description = StatementCFGCreatorHelper.astNodeToString(astNode);
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create an execution point for an Expression. So far we do not check there are method calls in the expression. Possibly we need
	 * more detailed implementation for creating an execution point for an expression in near future, since an expression may has 
	 * complex structures
	 */
	public static ExecutionPoint create(Expression astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.EXPRESSION;
		String description = astNode.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.NORMAL, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create a predicate execution point for enhanced for statement.
	 */
	public static ExecutionPoint createPredicate(EnhancedForStatement node) {
		SingleVariableDeclaration variable = node.getParameter();
		Expression exp = node.getExpression();
		
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(exp, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(exp, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.ENHANCED_FOR_PREDICATE;
		String description = variable.getName().getIdentifier() + " : " + exp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.LOOP_PREDICATE, exp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for if statement.
	 */
	public static ExecutionPoint createPredicate(IfStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.IF_PREDICATE;
		ExecutionPointType type = ExecutionPointType.BRANCH_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, root, fileName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for switch statement.
	 */
	public static ExecutionPoint createPredicate(SwitchStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.SWITCH_PREDICATE;
		ExecutionPointType type = ExecutionPointType.BRANCH_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, root, fileName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create a predicate execution point for do statement.
	 */
	public static ExecutionPoint createPredicate(DoStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.DO_WHILE_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, root, fileName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for while statement.
	 */
	public static ExecutionPoint createPredicate(WhileStatement node) {
		Expression condExp = node.getExpression();
		String label = ExecutionPointLabel.WHILE_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(condExp, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(condExp, root, fileName);
		String id = startLocation.toString();
		String description = condExp.toString();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a predicate execution point for for statement.
	 */
	public static ExecutionPoint createPredicate(ForStatement node) {
		String label = ExecutionPointLabel.FOR_PREDICATE;
		ExecutionPointType type = ExecutionPointType.LOOP_PREDICATE;
		Expression condExp = node.getExpression();
		String id = null;
		String description = null;
		SourceCodeLocation startLocation = null;
		SourceCodeLocation endLocation = null;
		if (condExp != null) {
			startLocation = SourceCodeLocation.getStartLocation(condExp, root, fileName);
			endLocation = SourceCodeLocation.getEndLocation(condExp, root, fileName);
			id = startLocation.toString();
			description = condExp.toString();
		} else {
			// The for statement may have not condition expression, such as for (;;) {...}
			// In this case, we use the start position of the for statement as the start position of its condition expression
			startLocation = SourceCodeLocation.getStartLocation(node, root, fileName);
			endLocation = SourceCodeLocation.getEndLocation(node, root, fileName);
			id = startLocation.toString();
			description = "true";
		}
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, type, condExp);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for do statement
	 */
	public static ExecutionPoint createVirtualStart(DoStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.DO_START;
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for try block statement
	 */
	public static ExecutionPoint createVirtualStart(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_BLOCK_START;
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for labeled statement
	 */
	public static ExecutionPoint createVirtualStart(LabeledStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.LABEL_START + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for synchronize statement
	 */
	public static ExecutionPoint createVirtualStart(SynchronizedStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SYNCHRONIZE_START;
		String description = astNode.getExpression().toString();

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.GROUP_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for switch statement
	 */
	public static ExecutionPoint createVirtualEnd(SwitchStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SWITCH_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual start node for if statement
	 */
	public static ExecutionPoint createVirtualEnd(IfStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.IF_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for do statement
	 */
	public static ExecutionPoint createVirtualEnd(DoStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.DO_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for while statement
	 */
	public static ExecutionPoint createVirtualEnd(WhileStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.WHILE_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for for statement
	 */
	public static ExecutionPoint createVirtualEnd(ForStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.FOR_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for enhanced for statement
	 */
	public static ExecutionPoint createVirtualEnd(EnhancedForStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.ENHANCED_FOR_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for try block statement
	 */
	public static ExecutionPoint createVirtualEnd(TryStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_BLOCK_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for labeled statement
	 */
	public static ExecutionPoint createVirtualEnd(LabeledStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.LABEL_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for synchronized statement
	 */
	public static ExecutionPoint createVirtualEnd(SynchronizedStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.SYNCHRONIZE_END;

		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create a virtual start node for the finally block of try statement
	 */
	public static ExecutionPoint createFinallyStart(Statement astNode) {
		if (astNode.getNodeType() != ASTNode.TRY_STATEMENT) throw new AssertionError("Meet unexpected statement type when create virtual start for finally block: " + astNode);

		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.FINALLY_START + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.FINALLY_START;
		
		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create a virtual end node for the finally block of try statement, this node is also the end node of the entire try statement!
	 */
	public static ExecutionPoint createFinallyEnd(Statement astNode) {
		if (astNode.getNodeType() != ASTNode.TRY_STATEMENT) throw new AssertionError("Meet unexpected statement type when create virtual end for finally block: " + astNode);

		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = ExecutionPointLabel.TRY_END + ExecutionPointLabel.ID_SEPERATOR + startLocation.toString();
		String label = ExecutionPointLabel.TRY_END;
		
		ExecutionPoint value = new ExecutionPoint(id, label, label, ExecutionPointType.GROUP_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create node for break statement
	 */
	public static ExecutionPoint createFlowControlNode(BreakStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.BREAK_LABEL;
		if (astNode.getLabel() != null) label = label + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create node for continue statement
	 */
	public static ExecutionPoint createFlowControlNode(ContinueStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.CONTINUE_LABEL;
		if (astNode.getLabel() != null) label = label + astNode.getLabel().getIdentifier();
		String description = label;

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create node for return statement
	 */
	public static ExecutionPoint createFlowControlNode(ReturnStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.RETURN_LABEL;
		String description = StatementCFGCreatorHelper.astNodeToString(astNode);

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}

	/**
	 * Create node for return statement
	 */
	public static ExecutionPoint createFlowControlNode(ThrowStatement astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.THROW_LABEL;
		String description = astNode.toString();

		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.FLOW_CONTROLLER, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create the start node for the entire method
	 */
	public static ExecutionPoint createStart(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.START;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_START, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create the end node for the entire method
	 */
	public static ExecutionPoint createEnd(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.END;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
	
	/**
	 * Create the abnormal end node for the entire method
	 */
	public static ExecutionPoint createAbnormalEnd(MethodDeclaration astNode) {
		SourceCodeLocation startLocation = SourceCodeLocation.getStartLocation(astNode, root, fileName);
		SourceCodeLocation endLocation = SourceCodeLocation.getEndLocation(astNode, root, fileName);
		String id = startLocation.toString();
		String label = ExecutionPointLabel.ABNORMAL_END;
		String description = astNode.getName().getIdentifier();
		
		ExecutionPoint value = new ExecutionPoint(id, label, description, ExecutionPointType.CFG_END, astNode);
		value.setStartPosition(startLocation);
		value.setEndPosition(endLocation);
		return value;
	}
}
