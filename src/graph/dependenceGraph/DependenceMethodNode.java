package graph.dependenceGraph;

import nameTable.nameDefinition.MethodDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceMethodNode extends DependenceGraphNode {

	public DependenceMethodNode(MethodDefinition entity) {
		super(entity);
	}

	@Override
	public String getId() {
		MethodDefinition methodDef = (MethodDefinition)entity;
		return methodDef.getUniqueId();
	}

	@Override
	public String getLabel() {
		MethodDefinition methodDef = (MethodDefinition)entity;
		return methodDef.getSimpleName();
	}

	@Override
	public String getDescription() {
		MethodDefinition methodDef = (MethodDefinition)entity;
		return "Method: " + methodDef.getFullQualifiedName();
	}

	@Override
	public String toFullString() {
		MethodDefinition methodDef = (MethodDefinition)entity;
		return methodDef.toFullString();
	}
}
