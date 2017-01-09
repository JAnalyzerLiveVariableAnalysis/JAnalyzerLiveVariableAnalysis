package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameBasedMethodCallMethodFinder extends NameBasedDependenceFinder {

	public NameBasedMethodCallMethodFinder(NameTableManager tableManager) {
		super(tableManager);
	}

	@Override
	public boolean hasRelation(NameScope start, NameScope end) {
		if (start.getScopeKind() != NameScopeKind.NSK_METHOD) return false;
		if (end.getScopeKind() != NameScopeKind.NSK_METHOD) return false;

		MethodDefinition method = (MethodDefinition)end;
		
		for (NameReference reference : referenceList) {
			List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
			for (NameReference leafReference : leafReferenceList) {
				if (leafReference.getDefinition() == method) return true;
			}
		}
		return false;
	}
}
