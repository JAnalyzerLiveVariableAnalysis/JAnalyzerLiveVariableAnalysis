package graph.variableImpactNetwork;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/31
 * @version 1.0
 */
public class VariableImpactEdge implements GraphEdge {
	public static final String VIE_LABEL_ASSIGN = "assign";
	public static final String VIE_LABEL_COMPONENT = "component";
	public static final String VIE_LABEL_CONTROL = "control";
	
	private VariableImpactNode startNode = null;
	private VariableImpactNode endNode = null;
	
	private String description = null;
	
	public VariableImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}

	public VariableImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode, String description) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
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
		return description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public VariableImpactEdgeKind getKind() {
		return VariableImpactEdgeKind.VIEK_ASSIGN;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof VariableImpactEdge)) return false;
		VariableImpactEdge otherEdge = (VariableImpactEdge)other;

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
		String label = getLabel();
		if (label != null) result = result + "[" + label + "]"; 
		return result;
	}

	@Override
	public String toFullString() {
		StringBuilder message = new StringBuilder("<" + startNode.getId() + ", " + endNode.getId() + ">");
		String label = getLabel();
		if (label != null) message.append("[label = " + label + "]"); 
		if (description != null) {
			if (!label.equals(description)) message.append("[description = " + description + "]");
		}
		return message.toString();
	}

	
}
