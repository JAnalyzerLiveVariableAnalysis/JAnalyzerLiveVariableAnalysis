package graph.callGraph;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;

public class CallGraphEdge implements GraphEdge {
	public final static String LABEL_TRUE = "true";
	public final static String LABEL_FALSE = "false";
	
	private CallGraphNode startNode;
	private CallGraphNode endNode;
	private String label;
	private String description;
	
	public CallGraphEdge(CallGraphNode startNode, CallGraphNode endNode){
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	public CallGraphEdge(CallGraphNode startNode, CallGraphNode endNode, String label){
		this.startNode = startNode;
		this.endNode = endNode;
		this.label = label;
	}
	
	@Override
	public boolean isDirected() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public GraphNode getStartNode() {
		// TODO Auto-generated method stub
		return this.startNode;
	}

	@Override
	public GraphNode getEndNode() {
		// TODO Auto-generated method stub
		return this.endNode;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return this.label;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return this.description;
	}

	@Override
	public String toFullString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) 
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}

		if (!(obj instanceof CallGraphEdge)) 
		{
			return false;
		}
		CallGraphEdge other = (CallGraphEdge) obj;
		if (this.getStartNode().equals(other.getStartNode())
				&& this.getEndNode().equals(other.getEndNode())) 
		{
			return true;
		} 
		return false;
	}
}
