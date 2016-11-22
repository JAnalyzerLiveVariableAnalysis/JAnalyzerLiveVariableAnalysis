package graph.callGraph;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;
import graph.cfg.creator.CFGCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.CompilationUnit;

import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import nameTable.NameTableASTBridge;
import nameTable.NameTableManager;
import nameTable.creator.NameReferenceCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.SystemScope;


/**
 * 生成方法调用图，在原来类的基础上，增加基于类 SoftwareStructManager 生成方法调用图的方法
 * 
 * @author Zhou Xiaocong
 * @update 2016年4月14日
 * @version 2.0
 */
public class CallGraphCreator 
{
	
	/**
	 * 根据源代码创建调用图
	 * @param path 源代码根路径
	 * @return 返回创建好的调用图
	 */
	public static AbstractGraph create(NameTableManager manager, SourceCodeFileSet parser, String id) {
		AbstractGraph graph = new CallGraph(id);
		
		// 创建调用图顶点和边
		List<GraphNode> nodes = creatNodes(manager.getSystemScope());
		List<GraphEdge> edges = createEdges(manager, parser);
		// 设置调用图顶点和边
		graph.setNodes(nodes);
		graph.setEdges(edges);
		//System.out.println(nodes.indexOf(edges.get(0).getStartNode()));
		return graph;
	}
	

	/**
	 * 生成一个类中实现的方法之间的方法调用图
	 */
	public static AbstractGraph create(String graphId, SoftwareStructManager manager, DetailedTypeDefinition type, boolean isPolymorphic) {
		List<MethodDefinition> implementedMethodList = manager.getImplementedMethodList(type);
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		for (MethodDefinition method : implementedMethodList) {
			nodeList.add(new CallGraphNode(method));
		}
		return create(graphId, manager, nodeList, isPolymorphic);
	}
	
	/**
	 * 生成一个类中实现的方法之间的方法调用图
	 */
	public static AbstractGraph create(String graphId, SoftwareStructManager manager, DetailedTypeDefinition type, boolean isPolymorphic, int maxCallDepth) {
		List<MethodDefinition> implementedMethodList = manager.getImplementedMethodList(type);
		TreeSet<MethodDefinition> methodNodeSet = new TreeSet<MethodDefinition>();
		for (MethodDefinition method : implementedMethodList) {
			methodNodeSet.add(method);
			Set<MethodDefinition> calledMethodSet = null;
			if (isPolymorphic) calledMethodSet = manager.getPolymorphicInvocationMethodSet(method, maxCallDepth);
			else calledMethodSet = manager.getStaticInvocationMethodSet(method, maxCallDepth);
			for (MethodDefinition calledMethod : calledMethodSet) methodNodeSet.add(calledMethod);
		}
		
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		for (MethodDefinition method : methodNodeSet) {
			nodeList.add(new CallGraphNode(method));
		}
		return create(graphId, manager, nodeList, isPolymorphic);
	}

	/**
	 * 生成一个方法及其调用深度最大为 maxCallDepth 的所有方法之间的调用图
	 */
	public static AbstractGraph create(String graphId, SoftwareStructManager manager, MethodDefinition method, boolean isPolymorphic, int maxCallDepth) {
		TreeSet<MethodDefinition> methodNodeSet = new TreeSet<MethodDefinition>();
		methodNodeSet.add(method);
		Set<MethodDefinition> calledMethodSet = null;
		if (isPolymorphic) calledMethodSet = manager.getPolymorphicInvocationMethodSet(method, maxCallDepth);
		else calledMethodSet = manager.getStaticInvocationMethodSet(method, maxCallDepth);
		for (MethodDefinition calledMethod : calledMethodSet) methodNodeSet.add(calledMethod);
		
		List<GraphNode> nodeList = new ArrayList<GraphNode>();
		for (MethodDefinition methodNode : methodNodeSet) {
			nodeList.add(new CallGraphNode(methodNode));
		}
		return create(graphId, manager, nodeList, isPolymorphic);
	}
	
