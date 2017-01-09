package graph.dependenceGraph;

import java.util.ArrayList;
import java.util.List;

import graph.basic.GraphNode;
import nameTable.nameScope.NameScope;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceGraphNode implements GraphNode, Comparable<DependenceGraphNode> {
	NameScope entity = null;

	public DependenceGraphNode(NameScope entity) {
		this.entity = entity;
	}

	@Override
	public String getId() {
		return entity.getScopeName();
	}

	@Override
	public String getLabel() {
		return entity.getScopeName();
	}

	@Override
	public String getDescription() {
		return entity.getScopeName();
	}

	@Override
	public String toFullString() {
		return entity.toString();
	}
	
	public NameScope getEntity() {
		return entity;
	}
	
	public List<NameScope> getBasicScopeList() {
		List<NameScope> result = new ArrayList<NameScope>();
		result.add(entity);
		return result;
	}

	@Override
	public int compareTo(DependenceGraphNode other) {
		if (entity == other.entity) return 0;
		else return getId().compareTo(other.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DependenceGraphNode)) return false;
		DependenceGraphNode other = (DependenceGraphNode)obj;
		return (entity == other.entity);
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
