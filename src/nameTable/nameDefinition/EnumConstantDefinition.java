package nameTable.nameDefinition;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;

import util.SourceCodeLocation;

/**
 * A class represent an enumeration constant definition
 * @author Zhou Xiaocong
 * @since 2013-2-27
 * @version 1.0
 */
public class EnumConstantDefinition extends NameDefinition {
	private List<NameReference> arguments = null;
	
	public EnumConstantDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_ENUM_CONSTANT;
	}

	/**
	 * Return the argument list defined in the enumeration constant
	 */
	public List<NameReference> getArguments() {
		return arguments;
	}
	
	/**
	 * Add an argument for the enumeration constant
	 */
	public void addArgument(NameReference argument) {
		if (arguments == null) arguments = new ArrayList<NameReference>();
		arguments.add(argument);
	}

	@Override
	public void printDefinitions(PrintWriter writer, int indent) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		writer.println(indentString + "Enum constant: " + simpleName);
	}
}