	/**
	 * 基于指定的方法调用图节点列表 nodeList 来生成这些节点之间的方法调用关系。如果 isPolymorphic 为 true， 将基于方法之间的多态调用关系生成调用图，否则基于方法之间
	 * 的静态调用关系生成调用图。
	 */
	private static CallGraph create(String graphId, SoftwareStructManager manager, List<GraphNode> nodeList, boolean isPolymorphic) {
		CallGraph result = new CallGraph(graphId);

		List<GraphEdge> edgeList = new ArrayList<GraphEdge>();
		
		for (GraphNode node : nodeList) {
			// 我们假定在 nodeList 中的节点都是 CallGraphNode
			CallGraphNode callerNode = (CallGraphNode)node;
			MethodDefinition caller = callerNode.getMethodDefinition();
			List<MethodDefinition> calleeList = null;
			if (isPolymorphic) calleeList = manager.getDirectPolymorphicInvocationMethodList(caller);
			else calleeList = manager.getDirectStaticInvocationMethodList(caller);
			
			if (calleeList != null) {
				for (MethodDefinition callee : calleeList) {
					CallGraphNode calleeNode = findMethodInNodeList(callee, nodeList);
					CallGraphEdge edge = new CallGraphEdge(callerNode, calleeNode);
					if (calleeNode != null) edgeList.add(edge);
				}
			}
		}
		
		result.setNodes(nodeList);
		result.setEdges(edgeList);
		return result;
	}
	
	private static CallGraphNode findMethodInNodeList(MethodDefinition method, List<GraphNode> nodeList) {
		for (GraphNode node : nodeList) {
			CallGraphNode callNode = (CallGraphNode)node;
			if (callNode.getMethodDefinition() == method) return callNode;
		}
		return null;
	}
	
	/**
	 * 创建调用图的顶点：先获得所有的DetailedTypeDefinition，然后其中所有的方法创建为顶点
	 * @param scope 系统空间
	 * @return 返回所有顶点构成的列表
	 */
	private static List<GraphNode> creatNodes(SystemScope scope) 
	{		
		List<GraphNode> nodes = new ArrayList<GraphNode>();
		
		List<DetailedTypeDefinition> types = scope.getAllDetailedTypeDefinitions();
		for(DetailedTypeDefinition type : types) {
			List<MethodDefinition> methods = type.getMethodList();
			if(methods != null) {
				for(MethodDefinition method : methods) {
					// 过滤掉构造方法
					if (!method.getSimpleName().equals(type.getSimpleName())) {
						CallGraphNode node = new CallGraphNode(method);
						nodes.add(node);
					}
					if (method.getSimpleName().equals(type.getSimpleName())) {
						//System.out.println(method.getFullQualifiedName());
					}
				}
			}
		}
		
		/*for(GraphNode n : nodes)
		{
			System.out.println(n.getId());
			
		}*/
		return nodes;
	}
	/**
	 * 创建调用图的边：通过扫描所有控制流图中的可执行点进行创建
	 * @param manager 名字表
	 * @param parser 用来将源代码解析成抽象语法树
	 * @return 返回所有边构成的列表
	 */
	private static List<GraphEdge> createEdges(NameTableManager manager, SourceCodeFileSet parser) 
	{		
		List<GraphEdge> edges = new ArrayList<GraphEdge>();
		
		List<CompilationUnitScope> units = manager.getAllCompilationUnitScopes();
		
		if (units == null || units.size() <= 0)  {
			return null;
		}
		System.out.println("Number of CompilationUnit: " + units.size());
		String systemPath = manager.getSystemPath();
		for (CompilationUnitScope unit : units) {
			String unitFileName = unit.getUnitName();
			
			CompilationUnit astRoot = parser.findSourceCodeFileASTRootByFileUnitName(unitFileName);
			if (astRoot == null) {
				throw new AssertionError("Can not find the compilation unit for the file: " + (systemPath + unitFileName));
			}
			
			CFGCreator cfgCreator = new CFGCreator(unitFileName, astRoot);
			List<ControlFlowGraph> cfgs = cfgCreator.create();		
			if (cfgs == null) return null;

			for (ControlFlowGraph cfg : cfgs) {
				// 不考虑构造方法
				if(cfg.getMethod().isConstructor()) {
					//System.out.println("isConstructor");
					continue;
				}
				// 获取调用方法节点，即调用者
				NameTableASTBridge bridge = new NameTableASTBridge(manager);
				MethodDefinition methodDefinition = bridge.findDefinitionForMethodDeclaration(unitFileName, cfg.getMethod()); 
				GraphNode caller = new CallGraphNode(methodDefinition);
				List<GraphNode> possibleCallees = null;
				
				List<GraphNode> nodes = cfg.getAllNodes();
				if (nodes != null) {
					for (GraphNode node : nodes) {
						ExecutionPoint point = (ExecutionPoint)node;
						if (point.isVirtual()) continue;
						else {
							NameReferenceCreator referenceCreator = new NameReferenceCreator(manager);
							List<NameReference> referenceList = referenceCreator.createReferencesForASTNode(unitFileName, point.getAstNode());
							for (NameReference reference : referenceList) {
								reference.resolveBinding();
								// 获取一个调用点可能的被调用方法节点，即可能的被调用者
								possibleCallees = getPossibleCallees(reference);
								
								if(possibleCallees != null) {
									for (GraphNode possibleCallee : possibleCallees) {
										GraphEdge edge = new CallGraphEdge((CallGraphNode)caller, (CallGraphNode)possibleCallee);
										if(!edges.contains(edge)) {
											edges.add(edge);
										}
									}
								}
							}
						}
					}
				}
			}
			parser.releaseFileContent(unitFileName);
			parser.releaseAST(unitFileName);
		}
		return edges;
	}

