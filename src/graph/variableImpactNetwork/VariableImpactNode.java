package graph.variableImpactNetwork;

import graph.basic.GraphNode;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public class VariableImpactNode implements GraphNode {
	protected String id = null;
	protected String label = null;
	protected String description = null;

	public VariableImpactNode(String id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		if (description != null) return description;
		else return label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toFullString() {
		String message = id + "[label = " + label;
		if (description != null) message = message + ", description = " + description + "]";
		else message = message + "]"; 
		return message;
	}
	
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_VARIABLE;
	}
	
	public boolean equals(Object other) {
		if (this == other) return true;
		return false;
	}
	
}
