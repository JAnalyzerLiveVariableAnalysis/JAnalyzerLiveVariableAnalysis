package nameTable.nameReference.referenceGroup;

import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to super method invocation expression. 
 * <p>		SuperMethodInvocation: [ ClassName . ] super .  [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] )
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGSuperMethodInvocation extends NameReferenceGroup {

	public NRGSuperMethodInvocation(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NRGSuperMethodInvocation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_SUPER_METHOD_INVOCATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The super method invocation reference group " + this.toFullString() + " has not sub-references!");
		
		// For super method invocation, if the first reference is a type reference, then resolve the reference, 
		// bind it to a type definition, and get the super type of this type definition, and resolve the 
		// second reference in the super type.
		// If the first reference is not a type reference, then find the first type definition enclosing 
		// the reference, and resolve the reference in the super type of the type definition
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		TypeDefinition superTypeDef = null;
		int startIndex = 0;
		TypeDefinition typeDef = null;
		if (firstRef.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			TypeReference typeRef = (TypeReference)firstRef;
			typeRef.resolveBinding();
			typeDef = (TypeDefinition)typeRef.getDefinition();
			methodRef = (MethodReference)subreferences.get(1);
			startIndex = 2;
		} else {
			typeDef = getEnclosingTypeDefinition();
			methodRef = (MethodReference)firstRef;
			startIndex = 1;
		}
		if (typeDef != null) {
			superTypeDef = typeDef.getSuperClassDefinition();
		}
		if (superTypeDef != null) {
			if (subreferences.size() > startIndex) {
				methodRef.setArguments(subreferences.subList(startIndex, subreferences.size()));
			}
			superTypeDef.resolve(methodRef);
		}
		
		// Bind the group reference to the return type of the method 
		if (methodRef.isResolved()) {
			MethodDefinition methodDef = (MethodDefinition)methodRef.getDefinition();
			TypeReference typeRef = methodDef.getReturnType();
			if (typeRef != null) {
				typeRef.resolveBinding();
				bindTo(typeRef.getDefinition());
			}
		}

		return isResolved();
	}
}
