package graph.variableImpactNetwork;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.referenceGroup.NRGArrayCreation;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/1
 * @version 1.0
 */
public class VariableImpactGraph extends AbstractGraph {
	private HashMap<Object, VariableImpactNode> nodesInCreation = null;
	private HashSet<VariableImpactEdge> edgesInCreation = null;

	public VariableImpactGraph(String graphName) {
		super(graphName);
		nodesInCreation = new HashMap<Object, VariableImpactNode>();
		edgesInCreation = new HashSet<VariableImpactEdge>();
	}
	
	private static String getNodeIdForDefinition(NameDefinition definition) {
		String locationString = definition.getLocation().toString();
		String id = definition.getSimpleName() + "@" + locationString;
		return id;
	}

	private static String getNodeLabelForDefinition(FieldDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		String label = definition.getSimpleName() + "@" + locationString;;
		return label;
	}

	private static String getNodeLabelForDefinition(MethodDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		String label = definition.getSimpleName() + ".return@" + locationString;;
		return label;
	}

	private static String getNodeLabelForDefinition(VariableDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		String label = definition.getSimpleName() + "@" + locationString;;
		return label;
	}

	private static String getNodeDescriptionForDefinition(TypeDefinition type, FieldDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		TypeDefinition fieldType = definition.getTypeDefinition();
		String typeString = "";
		if (fieldType != null) typeString = ":" + fieldType.getSimpleName();
		String description = type.getSimpleName() + "." + definition.getSimpleName() + typeString + "@" + locationString;;
		return description;
	}

	private static String getNodeDescriptionForDefinition(MethodDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		TypeDefinition returnType = definition.getReturnTypeDefinition();
		String typeString = "";
		if (returnType != null) typeString = ":" + returnType.getSimpleName();
		String description = definition.getSimpleName() + ".return" + typeString + "@" + locationString;;
		return description;
	}
	
	private static String getNodeDescriptionForDefinition(VariableDefinition definition) {
		String locationString = definition.getLocation().toFullString();
		TypeDefinition varType = definition.getTypeDefinition();
		String typeString = "";
		if (varType != null) typeString = ":" + varType.getSimpleName();
		String description = definition.getSimpleName() + typeString + "@" + locationString;;
		return description;
	}

	private FieldImpactNode findNodeByDefinition(FieldDefinition definition) {
		return (FieldImpactNode)nodesInCreation.get(definition);
	}
	
	private ReturnValueImpactNode findNodeByDefinition(MethodDefinition definition) {
		return (ReturnValueImpactNode)nodesInCreation.get(definition);
	}

	private LocalVariableImpactNode findNodeByDefinition(VariableDefinition definition) {
		return (LocalVariableImpactNode)nodesInCreation.get(definition);
	}
	
	public FieldImpactNode createAndAddNodeForDefinition(TypeDefinition type, FieldDefinition definition) {
		FieldImpactNode resultNode = findNodeByDefinition(definition);
		if (resultNode == null) {
			String id = getNodeIdForDefinition(definition);
			String label = getNodeLabelForDefinition(definition);
			String description = getNodeDescriptionForDefinition(type, definition);
			resultNode = new FieldImpactNode(id, label, definition);
			resultNode.setDescription(description);
			nodesInCreation.put(definition, resultNode);
		}
		return resultNode;
	}
	
	public ReturnValueImpactNode createAndAddNodeForDefinition(MethodDefinition definition) {
		ReturnValueImpactNode resultNode = findNodeByDefinition(definition);
		if (resultNode == null) {
			String id = getNodeIdForDefinition(definition);
			String label = getNodeLabelForDefinition(definition);
			String description = getNodeDescriptionForDefinition(definition);
			resultNode = new ReturnValueImpactNode(id, label, definition);
			resultNode.setDescription(description);
			nodesInCreation.put(definition, resultNode);
		}
		return resultNode;
	}

	public LocalVariableImpactNode createAndAddNodeForDefinition(VariableDefinition definition) {
		LocalVariableImpactNode resultNode = findNodeByDefinition(definition);
		if (resultNode == null) {
			String id = getNodeIdForDefinition(definition);
			String label = getNodeLabelForDefinition(definition);
			String description = getNodeDescriptionForDefinition(definition);
			resultNode = new LocalVariableImpactNode(id, label, definition);
			resultNode.setDescription(description);
			nodesInCreation.put(definition, resultNode);
		}
		return resultNode;
	}
	
	public ArrayImpactNode createAndAddNodeForReference(NRGArrayCreation reference) {
		String locationString = reference.getLocation().toString();
		String id = "array@" + locationString;
		
		locationString = reference.getLocation().toFullString();
		String typeString = null;
		if (reference.isResolved()) {
			NameDefinition definition = reference.getDefinition();
			typeString = definition.getSimpleName();
		}
		String label = "array@" + locationString;
		String description = label;
		if (typeString != null) description = "array:" + typeString + "@" + locationString;
		
		ArrayImpactNode resultNode = new ArrayImpactNode(id, label, reference);
		resultNode.setDescription(description);
		nodesInCreation.put(reference, resultNode);
		
		return resultNode;
	}

	public ObjectImpactNode createAndAddNodeForReference(NRGClassInstanceCreation reference) {
		String locationString = reference.getLocation().toString();
		String id = "object@" + locationString;
		
		locationString = reference.getLocation().toFullString();
		String typeString = null;
		if (reference.isResolved()) {
			NameDefinition definition = reference.getDefinition();
			typeString = definition.getSimpleName();
		}
		String label = "object@" + locationString;
		String description = label;
		if (typeString != null) description = "object:" + typeString + "@" + locationString;
		
		ObjectImpactNode resultNode = new ObjectImpactNode(id, label, reference);
		resultNode.setDescription(description);
		nodesInCreation.put(reference, resultNode);
		
		return resultNode;
	}

