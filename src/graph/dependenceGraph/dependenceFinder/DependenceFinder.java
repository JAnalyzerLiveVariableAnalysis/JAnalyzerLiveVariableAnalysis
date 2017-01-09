package graph.dependenceGraph.dependenceFinder;

import nameTable.nameScope.NameScope;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public interface DependenceFinder {

	boolean hasRelation(NameScope start, NameScope end);
}
