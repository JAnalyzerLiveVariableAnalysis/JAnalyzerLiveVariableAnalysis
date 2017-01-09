package graph.dependenceGraph;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import graph.dependenceGraph.dependenceFinder.DependenceFinderFactory;
import graph.dependenceGraph.dependenceFinder.NameBasedDependenceFinder;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.filter.NameScopeKindFilter;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.visitor.NameScopeVisitor;

/**
 * @author Zhou Xiaocong
 * @since 2017Äê1ÔÂ8ÈÕ
 * @version 1.0
 *
 */
public class NameBasedDependenceGraphCreator {
	private NameTableManager tableManager = null;
	// Determine if the created graph include those node with no edge connected them.
	private boolean includeZeroDegreeNode = true;
	private PrintStream progressWriter = null;
	
	public NameBasedDependenceGraphCreator(NameTableManager tableManager) {
		this.tableManager = tableManager;
	}

	/**
	 * Set if the created graph include those node with no edge connected them  
	 */
	public void setIncludeZeroDegreeNode(boolean flag) {
		this.includeZeroDegreeNode = flag;
	}
	
	/**
	 * Set progress writer to output the progression of creating the dependence graph  
	 */
	public void setProgressWriter(PrintStream writer) {
		this.progressWriter = writer;
	}
	
	
	/**
	 * Create the given kind dependence graph with edges between the entities in the entityList  
	 */
	public DependenceGraph create(String id, DependenceGraphKind kind, List<NameScope> entityList) {
		return create(id, kind, entityList, entityList);
	}
	
	
	/**
	 * Create the given kind dependence graph with edges from the entities in the startEntityList. The end nodes of the graph
	 * will be extracted by this method automatically according to the given name scope kind. 
	 * <p>Note: The given endEntityKind can be the same as the kind of entities in startEntityList, and then you may get edges 
	 * between entities in the startEntityList. However, there is no edge from those automatically extracted entities to the 
	 * entities in the startEntityList.  
	 * <p>The created graph will excludes those nodes with no edges connected them, even if the node is in the startEntityList
	 */
	public DependenceGraph create(String id, DependenceGraphKind kind, List<NameScope> startEntityList, NameScopeKind endEntityKind) {
		List<NameScope> endEntityList = null;
		if (endEntityKind == NameScopeKind.NSK_COMPILATION_UNIT || endEntityKind == NameScopeKind.NSK_DETAILED_TYPE ||
				endEntityKind == NameScopeKind.NSK_METHOD || endEntityKind == NameScopeKind.NSK_PACKAGE) {
			NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(endEntityKind));
			tableManager.accept(visitor);
			endEntityList = visitor.getResult();
		} else endEntityList = new ArrayList<NameScope>(); // Other kind scope can not be node of a dependence graph!
		
		boolean oldFlag = includeZeroDegreeNode;
		// We have to excludes those nodes with no edges connected them, since end entities are extracted automatically 
		includeZeroDegreeNode = false; 		 
		DependenceGraph result = create(id, kind, startEntityList, endEntityList);
		includeZeroDegreeNode = oldFlag;

