package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * A reference for a parameterized type, the information of the main type (for example, the container type List in a parameterized 
 * type List<NameDefinition>) is stored in the inherited fields (including name, location, scope, definition, kind), and the information
 * of parameter types (i.e. the type reference NameDefinition in List<NameDefinition>) is stored in a list of this class. 
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ6ÈÕ
 * @version 1.0
 */
public class ParameterizedTypeReference extends TypeReference {
	private List<TypeReference> parameterList = null;
	
	public ParameterizedTypeReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	public ParameterizedTypeReference(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public ParameterizedTypeReference(ParameterizedTypeReference other) {
		super(other);
		parameterList = new ArrayList<TypeReference>();
		for (TypeReference otherParameter : other.parameterList) parameterList.add(otherParameter);
	}
	
	/**
	 * Test if the type reference is a parameterized type reference
	 * @update: 2015/07/06
	 */
	public boolean isParameterizedType() {
		return true;
	}
	
	public void addParameterType(TypeReference parameterType) {
		if (parameterList == null) parameterList = new ArrayList<TypeReference>();
		parameterList.add(parameterType);
	}
	
	public void setParameterTypeList(List<TypeReference> parameterTypeList) {
		parameterList = parameterTypeList;
	}
	
	public List<TypeReference> getParameterTypes() {
		return parameterList;
	}
	
	public List<TypeDefinition> getDefinition(boolean flag) {
		List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
		if (flag == false || parameterList == null) {
			if (definition != null) resultList.add((TypeDefinition)definition);
			return resultList;
		}
		for (TypeReference parameterType : parameterList) {
			if (!parameterType.isParameterizedType()) {
				TypeDefinition parameterTypeDefinition = (TypeDefinition)parameterType.getDefinition();
				if (parameterTypeDefinition != null && !resultList.contains(parameterTypeDefinition)) 
					resultList.add(parameterTypeDefinition);
			} else {
				ParameterizedTypeReference reference = (ParameterizedTypeReference)parameterType;
				List<TypeDefinition> parameterTypeDefinitionList = reference.getDefinition(true);
				for (TypeDefinition type : parameterTypeDefinitionList) {
					if (!resultList.contains(type)) resultList.add(type);
				}
			}
		}
		return resultList;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		// Resolve the parameter types
		for (TypeReference parameterType : parameterList) parameterType.resolveBinding();
		
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
		StringBuffer parameterString = new StringBuffer("");
		if (parameterList.size() > 0) {
			parameterString.append("<" + parameterList.get(0).name);
			for (int index = 1; index < parameterList.size(); index++) {
				parameterString.append(", " + parameterList.get(index).name);
			}
			parameterString.append(">");
		}
		return "Reference [Type Name = " + name + parameterString.toString() + ", location = " + 
				location.toFullString() + ", scope = " + scope.getScopeName() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer parameterString = new StringBuffer("");
		if (parameterList.size() > 0) {
			parameterString.append("<" + parameterList.get(0).name);
			for (int index = 1; index < parameterList.size(); index++) {
				parameterString.append(", " + parameterList.get(index).name);
			}
			parameterString.append(">");
		}
		return "Reference [Type Name = " + name + parameterString.toString() + " @ " + location.toFullString() + "]";
	}
	
	public String toDelcarationString() {
		StringBuffer parameterString = new StringBuffer("");
		if (parameterList.size() > 0) {
			parameterString.append("<" + parameterList.get(0).name);
			for (int index = 1; index < parameterList.size(); index++) {
				parameterString.append(", " + parameterList.get(index).name);
			}
			parameterString.append(">");
		}
		return name + parameterString;
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
		if (parameterList.size() > 0) {
			buffer.append("<" + parameterList.get(0).name);
			for (int index = 1; index < parameterList.size(); index++) {
				buffer.append(", " + parameterList.get(index).name);
			}
			buffer.append(">");
		}
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.toFullString() + "]\n");
			
		return buffer.toString();		
	}
}
