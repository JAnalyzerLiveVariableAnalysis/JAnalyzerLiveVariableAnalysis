package nameTable.creator;

import java.util.List;

import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.TypeReference;
import nameTable.nameReference.ValueReference;
import nameTable.nameReference.referenceGroup.NRGArrayAccess;
import nameTable.nameReference.referenceGroup.NRGArrayCreation;
import nameTable.nameReference.referenceGroup.NRGArrayInitializer;
import nameTable.nameReference.referenceGroup.NRGAssignment;
import nameTable.nameReference.referenceGroup.NRGCast;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;
import nameTable.nameReference.referenceGroup.NRGConditional;
import nameTable.nameReference.referenceGroup.NRGFieldAccess;
import nameTable.nameReference.referenceGroup.NRGInfixExpression;
import nameTable.nameReference.referenceGroup.NRGInstanceof;
import nameTable.nameReference.referenceGroup.NRGMethodInvocation;
import nameTable.nameReference.referenceGroup.NRGPostfixExpression;
import nameTable.nameReference.referenceGroup.NRGPrefixExpression;
import nameTable.nameReference.referenceGroup.NRGQualifiedName;
import nameTable.nameReference.referenceGroup.NRGSuperFieldAccess;
import nameTable.nameReference.referenceGroup.NRGSuperMethodInvocation;
import nameTable.nameReference.referenceGroup.NRGThisExpression;
import nameTable.nameReference.referenceGroup.NRGTypeLiteral;
import nameTable.nameReference.referenceGroup.NRGVariableDeclaration;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.NameScope;

import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import util.SourceCodeLocation;

/**
 * An expression visitor for creating all name references in an expression or statement. The result will be a name reference
 * group which includes all name reference in the expression or statement. 
 * <p>Note that we will use the same location to generate all name references in the group, so the locations of 
 * the references are not the precise location of the name reference occurring in the source code file.
 * @author Zhou Xiaocong
 * @since 2013-4-13
 * @version 1.0
 */
public class ReferenceASTVisitor extends ASTVisitor {
	// The result reference corresponding to the expression (or the statement) is the last reference in the travel.
	// Note that the result reference may be a reference group!
	private NameReference lastReference = null;
	
	private NameScope scope = null;
	private CompilationUnit root = null;
	private String unitFullName = null;
	protected TypeASTVisitor typeVisitor = null;
	
	public ReferenceASTVisitor(String unitFullName, CompilationUnit root, NameScope scope) {
		this.scope = scope;
		this.root = root;
		this.unitFullName = unitFullName;
		
		typeVisitor = new TypeASTVisitor(unitFullName, root, scope);
	}
	
	public NameReference getResult() {
		return lastReference;
	}
	
	public void reset(String unitFullName, CompilationUnit root, NameScope scope) {
		lastReference = null;

		this.scope = scope;
		this.root = root;
		this.unitFullName = unitFullName;
		typeVisitor.reset(unitFullName, root, scope);
	}

	/**
	 * ArrayAccess: Expression[Expression]
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGArrayAccess(name, location, scope);

		// Visit the array expression of the node
		Expression arrayExpression = node.getArray();
		arrayExpression.accept(this);
		// Add the references corresponding to array expressions to the reference group
		referenceGroup.addSubReference(lastReference);

		// Visit the index expression of the node
		Expression indexExpression = node.getIndex();
		indexExpression.accept(this);
		// Add the references corresponding to index expressions to the reference group
		referenceGroup.addSubReference(lastReference);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ArrayCreation: new Type[Expression] ArrayInitializer
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ArrayCreation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGArrayCreation(name, location, scope);

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);

		// Visit the dimension expressions of the node
		List<Expression> dimensionExpressions = node.dimensions();
		for (Expression dimensionExp : dimensionExpressions) {
			dimensionExp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}
		
		// Visit the initializer of the node
		ArrayInitializer initializer = node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
			// Get the name reference corresponding to array initializer
			referenceGroup.addSubReference(lastReference);
		}
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ArrayInitializer: { Expression, Expression, .. }
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ArrayInitializer node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGArrayInitializer(name, location, scope);
		
		// Visit the initial expressions of the node
		List<Expression> initExpressions = node.expressions();
		for (Expression initExp : initExpressions) {
			initExp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Assignment: Expression AssignmentOperator Expression
	 */
	public boolean visit(Assignment node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGAssignment(name, location, scope);
		
		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);
		
