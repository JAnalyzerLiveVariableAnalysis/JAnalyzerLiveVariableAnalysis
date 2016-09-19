package nameTable.nameScope;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import util.SourceCodeLocation;
import nameTable.NameTableVisitor;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;

/**
 * The class represents the system scope
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class SystemScope implements NameScope {
	public static final String SYSTEM_PACKAGE_NAME = "java.lang";
	public static final String ROOT_OBJECT_NAME = "Object";
	
	private static final String SYSTEM_SCOPE_NAME = "<System>";	// The default name of the system scope
	private List<PackageDefinition> packages = null;			// The packages of the system
	private List<NameDefinition> names = null;					// The global names defined or used in the system
	
	private List<NameReference> references = null;				// The references occurs in the system scope. Generally, it will be null!
	
	private List<DetailedTypeDefinition> allDetailedTypes = null;	// A buffer to store a list of all detailed type definition.
	
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_PACKAGE) {
			if (packages == null) packages = new ArrayList<PackageDefinition>();
			packages.add((PackageDefinition) nameDef);
		} else {
			if (names == null) names = new ArrayList<NameDefinition>();
			names.add(nameDef);
		}
	}

	@Override
	public NameScope getEnclosingScope() {
		return null;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_SYSTEM;
	}

	@Override
	public String getScopeName() {
		return SYSTEM_SCOPE_NAME;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		if (packages == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		for (PackageDefinition pkgDef : packages) result.add(pkgDef);
		return result;
	}

	@Override
	/**
	 * For system scope, resolving a name reference is to match the reference in the name list 
	 * defined in the system scope!
	 */
	public boolean resolve(NameReference reference) {
		PackageDefinition systemPackage = null;

//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in system scope " + this.getScopeName());
		
		if (packages != null) {
			for (PackageDefinition packageDef : packages) {
				// Test if there is the system package!
				if (packageDef.getFullQualifiedName().equals(SystemScope.SYSTEM_PACKAGE_NAME)) systemPackage = packageDef;
				if (packageDef.match(reference)) return true;
			}
		}
		
		if (names != null){
			for (NameDefinition name : names) {
				if (name.match(reference)) return true;
			}
		}
		
		if (systemPackage != null) {
			if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE || reference.getReferenceKind() == NameReferenceKind.NRK_LITERAL) {
				// Finally, try to match the reference (a type, or a literal) in the system package!
				return systemPackage.matchTypeByReference(reference);
			} else return false;
		} else return false;
	}

	/**
	 * @return the packages
	 */
	public List<PackageDefinition> getPackages() {
		return packages;
	}

	/**
	 * Find the package definition by name
	 */
	public PackageDefinition findPackageByName(String packageName) {
		if (packages == null) return null;
		for (PackageDefinition packageDef : packages) {
			if (packageDef.getFullQualifiedName().equals(packageName)) return packageDef;
		}
		return null;
	}
	
	public PackageDefinition getUnnamedPackageDefinition() {
		if (packages == null) return null;
		for (PackageDefinition packageDef : packages) {
			if (packageDef.isUnnamedPackage()) return packageDef;
		}
		return null;
	}
	
	/**
	 * @return the names
	 */
	public List<NameDefinition> getNames() {
		return names;
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
	
	/**
	 * Display all definitions to a string for debugging
	 */
	@Override
	public void printDefinitions(PrintWriter writer, int indent) {
		StringBuffer buffer = new StringBuffer();
		
		if (names != null) {
			buffer.append("Global names: \n");
			for (NameDefinition name : names) { 
				buffer.append("\t" + name.toString() + "\n");
			}
		}
		writer.print(buffer);
		
		if (packages != null) {
			for (PackageDefinition packageDef : packages) {
				packageDef.printDefinitions(writer, 0);
			}
		}
	}
	
	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (names != null) {
			for (NameDefinition nameDef : names) {
				if (nameDef.match(namePostFix)) result.add(nameDef);
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
		if (names == null) return null;
		for (NameDefinition nameDef : names) {
			if (nameDef.match(name)) return nameDef;
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
		if (names == null) return null;
		for (NameDefinition nameDef : names) {
			if (id.equals(nameDef.getUniqueId())) return nameDef;
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
		if (names != null) {
			for (NameDefinition nameDef : names) {
				SourceCodeLocation location = nameDef.getLocation();
				if (location != null && location.isBetween(start, end)) result.add(nameDef);
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
	public boolean containsLocation(SourceCodeLocation location) {
		return true;
	}

	
	@Override
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition() {
		if (allDetailedTypes != null) return allDetailedTypes;	// If this method has been called, then we return the result directly

		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) {
				List<DetailedTypeDefinition> temp = subscope.getAllDetailedTypeDefinition();
				result.addAll(temp);
			}
		}
		
		// Buffered the result for all next calls! 
		// Note: we assume that the first call of this method is after that all definitions have been created!
		allDetailedTypes = result;		 
		return allDetailedTypes;
	}

	
	/**
	 * Return all methods defined in the sub-type of the baseType (and not equal to baseType), and redefine (i.e. override) the given method!
	 */
	public List<MethodDefinition> getAllOverrideMethods(DetailedTypeDefinition baseType, MethodDefinition method) {
		List<MethodDefinition> result = new ArrayList<MethodDefinition>();
		
		List<DetailedTypeDefinition> allDetailedTypeList = getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition type : allDetailedTypeList) {
			
//			DetailedTypeDefinition.counter = 0;
			
//			System.out.println("All type " + allDetailedTypeList.size() + ", In System scope, check type: " + type.getFullQualifiedName() + ", parent: " + baseType.getFullQualifiedName());
			if (type != baseType && type.isSubtypeOf(baseType)) {
				List<MethodDefinition> methodList = type.getMethodList();
				if (methodList != null) {
					for (MethodDefinition methodInSubType : methodList) {
						if (methodInSubType.isOverrideMethod(method)) {
							result.add(methodInSubType);
							
//							System.out.println("Add override method in [" + type.getSimpleName() + "] of base type [" + baseType.getSimpleName() + "]");
//							System.out.println("\tAdd method " + methodInSubType.toFullString());
//							System.out.println("\t\tIt override " + method.toFullString());
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Important note: In system scope, getTotalNumberOfDefinitions(NameDefinitionKind.NDK_TYPE) != getAllDetailedTypeDefinition().size()
	 * since the former includes enumeration types, while the latter does not include enumeration types!
	 */
	@Override
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		int result = 0;
		
		if (kind == NameDefinitionKind.NDK_PACKAGE) {
			if (packages == null) return 0;
			return packages.size();
		}
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN) {
			if (packages != null) result += packages.size();
			if (names != null) result += names.size();
		}
		
		List<NameScope> subscopes = getSubScopeList();
		if (subscopes != null) {
			for (NameScope subscope : subscopes) result += subscope.getTotalNumberOfDefinitions(kind);
		}
		
		return result;
	}
	
	
	public static SystemScope getRootScope(NameScope startScope) {
		NameScope currentScope = startScope;
		while (currentScope != null) {
			NameScope parent = currentScope.getEnclosingScope();
			if (parent == null) break;
			currentScope = parent;
		}
		if (currentScope == null || currentScope.getScopeKind() != NameScopeKind.NSK_SYSTEM) 
			throw new AssertionError("The root scope of " + startScope.getScopeName() + " is not a system scope!");
		return (SystemScope)currentScope;
	}

	@Override
	/**
	 * Accept a visitor to visit the current scope
	 */
	public void accept(NameTableVisitor visitor) {
		visitor.preVisit(this);
		
		boolean visitSubscope = visitor.visit(this);
		if (visitSubscope == true && packages != null) {
			for (PackageDefinition pkgDef : packages) pkgDef.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}

}
