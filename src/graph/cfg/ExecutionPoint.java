package graph.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

import util.SourceCodeLocation;

/**
 * The class of the execution point in the source code. 
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.02
 * @update 2013/3/29 Zhou Xiaocong
 * 		Add methods to maintain the end position of the execution point
 * @update 2013/06/12 Zhou Xiaocong
 * 		Add the implementations of the methods isVirtual(), isNormalEnd(), isAbnormalEnd(), isStart(), isPredicate() declared in the interface CFGNode
 * 		Modify the method equals() and hashCode() to include label as a critical field to identify an execution point 
 *
 */
public class ExecutionPoint implements CFGNode {
	private String id = null;
	private String label = null;
	private String description = null;
	private ExecutionPointType type = ExecutionPointType.NORMAL;
	private ASTNode astNode = null;					// The AST node corresponding to the execution point
	
	private SourceCodeLocation startPosition = null;	// the start position in the compilation unit of the execution point
	private SourceCodeLocation endPosition = null;		// the end position in the compilatin unit of the execution point

	public ExecutionPoint() {
		
	}
	
	public ExecutionPoint(String id, String label, String description, ExecutionPointType type, ASTNode astNode) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.type = type;
		this.astNode = astNode;
	}

	@Override
	public CFGNodeType getCFGNodeType() {
		return CFGNodeType.N_EXECUTION_POINT;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public ExecutionPointType getType() {
		return type;
	}

	public ASTNode getAstNode() {
		return astNode;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(ExecutionPointType type) {
		this.type = type;
	}

	public void setAstNode(ASTNode astNode) {
		this.astNode = astNode;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof ExecutionPoint)) return false;
		ExecutionPoint otherEp = (ExecutionPoint)other;
		if (id.equals(otherEp.id) && label.equals(otherEp.label)) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() * 13 + label.hashCode();
	}
	
	@Override 
	public String toString() {
		return id + "[" + label + "]"; 
	}
	
	public String toFullString() {
		return "Execution Point: " + id + "[" + label + "]" + "\n\tDescription: [" + description + "]"; 
	}

	public void setStartPosition(SourceCodeLocation startPosition) {
		this.startPosition = startPosition;
	}

	public SourceCodeLocation getStartPosition() {
		return startPosition;
	}
	
	public void setEndPosition(SourceCodeLocation endPosition) {
		this.endPosition = endPosition;
	}

	public SourceCodeLocation getEndPosition() {
		return endPosition;
	}
	
	/**
	 * If the execution point represents to a virtual start or end node, return true
	 */
	public boolean isVirtual() {
		return type.isVirtual();
	}
	
	/**
	 * If the execution point represents a predicate in a branch or loop statement
	 */
	public boolean isPredicate() {
		return type.isPredicate();
	}
	
	/**
	 * Test if the node is the start node of the entire CFG
	 */
	public boolean isStart() {
		return type == ExecutionPointType.CFG_START;
	}
	
	/**
	 * Test if the node is the end node of the entire CFG
	 */
	public boolean isNormalEnd() {
		return type == ExecutionPointType.CFG_END && label.equals(ExecutionPointLabel.END);
	}
	
	/**
	 * Test if the node is the abnormal end node of the entire CFG
	 */
	public boolean isAbnormalEnd() {
		return type == ExecutionPointType.CFG_END && label.equals(ExecutionPointLabel.ABNORMAL_END);
	}
	
}
