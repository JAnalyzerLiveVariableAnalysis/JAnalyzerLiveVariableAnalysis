package nameTable.nameReference.referenceGroup;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to class instance creation expression. 
 * <p>		[ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression { , Expression } ] ) [ AnonymousClassDeclaration ]
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGClassInstanceCreation extends NameReferenceGroup {

	public NRGClassInstanceCreation(String name, SourceCodeLocation location) {
		super(name, location);
		// TODO Auto-generated constructor stub
	}

	public NRGClassInstanceCreation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The class instance creation reference group " + this.toFullString() + " has not sub-references!");
		
		// Bind the group to the type definition of the first or second reference (which is a type reference) of 
		// the class instance expression
		NameReference firstRef = subreferences.get(0);
		NameReferenceKind firstRefKind = firstRef.getReferenceKind();
		if (firstRefKind == NameReferenceKind.NRK_TYPE){
			// The group is binded to the type definition of the first reference
			TypeReference typeRef = (TypeReference)firstRef;
			if (!typeRef.isResolved()) typeRef.resolveBinding();
			bindTo(typeRef.getDefinition());
			
			MethodReference methodRef = (MethodReference)subreferences.get(1);
			// Set the parameters of the constructor
			if (subreferences.size() > 2) {
				List<NameReference> argList = subreferences.subList(2, subreferences.size());
				for (NameReference argRef : argList) {
					argRef.resolveBinding();
				}
				methodRef.setArguments(argList);
			}
			if (typeRef.isResolved()) {
				// Resolved the constructor invocation in the definition of typeRef
				TypeDefinition typeDef = (TypeDefinition)typeRef.getDefinition();
				if (typeDef.isDetailedType()) {
					DetailedTypeDefinition detailedTypeDef = (DetailedTypeDefinition)typeDef;
					detailedTypeDef.resolve(methodRef);
				}
			}
		} else {
			NameReference secondRef = subreferences.get(1);
			if (secondRef != null && secondRef.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
				TypeReference typeRef = (TypeReference)secondRef;
				if (!typeRef.isResolved()) {
					// We need resolve the type reference in the scope of the type of the first reference
					TypeDefinition varTypeDef = getResultTypeDefinition(firstRef);
					if (varTypeDef != null) varTypeDef.resolve(typeRef);
				}
				bindTo(typeRef.getDefinition());

				MethodReference methodRef = (MethodReference)subreferences.get(2);
				// Set the parameters of the constructor
				if (subreferences.size() > 3) {
					List<NameReference> argList = subreferences.subList(3, subreferences.size());
					for (NameReference argRef : argList) {
						argRef.resolveBinding();
					}
					methodRef.setArguments(argList);
				}
				if (typeRef.isResolved()) {
					// Resolved the constructor invocation in the definition of typeRef
					TypeDefinition typeDef = (TypeDefinition)typeRef.getDefinition();
					if (typeDef.isDetailedType()) {
						DetailedTypeDefinition detailedTypeDef = (DetailedTypeDefinition)typeDef;
						detailedTypeDef.resolve(methodRef);
					}
				}
			} else {
				throw new AssertionError("The first and second operand in class instance createion expression " + this.toFullString() + " is not a type!");
			}
		}

		return isResolved();
	}
}
