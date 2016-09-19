package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.LiteralReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to infix expression. 
 * <p>		Expression InfixOperator Expression { InfixOperator Expression }
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGInfixExpression extends NameReferenceGroup {

	public NRGInfixExpression(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NRGInfixExpression(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_INFIX_EXPRESSION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences != null) 
			for (NameReference reference : subreferences) reference.resolveBinding();
		
		NameReference firstRef = null;
		NameReference secondRef = null;
		if (subreferences.size() > 0) firstRef = subreferences.get(0);
		if (subreferences.size() > 1) secondRef = subreferences.get(1);

		if (operator.equals(OPERATOR_DECREMENT) || operator.equals(OPERATOR_INCREMENT) || operator.equals(OPERATOR_PLUS) || operator.equals(OPERATOR_MINUS) ||
				operator.equals(OPERATOR_DIVIDE) || operator.equals(OPERATOR_TIMES) || operator.equals(OPERATOR_REMAINDER) || operator.equals(OPERATOR_AND) ||
				operator.equals(OPERATOR_COMPLEMENT) || operator.equals(OPERATOR_OR) || operator.equals(OPERATOR_XOR)) {
			if (firstRef != null && secondRef != null) {
				TypeDefinition firstType = getResultTypeDefinition(firstRef);
				TypeDefinition secondType = getResultTypeDefinition(secondRef);
				if (firstType != null && secondType != null) {
					if (firstType.isSubtypeOf(secondType)) bindTo(secondType);
					else bindTo(firstType);
				}
			} else if (firstRef != null) {
				TypeDefinition firstType = getResultTypeDefinition(firstRef);
				bindTo(firstType);
			}
		} else if (operator.equals(OPERATOR_LEFT_SHIFT) || operator.equals(OPERATOR_RIGHT_SHIFT_SIGNED) || operator.equals(OPERATOR_RIGHT_SHIFT_UNSIGNED)) {
			if (firstRef != null) {
				TypeDefinition firstType = getResultTypeDefinition(firstRef);
				bindTo(firstType);
			}
		} else {
			// All other operators are relation operators, bind to the type definition object represent boolean type
			LiteralReference booleanLiteral = new LiteralReference(NameReferenceLabel.TYPE_BOOLEAN, NameReferenceLabel.TYPE_BOOLEAN, null, scope);
			booleanLiteral.resolveBinding();
			bindTo(booleanLiteral.getDefinition());
		}

		return isResolved();
	}
	
}
