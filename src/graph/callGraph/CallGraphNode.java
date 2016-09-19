package graph.callGraph;

import graph.basic.GraphNode;
import nameTable.nameDefinition.MethodDefinition;

public class CallGraphNode implements GraphNode {

	private String id;
	private String label;
	private String description;
//	private S
	private MethodDefinition methodDefinition;
	
	public CallGraphNode()
	{
		
	}
	
	public CallGraphNode(MethodDefinition methodDefinition)
	{
		this.methodDefinition = methodDefinition;
		this.id = methodDefinition.getFullQualifiedName() + " [" + methodDefinition.getLocation().toString() + "]";
		this.label = methodDefinition.getSimpleName();
	}
	
	public CallGraphNode(String label, String description, MethodDefinition methodDefinition)
	{
		this.id = methodDefinition.getFullQualifiedName() + " [" + methodDefinition.getLocation().toString() + "]";
		this.label = label;
		this.description = description;
		this.methodDefinition = methodDefinition;
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return this.id;
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
	
	public MethodDefinition getMethodDefinition() 
	{
		return this.methodDefinition;
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

		if (!(obj instanceof CallGraphNode)) 
		{
			return false;
		}
		CallGraphNode other = (CallGraphNode) obj;
		if (this.methodDefinition.equals(other.methodDefinition)) 
		{
			return true;
		} 
		return false;
	}
}
