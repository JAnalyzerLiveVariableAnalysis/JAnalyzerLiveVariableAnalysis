package graph.variableImpactNetwork;

import nameTable.nameDefinition.FieldDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public class FieldImpactNode extends VariableImpactNode {
	private FieldDefinition definition = null;

	public FieldImpactNode(String id, String label) {
		super(id, label);
	}
	
	public FieldImpactNode(String id, String label, FieldDefinition definition) {
		super(id, label);
		this.definition = definition;
	}

	public void setDefinition(FieldDefinition definition) {
		this.definition = definition;
	}
	
	public FieldDefinition getDefinition() {
		return definition;
	}

	@Override
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_FIELD;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		
		if (!(other instanceof FieldImpactNode)) return false;
		FieldImpactNode otherNode = (FieldImpactNode)other;
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
