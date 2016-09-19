package graph.variableImpactNetwork;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2014Äê1ÔÂ30ÈÕ
 * @version 1.0
 */
public class ReturnValueImpactNode extends VariableImpactNode {
	private MethodDefinition definition = null;

	public ReturnValueImpactNode(String id, String label) {
		super(id, label);
	}

	public ReturnValueImpactNode(String id, String label, MethodDefinition definition) {
		super(id, label);
		this.definition = definition;
	}

	public void setDefinition(MethodDefinition definition) {
		this.definition = definition;
	}
	
	public MethodDefinition getDefinition() {
		return definition;
	}

	@Override
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_RETURN;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		
		if (!(other instanceof ReturnValueImpactNode)) return false;
		ReturnValueImpactNode otherNode = (ReturnValueImpactNode)other;
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
