package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameBasedTypeMemberTypeFinder extends NameBasedDependenceFinder {

	public NameBasedTypeMemberTypeFinder(NameTableManager tableManager) {
		super(tableManager);
	}

	@Override
	public boolean hasRelation(NameScope start, NameScope end) {
		if (start.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;
		if (end.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;

		DetailedTypeDefinition type = (DetailedTypeDefinition)start;
		List<FieldDefinition> fieldList = type.getFieldList();
		if (fieldList == null) return false;

		for (FieldDefinition field : fieldList) {
			List<TypeDefinition> fieldTypeList = field.getTypeDefinition(true);
			for (TypeDefinition fieldType : fieldTypeList) {
				if (fieldType == end) return true;
			}
		}
		return false;
	}

}
