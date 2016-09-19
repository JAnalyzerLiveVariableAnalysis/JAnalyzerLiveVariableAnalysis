package nameTable;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
public class FieldDefinitionFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	
	public FieldDefinitionFilter() {
	}
	
	public FieldDefinitionFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isFieldDefinition()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		else return true;
	}
}
