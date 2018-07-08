package nameTable.filter;

import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameDefinition.ImportedTypeDefinition;

/**
 * The base class for a filter used in visiting the name table
 * @author Zhou Xiaocong
 * @since 2015Äê6ÔÂ24ÈÕ
 * @version 1.0
 */
public class NameTableFilter {

	public boolean accept(NameDefinition definition) {
		return true;
	}
	
	/*public boolean accept(ImportedTypeDefinition type) {
		return true;
	}*/
	public boolean accept(NameReference reference) {
		return true;
	}
}
