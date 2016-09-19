package nameTable;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor to get all references in the accepted scope and its sub-scope
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameReferenceVisitor extends NameTableVisitor {

	// The result list of name definition after the visiting
	private List<NameReference> result = new ArrayList<NameReference>();
	// A filter to accept appropriate name definition
	private NameTableFilter filter = null;
	
	public void reset() {
		result = new ArrayList<NameReference>();
	}
	
	public List<NameReference> getResult() {
		return result;
	}
	
	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}
	
	/**
	 * Get the references in the system scope
	 */
	public boolean visit(SystemScope scope) {
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
	
	/**
	 * Get the references in a package definition
	 */
	public boolean visit(PackageDefinition scope) {
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
	
	
	/**
	 * Get the references in a compilation unit scope, including the reference in the import list!
	 */
	public boolean visit(CompilationUnitScope scope) {
		List<NameReference> imports = scope.getImportList();
		if (imports != null) {
			for (NameReference reference : imports) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
	
	/**
	 * Get the references in a detailed type, including the references of its super types.
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		List<TypeReference> superTypes = scope.getSuperList();
		if (superTypes != null) {
			for (TypeReference reference : superTypes) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
	
	/**
	 * Get the references defined in a enum type
	 */
	public boolean visit(EnumTypeDefinition scope) {
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
	
	/**
	 * Get the references in a method, including its return type reference and throw type references
	 */
	public boolean visit(MethodDefinition scope) {
		TypeReference returnType = scope.getReturnType();
		if (returnType != null) {
			if (filter == null) result.add(returnType);
			else if (filter.accept(returnType)) result.add(returnType);
		}
		
		List<TypeReference> throwTypes = scope.getThrowTypes();
		if (throwTypes != null) {
			for (TypeReference reference : throwTypes) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}

		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}

	/**
	 * Get references in a local types
	 */
	public boolean visit(LocalScope scope) {
		List<NameReference> references = scope.getReferences();
		if (references != null) {
			for (NameReference reference : references) {
				if (filter == null) result.add(reference);
				else if (filter.accept(reference)) result.add(reference);
			}
		}
		return true;
	}
}
