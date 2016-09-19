package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to conditional expression. 
 * <p>		ConditionalExpression: Expression ? Expression : Expression
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGConditional extends NameReferenceGroup {

	public NRGConditional(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NRGConditional(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_CONDITIONAL;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The conditional reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		NameReference secondRef = subreferences.get(1);
		NameReference thirdRef = subreferences.get(2);
		TypeDefinition secondType = getResultTypeDefinition(secondRef);
		TypeDefinition thirdType = getResultTypeDefinition(thirdRef);
		if (secondType != null && thirdType != null) {
			if (secondType.isSubtypeOf(thirdType)) bindTo(thirdType);
			else bindTo(secondType);
		} else if (secondType != null) bindTo(secondType);
		else if (thirdType != null) bindTo(thirdType);

		return isResolved();
	}
}
