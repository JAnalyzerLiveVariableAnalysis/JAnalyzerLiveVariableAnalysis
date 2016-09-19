package nameTable.nameReference;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.NameScope;
import util.SourceCodeLocation;

/**
 * A qualified type reference is a type reference with a qualifier, which can also be a qualified type reference,
 * and then it can be used to represent a type expression. A qualified type consists two parts: a qualifier and a
 * simple type (reference)
 * @author Zhou Xiaocong
 * @since 2013-3-27
 * @version 1.0
 */
public class QualifiedTypeReference extends TypeReference {
	private TypeReference qualifier = null;
	private TypeReference simpleType = null;

	public QualifiedTypeReference(String name, SourceCodeLocation location, NameScope scope) {
		super(name, location, scope);
		// TODO Auto-generated constructor stub
	}

	public QualifiedTypeReference(String name, SourceCodeLocation location) {
		super(name, location);
		// TODO Auto-generated constructor stub
	}

	public QualifiedTypeReference(QualifiedTypeReference other) {
		super(other);
		qualifier = other.qualifier;
		simpleType = other.simpleType;
	}

	/**
	 * @return the qualifier
	 */
	public TypeReference getQualifier() {
		return qualifier;
	}

	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier(TypeReference qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * @return the simpleType
	 */
	public TypeReference getSimpleType() {
		return simpleType;
	}

	/**
	 * @param simpleType the simpleType to set
	 */
	public void setSimpleType(TypeReference simpleType) {
		this.simpleType = simpleType;
	}

	/**
	 * Test if the type reference is a qualified type reference
	 */
	@Override
	public boolean isQualifiedType() {
		return true;
	}
	
	@Override
	public boolean resolveBinding() {
		if (definition != null) return true;
		
		// At first resolve the reference as an entire type name reference in the current scope
		// Note that scope.resolve() will match the entire qualified type name 
		if (scope.resolve(this)) return true;
		
		// If we can not resolve the entire qualified type name, then we resolve the qualifier
		if (scope.resolve(qualifier)) {
			NameDefinition nameDef = qualifier.getDefinition();
			NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
			if (nameDefKind == NameDefinitionKind.NDK_PACKAGE) {
				// Resolve the simple type reference in the package, and bind the entire reference to
				// the definition object binded to the simple type reference
				PackageDefinition packageDef = (PackageDefinition)nameDef;
				packageDef.resolve(simpleType);
				bindTo(simpleType.getDefinition());
			} else if (nameDefKind == NameDefinitionKind.NDK_TYPE) {
				// Resolve the simple type reference in the type, and bind the entire reference to
				// the definition object binded to the simple type reference
				TypeDefinition typeDef = (TypeDefinition)nameDef;
				typeDef.resolve(simpleType);
				bindTo(simpleType.getDefinition());
			}
		} 
		return isResolved();
	}
	
}
