package analyzer.valuedNode;

/**
 * Represent the kinds of a value nodes
 * @author Zhou Xiaocong
 * @since 2014/1/20
 * @version 1.0
 */
public enum ValuedNodeKind {
	VNK_CLASS, 		// A node corresponding to a class in a class dependence graph!
	VNK_METHOD, 	// A node corresponding to a method in a call graph!
	VNK_VARIABLE, 	// A node corresponding to a variable node in a variable impact network!
}
