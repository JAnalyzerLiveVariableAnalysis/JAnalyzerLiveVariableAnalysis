package graph.dependenceGraph;

import graph.basic.AbstractGraph;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceGraph extends AbstractGraph {
	DependenceGraphKind kind = DependenceGraphKind.DGK_ALL;

	public DependenceGraph(String id) {
		super(id);
	}

	public DependenceGraph(String id, DependenceGraphKind kind) {
		super(id);
		this.kind = kind;
	}
	
}
