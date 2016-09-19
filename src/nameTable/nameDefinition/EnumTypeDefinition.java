package nameTable.nameDefinition;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.NameTableVisitor;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import util.SourceCodeLocation;

/**
 * The class represent a enumeration type definition
 * @author Zhou Xiaocong
 * @since 2013-2-27
 * @version 1.0
 */
public class EnumTypeDefinition extends TypeDefinition implements NameScope {
	private List<EnumConstantDefinition> constants = null;
	private List<TypeReference> superList = null;
	private SourceCodeLocation endLocation = null;

	private List<NameReference> references = null;
	private int flag = 0; 									// The modifier flag of the enum type
	
	public EnumTypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, 
			NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(this.location, endLocation);
	}

	/* (non-Javadoc)
	 * @see nameTable.TypeDefinition#isDetailedType()
	 */
	@Override
	public boolean isDetailedType() {
		return false;
	}

	@Override
	public boolean isEnumeration() {
		return true;
	}
	
	public boolean isInterface() {
		return false;
	}

	/**
	 * Set the modifier flag 
	 */
	public void setModifierFlag(int flag) {
		this.flag = flag;
	}
	
	/**
	 * Return the enumeration constants defined in the enumeration type
	 */
	public List<EnumConstantDefinition> getConstants() {
		return constants;
	}
	
	/**
	 * Add an enumeration constant for the enumeratino type
	 */
	public void addConstant(EnumConstantDefinition constant) {
		if (constants == null) constants = new ArrayList<EnumConstantDefinition>();
		constants.add(constant);
	}

	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() != NameDefinitionKind.NDK_ENUM_CONSTANT) 
			throw new IllegalNameDefinition("Only enum constant can be defined in a enum definition!");
		if (constants == null) constants = new ArrayList<EnumConstantDefinition>();
		constants.add((EnumConstantDefinition) nameDef);
	}

	/**
	 * Return the package definition object which this detailed type belongs to 
	 */
	public PackageDefinition getEnclosingPackage() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_PACKAGE) currentScope = currentScope.getEnclosingScope();
		return (PackageDefinition)currentScope;
	}

	/**
	 * Test if the class is public according to the modifier flag
	 */
	public boolean isPublic() {
		return Modifier.isPublic(flag);
	}
	
	@Override
	public void printDefinitions(PrintWriter writer, int indent) {
		StringBuffer buffer = new StringBuffer();

		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);
		
		buffer.append(indentString + "Enum: ");
		buffer.append(simpleName + "\n");
		
		if (superList != null) {
			buffer.append(indentString + "\t Super types: ");
			for (TypeReference reference : superList) {
				buffer.append(indentString + "\t\t" + reference.getName() + " ");
			}
			buffer.append("\n");
		}
		writer.print(buffer);
		
		if (constants != null) {
			for (EnumConstantDefinition constant : constants) {
				constant.printDefinitions(writer, indent+1);
			}
		}
		writer.print(buffer);
	}

	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_TYPE;
	}

	@Override
	public String getScopeName() {
		return simpleName;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		return null;
	}

	@Override
	public boolean resolve(NameReference reference) {
		if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE){
			if (this.match(reference)) return true;
		}
		if (constants != null) {
			for (EnumConstantDefinition constant : constants) {
				if (constant.match(reference)) return true;
			}
		}

		// If we can not match the name in the fields, methods and types of the type, we resolve the name 
		// reference in the super class of the type
		TypeDefinition superTypeDef = getSuperClassDefinition();
		if (superTypeDef != null) {
			// Resolve the reference in the super class of the type 
			if (superTypeDef.resolve(reference)) return true;
		}
		
		// If we can resolve the name reference in the super class, we try to resolve it in the parent scope
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * Return the super types of the enumeration type
	 */
	public List<TypeReference> getSuperList() {
		return superList;
	}

	/**
	 * Add a super type for the enumeration type
	 */
	public void addSuperType(TypeReference superType) {
		if (superList == null) superList = new ArrayList<TypeReference>();
		superList.add(superType);
	}

	@Override
	public TypeDefinition getSuperClassDefinition() {
		if (superList == null) return null;
		if (superList.size() < 1) return null;
		TypeReference superClassRef = superList.get(0);
		if (!superClassRef.isResolved()) superClassRef.resolveBinding();
		
		return (TypeDefinition)superClassRef.getDefinition();
	}
	

	/**
	 * Test whether the current type is the subtype of the given type by the parameter
	 * @param parent
	 * @return
	 */
	@Override
	public boolean isSubtypeOf(TypeDefinition parent) {
		if (this == parent) return true;
		
		if (superList != null) {
			// Match the definition in the super list!
			for (TypeReference superType : superList) {
				if (!superType.isResolved()) superType.resolveBinding();
				if (superType.getDefinition() == parent) return true;
			}
			// If do match the definition in the super list, recursively judge the super type of the current type 
			// is the sub-type of parent
			for (TypeReference superType : superList) {
				TypeDefinition superDef = (TypeDefinition)superType.getDefinition();
				if (superDef != null) {
					if (superDef.isSubtypeOf(parent)) return true;
				}
			}
			return false;
		} else return matchSubtypeRelationsOfSimpleTypes(simpleName, parent.getSimpleName());
	}
	
	@Override
	public void addReference(NameReference reference) {
		if (reference == null) return;
		if (references == null) references = new ArrayList<NameReference>();
		references.add(reference);
		
	}

	@Override
	public List<NameReference> getReferences() {
		return references;
	}

	@Override
	public void printReferences(PrintWriter writer, boolean includeLiteral) {
		StringBuffer buffer = new StringBuffer();
		if (references != null) {
			buffer.append("\nReferences in scope " + getScopeName() + "\n");
			for (NameReference reference : references) {
				buffer.append(reference.referenceToString(0, includeLiteral));
			}
		}
		writer.print(buffer);
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				subscope.printReferences(writer, includeLiteral);
			}		
		}
	}
	
	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (constants != null) {
			for (EnumConstantDefinition constantDef : constants) {
				if (constantDef.match(namePostFix)) result.add(constantDef);
			}
		}
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<NameDefinition> temp = subscope.findAllDefinitionsByName(namePostFix);
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public List<NameReference> findAllReferencesByName(String name) {
		List<NameReference> result = new ArrayList<NameReference>();
		if (references != null) {
			for (NameReference reference : references) {
				if (reference.getName().equals(name)) result.add(reference);
			}
		}
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<NameReference> temp = subscope.findAllReferencesByName(name);
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public List<NameScope> findAllSubScopesByName(String name) {
		List<NameScope> result = new ArrayList<NameScope>();
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				if (subscope.getScopeName().equals(name)) result.add(subscope);
				List<NameScope> temp = subscope.findAllSubScopesByName(name);
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public NameDefinition getDefinition(String name, boolean includeSubscopes) {
		if (constants == null) return null;
		for (EnumConstantDefinition constant : constants) {
			if (constant.match(name)) return constant;
		}
		if (includeSubscopes) {
			List<NameScope> subscopes = getSubScopeList();
			if (subscopes != null) {
				for (NameScope subscope : subscopes) {
					NameDefinition target = subscope.getDefinition(name, includeSubscopes);
					if (target != null) return target;
				}		
			}
		}
		return null;
	}

	@Override
	public NameDefinition findDefinitionById(String id, boolean includeSubscopes) {
		if (constants == null) return null;
		for (EnumConstantDefinition constant : constants) {
			if (id.equals(constant.getUniqueId())) return constant;
		}
		if (includeSubscopes) {
			List<NameScope> subscopes = getSubScopeList();
			if (subscopes != null) {
				for (NameScope subscope : subscopes) {
					NameDefinition target = subscope.findDefinitionById(id, includeSubscopes);
					if (target != null) return target;
				}		
			}
		}
		return null;
	}

	@Override
	public List<NameReference> getReferences(String name) {
		List<NameReference> result = new ArrayList<NameReference>();
		
		if (references != null) {
			for (NameReference reference : references) {
				if (reference.getName().equals(name)) result.add(reference);
			}
		}
		return result;
	}

	@Override
	public boolean isEnclosedInScope(NameScope ancestorScope) {
		NameScope parent = getEnclosingScope();
		while (parent != null) {
			if (parent == ancestorScope) return true;
			parent = parent.getEnclosingScope();
		}
		return false;
	}

	@Override
	public List<NameDefinition> findAllDefinitionsByPosition(SourceCodeLocation start, SourceCodeLocation end) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (constants != null) {
			for (EnumConstantDefinition constantDef : constants) {
				SourceCodeLocation location = constantDef.getLocation();
				if (location.isBetween(start, end)) result.add(constantDef);
			}
		}
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<NameDefinition> temp = subscope.findAllDefinitionsByPosition(start, end);
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public List<NameReference> findAllReferencesByPosition(SourceCodeLocation start, SourceCodeLocation end) {
		List<NameReference> result = new ArrayList<NameReference>();
		if (references != null) {
			for (NameReference reference : references) {
				SourceCodeLocation location = reference.getLocation();
				if (location.isBetween(start, end)) result.add(reference);
			}
		}
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<NameReference> temp = subscope.findAllReferencesByPosition(start, end);
				result.addAll(temp);
			}
		}
		return result;
	}
	
	@Override
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();

		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<DetailedTypeDefinition> temp = subscope.getAllDetailedTypeDefinition();
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		int result = 0;
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_ENUM_CONSTANT) {
			if (constants != null) result += constants.size();
		}
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) result += subscope.getTotalNumberOfDefinitions(kind);
		}
		
		return result;
	}
	
	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		visitor.visit(this);
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
	
}
