package nameTable.nameScope;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableVisitor;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import util.SourceCodeLocation;

/**
 * The class represent a local scope
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class LocalScope implements NameScope {
	private SourceCodeLocation start = null;				// The start position of the scope
	private SourceCodeLocation end = null;					// The end position of the scope
	private List<VariableDefinition> variables = null;		// The variables defined in the scope
	private List<DetailedTypeDefinition> localTypes = null;	// The local types defined in the scope
	private List<LocalScope> subscopes = null;				// The sub-scopes enclosed in the scope
	private NameScope enclosingScope = null;				// The scope enclosed this scope

	private List<NameReference> references = null;			// The references occur in the scope
	
	public LocalScope() {
	}

	public LocalScope(NameScope parentScope) {
		this.enclosingScope = parentScope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		NameDefinitionKind nameKind = nameDef.getDefinitionKind();
		if (nameKind == NameDefinitionKind.NDK_TYPE) {
			if (localTypes == null) localTypes = new ArrayList<DetailedTypeDefinition>();
			localTypes.add((DetailedTypeDefinition) nameDef);
		} else if (nameKind == NameDefinitionKind.NDK_VARIABLE) {
			if (variables == null) variables = new ArrayList<VariableDefinition>();
			variables.add((VariableDefinition) nameDef);
		} else throw new IllegalNameDefinition("The name defined in a local scope must be a local type or a variable!");
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getEnclosingScope()
	 */
	@Override
	public NameScope getEnclosingScope() {
		return enclosingScope;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_LOCAL;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return "<Block>@" + start.toFullString();
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getSubScopeList()
	 */
	@Override
	public List<NameScope> getSubScopeList() {
		if (localTypes == null && subscopes == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		if (localTypes != null) {
			for (DetailedTypeDefinition type : localTypes) result.add(type);
		}
		if (subscopes != null) {
			for (LocalScope scope : subscopes) result.add(scope);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in local scope " + this.getScopeName());
		
		if (reference.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
			if (variables != null) {
				for (VariableDefinition var : variables) {
					if (var.match(reference)) return true;
				}
			}
		} else if (reference.getReferenceKind() == NameReferenceKind.NRK_TYPE) {
			if (localTypes != null) {
				for (TypeDefinition type : localTypes) {
					if (type.match(reference)) return true;
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the start
	 */
	public SourceCodeLocation getScopeStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public SourceCodeLocation getScopeEnd() {
		return end;
	}

	/**
	 * @return the variables
	 */
	public List<VariableDefinition> getVariableList() {
		return variables;
	}

	/**
	 * @return the localTypes
	 */
	public List<DetailedTypeDefinition> getLocalTypeList() {
		return localTypes;
	}

	/**
	 * @return the subscopes
	 */
	public List<LocalScope> getSubLocalScope() {
		return subscopes;
	}

	/**
	 * Set the start and end position of the local scope
	 */
	public void setScopeArea(SourceCodeLocation start, SourceCodeLocation end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Test if a location is included in the local scope 
	 */
	public boolean isInScopeArea(SourceCodeLocation location) {
		return location.isBetween(start, end);
	}

	/**
	 * Set the scope enclosed the scope
	 */
	public void setEnclosingScope(NameScope parent) {
		this.enclosingScope = parent;
	}

	/**
	 * Add a sub-scope to the local scope
	 */
	public void addSubLocalScope(LocalScope scope) {
		if (subscopes == null) subscopes = new ArrayList<LocalScope>();
		subscopes.add(scope);
	}
	
	/**
	 * Get all local variable definition in this local scope and its sub scopes
	 */
	public List<VariableDefinition> getAllLocalVaraibleDefinitions() {
		List<VariableDefinition> result = new ArrayList<VariableDefinition>();
		if (variables != null) result.addAll(variables);
		if (subscopes != null) {
			for (LocalScope subscope : subscopes) {
				List<VariableDefinition> subResult = subscope.getAllLocalVaraibleDefinitions();
				result.addAll(subResult);
			}
		}
		return result;
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
		
		List<NameScope> subscopeList = getSubScopeList();
		if (subscopeList != null) {
			for (NameScope subscope : subscopeList) {
				subscope.printReferences(writer, includeLiteral);
			}		
		}
	}

	/**
	 * Display all definitions to a string for debugging
	 */
	@Override
	public void printDefinitions(PrintWriter writer, int indent) {
		// Create a space string for indent;
		char[] indentArray = new char[indent];
		for (int index = 0; index < indentArray.length; index++) indentArray[index] = '\t';
		String indentString = new String(indentArray);

		StringBuffer buffer = new StringBuffer(indentString + "Local scope: " + getScopeName() + "\n");
		if (variables != null) {
			buffer.append(indentString + "Variables: \n");
			for (VariableDefinition variable : variables) {
				buffer.append(indentString + "\t" + variable.getType().getName() + " " + variable.getSimpleName() + "\n");
			}
		}
		writer.print(buffer);
		
		if (subscopes != null) {
			for (LocalScope subscope : subscopes) {
				subscope.printDefinitions(writer, indent + 1);
			}
		}
		if (localTypes != null) {
			writer.print(indentString + "Local types: \n");
			for (DetailedTypeDefinition type : localTypes) {
				type.printDefinitions(writer, indent+1);
			}
		}
	}

	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (variables != null) {
			for (VariableDefinition varDef : variables) {
				if (varDef.match(namePostFix)) result.add(varDef);
			}
		}
		if (localTypes != null) {
			for (DetailedTypeDefinition typeDef : localTypes) {
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
		if (variables != null) {
			for (VariableDefinition var : variables) {
				if (var.match(name)) return var;
			}
		}
		if (localTypes != null) {
			for (DetailedTypeDefinition type : localTypes) {
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
		if (variables != null) {
			for (VariableDefinition var : variables) {
				if (id.equals(var.getUniqueId())) return var;
			}
		}
		if (localTypes != null) {
			for (DetailedTypeDefinition type : localTypes) {
				if (id.equals(type.getUniqueId())) return type;
			}
		}
		if (includeSubscopes) {
			List<NameScope> subscopes = getSubScopeList();
			if (subscopes != null) {
				for (NameScope subscope : subscopes) {
//					if (id.contains("events@151:17")) {
//						System.out.println("\t\tBefore find in local scope "  + getScopeName() + " subscope " + subscope.getScopeName() + " for " + id);
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
			if (variables != null) {
				for (VariableDefinition varDef : variables) {
					SourceCodeLocation location = varDef.getLocation();
					if (location.isBetween(start, end)) result.add(varDef);
				}
			}
			if (localTypes != null) {
				for (DetailedTypeDefinition typeDef : localTypes) {
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
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(start, end);
	}
	
	@Override
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition() {
		List<DetailedTypeDefinition> result = new ArrayList<DetailedTypeDefinition>();
		if (localTypes != null) result.addAll(localTypes);
		
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
			if (localTypes != null) result += localTypes.size();
		}
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_VARIABLE) {
			if (variables != null) result += variables.size();
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
		if (visitSubscope == true && localTypes != null) {
			for (DetailedTypeDefinition type : localTypes) type.accept(visitor);
		}
		if (visitSubscope == true && subscopes != null) {
			for (LocalScope scope : subscopes) scope.accept(visitor);
		}
		
		visitor.endVisit(this);
		visitor.postVisit(this);
	}
	
}
