package nameTable.nameReference;

import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * This class is used to store the package reference in import declarations of a compilation units. When the import declaration is an on-demand declaration, 
 * it refer to a package indeed, i.e. it imports all types defined in the package! 
 * @author Zhou Xiaocong
 * @since 2014/1/1
 * @version 1.0
 */
public class PackageReference extends NameReference {

	public PackageReference(String name, SourceCodeLocation location) {
		super(name, location, NameReferenceKind.NRK_PACKAGE);
	}

	public PackageReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope, NameReferenceKind.NRK_PACKAGE);
	}
}
