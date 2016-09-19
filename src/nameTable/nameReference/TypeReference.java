package nameTable.nameReference;

import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The class represents a type reference
 * <p>Note that we store the dimensions of an array type in a type reference not in a type definition, i.e. 
 * there is no type definition for an array type. We just create type definition for the base type of an array 
 * type
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class TypeReference extends NameReference {
	// If the type reference refers to a array type, we save its dimension
	private int dimension = 0;

	public TypeReference(String name, SourceCodeLocation location,	NameScope scope) {
		super(name, location, scope, NameReferenceKind.NRK_TYPE);
	}

	public TypeReference(String name, SourceCodeLocation location) {
		super(name, location, NameReferenceKind.NRK_TYPE);
	}
	public TypeReference(TypeReference other) {
		super(other.name, other.location, other.scope, NameReferenceKind.NRK_TYPE);
		dimension = other.dimension;
	}
	
	/**
	 * Test if the type reference is an array type reference
	 */
	public boolean isArrayType() {
		if (dimension > 0) return true;
		else return false;
	}

	/**
	 * Get the dimension of an array type
	 */
	public int getDimension() {
		return dimension;
	}
	
	/**
	 * Set the dimension of an array type
	 */
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	/**
	 * Test if the type reference is a qualified type reference
	 */
	public boolean isQualifiedType() {
		return false;
	}
	
	/**
	 * Test if the type reference is a parameterized type reference
	 * @update: 2015/07/06
	 */
	public boolean isParameterizedType() {
		return false;
	}
	
	/**
	 * Test whether the reference is a type reference
	 */
	@Override
	public boolean isTypeReference() {
		return true;
	}

	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		if (name.equals(NameReferenceLabel.TYPE_BOOLEAN) || name.equals(NameReferenceLabel.TYPE_CHAR) || 
				name.equals(NameReferenceLabel.TYPE_BYTE) || name.equals(NameReferenceLabel.TYPE_DOUBLE) || 
				name.equals(NameReferenceLabel.TYPE_INT) || name.equals(NameReferenceLabel.TYPE_LONG) || 
				name.equals(NameReferenceLabel.TYPE_FLOAT) || name.equals(NameReferenceLabel.TYPE_SHORT) ||
				name.equals(NameReferenceLabel.TYPE_VOID) || name.equals(NameReferenceLabel.TYPE_STRING)) {
			// For primitive type, we resolve it in the system scope!
			NameScope currentScope = scope;
			while (currentScope != null) {
				if (currentScope.getEnclosingScope() == null) break;
				currentScope = currentScope.getEnclosingScope();
			}
			if (currentScope != null) currentScope.resolve(this);
		} else scope.resolve(this);

		return isResolved();
	}
	
	public String toFullString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return "Reference [Type Name = " + name + arrayString + ", location = " + 
				location.toFullString() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return "Reference [Type Name = " + name + arrayString + " @ " + location.toFullString() + "]";
	}
	
	public String toDelcarationString() {
		String arrayString = "";
		for (int count = 0; count < dimension; count++) {
			arrayString = arrayString + "[]";
		}
		return arrayString + name;
	}

	/**
	 * Return a better string of the reference for debugging
	 */
	public String referenceToString(int indent, boolean includeLiteral) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Reference: " + "[Type Name = " + name);
		for (int count = 0; count < dimension; count++) buffer.append("[]");
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.toFullString() + "]\n");
			
		return buffer.toString();		
	}
}
