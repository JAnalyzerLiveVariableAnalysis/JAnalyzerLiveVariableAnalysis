package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameBasedTypeFieldTypeFinder extends NameBasedDependenceFinder {

	public NameBasedTypeFieldTypeFinder(NameTableManager tableManager) {
		super(tableManager);
	}

	@Override
	public boolean hasRelation(NameScope start, NameScope end) {
		if (start.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;
		if (end.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;

		DetailedTypeDefinition type = (DetailedTypeDefinition)end;
		List<FieldDefinition> fieldList = type.getFieldList();
		if (fieldList == null) return false;
		
		for (NameReference reference : referenceList) {
			List<NameReference> leafReferenceList = reference.getReferencesAtLeaf();
			for (NameReference leafReference : leafReferenceList) {
				for (FieldDefinition field : fieldList) {
					if (leafReference.getDefinition() == field) return true;
				}
			}
		}
		return false;
	}

}
