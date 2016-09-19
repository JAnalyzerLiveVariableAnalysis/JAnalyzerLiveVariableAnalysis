package nameTable.nameDefinition;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import util.SourceCodeLocation;
import nameTable.NameTableVisitor;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * The class represent a package definition
 * @author Zhou Xiaocong
 * @since 2013-2-1
 * @version 1.0
 */
public class PackageDefinition extends NameDefinition implements NameScope {
	private static final String UNNAMED_PACKAGE_NAME = "<UnnamedPckage>";	// The name of the unnamed package
	private List<CompilationUnitScope> units = null; 		// The compilation units in the package
	
	private List<NameReference> references = null;			// The references defined in the package directly
															// Generally, it should be null!
	private File file;
	
	/**
	 * Constructor for unnamed package
	 */
	public PackageDefinition() {
		super(UNNAMED_PACKAGE_NAME);
	}
	
	public PackageDefinition(String packageName) {
		super(packageName);
	}

	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_PACKAGE;
	}

	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		throw new IllegalNameDefinition("Can not define names in a package directly!");
	}

	@Override
	public NameScope getEnclosingScope() {
		return scope;
	}

	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_PACKAGE;
	}

	@Override
	public String getScopeName() {
		return simpleName;
	}

	@Override
	public List<NameScope> getSubScopeList() {
		if (units == null) return null;
		List<NameScope> result = new ArrayList<NameScope>(units.size());
		for (CompilationUnitScope scope : units) result.add(scope);
		return result;
	}

	@Override
	public boolean resolve(NameReference reference) {
//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in package " + this.getScopeName());

		// In package definition, we match the name reference to the type defined in the package, i.e. 
		// the package member type definition. 
		if (units != null) {
			for (CompilationUnitScope unit : units) {
				List<TypeDefinition> types = unit.getTypeList();
				
				if (types != null) {
					for (TypeDefinition type : types) {
						if (type.match(reference)) return true;
					}
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * Match the reference in the type list of the compilation unit
	 */
	public boolean matchTypeByReference(NameReference reference) {
		if (units == null) return false;
		for (CompilationUnitScope unit : units) {
			if (unit.match(reference)) return true;
		}
		return false;
	}
	
	/**
	 * Return the compilation unit scope defined in the package
	 */
	public List<CompilationUnitScope> getCompilationUnitList() {
		return units;
	}

	/**
	 * Add a compilation unit scope for the package
	 */
	public void addCompilationUnit(CompilationUnitScope compUnit) {
		if (units == null) units = new ArrayList<CompilationUnitScope>();
		units.add(compUnit);
	}
	
	/**
	 * Test if the package represent the unnamed package
	 */
	public boolean isUnnamedPackage() {
		if (simpleName.equals(UNNAMED_PACKAGE_NAME)) return true;
		else return false;
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
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		writer.print(indentString + "Package: " + simpleName + "\n");
		for (CompilationUnitScope unit : units) {
			unit.printDefinitions(writer, indent + 1);
		}
	}

	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
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
		if (units == null) return null;
		for (CompilationUnitScope unit : units) {
			List<TypeDefinition> types = unit.getTypeList();
			for (TypeDefinition type : types) {
				if (type.match(name)) return type;
			}
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
		if (units == null) return null;
		for (CompilationUnitScope unit : units) {
			List<TypeDefinition> types = unit.getTypeList();
			if (types != null) {
				for (TypeDefinition type : types) {
					if (id.equals(type.getUniqueId())) return type;
				}
			}
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
	public List<NameDefinition> findAllDefinitionsByPosition(
			SourceCodeLocation start, SourceCodeLocation end) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
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
		if (units == null) return false;
		for (CompilationUnitScope unit : units) {
			if (unit.getUnitFullName().equals(location.getFullFileName())) return true;
		}
		return false;
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
		
		if (visitSubscope == true && units != null) {
			for (CompilationUnitScope scope : units) scope.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}

	public void bindPackageDir(File dir) {
		this.file = dir;
	}
	
	public File getPackageDir() {
		// TODO Auto-generated method stub
		return file;
	}
}


