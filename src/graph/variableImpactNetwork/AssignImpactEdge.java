package graph.variableImpactNetwork;

/**
 * @author Zhou Xiaocong
 * @since 2014��2��1��
 * @version 1.0
 */
public class AssignImpactEdge extends VariableImpactEdge {

	public AssignImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode) {
		super(startNode, endNode);
	}

	public AssignImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode, String description) {
		super(startNode, endNode, description);
	}
	
	public String getLabel() {
		return VIE_LABEL_ASSIGN;
	}

	public VariableImpactEdgeKind getKind() {
		return VariableImpactEdgeKind.VIEK_ASSIGN;
	}
}
