package graph.dependenceGraph;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceGraphEdge implements GraphEdge {
	private DependenceGraphNode start = null;
	private DependenceGraphNode end = null;
	private String label = null;
	
	public DependenceGraphEdge(DependenceGraphNode start, DependenceGraphNode end) {
		this.start = start;
		this.end = end;
		label = "<" + start.getEntity().getScopeKind() + "=>" + end.getEntity().getScopeKind() + ">"; 
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public GraphNode getStartNode() {
		return start;
	}

	@Override
	public GraphNode getEndNode() {
		return end;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return label;
	}

	@Override
	public String toFullString() {
		return "<" + start.getId() + ", " + end.getId() + ">";
	}

}
