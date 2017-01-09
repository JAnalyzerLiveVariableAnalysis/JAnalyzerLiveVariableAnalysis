package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.TypeReference;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameBasedMethodSignatureTypeFinder extends NameBasedDependenceFinder {

	public NameBasedMethodSignatureTypeFinder(NameTableManager tableManager) {
		super(tableManager);
	}

	@Override
	public boolean hasRelation(NameScope start, NameScope end) {
		if (start.getScopeKind() != NameScopeKind.NSK_METHOD) return false;
		if (end.getScopeKind() != NameScopeKind.NSK_DETAILED_TYPE) return false;

		MethodDefinition method = (MethodDefinition)start;
		DetailedTypeDefinition type = (DetailedTypeDefinition)end;
		
		List<TypeDefinition> returnTypeList = method.getReturnTypeDefinition(true);
		if (returnTypeList != null) {
			for (TypeDefinition returnType : returnTypeList) {
				if (returnType == type) return true;
			}
		}
		List<VariableDefinition> parameterList = method.getParameterList();
		if (parameterList != null) {
			for (VariableDefinition parameter : parameterList) {
				List<TypeDefinition> parameterTypeList = parameter.getTypeDefinition(true);
				for (TypeDefinition parameterType : parameterTypeList) {
					if (parameterType == type) return true;
				}
			}
			
		}
		List<TypeReference> throwTypeList = method.getThrowTypeList();
		if (throwTypeList != null) {
			for (TypeReference throwTypeReference : throwTypeList) {
				throwTypeReference.resolveBinding();
				List<NameReference> leafTypeReferenceList = throwTypeReference.getReferencesAtLeaf();
				for (NameReference leafReference : leafTypeReferenceList) {
					if (leafReference.getDefinition() == type) return true;
				}
			}
		}
		return false;
	}
}
