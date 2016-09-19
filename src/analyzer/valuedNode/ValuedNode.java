package analyzer.valuedNode;

import graph.basic.GraphNode;
import nameTable.nameDefinition.NameDefinition;

/**
 * A valued node is a graph node with a double value, which may be the authority value, hub value, or any other value (e.g. community id) attached to the 
 * node 
 * @author Zhou Xiaocong
 * @since 2014/1/18
 * @version 1.0
 */
public class ValuedNode implements GraphNode {
	protected String id = null;
	protected String label = null;
	protected String description = null;
	protected NameDefinition definition = null;
	
	protected boolean usableValue = false;
	protected double value = 0; 
	protected double rank = 0;
	
	public ValuedNode(String id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public void setValue(double value) {
		usableValue = true;
		this.value = value;
	}
	
	public void setUsableValue(boolean flag) {
		usableValue = flag;
	}
	
	public boolean hasUsableValue() {
		return usableValue;
	}
	
	public double getValue() {
		return value;
	}
	
	public double getRank() {
		return rank;
	}
	
	public void setRank(double rank) {
		this.rank = rank;
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

	public String toString() {
		return label;
	}
	
	@Override
	public String toFullString() {
		String result = label + "[value = " + value + ", id = " + id;
		if (description != null) result = result + ", description = " + description + "]";
		else result = result + "]";
		return result;
	}
	
	public void setDefinition(NameDefinition definition) {
		this.definition = definition; 
	}
	
	/**
	 * Return the binded name definition
	 */
	public NameDefinition getDefinition() {
		return definition;
	}
	
	
	/**
	 * Test if the node has been bind to the same name definition as another valued node 
	 */
	public boolean hasBindToSameDefinition(ValuedNode another) {
		if (definition == null) return false;
		return definition == another.definition;
	}
	
	/**
	 * Create a valued node instance according to the given kind
	 */
	protected static ValuedNode createValuedNodeInstance(String id, String label, ValuedNodeKind kind) {
		if (kind == ValuedNodeKind.VNK_CLASS) return new ValuedClassNode(id, label);
		else throw new AssertionError("So far, we can not create instance for valued node kind: " + kind);
	}
}
