package nameTable.nameDefinition;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import sourceCodeAST.SourceCodeLocation;

/**
 * A class represents type parameters for a generic class or a generic methods. However, so far, we do not match type parameters
 * when we resolve name references. 
 * 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ5ÈÕ
 * @version 1.0
 *
 */
public class TypeParameterDefinition extends TypeDefinition {
	private List<TypeReference> boundList = null;
	
	public TypeParameterDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, NameScope scope) {
		super(simpleName, fullQualifiedName, location, scope);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_TYPE_PARAMETER;
	}
	
	/**
	 * Test if the type is an interface. 
	 */
	public boolean isInterface() {
		return false;
	}
	
	/**
	 * Test if the type is defined in package, i.e. test if the type is not a member type
	 */
	public boolean isPackageMember() {
		return false;
	}
	
	public List<TypeReference> getBoundList() {
		return boundList;
	}
	
	public boolean addBoundType(TypeReference boundType) {
		if (boundList == null) boundList = new ArrayList<TypeReference>();
		return boundList.add(boundType);
	}
	
	@Override
	public List<TypeReference> getSuperList() {
		return boundList;
	}

}
