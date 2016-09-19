package nameTable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumConstantDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.SystemScope;

/**
 * A visitor to get all definitions in the accepted scope and its sub-scope
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameDefinitionVisitor extends NameTableVisitor {

	// The result list of name definition after the visiting
	private List<NameDefinition> result = new ArrayList<NameDefinition>();
	// A filter to accept appropriate name definition
	private NameTableFilter filter = null;
	
	public void reset() {
		result = new ArrayList<NameDefinition>();
	}
	
	public List<NameDefinition> getResult() {
		return result;
	}
	
	public TreeSet<NameDefinition> getResultAsTreeSet() {
		TreeSet<NameDefinition> resultSet = new TreeSet<NameDefinition>();
		for (NameDefinition definition : result) resultSet.add(definition);
		return resultSet;
	}
	
	public void setFilter(NameTableFilter filter) {
		this.filter = filter;
	}

	/**
	 * Get the definitions in the system scope including the global names defined in the system scope
	 * and the package definitions.
	 */
	public boolean visit(SystemScope scope) {
		List<NameDefinition> globalNames = scope.getNames();
		if (globalNames != null) {
			for (NameDefinition name : globalNames) {
				if (filter == null) result.add(name);
				else if (filter.accept(name)) result.add(name);
			}
		}
		
		List<PackageDefinition> packages = scope.getPackages();
		if (packages != null) {
			for (PackageDefinition name : packages) {
				if (filter == null) result.add(name);
				else if (filter.accept(name)) result.add(name);
			}
		}
		
		return true;
	}
	
	/**
	 * There is no definition defined in a package directly
	 */
	public boolean visit(PackageDefinition scope) {
		return true;
	}
	
	
	/**
	 * Get the type definitions in a compilation unit scope
	 */
	public boolean visit(CompilationUnitScope scope) {
		List<TypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (TypeDefinition type : types) {
				if (filter == null) result.add(type);
				else if (filter.accept(type)) result.add(type);
			}
		}
		return true;
	}
	
	/**
	 * Get the definitions defined in a detailed type, including its fields, methods and types.
	 */
	public boolean visit(DetailedTypeDefinition scope) {
		List<FieldDefinition> fields = scope.getFieldList();
		if (fields != null) {
			for (FieldDefinition field : fields) {
				if (filter == null) result.add(field);
				else if (filter.accept(field)) result.add(field);
			}
		}
		List<MethodDefinition> methods = scope.getMethodList();
		if (methods != null) {
			for (MethodDefinition method : methods) {
				if (filter == null) result.add(method);
				else if (filter.accept(method)) result.add(method);
			}
		}
		List<DetailedTypeDefinition> types = scope.getTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (filter == null) result.add(type);
				else if (filter.accept(type)) result.add(type);
			}
		}
		return true;
	}
	
	/**
	 * Get the constants defined in a enum type
	 */
	public boolean visit(EnumTypeDefinition scope) {
		List<EnumConstantDefinition> names = scope.getConstants();
		if (names != null) {
			for (EnumConstantDefinition name : names) {
				if (filter == null) result.add(name);
				else if (filter.accept(name)) result.add(name);
			}
		}
		return true;
	}
	
	/**
	 * Get the parameters defined in a method
	 */
	public boolean visit(MethodDefinition scope) {
		List<VariableDefinition> vars = scope.getParameters();
		if (vars != null) {
			for (VariableDefinition var : vars) {
				if (filter == null) result.add(var);
				else if (filter.accept(var)) result.add(var);
			}
		}
		return true;
	}

	/**
	 * Get local variables and types defined in a local types
	 */
	public boolean visit(LocalScope scope) {
		List<VariableDefinition> vars = scope.getVariableList();
		if (vars != null) {
			for (VariableDefinition var : vars) {
				if (filter == null) result.add(var);
				else if (filter.accept(var)) result.add(var);
			}
		}
		List<DetailedTypeDefinition> types = scope.getLocalTypeList();
		if (types != null) {
			for (DetailedTypeDefinition type : types) {
				if (filter == null) result.add(type);
				else if (filter.accept(type)) result.add(type);
			}
		}
		return true;
	}
	
}
