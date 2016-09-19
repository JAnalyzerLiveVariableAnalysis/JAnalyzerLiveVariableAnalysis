package nameTable.nameReference.referenceGroup;

import java.util.List;

import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The name reference group corresponds to method invocation expression. 
 * <p>		MethodInvocation: [ Expression . ] [ < Type { , Type } > ] Identifier ( [ Expression { , Expression } ] ) 
 * @author Zhou Xiaocong
 * @since 2013-3-13
 * @version 1.0
 */
public class NRGMethodInvocation extends NameReferenceGroup {

	public NRGMethodInvocation(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NRGMethodInvocation(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceGroupKind getGroupKind() {
		return NameReferenceGroupKind.NRGK_METHOD_INVOCATION;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;

		if (subreferences == null) throw new AssertionError("The method invocation reference group " + this.toFullString() + " has not sub-references!");
		
		// For method invocation, we should resolve reference before the method reference at first, 
		// it should be binded to a type definition, and then resolve the method reference in this 
		// definition with the remain references as the parameter!
		NameReference firstRef = subreferences.get(0);
		MethodReference methodRef = null;
		if (firstRef.getReferenceKind() != NameReferenceKind.NRK_METHOD) {
			methodRef = (MethodReference)subreferences.get(1);
			if (subreferences.size() > 2) {
				List<NameReference> argList = subreferences.subList(2, subreferences.size());
				for (NameReference argRef : argList) {
					argRef.resolveBinding();
				}
				methodRef.setArguments(argList);
			}
			// The first expression gives the object to call the method, we should resolve the method reference
			// in the type definition binded to the first expression
			TypeDefinition typeDef = null;
			firstRef.resolveBinding();
			if (firstRef.isResolved()) typeDef = getResultTypeDefinition(firstRef);
			else {
				// The first expression may be a class name (i.e. use a class name to call its static method!
				if (firstRef.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
					// This means the expression is a simple name, and when we create the reference, we set its kind to be NRK_VARIABLE
					// However, a simple name may be a class name! we set the kind to be NRK_TYPE, and try to resolve it!
					firstRef.setReferenceKind(NameReferenceKind.NRK_TYPE);
					firstRef.resolveBinding();
					if (firstRef.isResolved()) typeDef = getResultTypeDefinition(firstRef);
					else firstRef.setReferenceKind(NameReferenceKind.NRK_VARIABLE);   // We can not resolve it as a class name, restore its kind to NRK_VARIABLE! 
				}
			}
			
			if (typeDef != null) {
				typeDef.resolve(methodRef);
//				if (!methodRef.isResolved()) {
//					System.out.println("methodRef " + methodRef.getName() + " can not be resloved in type: " + typeDef.getFullQualifiedName());
//					NameDefinition firstDef = firstRef.getDefinition();
//					System.out.println("The first ref is " + firstRef.getName() + ", which is bind to " + firstRef.getDefinition().toString());
//					if (firstDef.getDefinitionKind() == NameDefinitionKind.NDK_PARAMETER) {
//						VariableDefinition varDef = (VariableDefinition) firstDef;
//						System.out.println("First definition's type ref is " + varDef.getType().getName() + ", and its bind to: " + varDef.getType().getDefinition());
//					}
//				}
				
			} else {
//				if (firstRef.isResolved()) System.out.println("First ref is bind to " + firstRef.getDefinition().toString() + ", but can not get its result type definition!");
//				else System.out.println("Can not resolve first ref " + firstRef.getName());
			}
		} else {
			// The first expression is the method reference, we resolve the method reference in the 
			// current scope
			methodRef = (MethodReference)firstRef;
			if (subreferences.size() > 1) {
				List<NameReference> argList = subreferences.subList(1, subreferences.size());
				for (NameReference argRef : argList) argRef.resolveBinding();
				methodRef.setArguments(argList);
			}
			methodRef.resolveBinding();
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
