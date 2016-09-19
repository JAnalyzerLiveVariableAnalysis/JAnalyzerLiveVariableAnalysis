package graph.dependenceGraph;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/29
 * @version 1.0
 */
public class PackageDependenceEdge implements GraphEdge {

	private PackageDependenceNode startNode = null;
	private PackageDependenceNode endNode = null;
	private ClassDependenceEdge dependenceWitness = null;
	
	public PackageDependenceEdge(PackageDependenceNode start, PackageDependenceNode end) {
		this.startNode = start;
		this.endNode = end;
	}

	public PackageDependenceEdge(PackageDependenceNode start, PackageDependenceNode end, ClassDependenceEdge witness) {
		this.startNode = start;
		this.endNode = end;
		this.dependenceWitness = witness;
	}

	public ClassDependenceEdge getDependenceWitness() {
		return dependenceWitness;
	}
	
	public void setDependenceWitness(ClassDependenceEdge witness) {
		dependenceWitness = witness;
	}
	
	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public GraphNode getStartNode() {
		return startNode;
	}

	@Override
	public GraphNode getEndNode() {
		return endNode;
	}

	@Override
	public String getLabel() {
		if (dependenceWitness != null) return dependenceWitness.getLabel();
		else return null;
	}

	@Override
	public String getDescription() {
		String result = "<" + startNode.toString() + ", " + endNode.toString() + ">";
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.getLabel() + ")";
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof PackageDependenceEdge)) return false;
		PackageDependenceEdge otherEdge = (PackageDependenceEdge)other;

		if (!startNode.equals(otherEdge.getStartNode())) return false;
		if (!endNode.equals(otherEdge.getEndNode())) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = startNode.hashCode() + 3 * endNode.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		String result = "<" + startNode.toString() + ", " + endNode.toString() + ">";
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.getLabel() + ")";
		return result;
	}
	
	@Override
	public String toFullString() {
		String result = "<" + startNode.toFullString() + ", " + endNode.toFullString() + ">";
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.toString() + ")";
		return result;
	}
}
