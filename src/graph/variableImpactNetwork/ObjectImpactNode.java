package graph.variableImpactNetwork;

import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public class ObjectImpactNode extends VariableImpactNode {
	private NRGClassInstanceCreation creation = null;
	
	public ObjectImpactNode(String id, String label) {
		super(id, label);
	}

	public ObjectImpactNode(String id, String label, NRGClassInstanceCreation reference) {
		super(id, label);
		this.creation = reference;
	}

	public void setCreationExpression(NRGClassInstanceCreation expression) {
		this.creation = expression;
	}

	public NRGClassInstanceCreation getCreationExpression() {
		return creation;
	}

	@Override
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_OBJECT;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof ObjectImpactNode)) return false;
		
		ObjectImpactNode otherNode = (ObjectImpactNode)other;
		if (this.creation == null && otherNode.creation == null) {
			return this.label.equals(otherNode.label);
		}
		if (this.creation == null || otherNode.creation == null) return false;
		if (this.creation == otherNode.creation) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {
		if (creation != null) return creation.hashCode();
		else return label.hashCode();
	}
}
