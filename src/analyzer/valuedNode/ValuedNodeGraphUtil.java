package analyzer.valuedNode;

import graph.basic.AbstractGraph;
import graph.basic.GraphNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A tool class for providing some methods to generate valued node managers from abstract graph node lists. 
 * @author Zhou Xiaocong
 * @since 2014/1/2
 * @version 1.0
 */
public class ValuedNodeGraphUtil {
	/**
	 * Calculate the importance of the graph nodes in the give graph by using PageRank algorithm 
	 */
	public static ValuedNodeManager calculateNodeImportanceByAuthority(AbstractGraph graph) {
		final double threshold = 10e-8;
		final int maxIteration = 50;
		
		double totalDegree = graph.getEdges().size();
		if (totalDegree <= 0.0) return null;

//		System.out.println("Before initialize");
		List<ValuedNode> nodeList = new ArrayList<ValuedNode>();
		// The total out degree is equal to the number of the edges in the graph
		// Use the out degree to initialize the authority and hub value in the node. Authority value is saved in the 
		// value of the node, and hub value is saved in the rank of the node
		List<GraphNode> graphNodeList = graph.getAllNodes();
		int size = graphNodeList.size();
		for (int i = 0; i < size; i++) {
			GraphNode node = graphNodeList.get(i);
			ValuedNode valuedNode = new ValuedNode(node.getId(), node.getLabel());
			double authority = graph.getInDegree(node) / totalDegree;
			double hub = graph.getOutDegree(node) / totalDegree;
			valuedNode.setValue(authority);
			valuedNode.setRank(hub);
			nodeList.add(valuedNode);
		}
//		System.out.println("After initialize");

//		System.out.println("Initialize, before normalization: ");
//		checkAuthorityValue(nodeList);
		
		double[] lastAuthority = new double[size];
		double[] lastHub = new double[size];
		double authMod = 0;
		double hubMod = 0; 
		for (int i = 0; i < size; i++) {
			ValuedNode nodeI = nodeList.get(i);
			authMod = authMod + nodeI.getValue() * nodeI.getValue();
			hubMod = hubMod + nodeI.getRank() * nodeI.getRank(); 
		}
		authMod = Math.sqrt(authMod);
		hubMod = Math.sqrt(hubMod);
		for (int i = 0; i < size; i++) {
			ValuedNode nodeI = nodeList.get(i);
			lastAuthority[i] = nodeI.getValue() / authMod;
			lastHub[i] = nodeI.getRank() / hubMod;
			nodeI.setValue(lastAuthority[i]);
			nodeI.setRank(lastHub[i]);
		}

//		System.out.println("Initialize, after normalization: ");
//		checkAuthorityValue(nodeList);
//		System.out.println("Before adjacent");
		
		int[][] adjacentMatrix = graph.getAdjacentMatrix();

//		System.out.println("Before iteration....");
		
		// Test if we can complete the calculation!
		boolean complete = true;
		int iteration = 0;
		do {
			complete = true;
			// Modify the value by the PageRank algorithm
			for (int i = 0; i < size; i++) {
				ValuedNode nodeI = nodeList.get(i);
				
				double newAuthority = 0;
				for (int j = 0; j < size; j++) {
					ValuedNode nodeJ = nodeList.get(j);
					newAuthority = newAuthority + adjacentMatrix[j][i] * nodeJ.getRank();
				}
//				System.out.println("i = " + i + " new authority = " + newAuthority);
				nodeI.setValue(newAuthority);
			}
			for (int i = 0; i < size; i++) {
				ValuedNode nodeI = nodeList.get(i);
				double newHub = 0;
				for (int j = 0; j < size; j++) {
					ValuedNode nodeJ = nodeList.get(j);
					newHub = newHub + adjacentMatrix[i][j] * nodeJ.getValue();
				}
//				System.out.println("i = " + i + " new hub = " + newHub);
				nodeI.setRank(newHub);
			}

//			System.out.println("Iteration = " + iteration + ", before normalization: ");
//			checkAuthorityValue(nodeList);

			authMod = 0;
			hubMod = 0; 
			for (int i = 0; i < size; i++) {
				ValuedNode nodeI = nodeList.get(i);
				authMod = authMod + nodeI.getValue() * nodeI.getValue();
				hubMod = hubMod + nodeI.getRank() * nodeI.getRank(); 
			}
			authMod = Math.sqrt(authMod);
			hubMod = Math.sqrt(hubMod);
			for (int i = 0; i < size; i++) {
				ValuedNode nodeI = nodeList.get(i);
				double newAuth = nodeI.getValue() / authMod;
				double newHub = nodeI.getRank() / hubMod;

				if (complete == true && Math.abs(newAuth - lastAuthority[i]) > threshold) {
//					System.out.println("Index i = " + i + ", new authority = " + newAuth + ", last = " + lastAuthority[i]);
					complete = false;
				}
				if (complete == true && Math.abs(newHub - lastHub[i]) > threshold) {
//					System.out.println("Index i = " + i + ", new hub = " + newHub + ", last = " + lastHub[i]);
					complete = false;
				}
				
				nodeI.setValue(newAuth);
				lastAuthority[i] = newAuth;
				nodeI.setRank(newHub);
				lastHub[i] = newHub;
			}

//			System.out.println("Iteration = " + iteration + ", After normalization: ");
//			checkAuthorityValue(nodeList);
			
			iteration = iteration + 1;
		} while (complete == false && iteration < maxIteration);
		
//		System.out.println("Iteration = " + iteration);
		
		ValuedNodeManager result = new ValuedNodeManager();
		for (ValuedNode node : nodeList) result.addValuedNode(node);
		
		return result;
	}

