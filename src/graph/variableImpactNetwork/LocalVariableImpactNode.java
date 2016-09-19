package graph.variableImpactNetwork;

import nameTable.nameDefinition.VariableDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public class LocalVariableImpactNode extends VariableImpactNode {
	private VariableDefinition definition;

	public LocalVariableImpactNode(String id, String label) {
		super(id, label);
	}

	public LocalVariableImpactNode(String id, String label, VariableDefinition definition) {
		super(id, label);
		this.definition = definition;
	}

	public VariableDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(VariableDefinition definition) {
		this.definition = definition;
	}

	@Override
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_VARIABLE;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		
		if (!(other instanceof LocalVariableImpactNode)) return false;
		LocalVariableImpactNode otherNode = (LocalVariableImpactNode)other;
		if (this.definition == null && otherNode.definition == null) {
			return this.label.equals(otherNode.label);
		}
		if (this.definition == null || otherNode.definition == null) return false;
		if (this.definition == otherNode.definition) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {
		if (definition != null) return definition.hashCode();
		else return label.hashCode();
	}
}
