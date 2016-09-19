package graph.variableImpactNetwork;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/1
 * @version 1.0
 */
public class ComponentImpactEdge extends VariableImpactEdge {

	public ComponentImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode) {
		super(startNode, endNode);
	}

	public ComponentImpactEdge(VariableImpactNode startNode, VariableImpactNode endNode, String description) {
		super(startNode, endNode, description);
	}

	public String getLabel() {
		return VIE_LABEL_COMPONENT;
	}

	public VariableImpactEdgeKind getKind() {
		return VariableImpactEdgeKind.VIEK_COMPONENT;
	}
}
