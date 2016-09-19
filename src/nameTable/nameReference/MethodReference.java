package nameTable.nameReference;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The class represent a reference to a method call
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class MethodReference extends NameReference {
	private List<NameReference> arguments = null;		// The arguments in the method call
	// Because there are function overloads, and we can not infer the exact type of the argument, we 
	// can not find the exact method binded to the method call, and then we store all alternative methods
	// for the method call
	private List<MethodDefinition> alternatives = null;	 

	public MethodReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope, NameReferenceKind.NRK_METHOD);
	}

	public MethodReference(String name, SourceCodeLocation location) {
		super(name, location, NameReferenceKind.NRK_METHOD);
	}

	/**
	 * Return the alternative methods can be binded to the method call
	 */
	public List<MethodDefinition> getAlternatives() {
		return alternatives;
	}

	/**
	 * Add an alternative method for this reference
	 */
	public void addAlternative(MethodDefinition method) {
		if (alternatives == null) alternatives = new ArrayList<MethodDefinition>();
		alternatives.add(method);
	}

	/**
	 * Add a list of alternative methods for this reference
	 */
	public void addAlternative(List<MethodDefinition> methods) {
		if (alternatives == null) alternatives = new ArrayList<MethodDefinition>();
		alternatives.addAll(methods);
	}

	@Override
	public NameReferenceKind getReferenceKind() {
		return NameReferenceKind.NRK_METHOD;
	}

	/**
	 * @return the arguments
	 */
	public List<NameReference> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(List<NameReference> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Test whether the reference is a method reference
	 */
	@Override
	public boolean isMethodReference() {
		return true;
	}
	
	/**
	 * Return a better string of the reference for debugging
	 */
	public String referenceToString(int indent, boolean includeLiteral) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Reference: [Method Name = " + name);
		if (indent > 0) buffer.append(" @" + location.toString() + "]\n");
		else buffer.append(" @" + location.toFullString() + "]\n");
			
		return buffer.toString();
	}
}
