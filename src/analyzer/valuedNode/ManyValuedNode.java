package analyzer.valuedNode;

import nameTable.nameDefinition.NameDefinition;

/**
 * A node (possible a class, a method) with many values
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ18ÈÕ
 * @version 1.0
 */
public class ManyValuedNode {
	
	protected String id = null;
	protected String label = null;
	protected NameDefinition definition = null;
	protected int length = 0;
	
	protected boolean[] usableValues = null;
	protected double[] values = null; 
	
	public ManyValuedNode(String id, String label, int valueLength) {
		this.id = id;
		this.label = label;
		this.length = valueLength;
		if (this.length <= 0) this.length = 1;
		usableValues = new boolean[this.length];
		values = new double[this.length];
	}
	
	public void setValue(int index, double value) {
		usableValues[index] = true;
		values[index] = value;
	}
	
	public void setUsableValue(int index, boolean flag) {
		usableValues[index] = flag;
	}
	
	public boolean hasUsableValue(int index) {
		return usableValues[index];
	}
	
	public double getValue(int index) {
		return values[index];
	}
	
	public int valueLength() {
		return length;
	}
	
	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String toString() {
		return label;
	}
	
	public String toFullString() {
		String result = label + "[value = " + values[0];
		for (int i = 1; i < length; i++) result = result + ", " + values[i];
		result = result + "], id = " + id;		
		return result;
	}
	
	public void setDefinition(NameDefinition definition) {
		this.definition = definition; 
	}
	
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
	protected static ManyValuedNode createManyValuedNodeInstance(String id, String label, ValuedNodeKind kind, int valueLength) {
		if (kind == ValuedNodeKind.VNK_CLASS) return new ManyValuedClassNode(id, label, valueLength);
		else throw new AssertionError("So far, we can not create instance for valued node kind: " + kind);
	}
}
