package graph.dependenceGraph.dependenceFinder;

import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameReference.NameReference;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public abstract class NameBasedDependenceFinder implements DependenceFinder {
	protected NameTableManager tableManager = null;
	protected List<NameReference> referenceList = null;
	
	public NameBasedDependenceFinder(NameTableManager tableManager) {
		this.tableManager = tableManager;
	}
	
	public void setReferenceList(List<NameReference> referenceList) {
		this.referenceList = referenceList;
	}
}