	/**
	 * Calculate the importance of the graph nodes in the give graph by using PageRank algorithm 
	 */
	public static ValuedNodeManager calculateNodeImportanceByPageRanking(AbstractGraph graph) {
		final double threshold = 10e-6;
		final int maxIteration = 50;
		final double select = 0.85;
		
		double totalOutDegree = graph.getEdges().size();
		if (totalOutDegree <= 0.0) return null;
		
		List<ValuedNode> nodeList = new ArrayList<ValuedNode>();
		// The total out degree is equal to the number of the edges in the graph
		// Use the out degree to initialize the value in the node
		List<GraphNode> graphNodeList = graph.getAllNodes();
		int size = graphNodeList.size();
		int[] nodeOutDegree = new int[size];
		for (int i = 0; i < size; i++) {
			GraphNode node = graphNodeList.get(i);
			ValuedNode valuedNode = new ValuedNode(node.getId(), node.getLabel());
			nodeOutDegree[i] = graph.getOutDegree(node);
			double value = nodeOutDegree[i] / totalOutDegree;
			valuedNode.setValue(value);
			nodeList.add(valuedNode);
		}
		
		int[][] adjacentMatrix = graph.getAdjacentMatrix();
		double[][] googleMatrix = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (nodeOutDegree[i] > 0) {
					if (adjacentMatrix[i][j] == 1) {
						googleMatrix[i][j] = 1.0/nodeOutDegree[i];
					} else googleMatrix[i][j] = 0.0;
				} else googleMatrix[i][j] = 1.0/size;
			}
		}
		
		// Test if we can complete the calculation!
		boolean complete = true;
		int iteration = 0;
		do {
//			System.out.println("Iteration = " + iteration);
//			checkPageRankingValue(nodeList);
			
			complete = true;
			// Modify the value by the PageRank algorithm
			for (int i = 0; i < size; i++) {
				ValuedNode nodeI = nodeList.get(i);
				
				double sum = 0;
				for (int j = 0; j < size; j++) {
					ValuedNode nodeJ = nodeList.get(j);
					sum = sum + googleMatrix[j][i] * nodeJ.getValue();
				}
				
				double newValue = select * sum + (1-select) * (1.0/size);
				if (complete == true && Math.abs(newValue - nodeI.getValue()) > threshold) complete = false;
				nodeI.setValue(newValue);
			}
			iteration = iteration + 1;
		} while (complete == false && iteration < maxIteration);
		
		System.out.println("Iteration = " + iteration);
		
		ValuedNodeManager result = new ValuedNodeManager();
		for (ValuedNode node : nodeList) result.addValuedNode(node);
		
		return result;
	}
	
	
	
	@SuppressWarnings("unused")
	private static void checkPageRankingValue(List<ValuedNode> nodeList) {
		double sum = 0;
		for (ValuedNode node : nodeList) {
			System.out.println("Node [" + node.getLabel() + "], value = " + node.getValue());
			sum = sum + node.getValue();
		}
		
		if (Math.abs(sum - 1.0) > 10e-6) {
			System.out.println("The summation (= " + sum + ") of the page rank values is not equal to 1.0");
		}
	}

	@SuppressWarnings("unused")
	private static void checkAuthorityValue(List<ValuedNode> nodeList) {
		double authSum = 0;
		double hubSum = 0;
		for (ValuedNode node : nodeList) {
			authSum = authSum + node.getValue() * node.getValue();
			hubSum = hubSum + node.getRank() * node.getRank();
			
			System.out.println("Node [" + node.getLabel() + "], value = " + node.getValue() + ", rank = " + node.getRank());
		}

		if (Math.abs(authSum - 1.0) > 10e-6 || Math.abs(hubSum - 10.) > 10e-6) {
			System.out.println("The authority summation (= " + authSum + ") or hub summation (= " + hubSum + " is not equal to 1.0");
		}
	}
	
	
}
