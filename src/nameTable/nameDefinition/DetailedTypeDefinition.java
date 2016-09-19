package nameTable.nameDefinition;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.NameTableVisitor;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import util.SourceCodeLocation;

/**
 * The name definition represent a type with detailed field, method and member type definitions, i.e. we can
 * access the source code of the type definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class DetailedTypeDefinition extends TypeDefinition implements NameScope {
	private List<FieldDefinition> fieldList = null;			// The fields of the type
	private List<MethodDefinition> methodList = null;		// The methods of the type
	private List<DetailedTypeDefinition> typeList = null;	// The member types of the type
	private List<TypeReference> superList = null;			// The super types of the type, which include the super class and interfaces of the type. 
															// The first super type must be the super class of the type
	private List<NameReference> references = null;
	private SourceCodeLocation endLocation = null;
	private int flag = 0; 									// The modifier flag of the detailed type
	private String filePath;
	
	public DetailedTypeDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, 
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
		return true;
	}

	@Override
	public boolean isEnumeration() {
		return false;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		switch (nameDef.getDefinitionKind()) {
		case NDK_TYPE: 
			if (typeList == null) typeList = new ArrayList<DetailedTypeDefinition>();
			TypeDefinition typeNameDef = (TypeDefinition)nameDef;
			if (!typeNameDef.isDetailedType()) throw new IllegalNameDefinition("The nested type definition added to a type must to be a detailed type definition!");
			typeList.add((DetailedTypeDefinition) nameDef);
			break;
		case NDK_METHOD: 
			if (methodList == null) methodList = new ArrayList<MethodDefinition>();
			methodList.add((MethodDefinition) nameDef);
			break;
		case NDK_FIELD: 
			if (fieldList == null) fieldList = new ArrayList<FieldDefinition>();
			fieldList.add((FieldDefinition) nameDef);
			break;
		default:
			throw new IllegalNameDefinition("The kind of name definition in a type have to be NDK_TYPE, NDK_METHOD or NDK_FIELD!");
		}
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	/**
	 * Return the package definition object which this detailed type belongs to 
	 */
	@Override
	public PackageDefinition getEnclosingPackage() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_PACKAGE) currentScope = currentScope.getEnclosingScope();
		return (PackageDefinition)currentScope;
	}
	
	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_TYPE;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return simpleName;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (typeList == null && methodList == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		if (typeList != null) {
			for (DetailedTypeDefinition type : typeList) result.add(type);
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) result.add(method);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
		NameReferenceKind refKind = reference.getReferenceKind();
		if (refKind == NameReferenceKind.NRK_FIELD || refKind == NameReferenceKind.NRK_VARIABLE) {
			if (fieldList != null) {
				for (FieldDefinition field : fieldList) {
					if (field.match(reference)) return true;
				}
			}
		} else if (refKind == NameReferenceKind.NRK_METHOD) {
			if (methodList != null) {
				MethodReference methodRef = (MethodReference)reference;
				for (MethodDefinition method : methodList) {
					if (method.matchMethod(methodRef)) {
						methodRef.addAlternative(method);
						
						// Find all methods defined in the sub-type of the current detailed type definition and redefine (i.e. override) the matched method
						SystemScope root = SystemScope.getRootScope(scope);		// We do this in the root scope (i.e. the system scope)!
						List<MethodDefinition> redefinedMethods = root.getAllOverrideMethods(this, method);
						methodRef.addAlternative(redefinedMethods);  // Add all alternatives to the method reference!
					} else {
						// Do nothing if we do not match any method!
					}
				}
				List<MethodDefinition> methodDefList = methodRef.getAlternatives();
				if (methodDefList != null) {
					if (methodDefList.size() > 0) {
						MethodDefinition firstMethodDef = methodDefList.get(0);
						methodRef.bindTo(firstMethodDef);
						return true;
					}
				}
			}
		} else if (refKind == NameReferenceKind.NRK_TYPE) {
			if (this.match(reference)) return true;
			if (typeList != null) {
				for (TypeDefinition type : typeList) {
					if (type.match(reference)) return true;
				}
			}
		}

		// If we can not match the name in the fields, methods and types of the type, we resolve the  
		// reference in the super class and super interface of the type. However, if the reference is a type reference, we should not resolve it 
		// in super class or super interface, since a class can not inherent a type, and an inner class name must be accessed by given its outter class name!
		if (refKind != NameReferenceKind.NRK_TYPE && superList != null) {
			for (TypeReference superTypeRef : superList) {
				if (!superTypeRef.isResolved()) superTypeRef.resolveBinding();
				if (superTypeRef.isResolved()) {
					TypeDefinition superTypeDef = (TypeDefinition)superTypeRef.getDefinition();
					// Resolve the reference in the super class or super interface of the type 
					if (superTypeDef.resolve(reference)) return true;
				}
			}
		}
		
		// If we can resolve the name reference in the super class, we try to resolve it in the parent scope
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * Get the list of fields defined in this type
	 */
	public List<FieldDefinition> getFieldList() {
		return fieldList;
	}
	
	/**
	 * Get the list of all fields defined in this type and all non-static fields defined in its all ancestor classes
	 * @return
	 */
	public List<FieldDefinition> getAllFieldList() {
		List<FieldDefinition> result = new ArrayList<FieldDefinition>();
		if (fieldList != null) result.addAll(fieldList);
		
		TypeDefinition superClassDefinition = getSuperClassDefinition();
		if (superClassDefinition != null) {
			if (superClassDefinition.isDetailedType()) {
				DetailedTypeDefinition superDetailedType = (DetailedTypeDefinition)superClassDefinition;
				List<FieldDefinition> superFields = superDetailedType.getAllFieldList();
				result.addAll(superFields);
			}
		}
		return result;
	}

	/**
	 * Get the list of methods defined in this type
	 */
	public List<MethodDefinition> getMethodList() {
		return methodList;
	}

	/**
	 * Get the list of member types defined in this type
	 */
	public List<DetailedTypeDefinition> getTypeList() {
		return typeList;
	}

	/**
	 * Get the list of super type, which include super class and super interfaces of the current type
	 */
	public List<TypeReference> getSuperList() {
		return superList;
	}

	public void addSuperType(TypeReference superType) {
		if (superList == null) superList = new ArrayList<TypeReference>();
		superList.add(superType);
	}

	/**
	 * Resolve super type references of the type
	 */
	public boolean resolveAllSupertypes() {
		if (superList == null) return true;
		for (TypeReference superType : superList) superType.resolveBinding();
		return true;
	}
	
	@Override
	public TypeDefinition getSuperClassDefinition() {
		if (superList == null) return null;
		if (superList.size() < 1) return null;
		TypeReference superClassRef = superList.get(0);
		if (!superClassRef.isResolved()) superClassRef.resolveBinding();
		
		return (TypeDefinition)superClassRef.getDefinition();
	}

	
	@Override
	public boolean isSubtypeOf(TypeDefinition parent) {
		if (this == parent) return true;

/*		
		counter = counter + 1;
		if (counter > 100) {
			System.out.println("This type is: " + this.getFullQualifiedName() + ", and its super list: ");
			if (superList != null) {
				for (TypeReference superRef : superList) {
					superRef.resolveBinding();
					DetailedTypeDefinition superDef = (DetailedTypeDefinition)superRef.getDefinition();
					System.out.println("\tSuper " + superRef.getName() + ", its definition: " + superDef.getFullQualifiedName());
					
					List<TypeReference> superSuperList = superDef.superList;
					System.out.println("Super's super: ");
					for (TypeReference superSuperRef : superSuperList) {
						superSuperRef.resolveBinding();
						System.out.println(superSuperRef.getName() + ", and its definition: " + superSuperRef.getDefinition().getFullQualifiedName());
					}
				}
			}
			
			throw new AssertionError("More sub-type!!!");
		} else if (counter > 50) {
			System.out.println("Counter " + counter + ", check " + this.fullQualifiedName + " is sub-type of " + parent.fullQualifiedName);
		}
*/

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
	public void printDefinitions(PrintWriter writer, int indent) {
		StringBuffer buffer = new StringBuffer();

		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);
		
		if (isInterface) buffer.append(indentString + "Interface: ");
		else buffer.append(indentString + "Class ");
		buffer.append(simpleName + "\n");
		
		if (superList != null) {
			buffer.append(indentString + "\t Super types: ");
			for (TypeReference reference : superList) {
				buffer.append(reference.getName() + " ");
			}
			buffer.append("\n");
		}
		writer.print(buffer);
		
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				field.printDefinitions(writer, indent+1);
			}
		}
		
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				method.printDefinitions(writer, indent+1);
			}
		}
		
		if (typeList != null) {
			for (DetailedTypeDefinition type : typeList) {
				type.printDefinitions(writer, indent+1);
			}
		}
	}
	
	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				if (field.match(namePostFix)) result.add(field);
			}
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				if (method.match(namePostFix)) result.add(method);
			}
		}
		if (typeList != null) {
			for (TypeDefinition typeDef : typeList) {
				if (typeDef.match(namePostFix)) result.add(typeDef);
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
	public NameDefinition getDefinition(String id, boolean includeSubscopes) {
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				if (field.match(id)) return field;
			}
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				if (method.match(id)) return method;
			}
		}
		if (typeList != null) {
			for (TypeDefinition type : typeList) {
				if (type.match(id)) return type;
			}
		}
		if (includeSubscopes) {
			List<NameScope> subscopes = getSubScopeList();
			if (subscopes != null) {
				for (NameScope subscope : subscopes) {
					NameDefinition target = subscope.getDefinition(id, includeSubscopes);
					if (target != null) return target;
				}		
			}
		}
		return null;
	}

	@Override
	public NameDefinition findDefinitionById(String id, boolean includeSubscopes) {
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				if (id.contains("events@151:17")) {
					System.out.println("\tBefore match field " + field.getUniqueId() + " in type "  + fullQualifiedName + " for " + id);
				}
				if (id.equals(field.getUniqueId())) return field;
			}
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				if (id.equals(method.getUniqueId())) return method;
			}
		}
		if (typeList != null) {
			for (TypeDefinition type : typeList) {
				if (id.equals(type.getUniqueId())) return type;
			}
		}
		if (includeSubscopes) {
			List<NameScope> subscopes = getSubScopeList();
			if (subscopes != null) {
				for (NameScope subscope : subscopes) {
//					if (id.contains("events@151:17")) {
//						System.out.println("\tBefore find in type "  + fullQualifiedName + " subscope " + subscope.getScopeName() + " for " + id);
//					}
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
		if (fieldList != null) {
			for (FieldDefinition field : fieldList) {
				SourceCodeLocation location = field.getLocation();
				if (location.isBetween(start, end)) result.add(field);
			}
		}
		if (methodList != null) {
			for (MethodDefinition method : methodList) {
				SourceCodeLocation location = method.getLocation();
				if (location.isBetween(start, end)) result.add(method);
			}
		}
		if (typeList != null) {
			for (TypeDefinition typeDef : typeList) {
				SourceCodeLocation location = typeDef.getLocation();
				if (location.isBetween(start, end)) result.add(typeDef);
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

		if (typeList != null) {
			for (DetailedTypeDefinition type : typeList) result.add(type);
		}
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<DetailedTypeDefinition> temp = subscope.getAllDetailedTypeDefinition();
				result.addAll(temp);
			}
		}
		return result;
	}
	
	/**
	 * Get the end location of the type definition
	 */
	public SourceCodeLocation getEndLocation() {
		return endLocation;
	}
	
	@Override
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		int result = 0;
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_FIELD) {
			if (fieldList != null) result += fieldList.size();
		}
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_TYPE) {
			if (typeList != null) result += typeList.size();
		}

		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_METHOD) {
			if (methodList != null) result += methodList.size();
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
		
		boolean visitSubscope = visitor.visit(this);

		if (visitSubscope == true && typeList != null) {
			for (DetailedTypeDefinition type : typeList) type.accept(visitor);
		}

		if (visitSubscope == true && methodList != null) {
			for (MethodDefinition method : methodList) method.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
	
	
	/**
	 * Set the modifier flag 
	 */
	public void setModifierFlag(int flag) {
		this.flag = flag;
	}
	
	/**
	 * Test if the class is public according to the modifier flag
	 */
	@Override
	public boolean isPublic() {
		return Modifier.isPublic(flag);
	}

	/**
	 * Test if the class is private according to the modifier flag
	 */
	public boolean isPrivate() {
		return Modifier.isPrivate(flag);
	}

	/**
	 * Test if the class is protected according to the modifier flag
	 */
	public boolean isProtected() {
		return Modifier.isProtected(flag);
	}

	/**
	 * Test if the class is static according to the modifier flag
	 */
	public boolean isStatic() {
		return Modifier.isStatic(flag);
	}

	/**
	 * Test if the class is protected according to the modifier flag
	 */
	public boolean isFinal() {
		return Modifier.isFinal(flag);
	}

	/**
	 * Test if the class is abstract according to the modifier flag
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(flag);
	}

	public void setCorrespondingFile(String filePath) {
		this.filePath = filePath;
	}
	
	public String getCorrespondingFilePath() {
		return filePath;
	}
}
