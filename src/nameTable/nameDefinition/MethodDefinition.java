package nameTable.nameDefinition;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import nameTable.NameTableVisitor;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.LocalScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import util.SourceCodeLocation;

/**
 * The class represent a method definition
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 * 
 * @update 2013-12-28
 * 		Add method mathMethod(MethodReference) to match the method definition with a method reference. Now we consider the number of parameter, but we
 * 			do not consider the type conversion between the actual parameters and formal parameters.
 * @update 2013-12-29
 * 		Add method getReturnTypeDefinition()
 * 		Add method isOverrideMethod()
 */
public class MethodDefinition extends NameDefinition implements NameScope {
	private TypeReference returnType = null;			// The return type of the method
	private List<VariableDefinition> parameters = null;	// The parameter list of the method
	private List<TypeReference> throwTypes = null;		// The throw types declared for the method
	private LocalScope bodyScope = null;				// The local scope corresponding to the body of the method
	private SourceCodeLocation endLocation = null;
	
	private List<NameReference> references = null;		// The reference defined in the method, i.e. the type references of the parameters of the method
	private int flag = 0;								// The modifier flag of the method
	private boolean constructorFlag = false;
	
	public MethodDefinition(String simpleName, String fullQualifiedName, SourceCodeLocation location, 
			NameScope scope, SourceCodeLocation endLocation) {
		super(simpleName, fullQualifiedName, location, scope);
		this.endLocation = endLocation;
	}

	@Override
	public boolean containsLocation(SourceCodeLocation location) {
		return location.isBetween(this.location, endLocation);
	}

