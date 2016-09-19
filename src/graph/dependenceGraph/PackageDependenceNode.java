package graph.dependenceGraph;

import graph.basic.GraphNode;
import nameTable.nameDefinition.PackageDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/29
 * @version 1.0
 */
public class PackageDependenceNode implements GraphNode {

	private PackageDefinition packageDefinition = null;
	private String id = null;
	private String label = null;
	
	public PackageDependenceNode(PackageDefinition packageDefinition) {
		this.packageDefinition = packageDefinition;
		this.id = packageDefinition.getSimpleName(); 
		this.label = packageDefinition.getFullQualifiedName(); 
	}
	
	public PackageDependenceNode(String id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public PackageDefinition getDefinition() {
		return packageDefinition;
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
		return label;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof PackageDependenceNode)) return false;
		PackageDependenceNode otherNode = (PackageDependenceNode)other;

		if (packageDefinition != null && otherNode.packageDefinition != null) {
			if (packageDefinition == otherNode.packageDefinition) return true;
			else return false;
		}
		if (id.equals(otherNode.id)) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {
		if (packageDefinition != null) return packageDefinition.hashCode(); 
		else return id.hashCode();
	}
	
	@Override
	public String toString() {
		String result = packageDefinition.getSimpleName(); 
		return result;
	}

	@Override
	public String toFullString() {
		return packageDefinition.toFullString();
	}


}
