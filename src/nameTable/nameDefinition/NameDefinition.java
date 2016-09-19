package nameTable.nameDefinition;

import java.io.PrintWriter;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The abstract base class for the class which represents a name definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public abstract class NameDefinition implements Comparable<NameDefinition> {
	protected String simpleName = null;
	protected String fullQualifiedName = null;
	protected SourceCodeLocation location = null;
	protected NameScope scope = null;
	
	public static final char LOCATION_ID_BEGINNER = '@';
	
	public NameDefinition(String simpleName) {
		this.simpleName = simpleName;
		fullQualifiedName = simpleName;
	}
	
	public NameDefinition(String simpleName, String fullQualifiedName) {
		this.simpleName = simpleName;
		this.fullQualifiedName = fullQualifiedName;
	}

	public NameDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		this.simpleName = simpleName;
		this.fullQualifiedName = fullQualifiedName;
		this.location = location;
		this.scope = scope;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	public SourceCodeLocation getLocation() {
		return location;
	}

	public NameScope getScope() {
		return scope;
	}

	public void setLocation(SourceCodeLocation location) {
		this.location = location;
	}

	public void setScope(NameScope scope) {
		this.scope = scope;
	}

	public abstract NameDefinitionKind getDefinitionKind();
	
	public boolean isTypeDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_TYPE);
	}
	
	public boolean isDetailedType() {
		if (getDefinitionKind() != NameDefinitionKind.NDK_TYPE) return false;
		TypeDefinition type = (TypeDefinition)this;
		return type.isDetailedType(); 
	}

	public boolean isMethodDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_METHOD);
	}
	
	public boolean isFieldDefinition() {
		return (getDefinitionKind() == NameDefinitionKind.NDK_FIELD);
	}

	public boolean isVariableDefinition() {
		NameDefinitionKind kind = getDefinitionKind();
		return (kind == NameDefinitionKind.NDK_VARIABLE || kind == NameDefinitionKind.NDK_PARAMETER);
	}

	/**
	 * Match a reference name to the current name definition name. If the reference name contains qualifier ("."), 
	 *    then match the reference name to the fuallqualifiedName from the right to left, else match the reference 
	 *    name to the simpleName. 
	 * @param reference
	 * @return If the match is successful return true and bind the reference to the definition, else return false
	 */
	public final boolean match(NameReference reference) {
		String refStr = reference.getName();
		if (refStr == null) return false;
		
		if (match(refStr)) {
			reference.bindTo(this);
			return true;
		} else return false;
	}

	/**
	 * Match a reference name to the current name definition name. If the reference name contains qualifier ("."), 
	 *    then match the reference name to the fuallqualifiedName from the right to left, else match the reference 
	 *    name to the simpleName. 
	 * @param reference
	 * @return If the match is successful return true , else return false
	 */
	public final boolean match(String namePostFix) {
		if (namePostFix.contains(NameReferenceLabel.NAME_QUALIFIER)) {
			if (fullQualifiedName == null) return false;
			
			// Update: 2014-1-1 Zhou Xiaocong
			// In JDK, there is a class org.omg.CORBA.ORB and a class com.sun.corba.se.org.omg.CORBA.ORG. If we just use endsWith() to 
			// match, then the latter will match the former, but it is an error! 
			// So we use the precise match method, i.e. equals()!!!
//			return fullQualifiedName.endsWith(namePostFix);
			return fullQualifiedName.equals(namePostFix);
		} else {
			if (simpleName == null) return false;
			return namePostFix.equals(simpleName);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return false;
	}

	@Override
	public int compareTo(NameDefinition other) {
		if (this == other) return 0;

		if (location == null) {
			if (other.location != null) return -1;
			else return fullQualifiedName.compareTo(other.fullQualifiedName);
		} else {
			if (other.location == null) return 1;
			else {
				int result = location.compareTo(other.location);
				if (result != 0) return result;
			}
		}
		return simpleName.compareTo(other.simpleName);
	}
	
	public String getUniqueId() {
		if (location != null) return simpleName + LOCATION_ID_BEGINNER + location.getUniqueId();
		else return fullQualifiedName;
	}
	
	public static String getDefinitionNameFromId(String id) {
		int indexOfAt = id.indexOf(LOCATION_ID_BEGINNER);
		if (indexOfAt < 0) return id;
		else return id.substring(0, indexOfAt);
	}
	
	public static String getDefinitionLocationStringFromId(String id) {
		int indexOfAt = id.indexOf(LOCATION_ID_BEGINNER);
		if (indexOfAt < 0) return null;
		else return id.substring(indexOfAt+1);
	}

	public String toFullString() {
		StringBuffer buffer = new StringBuffer("Name Definition [fullQualifiedName = " + fullQualifiedName);
		if (location != null) buffer.append(", location = " + location.toFullString());
		if (scope != null) buffer.append(", scope = " + scope.getScopeName());
		buffer.append(", simpleName = " + simpleName + "]");
		return  buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "Definition [name = " + simpleName;
		if (location != null) result = result  + " @ " + location.toFullString() + "]";
		else result = result + "]";
		return result;
	}

	public abstract void printDefinitions(PrintWriter writer, int indent);
	
}
