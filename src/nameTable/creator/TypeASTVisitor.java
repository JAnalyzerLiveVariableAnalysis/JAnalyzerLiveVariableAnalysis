package nameTable.creator;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameReference.NameReferenceLabel;
import nameTable.nameReference.ParameterizedTypeReference;
import nameTable.nameReference.QualifiedTypeReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import util.SourceCodeLocation;

/**
 * Visit a type node, create a type reference for the node. Note the type reference created by the class DO NOT
 * add to the creator
 * @author Zhou Xiaocong
 * @since 2013-2-21
 * @version 1.0
 */
public class TypeASTVisitor extends ASTVisitor {
	private String name = null;				// Name of the type reference
	private int dimension = 0;				// Possible dimension number of the type
	
	private NameScope currentScope = null;
	private String unitFullName = null;
	private CompilationUnit root = null;
	
	private SourceCodeLocation resultLocation = null;
	private TypeReference result = null;
	
	public TypeASTVisitor(String unitFullName, CompilationUnit root, NameScope currentScope) {
		this.unitFullName = unitFullName;
		this.root = root;
		this.currentScope = currentScope;
	}
	
	public void reset(String unitFullName, CompilationUnit root, NameScope currentScope) {
		this.currentScope = currentScope;
		this.unitFullName = unitFullName;
		this.root = root;

		name = null;
		dimension = 0;
		result = null;
	}
	
	public TypeReference getResult() {
		if (result != null) return result;
		
		result = new TypeReference(name, resultLocation, currentScope);
		result.setDimension(dimension);
		return result;
	}

	/**
	 * ArrayType Type [][]
	 * Use this visitor to find the name of the element type recursively!
	 */
	public boolean visit(ArrayType node) {
		dimension = node.getDimensions();
		node.getElementType().accept(this);
		return false;
	}

	/**
	 * ParameterizedType: Type < Type { , Type } >
	 * We only add the reference for the first type, the type arguments are ignored
	 * So we use this visitor to find the name of the first type recursively!
	 */
	public boolean visit(ParameterizedType node) {
		node.getType().accept(this);

		ParameterizedTypeReference resultTypeRef = new ParameterizedTypeReference(name, resultLocation, currentScope);
		
		List<TypeReference> parameterList = new ArrayList<TypeReference>();
		@SuppressWarnings("unchecked")
		List<Type> typeList = node.typeArguments();
		for (Type type : typeList) {
			reset(unitFullName, root, currentScope);
			type.accept(this);
			if (name != null) {
				// Possibly, we can not get the name of a type reference (when the type reference is a Wildcard type ?)
				// Therefore, we do not add such wildcard type to the list!
				TypeReference lastResult = getResult();
				parameterList.add(lastResult);
			}
		}
		resultTypeRef.setParameterTypeList(parameterList);
		result = resultTypeRef;
		return false;
	}
	
	/**
	 * Use the type visitor to visit the node
	 */
	public boolean visit(PrimitiveType node) {
		name = node.getPrimitiveTypeCode().toString();
		resultLocation = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		return false;
	}

	/**
	 * QualifiedType: Type . SimpleName
	 * We use this visitor to composite the name of the type
	 */
	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		TypeReference lastResult = getResult();
		
		// Composite the name of the qualified type
		name = name + NameReferenceLabel.NAME_QUALIFIER + node.getName().getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		QualifiedTypeReference qualifiedTypeRef = new QualifiedTypeReference(name, resultLocation, currentScope);

		SimpleName simpleName = node.getName();
		resultLocation = SourceCodeLocation.getStartLocation(simpleName, root, unitFullName);
		TypeReference simpleTypeRef = new TypeReference(simpleName.getFullyQualifiedName(), resultLocation, currentScope);
		qualifiedTypeRef.setQualifier(lastResult);
		qualifiedTypeRef.setSimpleType(simpleTypeRef);
		
		// Set the result to the qualified type reference represent the entire node!
		result = qualifiedTypeRef;
		return false;
	}
	
	/**
	 * Since a simple type can be a qualified name, so we should to visit its children
	 */
	public boolean visit(SimpleType node) {
		return true;
	}

	/**
	 * Get the name for create the result type reference
	 */
	public boolean visit(SimpleName node) {
		name = node.getFullyQualifiedName();
		resultLocation = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		return false;
	}

	/**
	 * Visit the qualified name as a qualified type!
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		TypeReference lastResult = getResult();
		
		// Composite the name of the qualified type
		name = name + NameReferenceLabel.NAME_QUALIFIER + node.getName().getFullyQualifiedName();

		resultLocation = SourceCodeLocation.getStartLocation(node, root, unitFullName);
		QualifiedTypeReference qualifiedTypeRef = new QualifiedTypeReference(name, resultLocation, currentScope);

		SimpleName simpleName = node.getName();
		resultLocation = SourceCodeLocation.getStartLocation(simpleName, root, unitFullName);
		TypeReference simpleTypeRef = new TypeReference(simpleName.getFullyQualifiedName(), resultLocation, currentScope);
		qualifiedTypeRef.setQualifier(lastResult);
		qualifiedTypeRef.setSimpleType(simpleTypeRef);
		
		// Set the result to the qualified type reference represent the entire node!
		result = qualifiedTypeRef;
		
		return false;
	}
	
	/**
	 * WildcardType: ? [ ( extends | super) Type ]
	 * We only add the reference for the bound type
	 * So we use this visitor to find the name of the first type recursively!
	 */
	public boolean visit(WildcardType node) {
		Type boundType = node.getBound();
		if (boundType != null) boundType.accept(this);
		return false;
	}
	
}
