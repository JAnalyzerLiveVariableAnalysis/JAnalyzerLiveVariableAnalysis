package graph.dependenceGraph.dependenceFinder;

import java.util.ArrayList;
import java.util.List;

import graph.dependenceGraph.DependenceGraphKind;
import nameTable.NameTableManager;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class DependenceFinderFactory {

	public static List<NameBasedDependenceFinder> createNameBasedDependenceFinderList(NameTableManager tableManager, DependenceGraphKind kind) {
		List<NameBasedDependenceFinder> result = new ArrayList<NameBasedDependenceFinder>();
		if (kind == DependenceGraphKind.DGK_INVOCATION) {
			result.add(new NameBasedMethodCallMethodFinder(tableManager));
			result.add(new NameBasedMethodCallTypeFinder(tableManager));
			result.add(new NameBasedTypeCallMethodFinder(tableManager));
			result.add(new NameBasedTypeCallTypeFinder(tableManager));
		} else if (kind == DependenceGraphKind.DGK_COMPOSITION) {
			result.add(new NameBasedTypeMemberTypeFinder(tableManager));
		} else if (kind == DependenceGraphKind.DGK_STRONG) {
			result.add(new NameBasedTypeMemberTypeFinder(tableManager));
			result.add(new NameBasedMethodCallMethodFinder(tableManager));
			result.add(new NameBasedMethodCallTypeFinder(tableManager));
			result.add(new NameBasedTypeCallMethodFinder(tableManager));
			result.add(new NameBasedTypeCallTypeFinder(tableManager));
		} else if (kind == DependenceGraphKind.DGK_ALL) {
			result.add(new NameBasedTypeMemberTypeFinder(tableManager));
			result.add(new NameBasedMethodSignatureTypeFinder(tableManager));
			result.add(new NameBasedMethodCallMethodFinder(tableManager));
			result.add(new NameBasedMethodCallTypeFinder(tableManager));
			result.add(new NameBasedTypeCallMethodFinder(tableManager));
			result.add(new NameBasedTypeCallTypeFinder(tableManager));
			result.add(new NameBasedTypeFieldTypeFinder(tableManager));
			result.add(new NameBasedMethodBodyTypeFinder(tableManager));
			result.add(new NameBasedTypeOtherTypeFinder(tableManager));
		}
		return result;
	}
	
}
