package nameTable.nameReference.referenceGroup;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameScope.NameScope;

import util.SourceCodeLocation;

/**
 * A name reference group is used to group all name references for an expression, in order to support to infer the 
 * static type of the expression
 * @author Zhou Xiaocong
 * @since 2013-2-23
 * @version 1.0
 */
public abstract class NameReferenceGroup extends NameReference {
	public static final String OPERATOR_TIMES = "*";
	public static final String OPERATOR_DIVIDE = "/";
	public static final String OPERATOR_REMAINDER = "%";
	public static final String OPERATOR_PLUS = "+";
	public static final String OPERATOR_MINUS = "-";
	public static final String OPERATOR_LEFT_SHIFT = "<<";
	public static final String OPERATOR_RIGHT_SHIFT_SIGNED = ">>";
	public static final String OPERATOR_RIGHT_SHIFT_UNSIGNED = ">>>";
	public static final String OPERATOR_LESS = "<";
	public static final String OPERATOR_GREATER = ">";
	public static final String OPERATOR_LESS_EQUALS = "<=";
	public static final String OPERATOR_GREATER_EQUALS = ">=";
	public static final String OPERATOR_EQUALS = "==";
	public static final String OPERATOR_NOT_EQUALS = "!=";
	public static final String OPERATOR_XOR = "^";
	public static final String OPERATOR_AND = "&";
	public static final String OPERATOR_OR = "|";
	public static final String OPERATOR_CONDITIONAL_AND = "&&";
	public static final String OPERATOR_CONDITIONAL_OR = "||";
	public static final String OPERATOR_INCREMENT = "++";
	public static final String OPERATOR_DECREMENT = "--";
	public static final String OPERATOR_COMPLEMENT = "~";
	public static final String OPERATOR_NOT = "!";
	public static final String OPERATOR_ASSIGN = "=";
	public static final String OPERATOR_PLUS_ASSIGN = "+=";
	public static final String OPERATOR_MINUS_ASSIGN = "-=";
	public static final String OPERATOR_TIMES_ASSIGN = "*=";
	public static final String OPERATOR_DIVIDE_ASSIGN = "/=";
	public static final String OPERATOR_BIT_AND_ASSIGN = "&=";
	public static final String OPERATOR_BIT_OR_ASSIGN = "|=";
	public static final String OPERATOR_BIT_XOR_ASSIGN = "^=";
	public static final String OPERATOR_REMAINDER_ASSIGN = "%=";
	public static final String OPERATOR_LEFT_SHIFT_ASSIGN = "<<=";
	public static final String OPERATOR_RIGHT_SHIFT_SIGNED_ASSIGN = ">>=";
	public static final String OPERATOR_RIGHT_SHIFT_UNSIGNED_ASSIGN = ">>>=";

	protected String operator = null;			// The string of the operator, its value is included in the above final strings
	protected List<NameReference> subreferences = null;		// The references in the group
	
	public NameReferenceGroup(String name, SourceCodeLocation location) {
		super(name, location);
	}

	public NameReferenceGroup(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
	}

	@Override
	public NameReferenceKind getReferenceKind() {
		return NameReferenceKind.NRK_GROUP;
	}

	/**
	 * Get the list of all references in the group
	 */
	public List<NameReference> getSubReference() {
		return subreferences;
	}

	/**
	 * Add a reference to the group
	 */
	public void addSubReference(NameReference reference) {
		if (reference == null) return;
		if (subreferences == null) subreferences = new ArrayList<NameReference>();
		subreferences.add(reference);
	}

	/**
	 * Return the string representing the operator in the expression corresponding to the group
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * Set the string representing the operator in the expression corresponding to the group
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	/**
	 * Return the kind of the group. The sub-class itself determines its kind.
	 */
	public abstract NameReferenceGroupKind getGroupKind();
	
	/**
	 * Test whether the reference is a name group reference
	 */
	@Override
	public boolean isGroupReference() {
		return true;
	}
	
	/**
	 * Set all reference in the group to be left value reference. Actually, only those value references
	 * are set to be left value reference indeed
	 */
	@Override
	public void setLeftValueReference() {
		if (subreferences == null) return;
		for (NameReference reference : subreferences) reference.setLeftValueReference();
	}
	
	/**
	 * Resolve all references in the group. The sub-class determines how to resolve the references in
	 * the group according to the syntax of its corresponding expression.
	 */
	public abstract boolean resolveBinding();
	
	
	/**
	 * Return all reference at the leaf in the group, i.e. return all non-group-reference in 
	 * this reference group
	 */
	public List<NameReference> getReferencesAtLeaf() {
		if (subreferences == null) return null;
		List<NameReference> result = new ArrayList<NameReference>();
		
		for (NameReference reference : subreferences) {
			if (reference.isGroupReference()) {
				NameReferenceGroup group = (NameReferenceGroup)reference;
				List<NameReference> referencesInGroup = group.getReferencesAtLeaf();
				if (referencesInGroup != null) result.addAll(referencesInGroup);
			} else result.add(reference);
		}
		return result;
	}
	
	@Override
	public String toFullString() {
		final int MAX_LENGTH = 20;
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		StringBuffer buffer = new StringBuffer("Name Reference Group [Name = " + nameString + ", location = " + 
								location.toFullString() + ", scope = " + scope.getScopeName() + "]");
		if (subreferences != null) {
			buffer.append("\n");
			for (NameReference reference : subreferences) {
				buffer.append("\t" + reference.toFullString() + "\n");
			}
		}
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int MAX_LENGTH = 20;
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		StringBuffer buffer = new StringBuffer("Reference Group [Name = " + nameString + ", location = " + 
				location.toString() + ", scope = " + scope.getScopeName() + "]");
		if (subreferences != null) {
			for (NameReference reference : subreferences) {
				buffer.append("\r\n\t" + reference.toString());
			}
		}
		return buffer.toString();
	}

	/**
	 * Return a better string of the reference for debugging
	 */
	public String referenceToString(int indent, boolean includeLiteral) {
		final int MAX_LENGTH = 20;
		
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Reference Group: ");
		String nameString = name;
		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		buffer.append("[Name = " + nameString);

		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.toFullString() + "]\n");

		if (subreferences != null) {
			for (NameReference reference : subreferences) {
				buffer.append(reference.referenceToString(indent+1, includeLiteral));
			}
		}
		return buffer.toString();
	}

	/**
	 * Display the definition binded to the reference
	 */
	public String bindedDefinitionToString() {
		final int MAX_LENGTH = 20;
		StringBuffer buffer = new StringBuffer();
		String nameString = name;

		if (nameString.length() > MAX_LENGTH) nameString = nameString.substring(0, MAX_LENGTH) + "...";
		if (definition != null) buffer.append("The group [" + nameString + "] is binded to: " + definition.toFullString());
		else buffer.append("The group [" + nameString + "] has not been resolved!");
		if (subreferences != null) {
			for (NameReference reference : subreferences) {
				buffer.append("\r\n");
				buffer.append(reference.bindedDefinitionToString());
			}
		}
		return buffer.toString();
	}
}
