package graph.dependenceGraph;

import graph.basic.GraphEdge;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;

/**
 * A class to create the package dependence graph of a system by using the class dependence graph of the system
 * @author Zhou Xiaocong
 * @since 2014/1/29
 * @version 1.0
 */
public class PackageDependenceCreator {
	ClassDependenceGraph baseCDG = null;
	
	public PackageDependenceCreator(ClassDependenceGraph cdg) {
		this.baseCDG = cdg;
	}
	
	public PackageDependenceGraph create() {
		PackageDependenceGraph result = new PackageDependenceGraph(baseCDG.getId());

		List<GraphEdge> edgeList = baseCDG.getEdges();
		for (GraphEdge edge : edgeList) {
			ClassDependenceEdge cdgEdge = (ClassDependenceEdge)edge;
			
			ClassDependenceNode startClass = (ClassDependenceNode)cdgEdge.getStartNode();
			DetailedTypeDefinition startDef = startClass.getDefinition();
			PackageDefinition startPackage = getEnclosingPackage(startDef);
			PackageDependenceNode startNode = result.findNodeByDefinition(startPackage);
			if (startNode == null) {
				startNode = new PackageDependenceNode(startPackage);
				result.addNode(startNode);
			}
			
			ClassDependenceNode endClass = (ClassDependenceNode)cdgEdge.getEndNode();
			DetailedTypeDefinition endDef = endClass.getDefinition();
			PackageDefinition endPackage = getEnclosingPackage(endDef);
			
			// We do not consider a package dependent on itself!
			if (startPackage == endPackage) continue;
			
			PackageDependenceNode endNode = result.findNodeByDefinition(endPackage);
			if (endNode == null) {
				endNode = new PackageDependenceNode(endPackage);
				result.addNode(endNode);
			}
			
			if (!result.hasEdge(startNode, endNode)) {
				PackageDependenceEdge resultEdge = new PackageDependenceEdge(startNode, endNode, cdgEdge);
				result.addEdge(resultEdge);
			}
			
		}
		
		return result;
	}
	
	private PackageDefinition getEnclosingPackage(DetailedTypeDefinition type) {
		NameScope scope = type.getEnclosingScope();
		boolean foundPackage = false;
		while (scope != null) {
			if (scope.getScopeKind() == NameScopeKind.NSK_PACKAGE) {
				foundPackage = true;
				break;
			}
			scope = scope.getEnclosingScope();
		}
		if (foundPackage == true) {
			return (PackageDefinition)scope;
		} else {
			throw new AssertionError("Can not find the package of the detailed type definition " + type.getFullQualifiedName());
		}
	}

}
