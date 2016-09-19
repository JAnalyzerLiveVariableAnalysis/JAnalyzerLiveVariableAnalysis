package nameTable.nameScope;

import java.io.PrintWriter;
import java.util.List;

import util.SourceCodeLocation;
import nameTable.NameTableVisitor;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.IllegalNameDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameReference.NameReference;

/**
 * The interface for scopes
 * @author Zhou Xiaocong
 * @since 2013/2/21
 * @version 1.0
 */
public interface NameScope {
	/**
	 * @return The name of the scope
	 */
	public String getScopeName();
	
	/**
	 * @return The enclosing scope (i.e. the parent) of the scope
	 */
	public NameScope getEnclosingScope();
	
	/**
	 * @return The list of scopes contained in the scope, i.e. the children of the scope
	 */
	public List<NameScope> getSubScopeList();
	
	/**
	 * Define a name in the scope
	 */
	public void define(NameDefinition nameDef) throws IllegalNameDefinition;
	
	/**
	 * Resolve a name reference in the scope. If it is successful, call reference.bindTo() to bind the reference 
	 *   to its definition.
	 * @return If it is successful, return true, otherwise return false
	 */
	public boolean resolve(NameReference reference);
	
	public NameScopeKind getScopeKind();
	
	/**
	 * Add a reference to the current scope
	 */
	public void addReference(NameReference reference);
	
	/**
	 * Get the reference list in the current scope
	 */
	public List<NameReference> getReferences();


	/**
	 * Get a definition with the given name in the scope (and its sub-scopes, if includeSubScopes is true)
	 */
	public NameDefinition getDefinition(String name, boolean includeSubscopes);
	
	
	/**
	 * Find name definition by a unique id in the scope  (and its sub-scopes, if includeSubScopes is true)
	 */
	public NameDefinition findDefinitionById(String id, boolean includeSubscopes);
	
	/**
	 * Get all references with the given name in the scope (not include its sub-scopes)
	 */
	public List<NameReference> getReferences(String name);

	/**
	 * Get all detailed type definition defined in the name scope (and its sub-scope)
	 */
	public List<DetailedTypeDefinition> getAllDetailedTypeDefinition();
	
	/**
	 * Find all definitions in the scope (include its sub-scope) by a name post-fix, 
	 * i.e. if the name of a definition is end with the namePostFix, we return the definition
	 */
	public List<NameDefinition> findAllDefinitionsByName(String namePostFix);
	
	/**
	 * Find all references in the scope (include its sub-scope) by name
	 * i.e. if the name of a reference is equal to the give name, we return the reference
	 */
	public List<NameReference> findAllReferencesByName(String name);

	/**
	 * Find all (sub)scopes in the scope by name
	 * i.e. if the name of a sub-scope is equal to the give name, we return the sub-scope
	 */
	public List<NameScope> findAllSubScopesByName(String name);

	/**
	 * Find all definitions in the scope (include its sub-scope) by position, 
	 * i.e. if the start position of the definition is between the give start and end position, we return the definition
	 */
	public List<NameDefinition> findAllDefinitionsByPosition(SourceCodeLocation start, SourceCodeLocation end);
	
	/**
	 * Find all references in the scope (include its sub-scope) by position
	 * i.e. if the start position of the reference is between the give start and end position, we return the reference
	 */
	public List<NameReference> findAllReferencesByPosition(SourceCodeLocation start, SourceCodeLocation end);
	
	
	/**
	 * Test whether the current scope in in the give scope
	 */
	public boolean isEnclosedInScope(NameScope ancestorScope);
	
	/**
	 * Test whether the given location is in the current scope 
	 */
	public boolean containsLocation(SourceCodeLocation location);
	
	/**
	 * Display all definitions to a writer for debugging
	 */
	public void printDefinitions(PrintWriter writer, int indent);
	
	/**
	 * Display all references to a writer for debugging
	 */
	public void printReferences(PrintWriter writer, boolean includeLiteral);
	
	
	/**
	 * Calculate total number of definitions with given kind! 
	 * <p>If kind == NDK_UNKNOWN, calculate all kinds of definitions! 
	 * <p>If kind == NDK_PACKAGE, calculate all packages
	 * <p>If kind == NDK_TYPE, calculate all detailed types!
	 * <p>If kind == NDK_METHOD, calculate all methods declared in all classes!
	 * <p>If kind == NDK_FIELD, calculate all fields declared in all classes!
	 * <p>If kind == NDK_VARIABLE, calculate all (local) variables declared in all files!
	 * <p>If kind == NDK_PARAMETER, calculate all parameters declared in all methods!
	 */
	public int getTotalNumberOfDefinitions(NameDefinitionKind kind); 
	
	
	/**
	 * For implement the visitor patter for visit the name table
	 */
	public void accept(NameTableVisitor visitor);
}
