package graph.variableImpactNetwork;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public enum VariableImpactNodeKind {
	VINK_VARIABLE,		// Local variables or method parameters 
	VINK_FIELD, 		// Static or non static fields in the class declaration
	VINK_OBJECT, 		// Object created by class instance creation statement
	VINK_ARRAY, 		// Array created by array creation statement
	VINK_RETURN, 		// Return value of a method
}
