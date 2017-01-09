package graph.dependenceGraph;

import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceTypeNode extends DependenceGraphNode {

	public DependenceTypeNode(DetailedTypeDefinition entity) {
		super(entity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId() {
		DetailedTypeDefinition typeDef = (DetailedTypeDefinition)entity;
		return typeDef.getUniqueId();
	}

	@Override
	public String getLabel() {
		DetailedTypeDefinition typeDef = (DetailedTypeDefinition)entity;
		return typeDef.getSimpleName();
	}

	@Override
	public String getDescription() {
		DetailedTypeDefinition typeDef = (DetailedTypeDefinition)entity;
		return "Type: " + typeDef.getFullQualifiedName();
	}

	@Override
	public String toFullString() {
		DetailedTypeDefinition typeDef = (DetailedTypeDefinition)entity;
		return typeDef.toFullString();
	}
}
