package nameTable.nameDefinition;

import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * The class represent a simple type definition, i.e. we can not get the source code of the type definition, and
 * then we do not its member definitions.
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class SimpleTypeDefinition extends TypeDefinition {

	public SimpleTypeDefinition(String simpleName) {
		super(simpleName);
	}

	public SimpleTypeDefinition(String simpleName, String fullQualifiedName) {
		super(simpleName, fullQualifiedName);
	}

	public SimpleTypeDefinition(String simpleName, String fullQualifiedName, NameScope scope) {
		super(simpleName, fullQualifiedName, null, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isDetailedType() {
		return false;
	}

	@Override
	public SourceCodeLocation getLocation() {
		return null;
	}

	@Override
	public boolean isEnumeration() {
		// TODO Auto-generated method stub
		return false;
	}
}
