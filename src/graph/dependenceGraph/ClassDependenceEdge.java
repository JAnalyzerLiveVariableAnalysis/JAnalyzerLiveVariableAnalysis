package graph.dependenceGraph;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import nameTable.nameReference.NameReference;

public class ClassDependenceEdge implements GraphEdge {
	private ClassDependenceNode startNode = null;
	private ClassDependenceNode endNode = null;
	private NameReference dependenceWitness = null;
	
	public ClassDependenceEdge(ClassDependenceNode start, ClassDependenceNode end) {
		this.startNode = start;
		this.endNode = end;
	}

	public ClassDependenceEdge(ClassDependenceNode start, ClassDependenceNode end, NameReference witness) {
		this.startNode = start;
		this.endNode = end;
		this.dependenceWitness = witness;
	}

	public NameReference getDependenceWitness() {
		return dependenceWitness;
	}
	
	public void setDependenceWitness(NameReference witness) {
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
		if (dependenceWitness != null) return dependenceWitness.getName();
		else return null;
	}

	@Override
	public String getDescription() {
		String result = "<" + startNode.toString() + ", " + endNode.toString() + ">";
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.getName() + ")";
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof ClassDependenceEdge)) return false;
		ClassDependenceEdge otherEdge = (ClassDependenceEdge)other;

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
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.getName() + ")";
		return result;
	}
	
	@Override
	public String toFullString() {
		String result = "<" + startNode.toFullString() + ", " + endNode.toFullString() + ">";
		if (dependenceWitness != null) result = result + "(" + dependenceWitness.toString() + ")";
		return result;
	}

}