	public void createAndAddComponentImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;
		
		if (startNode == endNode) return;
		ComponentImpactEdge edge = new ComponentImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}

	public void checkAndAddComponentImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;

		if (startNode == endNode) return;
//		if (hasEdge(startNode, endNode)) return;
		ComponentImpactEdge edge = new ComponentImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}
	
	public void createAndAddAssignImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;

		if (startNode == endNode) return;
		AssignImpactEdge edge = new AssignImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}

	public void checkAndAddAssignImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;

		if (startNode == endNode) return;
//		if (hasEdge(startNode, endNode)) return;
		AssignImpactEdge edge = new AssignImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}

	public void createAndAddControlImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;

		if (startNode == endNode) return;
		ControlImpactEdge edge = new ControlImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}

	public void checkAndAddControlImpact(VariableImpactNode startNode, VariableImpactNode endNode) {
//		if (edges != null) return;

		if (startNode == endNode) return;
//		if (hasEdge(startNode, endNode)) return;
		ControlImpactEdge edge = new ControlImpactEdge(startNode, endNode);
		edgesInCreation.add(edge);
	}
	
	public void normalize() {
		if (nodes != null || nodesInCreation == null) return;
		
		nodes = new ArrayList<GraphNode>();
		Collection<VariableImpactNode> tempNodes = nodesInCreation.values();
		nodes.addAll(tempNodes);
		nodesInCreation = null;
		
		edges = new ArrayList<GraphEdge>();
		edges.addAll(edgesInCreation);
		
		nodesInCreation = null;
		edgesInCreation = null;
	}

	/**
	 * Check if the edge between two nodes is in the graph
	 */
	public boolean hasEdge(VariableImpactNode from, VariableImpactNode to) {
		if (edges == null) return false;
		for (GraphEdge edge : edges) {
			VariableImpactNode start = (VariableImpactNode)edge.getStartNode(); 
			VariableImpactNode end = (VariableImpactNode)edge.getEndNode(); 
			if (start.equals(from) && end.equals(to)) return true;
		}
		return false;
	}
	
	/**
	 * Add an edge to the node. If the graph has an edge with the same start and end, then we DO NOT add it!
	 */
	public void addEdge(VariableImpactEdge edge) {
		VariableImpactNode start = (VariableImpactNode)edge.getStartNode();
		VariableImpactNode end = (VariableImpactNode)edge.getEndNode();
		if (start.equals(end)) return;		// We do not add the edge from a node to itself.
		edges.add(edge);
	}

	public static VariableImpactGraph readFromNetFile(String netFile) throws IOException {
		final char splitter = ' ';		// the space char is used as splitter in the .net file
		
		// Read the node list from the .net file
		Scanner netScanner = new Scanner(new File(netFile));

		VariableImpactGraph result = new VariableImpactGraph(netFile);
		
		boolean isVertice = false;
		boolean isArcs = false;
		
		while(netScanner.hasNextLine()) {
			String line = netScanner.nextLine();
			if (line.contains("*Vertices")) {
				// This is the head line of the .net file, which show that the next line begins the node of the graph, and give the total number of nodes
				isVertice = true;
			} else if (line.contains("*Arcs") || line.contains("*Edges")) {
				// This line shows that the next line begins the edges (or arcs, i.e. directed edges)  of the graph
				isVertice = false;
				isArcs = true;
			} else if (line.trim().equals("")) {
				// This line shows that the next line begins the edges (or arcs, i.e. directed edges)  of the graph
				continue;
			} else if (isVertice == true) {
				// This line give a vertex (i.e. a node) of the graph, which gives the id and the label of the node, and uses a space
				// to split the id and the label
				int spacePosition = line.indexOf(splitter);
				if (spacePosition < 0) {
					netScanner.close();
					throw new AssertionError("In file [" + netFile + "], illegal line: [" + line + "]");
				}
				String idString = line.substring(0, spacePosition);
				String labelString = line.substring(spacePosition+1, line.length());
				if (labelString.contains("\"")) {
					// Clear the quota character (i.e. '\"') in the label string!
					labelString = labelString.replace("\"", "");
				}
				
				VariableImpactNode node = new VariableImpactNode(idString, labelString);
				result.addNode(node);
			} else if (isArcs == true) {
				// This line give a arc (i.e. an edge) of the graph, which gives the start node id and end node id
				int spacePosition = line.indexOf(splitter);
				if (spacePosition < 0) {
					netScanner.close();
					throw new AssertionError("In file [" + netFile + "], illegal line: [" + line + "]");
				}

				String startId = line.substring(0, spacePosition);
				String endId = line.substring(spacePosition+1, line.length());
				
				VariableImpactNode startNode = (VariableImpactNode)result.findById(startId);
				if (startNode == null) {
					netScanner.close();
					throw new AssertionError("Can not find node id " + startId);
				}
				VariableImpactNode endNode = (VariableImpactNode)result.findById(endId);
				if (endNode == null) {
					netScanner.close();
					throw new AssertionError("Can not find node id " + startId);
				}
				
				result.addEdge(new VariableImpactEdge(startNode, endNode));
			}
		}
		netScanner.close();
		return result;
	}
	
}