	/* (non-Javadoc)
	 * @see nameTable.NameDefinition#getNameDefinitionKind()
	 */
	@Override
	public NameDefinitionKind getDefinitionKind() {
		return NameDefinitionKind.NDK_METHOD;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#define(nameTable.NameDefinition)
	 */
	@Override
	public void define(NameDefinition nameDef) throws IllegalNameDefinition {
		if (nameDef.getDefinitionKind() == NameDefinitionKind.NDK_PARAMETER) {
			if (parameters == null) parameters = new ArrayList<VariableDefinition>();
			parameters.add((VariableDefinition)nameDef);
		} else throw new IllegalNameDefinition("Only parameters can be defined in a method definition!");
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
	public DetailedTypeDefinition getEnclosingType() {
		NameScope currentScope = scope;
		while (currentScope.getScopeKind() != NameScopeKind.NSK_TYPE) currentScope = currentScope.getEnclosingScope();
		return (DetailedTypeDefinition)currentScope;
	}
	

	/* (non-Javadoc)
	 * @see nameTable.NameScope#getNameScopeKind()
	 */
	@Override
	public NameScopeKind getScopeKind() {
		return NameScopeKind.NSK_METHOD;
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
		if (bodyScope == null) return null;
		List<NameScope> result = new ArrayList<NameScope>();
		result.add(bodyScope);
		return result;
	}

	/* (non-Javadoc)
	 * @see nameTable.NameScope#resolve(nameTable.NameReference)
	 */
	@Override
	public boolean resolve(NameReference reference) {
//		if (reference.getName().equals("ORB")) System.out.println("Resolve ORB in method " + this.getScopeName());

		// In a method definitions, we can only resolve the parameters defined in the method or the method itself.
		if (reference.getReferenceKind() == NameReferenceKind.NRK_VARIABLE) {
			if (parameters != null) {
				for (VariableDefinition var : parameters) {
					if (var.match(reference)) return true;
				}
			}
		}
		return getEnclosingScope().resolve(reference);
	}

	/**
	 * @return the reference of the return type
	 */
	public TypeReference getReturnType() {
		return returnType;
	}
	
	/**
	 * @return the definition of the return type
	 */
	public TypeDefinition getReturnTypeDefinition() {
		if (returnType == null) return null;
		returnType.resolveBinding();
		return (TypeDefinition)returnType.getDefinition();
	}

	/**
	 * @param flag: if flag == true, then we return its main type and its parameter types when the return type of the method
	 * is a parameterized type, otherwise we only return its main type
	 * @return the possible list of type definition of the return type. If the return type of the method is a parameterized
	 * type, then we return its main type and its parameter types.
	 */
	public List<TypeDefinition> getReturnTypeDefinition(boolean flag) {
		if (returnType == null) return null;
		returnType.resolveBinding();
		if (flag == false || !returnType.isParameterizedType()) {
			List<TypeDefinition> resultList = new ArrayList<TypeDefinition>();
			TypeDefinition type = (TypeDefinition)returnType.getDefinition(); 
			if (type != null) resultList.add(type);
			return resultList;
		}
		
		ParameterizedTypeReference reference = (ParameterizedTypeReference)returnType;
		return reference.getDefinition(true);
	}
	
	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(TypeReference returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the parameters
	 */
	public List<VariableDefinition> getParameters() {
		return parameters;
	}

	/**
	 * @return the bodyScope
	 */
	public LocalScope getBodyScope() {
		return bodyScope;
	}

	/**
	 * @param bodyScope the bodyScope to set
	 */
	public void setBodyScope(LocalScope bodyScope) {
		this.bodyScope = bodyScope;
	}
	
	/**
	 * Return the throw types declared for the method
	 */
	public List<TypeReference> getThrowTypes() {
		return throwTypes;
	}
	
	/**
	 * Add a throw type for the method
	 */
	public void addThrowTypes(TypeReference type) {
		if (throwTypes == null) throwTypes = new ArrayList<TypeReference>();
		throwTypes.add(type);
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

		buffer.append(indentString + "Method: " + simpleName + "\n");
		if (returnType != null) {
			String typeString = returnType.getName();
			for (int count = 0; count < returnType.getDimension(); count++) typeString += "[]";
			buffer.append(indentString + "\t Return type: " + typeString + "\n");
		}
		
		if (parameters != null) {
			buffer.append(indentString + "\t Parameters: \n");
			for (VariableDefinition parameter : parameters) {
				TypeReference paraType = parameter.getType();
				String typeString = paraType.getName();
				for (int count = 0; count < paraType.getDimension(); count++) typeString += "[]";
				buffer.append(indentString + "\t\t " + typeString + " " + parameter.getSimpleName() + "\n");
			}
		}
		writer.print(buffer);
		
		if (bodyScope != null) bodyScope.printDefinitions(writer, indent+1);
	}
	
	@Override
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix) {
		List<NameDefinition> result = new ArrayList<NameDefinition>();
		if (parameters != null) {
			for (VariableDefinition varDef : parameters) {
				if (varDef.match(namePostFix)) result.add(varDef);
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
		if (parameters != null) {
			for (VariableDefinition varDef : parameters) {
				if (varDef.match(name)) return varDef;
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
		if (parameters != null) {
			for (VariableDefinition varDef : parameters) {
				if (id.equals(varDef.getUniqueId())) return varDef;
			}
		}
		if (includeSubscopes) {
			if (bodyScope != null) {
				NameDefinition target = bodyScope.findDefinitionById(id, includeSubscopes);
				if (target != null) return target;
			} else {
//				if (id.contains("events@151:17")) {
//					System.out.println("\t\t\tThe body scope of method " + fullQualifiedName + " is null!");
//				}
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
		if (parameters != null) {
			for (VariableDefinition varDef : parameters) {
				SourceCodeLocation location = varDef.getLocation();
				if (location.isBetween(start, end)) result.add(varDef);
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
	
	/**
	 * Match the current method definition with a method reference. Since it can not only match the method name, so we must use this method for 
	 * resolve a method reference.
	 * 
	 * @since 2013-12-28
	 */
	public boolean matchMethod(MethodReference reference) {
		String referenceName = reference.getName();
		
		// At first we should match the method name
		if (!match(referenceName)) return false;
		
		List<NameReference> args = reference.getArguments();
		
		// Test if the number of arguments is equal to the number of parameters of the method
		if (args == null && parameters == null) return true;
		if (args == null && parameters != null) return false;
		if (args != null && parameters == null) return false;
		if (args.size() != parameters.size()) return false;
		
		// Test if the type of the argument is the sub-type of the type of the corresponding parameter
		for (int index = 0; index < args.size(); index++) {
			NameReference argument = args.get(index);
			VariableDefinition parameter = parameters.get(index);
			TypeDefinition argumentType = getArgumentType(argument);
			TypeDefinition paraType = parameter.getTypeDefinition();
			
			if (argumentType != null && paraType != null) {
				// The type of the argument should be the sub-type of the type of the parameter, otherwise the (actual) argument can not 
				// be assigned to the (formal) parameter!
				if (!argumentType.isSubtypeOf(paraType)) return false;
			} // If we can not resolve the argument type or the parameter type, then we ignore the type compatibility between the argument  and the parameter.
		}
		return true;
	}

	private TypeDefinition getArgumentType(NameReference reference) {
		reference.resolveBinding();
		NameDefinition nameDef = reference.getDefinition();
		if (nameDef == null) return null;

		NameDefinitionKind nameDefKind = nameDef.getDefinitionKind();
		if (nameDefKind == NameDefinitionKind.NDK_TYPE) return (TypeDefinition)nameDef;
		else if (nameDefKind == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDef = (FieldDefinition)nameDef;
			return fieldDef.getTypeDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_VARIABLE || nameDefKind == NameDefinitionKind.NDK_PARAMETER) {
			VariableDefinition varDef = (VariableDefinition)nameDef;
			return varDef.getTypeDefinition();
		} else if (nameDefKind == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDef = (MethodDefinition)nameDef;
			return methodDef.getReturnTypeDefinition();
		} else throw new AssertionError("Internal error: the argument " + reference.toString() + " bind to unexpected definition " + nameDef.toString() + ", kind = " + nameDefKind);
	}

	/**
	 * Test if the current method redefine a given method, i.e. the current method and the given method have the same name and 
	 * the same signature (parameter type and return type). Of course, the override relation indeed hold only when the current 
	 * method and the given method in two types has inheritance relations. 
	 * 
	 * @since 2013-12-29
	 */
	public boolean isOverrideMethod(MethodDefinition other) {
		String otherSimpleName = other.getSimpleName();
		
		// At first the two methods must have the same simple name
		if (!simpleName.equals(otherSimpleName)) return false;
		
		List<VariableDefinition> otherParas = other.getParameters();

		// The return types of the two methods should bind to the same type definition, otherwise they do not have the same signature 
		if (getReturnTypeDefinition() != other.getReturnTypeDefinition()) return false;
		
		// Test if the number of arguments is equal to the number of parameters of the method
		if (otherParas == null && parameters == null) return true;
		if (otherParas == null && parameters != null) return false;
		if (otherParas != null && parameters == null) return false;
		if (otherParas.size() != parameters.size()) return false;
		
		for (int index = 0; index < otherParas.size(); index++) {
			VariableDefinition parameter = parameters.get(index);
			VariableDefinition otherPara = otherParas.get(index);
			
			// The two parameters' type should bind to the same type definition, otherwise they do not have the same signature
			if (parameter.getTypeDefinition() != otherPara.getTypeDefinition()) return false;
		}
		
		return true;
	}
	
	/**
	 * Test if the current method overload a given method, i.e. the current method and the given method have the same name but have  
	 * different parameter types. Of course, the overload relation indeed hold only when the current  method and the given method 
	 * in the same types or in two types has inheritance relations.
	 * 
	 * @since 2015-10-24
	 */
	public boolean isOverloadMethod(MethodDefinition other) {
		String otherSimpleName = other.getSimpleName();
		
		// At first the two methods must have the same simple name
		if (!simpleName.equals(otherSimpleName)) return false;
		
		List<VariableDefinition> otherParas = other.getParameters();
		
		// Test if the number of arguments is equal to the number of parameters of the method
		if (otherParas == null && parameters == null) return false;		// This case means two methods overridden rather than overloaded
		if (otherParas == null && parameters != null) return true;
		if (otherParas != null && parameters == null) return true;
		if (otherParas.size() != parameters.size()) return true;
		
		for (int index = 0; index < otherParas.size(); index++) {
			VariableDefinition parameter = parameters.get(index);
			VariableDefinition otherPara = otherParas.get(index);
			
			// The two parameters' type should bind to the same type definition, otherwise they do not have the same signature
			if (parameter.getTypeDefinition() != otherPara.getTypeDefinition()) return true;
		}
		return false;		// Also, this case means two methods overridden rather than overloaded.
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
	
	public SourceCodeLocation getEndLocation() {
		return endLocation;
	}

	@Override
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind) {
		int result = 0;
		
		if (kind == NameDefinitionKind.NDK_UNKNOWN || kind == NameDefinitionKind.NDK_PARAMETER) {
			if (parameters != null) result += parameters.size();
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
		
		if (visitSubscope == true && bodyScope != null) bodyScope.accept(visitor);
		
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
	
	public void setConstructor(boolean isConstruct) {
		constructorFlag = isConstruct;
	}
	
	public void setConstructor() {
		constructorFlag = true;
	}
	
	public boolean isConstructor() {
		return constructorFlag;
	}
	
	public boolean isAutoGenerated() {
		return false;
	}
	
	
}
