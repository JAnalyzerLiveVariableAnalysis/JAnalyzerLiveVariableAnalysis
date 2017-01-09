package graph.dependenceGraph;

import java.util.List;

import nameTable.filter.NameScopeKindFilter;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.visitor.NameScopeVisitor;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceCompilationUnitNode extends DependenceGraphNode {

	public DependenceCompilationUnitNode(CompilationUnitScope entity) {
		super(entity);
	}

	@Override
	public String getId() {
		CompilationUnitScope unitScope = (CompilationUnitScope)entity;
		return unitScope.getUnitName();
	}

	@Override
	public String getLabel() {
		CompilationUnitScope unitScope = (CompilationUnitScope)entity;
		return unitScope.getUnitName();
	}

	@Override
	public String getDescription() {
		CompilationUnitScope unitScope = (CompilationUnitScope)entity;
		return "Compilation Unit: " + unitScope.getUnitName();
	}

	@Override
	public String toFullString() {
		CompilationUnitScope unitScope = (CompilationUnitScope)entity;
		return "Compilation Unit: " + unitScope.getUnitName();
	}

	@Override
	public List<NameScope> getBasicScopeList() {
		// Extend the compilation unit to the list of types
		NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(NameScopeKind.NSK_DETAILED_TYPE));
		entity.accept(visitor);
		List<NameScope> result = visitor.getResult();
		return result;
	}
}
