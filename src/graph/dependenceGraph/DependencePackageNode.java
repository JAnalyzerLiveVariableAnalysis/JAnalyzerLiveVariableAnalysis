package graph.dependenceGraph;

import java.util.List;

import nameTable.filter.NameScopeKindFilter;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.visitor.NameScopeVisitor;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependencePackageNode extends DependenceGraphNode {

	public DependencePackageNode(PackageDefinition entity) {
		super(entity);
	}

	@Override
	public String getId() {
		PackageDefinition packageDef = (PackageDefinition)entity;
		return packageDef.getUniqueId();
	}

	@Override
	public String getLabel() {
		PackageDefinition packageDef = (PackageDefinition)entity;
		return packageDef.getUniqueId();
	}

	@Override
	public String getDescription() {
		PackageDefinition packageDef = (PackageDefinition)entity;
		return "Package: " + packageDef.getUniqueId();
	}

	@Override
	public String toFullString() {
		PackageDefinition packageDef = (PackageDefinition)entity;
		return packageDef.toFullString();
	}
	
	@Override
	public List<NameScope> getBasicScopeList() {
		// Extend the package to the list of types
		NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(NameScopeKind.NSK_DETAILED_TYPE));
		entity.accept(visitor);
		List<NameScope> result = visitor.getResult();
		return result;
	}
}