	/**
	 * 辅助方法，用以获得一个引用或引用组中可能涉及的被调用方法
	 * @param reference 传入的引用
	 * @return 返回可能涉及的被调用方法的列表
	 */
	private static List<GraphNode> getPossibleCallees(NameReference reference) 
	{
		List<GraphNode> possibleCallees = new ArrayList<GraphNode>();
		List<MethodReference> methodReferences = new ArrayList<MethodReference>();
		
		if (reference.isGroupReference()) {
			List<NameReference> referencesInGroup = ((NameReferenceGroup)reference).getReferencesAtLeaf();
			for (NameReference ref : referencesInGroup) {
				if (ref.isMethodReference()) {
					methodReferences.add((MethodReference)ref);
				}
			}
		} else if (reference.isMethodReference()) {
			methodReferences.add((MethodReference)reference);
		} else return null;
		
		for(MethodReference methodRef : methodReferences) {
			List<MethodDefinition> methodList = methodRef.getAlternativeList();
			if (methodList != null && methodList.size() >= 1) {
				for (MethodDefinition method : methodList) {
					possibleCallees.add(new CallGraphNode(method));
				}
			}
		}
		return possibleCallees;
	}
	
	/**
	 * Return all Class Instance Creation References in the group
	 */
	public static List<NameReference> getAllClassInstanceCreationReferences(NameReferenceGroup nameReferenceGroup) 
	{
		List<NameReference> result = new ArrayList<NameReference>();
		if(nameReferenceGroup instanceof NRGClassInstanceCreation) {
			result.add(nameReferenceGroup);
		}
		
		List<NameReference> subreferences = nameReferenceGroup.getSubReference();
		if (subreferences == null) {
			return result;
		}
		
		for (NameReference reference : subreferences) {
			if (reference.isGroupReference()) {
				NameReferenceGroup group = (NameReferenceGroup)reference;
				List<NameReference> referencesInGroup = getAllClassInstanceCreationReferences(group);
				result.addAll(referencesInGroup);
			} else continue;
		}
		return result;
	}
}

