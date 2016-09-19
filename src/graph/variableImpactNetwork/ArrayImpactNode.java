package graph.variableImpactNetwork;

import nameTable.nameReference.referenceGroup.NRGArrayCreation;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/30
 * @version 1.0
 */
public class ArrayImpactNode extends VariableImpactNode {
	private NRGArrayCreation creation = null;

	public ArrayImpactNode(String id, String label) {
		super(id, label);
	}

	public ArrayImpactNode(String id, String label, NRGArrayCreation reference) {
		super(id, label);
		this.creation = reference;
	}

	@Override
	public VariableImpactNodeKind getNodeKind() {
		return VariableImpactNodeKind.VINK_ARRAY;
	}

	public void setCreationExpression(NRGArrayCreation expression) {
		this.creation = expression;
	}

	public NRGArrayCreation getCreationExpression() {
		return creation;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof ArrayImpactNode)) return false;
		
		ArrayImpactNode otherNode = (ArrayImpactNode)other;
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
	
