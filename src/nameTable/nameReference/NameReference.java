package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import sourceCodeAST.SourceCodeLocation;

/**
 * The abstract base class for the class representing a name reference
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2015/11/6
 * 		Refactor the class according to the design document
 */
public class NameReference implements Comparable<NameReference> {
	// The name of the reference, it maybe contains '.' as a partial qualified name
	protected String name = null;	
	// The location of the reference in the source code. 
	protected SourceCodeLocation location = null;
	// The definition object which the reference bind to
	protected NameDefinition definition = null;
	protected NameScope scope = null;
	protected NameReferenceKind kind = null;
	
	public NameReference(String name, SourceCodeLocation location, NameScope scope) {
		this.name = name;
		this.location = location;
		this.scope = scope;
	}

	public NameReference(String name, SourceCodeLocation location, NameScope scope, NameReferenceKind kind) {
		this.name = name;
		this.location = location;
		this.scope = scope;
		this.kind = kind;
	}

	/**
	 * Return the name of the reference
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the location in the source code of the reference
	 */
	public SourceCodeLocation getLocation() {
		return location;
	}

	/**
	 * Return the definition object which the reference binded to
	 */
	public NameDefinition getDefinition() {
		return definition;
	}

	/**
	 * Return the scope of the reference 
	 */
	public NameScope getScope() {
		return scope;
	}

	/**
	 * Return the kind of the reference
	 */
	public NameReferenceKind getReferenceKind() {
		return kind;
	}

	/**
	 * Bind the reference to the give name definition
	 */
	public void bindTo(NameDefinition definition) {
		this.definition = definition;
	}

	/**
	 * <p>Resolve the current reference. Provide this method is for redefining in a reference group to resolve
	 * the group according its syntax structure.
	 * 
	 * <p>Important Note: In resolveBinding() method of references, we may call resolve() method provided by 
	 * name scope. So the implementation of resolve() method in name scope can NOT call resolveBinding() method
	 * of reference in order to avoid method calling in self-circulation.
	 */
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (scope != null) { 
			return scope.resolve(this);
		} else return false;
	}
	
	/**
	 * Set the kind of the reference
	 */
	public void setReferenceKind(NameReferenceKind kind) {
		this.kind = kind;
	}
	
	/**
	 * Test if the reference has been resolved, i.e. test if the reference is binded to a name definition
	 */
	public boolean isResolved() {
		if (definition == null) return false;
		return true;
	}

	/**
	 * Return the type definition enclosing this name reference. Generally, any reference should occur in
	 * a type definition.
	 */
	public TypeDefinition getEnclosingTypeDefinition() {
		NameScope currentScope = scope;
		while (currentScope != null) {
			if (currentScope.getScopeKind() == NameScopeKind.NSK_TYPE) return (TypeDefinition)currentScope; 
			currentScope = currentScope.getEnclosingScope();
		}
		return null;
	}
	
	/**
	 * Get the result type definition for a reference which is regarded as an expression. 
	 * <OL><LI>If the reference is bind to a variable, field or a parameter definition, then return the type definition of the variable
	 * <LI>If the reference is bind to a method definition, then return the return type of the method
	 * <LI>Otherwise return null</OL>
	 */
	public TypeDefinition getResultTypeDefinition() {
		if (!isResolved()) resolveBinding();
		
		NameDefinition nameDef = getDefinition();
		if (nameDef == null) return null;
		
		NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_TYPE) return (TypeDefinition)nameDef;
		else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)nameDef;
			return fieldDef.getTypeDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)nameDef;
			return varDef.getTypeDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)nameDef;
			return methodDef.getReturnTypeDefinition();
		} else return null;
	}
	
	/**
	 * Return all reference at the leaf in the name reference. Many references indeed include other references as parts of
	 * themselves. This method will return the basic references in the current reference. The basic reference do not include
	 * any other references as parts of itself.   
	 */
	public List<NameReference> getReferencesAtLeaf() {
		List<NameReference> result = new ArrayList<NameReference>();
		result.add(this);
		return result;
	}
	

	/**
	 * Test whether the reference is a literal
	 */
	public boolean isLiteralReference() {
		return false;
	}

	/**
	 * Test whether the reference is a method reference
	 */
	public boolean isMethodReference() {
		return false;
	}

	/**
	 * Test whether the reference is a type reference
	 */
	public boolean isTypeReference() {
		return false;
	}

	/**
	 * Test whether the reference is a name group reference
	 */
	public boolean isGroupReference() {
		return false;
	}
	
	/**
	 * Test whether the reference is represent a left value reference
	 */
	public boolean isLeftValue() {
		return false;
	}

	/**
	 * Set the reference is a left value reference
	 */
	public void setLeftValueReference() {
		return;
	}

	public String getUniqueId() {
		if (location != null) return name + "@" + location.getUniqueId();
		else return name;
	}

	
	@Override
	public int hashCode() {
		return getUniqueId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof NameReference)) return false;
		
		NameReference other = (NameReference) obj;
		return getUniqueId().equals(other.getUniqueId());
	}
	
	@Override
	public int compareTo(NameReference other) {
		if (this == other) return 0;

		return getUniqueId().compareTo(other.getUniqueId());
	}
	
	public String toFullString() {
		return kind.id + " Reference [Name = " + name + ", location = " + 
				location.getUniqueId() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return kind.id + " Reference [Name = " + name + " @ " + location.getUniqueId() + "]";
	}
	
	/**
	 * Return a better string of the reference for debugging
	 */
	public String toMultilineString(int indent, boolean includeLiteral) {
		if (!includeLiteral && kind == NameReferenceKind.NRK_LITERAL) return "";
		
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + kind.id + " Reference [Name = " + name);
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.getUniqueId() + "]\n");
			
		return buffer.toString();
	}
	
	/**
	 * Display the definition binded to the reference
	 */
	public String bindedDefinitionToString() {
		if (definition == null) return kind.id + " Reference [" + name + "] has not been resolved!";
		else return kind.id + " Reference [" + name + "] is binded to: [" + definition.getUniqueId() + "]";
	}
}
