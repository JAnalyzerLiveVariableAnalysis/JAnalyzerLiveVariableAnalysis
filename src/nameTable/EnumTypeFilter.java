package nameTable;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
public class EnumTypeFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	
	public EnumTypeFilter() {
	}
	
	public EnumTypeFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isTypeDefinition()) return false;
		TypeDefinition type = (TypeDefinition)definition;
		if (!type.isEnumeration()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		return true;
	}
}
