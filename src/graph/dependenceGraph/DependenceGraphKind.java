package graph.dependenceGraph;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public enum DependenceGraphKind {
	DGK_INVOCATION, 		// based on method invocation
	DGK_COMPOSITION, 		// based on member relation
	DGK_STRONG, 			// based on method invocation or member relation
	DGK_ALL,				// based on all using relation
	DGK_INHERITANCE,		// based on extends and implements declaration 
}
