package graph.dependenceGraph;

import graph.basic.GraphNode;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * The edge of class dependence graph 
 * @author Zhou Xiaocong
 * @since 2014/1/3
 * @version 1.0
 */
public class ClassDependenceNode implements GraphNode {
	private DetailedTypeDefinition classDefinition = null;
	private String id = null;
	private String label = null;
	
	public ClassDependenceNode(DetailedTypeDefinition classDefinition) {
		this.classDefinition = classDefinition;
		String locationString = classDefinition.getLocation().toString();
		this.id = classDefinition.getSimpleName() + "@" + locationString; 
		locationString = classDefinition.getLocation().getUniqueId();
		this.label = classDefinition.getSimpleName() + "@" + locationString;; 
	}
	
	public ClassDependenceNode(String id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public DetailedTypeDefinition getDefinition() {
		return classDefinition;
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
		if (!(other instanceof ClassDependenceNode)) return false;
		ClassDependenceNode otherNode = (ClassDependenceNode)other;

		if (classDefinition != null && otherNode.classDefinition != null) {
			if (classDefinition == otherNode.classDefinition) return true;
			else return false;
		}
		if (id.equals(otherNode.id)) return true;
		else return false;
	}
	
	@Override
	public int hashCode() {
		if (classDefinition != null) return classDefinition.hashCode(); 
		else return id.hashCode();
	}
	
	@Override
	public String toString() {
		String locationString = classDefinition.getLocation().toString();
		String result = classDefinition.getSimpleName() + "@" + locationString; 
		return result;
	}

	@Override
	public String toFullString() {
		return classDefinition.toFullString();
	}

}
