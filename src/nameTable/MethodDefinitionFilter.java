package nameTable;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê4ÔÂ1ÈÕ
 * @version 1.0
 */
public class MethodDefinitionFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	
	public MethodDefinitionFilter() {
	}
	
	public MethodDefinitionFilter(NameTableFilter wrapped) {
		wrappedFilter = wrapped;
	}

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isMethodDefinition()) return false;
		if (wrappedFilter != null) return wrappedFilter.accept(definition);
		else return true;
	}
}