		return result;
	}
	
	/**
	 * Create the given kind dependence graph with edges from the entities in the startEntityList. The end nodes of the graph
	 * will be extracted by this method automatically according to the given name scope kind. 
	 * <p>Note: The given endEntityKind can be the same as the kind of entities in startEntityList, and then you may get edges 
	 * between entities in the startEntityList. However, there is no edge from those automatically extracted entities to the 
	 * entities in the startEntityList.  
	 * <p>The created graph will excludes those nodes with no edges connected them, even if the node is in the endEntityList
	 */
	public DependenceGraph create(String id, DependenceGraphKind kind, NameScopeKind startEntityKind, List<NameScope> endEntityList) {
		List<NameScope> startEntityList = null;
		if (startEntityKind == NameScopeKind.NSK_COMPILATION_UNIT || startEntityKind == NameScopeKind.NSK_DETAILED_TYPE ||
				startEntityKind == NameScopeKind.NSK_METHOD || startEntityKind == NameScopeKind.NSK_PACKAGE) {
			NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(startEntityKind));
			tableManager.accept(visitor);
			startEntityList = visitor.getResult();
		} else startEntityList = new ArrayList<NameScope>(); // Other kind scope can not be node of a dependence graph!
		
		boolean oldFlag = includeZeroDegreeNode;
		// We have to excludes those nodes with no edges connected them, since start entities are extracted automatically 
		includeZeroDegreeNode = false;
		DependenceGraph result = create(id, kind, startEntityList, endEntityList);
		includeZeroDegreeNode = oldFlag;

		return result;
	}

	/**
	 * Create the given kind dependence graph with edges from the entities in the startEntityList to the entities 
	 * in the endEntityList 
	 * 
	 * <p> Note: You can make some entities both in startEntityList and endEntityList, and then you will get edges between those 
	 * entities.  
	 */
	public DependenceGraph create(String id, DependenceGraphKind kind, List<NameScope> startEntityList, List<NameScope> endEntityList) {
		DependenceGraph result = new DependenceGraph(id);
		TreeSet<GraphNode> nodeSet = new TreeSet<GraphNode>();
		
		NameReferenceCreator referenceCreator = new NameReferenceCreator(tableManager);
		List<NameBasedDependenceFinder> finderList = DependenceFinderFactory.createNameBasedDependenceFinderList(tableManager, kind);
		
		ArrayList<GraphEdge> edgeList = new ArrayList<GraphEdge>();
		for (NameScope startEntity : startEntityList) {
			if (progressWriter != null) progressWriter.println("Scan start node " + startEntity.getScopeName());

			DependenceGraphNode startNode = null;
			if (includeZeroDegreeNode) {
				startNode = createDependenceGraphNode(startEntity);
				nodeSet.add(startNode);
			}

			List<NameScope> startBasicScopeList = getBasicScopeList(startEntity);
			for (NameScope endEntity : endEntityList) {
				if (startEntity == endEntity) continue;		// Do not consider that the node depends on itself
				if (progressWriter != null) progressWriter.println("\tScan end node " + endEntity.getScopeName());

				DependenceGraphNode endNode = null;
				if (includeZeroDegreeNode) {
					endNode = createDependenceGraphNode(endEntity);
					nodeSet.add(endNode);
				}

				List<NameScope> endBasicScopeList = getBasicScopeList(endEntity);
				boolean hasRelation = false;
				for (NameScope startBasicScope : startBasicScopeList) {
					List<NameReference> referenceList = createReferenceForBasicScope(referenceCreator, startBasicScope);
					for (NameScope endBasicScope : endBasicScopeList) {
						for (NameBasedDependenceFinder finder : finderList) {
							finder.setReferenceList(referenceList);
							hasRelation = finder.hasRelation(startBasicScope, endBasicScope);
							if (hasRelation) break;
						}
						// If we have found that there is a dependence relation between start node and end node, we do not 
						// need to check the left basic scope in the end node
						if (hasRelation) break;		
					}
					// If we have found that there is a dependence relation between start node and end node, we do not 
					// need to check the left basic scope in the start node
					if (hasRelation) break;		
				}
				
				if (hasRelation) {
					if (!includeZeroDegreeNode) {
						startNode = createDependenceGraphNode(startEntity);
						nodeSet.add(startNode);
						endNode = createDependenceGraphNode(endEntity);
						nodeSet.add(endNode);
					}
					// Add an edge to the creating dependence graph
					DependenceGraphEdge edge = new DependenceGraphEdge(startNode, endNode);
					edgeList.add(edge);
				}
			}
		}
		
		ArrayList<GraphNode> nodeList = new ArrayList<GraphNode>();
		for (GraphNode node : nodeSet) nodeList.add(node);
		result.setAllNodes(nodeList);
		result.setAllEdges(edgeList);
		return result;
	}

	private List<NameScope> getBasicScopeList(NameScope scope) {
		NameScopeKind scopeKind = scope.getScopeKind();
		if (scopeKind == NameScopeKind.NSK_COMPILATION_UNIT || scopeKind == NameScopeKind.NSK_PACKAGE) {
			NameScopeVisitor visitor = new NameScopeVisitor(new NameScopeKindFilter(NameScopeKind.NSK_DETAILED_TYPE));
			scope.accept(visitor);
			List<NameScope> result = visitor.getResult();
			return result;
		} else if (scopeKind == NameScopeKind.NSK_DETAILED_TYPE || scopeKind == NameScopeKind.NSK_METHOD) {
			List<NameScope> result = new ArrayList<NameScope>();
			result.add(scope);
			return result;
		} else return new ArrayList<NameScope>();
	}
	
	private DependenceGraphNode createDependenceGraphNode(NameScope entity) {
		NameScopeKind kind = entity.getScopeKind();
		
		switch (kind) {
		case NSK_PACKAGE : return new DependencePackageNode((PackageDefinition)entity);
		case NSK_COMPILATION_UNIT : return new DependenceCompilationUnitNode((CompilationUnitScope)entity);
		case NSK_DETAILED_TYPE : return new DependenceTypeNode((DetailedTypeDefinition)entity);
		case NSK_METHOD : return new DependenceMethodNode((MethodDefinition)entity);
		default : throw new AssertionError("Unsupportted kind of dependence graph node entity : " + kind);
		}
	}
	
	private List<NameReference> createReferenceForBasicScope(NameReferenceCreator referenceCreator, NameScope scope) {
		List<NameReference> result = new ArrayList<NameReference>();
		if (scope.getScopeKind() == NameScopeKind.NSK_DETAILED_TYPE) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)scope;
			result = referenceCreator.createReferencesForTypeBody(type);
		} else if (scope.getScopeKind() == NameScopeKind.NSK_METHOD) {
			MethodDefinition method = (MethodDefinition)scope;
			result = referenceCreator.createReferencesForMethodBody(method);
		} 
		
		for (NameReference reference : result) reference.resolveBinding();

		return result;
	}
}
