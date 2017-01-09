package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ9ÈÕ
 * @version 1.0
 *
 */
public class NameBasedTypeInheritTypeFinder extends NameBasedDependenceFinder {

	public NameBasedTypeInheritTypeFinder(NameTableManager tableManager) {
		super(tableManager);
	}

	@Override
	public boolean hasRelation(NameScope start, NameScope end) {
		if (start.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;
		if (end.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;

		DetailedTypeDefinition subType = (DetailedTypeDefinition)start;
		DetailedTypeDefinition superType = (DetailedTypeDefinition)end;
		
		List<TypeReference> superTypeList = subType.getSuperList();
		if (superTypeList == null) return false;
		
		for (TypeReference typeReference : superTypeList) {
			if (typeReference.resolveBinding()) {
				if (typeReference.getDefinition() == superType) return true;
			}
		}
		return false;
	}

}
