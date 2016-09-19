package nameTable.nameScope;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import util.SourceCodeLocation;
import nameTable.NameTableVisitor;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.SimpleTypeDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.NameReferenceLabel;

/**
 * The class represent a compilation unit scope
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2014-1-1 Zhou Xiaocong
 * 		Modify the method resolve() and bindImportDeclaration() to support on-demand import declaration!
 * 		Modify the field 'types' to List<DetailedTypeDefinition>
 */
public class CompilationUnitScope implements NameScope, Comparable<CompilationUnitScope> {
	// File name of the compilation unit. It is used to generation the source code location of the name definitions and name references
	private String unitFullName = null;				
	private PackageDefinition packageScope = null;	// The package of the compilation unit
	private List<TypeDefinition> types = null;		// The type definitions defined in the compilation unit
	private List<NameReference> imports = null;		// The type reference or package reference imported by the compilation unit
	
	private List<NameReference> references = null;	// The references occurs in the compilation unit directly. Generally, it will be null!

	public CompilationUnitScope(String unitFullName) {
		this.unitFullName = unitFullName;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_TYPE) {
			TypeDefinition typeDef = (TypeDefinition)nameDef;
			if (!typeDef.isDetailedType() && !typeDef.isEnumeration()) throw new IllegalNameDefinition("The name defined in a compilation unit must be a detailed type or enumeration!");
			if (!typeDef.isPackageMember()) throw new IllegalNameDefinition("The name defined in a compilation unit must be a top level type!");
			if (types == null) types = new ArrayList<TypeDefinition>();
			types.add(typeDef);
		} else throw new IllegalNameDefinition("The name defined in a compilation unit must be a type!");
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return packageScope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_COMPILATION_UNIT;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return unitFullName;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (types == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		for (TypeDefinition type : types) {
			if (type.isEnumeration()) result.add((EnumTypeDefinition)type);
			else if (type.isDetailedType()) result.add((DetailedTypeDefinition)type);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in compilation unit " + this.getScopeName());

		NameReferenceKind refKind = reference.getReferenceKind();
		if (refKind != NameReferenceKind.NRK_TYPE && refKind != NameReferenceKind.NRK_PACKAGE) {
			// In a compilation unit, and its enclosing scope (i.e. a package, or the system scope), we can only resolve 
			// a package reference or a type reference 
			return false;
		}
		
		// Match the reference in the type list
		if (refKind == NameReferenceKind.NRK_TYPE && types != null) {
			for (TypeDefinition type : types) {
				if (type.match(reference)) return true;
			}
		}
		// Match the reference in the imported type list
		if (imports != null) {
			for (NameReference importedType : imports) {
				NameDefinition nameDef = importedType.getDefinition();
				
				if (nameDef != null) {
					NameDefinitionKind defKind = nameDef.getDefinitionKind();
					if (defKind == NameDefinitionKind.NDK_PACKAGE && refKind == NameReferenceKind.NRK_TYPE) {
						// The import declaration refer to a package (i.e. an on-demand declaration), and the reference is a type reference, 
						// we try to match the reference with the detailed type defined in the package!
						PackageDefinition packageDef = (PackageDefinition)nameDef;
						if (packageDef.resolve(reference)) return true;
					} else {
						// The import declaration refer to a package and the reference is a package reference, or the import declaration refer to
						// a type and the reference is also a type reference, we try to match their names
						if (nameDef.match(reference)) return true; 
					}
				}
			}
		}
		
		// Resolve the reference in the enclosing scope
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the fullFileName
	 */
	public String getUnitFullName() {
		return unitFullName;
	}

	/**
	 * @return the packageScope
	 */
	public PackageDefinition getPackage() {
		return packageScope;
	}
	
	public void setPackage(PackageDefinition pkgDef) {
		packageScope = pkgDef;
	}

	/**
	 * @return the types
	 */
	public List<TypeDefinition> getTypeList() {
		return types;
	}

	/**
	 * @return the imports
	 */
	public List<NameReference> getImportList() {
		return imports;
	}

	public void addImportDeclaration(NameReference importedType) {
		if (imports == null) imports = new ArrayList<NameReference>();
		imports.add(importedType);
	}
	
	/**
	 * Match the reference in the type list of the compilation unit
	 */
	public boolean match(NameReference reference) {
		if (types == null) return false;
		for (TypeDefinition type : types) {
			if (type.match(reference)) return true;
		}
		return false;
	}
	
	/**
	 * Bind the type reference in the import list to the appropriate type definition
	 */
	public void bindImportDeclaration() {
		SystemScope systemScope = (SystemScope)getEnclosingScope().getEnclosingScope();
		if (systemScope == null) throw new AssertionError("The system scope is null in compilation unit: " + unitFullName + "!");
		
		if (imports == null) return;
		
		for (NameReference importDecl : imports) {
			if (importDecl.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
				// The import declaration is a single-type-import declaration
				boolean success = false;
				String name = importDecl.getName(); 
				int dotIndex = name.lastIndexOf(NameReferenceLabel.NAME_QUALIFIER);
				if (dotIndex < 1) throw new AssertionError("The imported type [" + name + "] at " + importDecl.getLocation().getFullFileName() + " have not package name!");
				
				String packageName = name.substring(0, dotIndex);
				String typeName = name.substring(dotIndex + 1);
				
				PackageDefinition packageDef = systemScope.findPackageByName(packageName);
				if (packageDef != null) {
					success =  packageDef.matchTypeByReference(importDecl);
				} else {
					success = systemScope.resolve(importDecl);
				}

				if (!success) {
					SimpleTypeDefinition typeDef = new SimpleTypeDefinition(typeName, name, systemScope);
					systemScope.define(typeDef);
					importDecl.bindTo(typeDef);
				} else {
					if (importDecl.getDefinition() == null) throw new AssertionError("Internal error for bind imported type!");
				}
			} else {
				// The import declaration is an on-demand import, and it refer to a package reference
				String packageName = importDecl.getName();
				PackageDefinition packageDef = systemScope.findPackageByName(packageName);
				// If we find a package definition, we bind the reference to this package definition, otherwise we do nothing!
				if (packageDef != null) importDecl.bindTo(packageDef);
			}
		}
	}

	
	/**
	 * Display all definitions to a string for debugging
	 */
	public void printDefinitions(PrintWriter writer, int indent) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);
		
		StringBuffer buffer = new StringBuffer(indentString + "Compilation unit: ");
		buffer.append(unitFullName);
		writer.println(buffer);
		
		if (types != null) {
			for (TypeDefinition type : types) {
				type.printDefinitions(writer, indent+1);
			}		
		}
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
		if (types != null) {
			for (TypeDefinition typeDef : types) {
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
	public NameDefinition getDefinition(String name, boolean includeSubscopes) {
		if (types == null) return null;
		for (TypeDefinition type : types) {
			if (type.match(name)) return type;
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
		if (types == null) return null;
		for (TypeDefinition type : types) {
			if (id.equals(type.getUniqueId())) return type;
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
		if (types != null) {
			for (TypeDefinition typeDef : types) {
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
				List<NameReference> temp = subscope.findAllReferencesByPosition(start,end);
				result.addAll(temp);
			}
		}
		return result;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return unitFullName.equals(location.getFullFileName());
	}
	
	@Override
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();

		if (types != null) {
			for (TypeDefinition type : types) {
				if (type.isDetailedType()) result.add((DetailedTypeDefinition)type);
			}
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

	@Override
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		int result = 0;
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_TYPE) {
			if (types != null) result += types.size();
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
		
		if (visitSubscope == true && types != null) {
			for (TypeDefinition type : types) {
				if (type.isEnumeration()) {
					EnumTypeDefinition enumType = (EnumTypeDefinition)type;
					enumType.accept(visitor);
				} else if (type.isDetailedType()) {
					DetailedTypeDefinition detailedType = (DetailedTypeDefinition)type;
					detailedType.accept(visitor);
				}
			}
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unitFullName == null) ? 0 : unitFullName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;

		if (!(obj instanceof CompilationUnitScope)) return false;
		CompilationUnitScope other = (CompilationUnitScope)obj;
		return unitFullName.equals(other.unitFullName);
	}
	
	@Override
	public int compareTo(CompilationUnitScope other) {
		return unitFullName.compareTo(other.unitFullName);
	}
}
