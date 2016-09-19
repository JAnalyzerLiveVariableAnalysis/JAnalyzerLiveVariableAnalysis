package graph.dependenceGraph;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * The class correspond to the class dependence graph. 
 * @author Zhou Xiaocong
 * @since 2014/1/4
 * @version 1.0
 */
public class ClassDependenceGraph extends AbstractGraph {
	private String systemPath = null; 
	
	public ClassDependenceGraph(String path) {
		super(path);
		this.systemPath = path;
	}

	@Override
	public String getId() {
		return systemPath;
	}

	public ClassDependenceNode findNodeByDefinition(DetailedTypeDefinition definition) {
		if (nodes == null) return null;
		for (GraphNode node : nodes) {
			ClassDependenceNode dNode = (ClassDependenceNode)node;
			if (dNode.getDefinition() == definition) return dNode;
		}
		return null;
	}
	
	/**
	 * Check if the edge between two nodes is in the graph
	 */
	public boolean hasEdge(ClassDependenceNode from, ClassDependenceNode to) {
		if (edges == null) return false;
		for (GraphEdge edge : edges) {
			ClassDependenceNode start = (ClassDependenceNode)edge.getStartNode(); 
			ClassDependenceNode end = (ClassDependenceNode)edge.getEndNode(); 
			if (start.equals(from) && end.equals(to)) return true;
		}
		return false;
	}
	
	/**
	 * Add an edge to the node. If the graph has an edge with the same start and end, then we DO NOT add it!
	 */
	public void addEdge(ClassDependenceEdge edge) {
		if (edges == null) edges = new ArrayList<GraphEdge>(10);
		ClassDependenceNode start = (ClassDependenceNode)edge.getStartNode();
		ClassDependenceNode end = (ClassDependenceNode)edge.getEndNode();
		if (start.equals(end)) return;		// We do not add the edge from a node to itself.
		if (hasEdge(start, end)) return;
		else edges.add(edge);
	}

	public static ClassDependenceGraph readFromNetFile(String netFile) throws IOException {
		final char splitter = ' ';		// the space char is used as splitter in the .net file
		
		// Read the node list from the .net file
		Scanner netScanner = new Scanner(new File(netFile));

		ClassDependenceGraph result = new ClassDependenceGraph(netFile);
		
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
				
				ClassDependenceNode node = new ClassDependenceNode(idString, labelString);
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
				
				ClassDependenceNode startNode = (ClassDependenceNode)result.findById(startId);
				if (startNode == null) {
					netScanner.close();
					throw new AssertionError("Can not find node id " + startId);
				}
				ClassDependenceNode endNode = (ClassDependenceNode)result.findById(endId);
				if (endNode == null) {
					netScanner.close();
					throw new AssertionError("Can not find node id " + startId);
				}
				
				result.addEdge(new ClassDependenceEdge(startNode, endNode));
			}
		}
		
		netScanner.close();
		return result;
	}
}