		// Visit the left expressions of the node
		Expression leftExp = node.getLeftHandSide();
		leftExp.accept(this);
		lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// Visit the right expressions of the node
		Expression rightExp = node.getRightHandSide();
		rightExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * CastExpression: (Type) Expression
	 */
	public boolean visit(CastExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGCast(name, location, scope);

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);

		// Visit the expressions of the node
		Expression exp = node.getExpression();
		exp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * ClassInstanceCreation: [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(ClassInstanceCreation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGClassInstanceCreation(name, location, scope);

		// Visit the possible expressions of the node
		Expression exp = node.getExpression();
		if (exp != null) {
			exp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// Visit the type of the node
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);

		String methodName = typeRef.getName();
		location = SourceCodeLocation.getStartLocation(type, root, unitFullName);
		MethodReference methodRef = new MethodReference(methodName, location, scope);
		// Add method (constructor invocation) reference to the group
		referenceGroup.addSubReference(methodRef);

		// Visit the argument list of the node
		List<Expression> arguments = node.arguments();
		for (Expression arg : arguments) {
			arg.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// We ignore the anonymous class declaration in the node!
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * ConditionalExpression: Expression ? Expression : Expression
	 */
	public boolean visit(ConditionalExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGConditional(name, location, scope);

		// Visit the condition expressions of the node
		Expression condition = node.getExpression();
		condition.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the condition expressions of the node
		Expression thenExp = node.getThenExpression();
		thenExp.accept(this);
		referenceGroup.addSubReference(lastReference);
	
		// Visit the condition expressions of the node
		Expression elseExp = node.getElseExpression();
		elseExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReference() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * FieldAccess: Expression.Identifier ;
	 */
	public boolean visit(FieldAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGFieldAccess(name, location, scope);

		// Visit the expressions of the node
		Expression expression = node.getExpression();
		expression.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		NameReference fieldNameRef = createReferenceForName(node.getName(), NameReferenceKind.NRK_FIELD);
		referenceGroup.addSubReference(fieldNameRef);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Expression InfixOperator Expression { InfixOperator Expression }
	 * Use the expression visitor to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(InfixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGInfixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);
		
		// Visit the left operand expressions of the node
		Expression leftExp = node.getLeftOperand();
		leftExp.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the left operand expressions of the node
		Expression rightExp = node.getRightOperand();
		rightExp.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		// Visit the extended operand of the node
		if (node.hasExtendedOperands()) {
			List<Expression> extendedOperands = node.extendedOperands();
			for (Expression operand : extendedOperands) {
				operand.accept(this);
				referenceGroup.addSubReference(lastReference);
			}
		}

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReference() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * InstanceofExpression: Expression instanceof Type
	 * Use the expression visitor to visit the node
	 */
	public boolean visit(InstanceofExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGInstanceof(name, location, scope);

		// Visit the left operand expressions of the node
		Expression leftExp = node.getLeftOperand();
		leftExp.accept(this);
		referenceGroup.addSubReference(lastReference);

		// Visit the type (i.e. the right operand) of the node
		Type type = node.getRightOperand();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		// Add type reference to the group
		referenceGroup.addSubReference(typeRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression visitor to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(MethodInvocation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGMethodInvocation(name, location, scope);

		// Visit the expressions of the node
		Expression expression = node.getExpression();
		if (expression != null) {
			expression.accept(this);
			referenceGroup.addSubReference(lastReference);
		}
		
		String methodName = node.getName().getFullyQualifiedName();
		MethodReference methodNameRef = new MethodReference(methodName, location, scope);
		referenceGroup.addSubReference(methodNameRef);
	
		// Visit the arguments of the method invocation
		List<Expression> arguments = node.arguments();
		for (Expression arg : arguments) {
			arg.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * PostfixExpression: Expression PostfixOperator
	 */
	public boolean visit(PostfixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGPostfixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);

		// Visit the expressions of the node
		Expression expression = node.getOperand();
		expression.accept(this);
		lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReference() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * PrefixExpression: PrefixOperator Expression 
	 */
	public boolean visit(PrefixExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGPrefixExpression(name, location, scope);

		// Set the operator of the reference group
		String operator = node.getOperator().toString();
		referenceGroup.setOperator(operator);

		// Visit the expressions of the node
		Expression expression = node.getOperand();
		expression.accept(this);
		if (operator.equals(NameReferenceGroup.OPERATOR_INCREMENT) || operator.equals(NameReferenceGroup.OPERATOR_DECREMENT))
			lastReference.setLeftValueReference();
		referenceGroup.addSubReference(lastReference);

		// The reference group is the result reference of the node, save it to the lastReference
		// If there is no sub reference in the referenceGroup, then it represent a constant expression, we do not add it 
		// to the reference list!
		if (referenceGroup.getSubReference() != null) lastReference = referenceGroup;
		else lastReference = null;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 */
	public boolean visit(QualifiedName node) {
		// Create a reference group for the node
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGQualifiedName(name, location, scope);

		// Visit the expressions of the node
		Name qualifier = node.getQualifier();
		qualifier.accept(this);
		referenceGroup.addSubReference(lastReference);
		
		NameReference simpleNameRef = createReferenceForName(node.getName(), NameReferenceKind.NRK_VARIABLE);
		referenceGroup.addSubReference(simpleNameRef);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * Create a variable reference for the fully qualified name of the node
	 */
	public boolean visit(SimpleName node) {
		// Create a reference for the node
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReference reference = new ValueReference(name, location, scope, NameReferenceKind.NRK_VARIABLE);

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	

	/**
	 * SuperFieldAccess: [ ClassName . ] super . Identifier
	 * Use the expression to visit the node
	 */
	public boolean visit(SuperFieldAccess node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGSuperFieldAccess(name, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			String className = classNameNode.getFullyQualifiedName();
			location = SourceCodeLocation.getStartLocation(classNameNode, root, unitFullName);
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);
		}
		
		// Create a field reference for field name in the node
		Name fieldNameNode = node.getName();
		String fieldName = fieldNameNode.getFullyQualifiedName();
		location = SourceCodeLocation.getStartLocation(fieldNameNode, root, unitFullName);
		NameReference fieldRef = new ValueReference(fieldName, location, scope, NameReferenceKind.NRK_FIELD);
		referenceGroup.addSubReference(fieldRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
	 * Use the expression to visit the node
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(SuperMethodInvocation node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGSuperMethodInvocation(name, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			String className = classNameNode.getFullyQualifiedName();
			location = SourceCodeLocation.getStartLocation(classNameNode, root, unitFullName);
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);
		}
		
		// Create a method reference for field name in the node
		Name methodNameNode = node.getName();
		String methodName = methodNameNode.getFullyQualifiedName();
		location = SourceCodeLocation.getStartLocation(methodNameNode, root, unitFullName);
		MethodReference methodRef = new MethodReference(methodName, location, scope);
		referenceGroup.addSubReference(methodRef);
		
		// Visit the argument expressions of the node
		List<Expression> arguments = node.arguments();
		for (Expression argExp : arguments) {
			argExp.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * ThisExpression : [ClassName.] this
	 * Create a literal reference for the keyword "this" and a type reference for the class name 
	 * in the expression
	 */
	public boolean visit(ThisExpression node) {
		// Create a literal reference for the key word 
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		LiteralReference thisReference = new LiteralReference(NameReferenceLabel.KEYWORD_THIS, NameReferenceLabel.KEYWORD_THIS, location, scope);
		
		// Create a type reference for class name in the node
		Name classNameNode = node.getQualifier();
		if (classNameNode != null) {
			// Create a reference group for the node
			String name = node.toString();
			NameReferenceGroup referenceGroup = new NRGThisExpression(name, location, scope);
			
			String className = classNameNode.getFullyQualifiedName();
			location = SourceCodeLocation.getStartLocation(classNameNode, root, unitFullName);
			TypeReference classRef = new TypeReference(className, location, scope);
			referenceGroup.addSubReference(classRef);

			referenceGroup.addSubReference(thisReference);

			// The reference group is the result reference of the node, save it to the lastReference
			lastReference = referenceGroup;
		} else {
			// The reference group is the result reference of the node, save it to the lastReference
			lastReference = thisReference;
		}
		
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * VariableDeclarationExpression: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * <p>VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGVariableDeclaration(name, location, scope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// This visitor do not need to define the variable to the current scope
			// creator.defineVariable(varNode, typeRef, scope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				initializer.accept(this);
				referenceGroup.addSubReference(lastReference);
			}
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(BooleanLiteral node) {
		// Create a reference for the node
		String literal = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		LiteralReference reference = new LiteralReference(literal, NameReferenceLabel.TYPE_BOOLEAN, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	
	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(CharacterLiteral node) {
		// Create a reference for the node
		String literal = node.getEscapedValue();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		LiteralReference reference = new LiteralReference(literal, NameReferenceLabel.TYPE_CHAR, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(NullLiteral node) {
		// Create a reference for the node
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		LiteralReference reference = new LiteralReference(NameReferenceLabel.KEYWORD_NULL, NameReferenceLabel.KEYWORD_NULL, location, scope);
		
		// The reference is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(NumberLiteral node) {
		// Create a reference for the node
		String literal = node.getToken();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		
		// Judge the type of the literal, we use the simplest way to get it!
		String typeName = NameReferenceLabel.TYPE_INT;
		if (literal.contains("l") || literal.contains("L")) typeName = NameReferenceLabel.TYPE_LONG;
		else if (literal.contains(".") || literal.contains("e") || literal.contains("E")) typeName = NameReferenceLabel.TYPE_DOUBLE;
		else if (literal.contains("f") || literal.contains("F")) typeName = NameReferenceLabel.TYPE_FLOAT;

		LiteralReference reference = new LiteralReference(literal, typeName, location, scope);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Create a literal reference for the node
	 */
	public boolean visit(StringLiteral node) {
		// Create a reference for the node
		String name = node.getEscapedValue();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		LiteralReference reference = new LiteralReference(name, NameReferenceLabel.TYPE_STRING, location, scope);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = reference;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * TypeLiteral: ( Type | void ) . class
	 */
	public boolean visit(TypeLiteral node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGTypeLiteral(name, location, scope);

		typeVisitor.reset(unitFullName, root, scope);
		node.getType().accept(typeVisitor);
		NameReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * SingleVariableDeclaration: { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
	 * Create a reference for the type reference in the declaration, and visit the initializer of the declaration
	 */
	public boolean visit(SingleVariableDeclaration node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGVariableDeclaration(name, location, scope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// Here we do not need to define the variable to the current scope
		// creator.defineVariable(node, typeRef, currentScope);
		
		// Visit the initializer in the variable declaration
		Expression initializer = node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
			referenceGroup.addSubReference(lastReference);
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}
	
	/**
	 * VariableDeclarationStatement: { ExtendedModifier } Type VariableDeclarationFragment  { , VariableDeclarationFragment }
	 * VariableDeclarationFragment:  Identifier { [] } [ = Expression ]
	 * Do the same things as to visit VariableDeclarationExpression!
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationStatement node) {
		// Create a reference group for the node
		String name = node.toString();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReferenceGroup referenceGroup = new NRGVariableDeclaration(name, location, scope);

		// Get the type reference for the variable declaration
		Type type = node.getType();
		typeVisitor.reset(unitFullName, root, scope);
		type.accept(typeVisitor);
		TypeReference typeRef = typeVisitor.getResult();
		referenceGroup.addSubReference(typeRef);
		
		// Visit the variable list defined in the node
		List<VariableDeclarationFragment> fragments = node.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			// This visitor do not need to define the variable to the current scope
			// creator.defineVariable(varNode, typeRef, scope);
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				initializer.accept(this);
				referenceGroup.addSubReference(lastReference);
			}
		}

		// The reference group is the result reference of the node, save it to the lastReference
		lastReference = referenceGroup;
		// The children of the node have been visited, so we do not need to goto its children
		return false;
	}

	/**
	 * Ignore this kind of node
	 */
	public boolean visit(TypeDeclarationStatement node) {
		return false;
	}

	/**
	 * Create a name reference for a name node
	 */
	private NameReference createReferenceForName(Name node, NameReferenceKind kind) {
		String name = node.getFullyQualifiedName();
		SourceCodeLocation location = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		NameReference result = new ValueReference(name, location, scope, kind);
		return result;
	}
}
