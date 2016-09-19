package nameTable.nameReference.referenceGroup;

import util.SourceCodeLocation;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;

/**
 * The name reference group corresponds to array access expression. 
 * <p>	ArrayAccess: Expression[Expression]
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGArrayAccess extends NameReferenceGroup{

	public NRGArrayAccess(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	public NRGArrayAccess(String name, SourceCodeLocation location) {
		super(name, location);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_ARRAY_ACCESS;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (subreferences == null) throw new AssertionError("The array access reference group " + this.toFullString() + " has not sub-references!");
		
		for (NameReference reference : subreferences) reference.resolveBinding();
		// Bind the group to the type definition of the variable definition in the first operand of 
		// the array access expression
		NameReference firstRef = subreferences.get(0);
		TypeDefinition resultTypeDef = getResultTypeDefinition(firstRef);
		bindTo(resultTypeDef);

		return isResolved();
	}
}
