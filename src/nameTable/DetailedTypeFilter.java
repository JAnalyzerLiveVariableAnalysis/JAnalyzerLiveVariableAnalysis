package nameTable;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
public class DetailedTypeFilter extends NameTableFilter {
	NameTableFilter wrappedFilter = null;

	public DetailedTypeFilter() {
	}
	
	public DetailedTypeFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isDetailedType()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		else return true;
	}
}
