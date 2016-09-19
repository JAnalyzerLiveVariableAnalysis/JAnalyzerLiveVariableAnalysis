package analyzer.valuedNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Comparator;

import nameTable.nameDefinition.NameDefinition;

/**
 * To manage a list of valued node
 * @author Zhou Xiaocong
 * @since 2014/1/20
 * @version 1.0
 */
public class ValuedNodeManager {

	private List<ValuedNode> nodeList = null;
	private boolean hasSorted = false;
	
	/**
	 * Set name definition in the valued node to null so that we have chance to release the memory 
	 * occupied by the name table.
	 */
	public void clearNodeNameDefinition() {
		for (ValuedNode node: nodeList) {
			node.setDefinition(null);
		}
	}
	
	public void addValuedNode(ValuedNode node) {
		if (nodeList == null) nodeList = new ArrayList<ValuedNode>();
		nodeList.add(node);
	}
	
	public List<ValuedNode> getNodeList() {
		return nodeList;
	}

	public List<ValuedNode> getNodeListCopy(double valueThreshold) {
		if (nodeList == null) return null;
		
		List<ValuedNode> result = new ArrayList<ValuedNode>(nodeList.size());
		for (ValuedNode node : nodeList) {
			if (node.getValue() >= valueThreshold) result.add(node);
		}
		
		return result;
	}
	
	public List<ValuedNode> getNodeListCopy() {
		if (nodeList == null) return null;
		
		List<ValuedNode> result = new ArrayList<ValuedNode>(nodeList.size());
		for (ValuedNode node : nodeList) result.add(node);
		
		return result;
	}

	public ValuedNodeManager getManagerCopy(double valueThreshold) {
		ValuedNodeManager result = new ValuedNodeManager();
		result.nodeList = new ArrayList<ValuedNode>();
		for (ValuedNode node : nodeList) {
			if (node.getValue() >= valueThreshold) result.nodeList.add(node);
		}
		
		return result;
	}

	
	/**
	 * Read a list of node from a .net file and a .vec file. See the manual of Pajek for referring the format of .net file or .vec file.
	 * @param netFile: give the file name of a .net file, which gives the nodes and edges of a graph. 
	 * @param vecFile: give the file name of a .vec file, which gives the values of the nodes defined in the .net file.
	 * @param kind: give the kind of nodes in the .net file, i.e. a node in the .net file should correspond to a class, a method or a variable?  
	 */
	public void read(String netFile, String vecFile, ValuedNodeKind kind) throws IOException {
		final char splitter = ' ';		// the space char is used as splitter in the .net file
		final String nodeStartFlag = "*Vertices";
		
		// Read the node list from the .net file
		Scanner netScanner = new Scanner(new File(netFile));
		
		boolean isVertice = false;
		int nodeCounter = 0;
		int nodeTotalNumber = 0;
		while(netScanner.hasNextLine()) {
			String line = netScanner.nextLine();
			if (line.contains(nodeStartFlag)) {
				// This is the head line of the .net file, which show that the next line begins the node of the graph, and give the total number of nodes
				int spacePosition = line.indexOf(splitter);
				if (spacePosition < 0) {
					netScanner.close();
					throw new AssertionError("In file [" + netFile + "], illegal line: [" + line + "]");
				}
				String nodeTotalNumberString = line.substring(spacePosition+1, line.length());
				nodeTotalNumber = Integer.parseInt(nodeTotalNumberString);
				
				nodeList = new ArrayList<ValuedNode>(nodeTotalNumber);
				isVertice = true;
			} else if (line.contains("*Arcs") || line.contains("*Edges")) {
				// This line shows that the next line begins the edges (or arcs, i.e. directed edges)  of the graph
				isVertice = false;
				break;
			} else if (isVertice == true) {
				// This is line give a vertex (i.e. a node) of the graph, which gives the id and the label of the node, and uses a space
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
				nodeCounter = nodeCounter + 1;
				if (nodeCounter > nodeTotalNumber+1) {
					netScanner.close();
					throw new AssertionError("In file [" + netFile + "], total node number [" + nodeTotalNumber + "] is incorrect!");
				}
				
				ValuedNode node = ValuedNode.createValuedNodeInstance(idString, labelString, kind);
				nodeList.add(node);
			}
		}
		netScanner.close();
		
		if (nodeList == null) return;
		
		// Read the values of nodes from the .vec file
		Scanner vecScanner = new Scanner(new File(vecFile));
		int nodeIndex = 0;
		while (vecScanner.hasNextLine()) {
			String line = vecScanner.nextLine();
			if (line.contains(nodeStartFlag)) {
				// This is the head line of the .vec file, which show that the next line begins the values of nodes, and give the total number of nodes
				int spacePosition = line.indexOf(splitter);
				if (spacePosition < 0) {
					vecScanner.close();
					throw new AssertionError("In file [" + vecFile + "], illegal line: [" + line + "]");
				}
				String nodeTotalNumberString = line.substring(spacePosition+1, line.length());
				nodeCounter = Integer.parseInt(nodeTotalNumberString);

				if (nodeCounter != nodeTotalNumber) {
					// The node total number given in the .vec file does not equal to the number given in the .net file. 
					// This means that the .vec file does not match to the .net file, that may be an error!
					vecScanner.close();
					throw new AssertionError("Total node number [" + nodeCounter + "] in " + vecFile + " does not equal to [" + nodeTotalNumber + "] given in " + netFile);
				}
			} else {
				if (nodeIndex > nodeCounter) {
					vecScanner.close();
					throw new AssertionError("In file [" + vecFile + "], total line [" + nodeIndex + "] is greater than total node number [" + nodeCounter + "]");
				}

				// We assume this line only include a double value of the node, and the sequence of the values is as the same as the sequence of the 
				// nodes given in the .net file
				double value = Double.parseDouble(line); 
				ValuedNode node = nodeList.get(nodeIndex);
				node.setValue(value);
				nodeIndex = nodeIndex + 1;
			}
		}
		vecScanner.close();
	}


	/**
	 * Read a list of node from a file which combines a node and its value.  
	 * @param file: Each line of the file gives a node and its value as "id\tlabel\tvalue" 
	 * @param kind: give the kind of nodes in the .net file, i.e. a node in the .net file should correspond to a class, a method or a variable? 
	 * @param hasTitle: the first line is the title or the node? 
	 */
	public void read(String file, ValuedNodeKind kind, boolean hasTitle) throws IOException {
		final String splitter = "\t";		// the table char is used as splitter in the file
		final int infoNumber = 3;
		
		nodeList = new ArrayList<ValuedNode>();
		
		Scanner scanner = new Scanner(new File(file));
		
		int lineCounter = 0;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (lineCounter == 0 && hasTitle == true) {
				// The first line is the title line of the file, ignore this line
				lineCounter = lineCounter + 1;
				continue;
			} else {
				// This is line give a vertex (i.e. a node) of the graph, which gives the id and the label of the node, and uses a space
				// to split the id and the label
				String[] infoStrings = line.split(splitter);
				if (infoStrings.length < infoNumber) {
					scanner.close();
					throw new AssertionError("In file [" + file + ", illegal line [" + line + "]"); 
				}
				
				String idString = infoStrings[0];
				String labelString = infoStrings[1];
				if (labelString.contains("\"")) {
					// Clear the quota character (i.e. '\"') in the label string!
					labelString = labelString.replace("\"", "");
				}
				double value = Double.parseDouble(infoStrings[2]);
				lineCounter = lineCounter + 1;
				
				ValuedNode node = ValuedNode.createValuedNodeInstance(idString, labelString, kind);
				node.setValue(value);
				nodeList.add(node);
			}
		}
		scanner.close();
	}
	
	/**
	 * Write the node list to the given text file.
	 * @param file: each line of the file gives a node and its value as "id\tlabel\tvalue", which can be read by the above method
	 * 		read(String file, ValuedNodeKind kind, boolean hasTitle)
	 * @param title: if it is not a null, the write it to the file as the title.
	 */
	public void write(String file, String title, boolean includeDefinition) throws IOException {
		if (nodeList == null) return;
		
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		if (title != null) writer.println(title);
		
		for (ValuedNode node : nodeList) {
			String message = node.getId() + "\t" + node.getLabel() + "\t" + node.getValue();
			if (includeDefinition == true) {
				NameDefinition definition = node.getDefinition();
				if (definition != null) message = message + "\t" + definition.toString();
			}
			writer.println(message);
		}
		writer.close();
	}
	
	
	/**
	 * If order == true, we sort the node as ascending order, if order == false, we sort the node as descending order
	 */
	public void sortNodeByValue(boolean order) {
		if (nodeList == null) return;
		if (hasSorted == true) return;

		Collections.sort(nodeList, new ValueComparator(order));

		for (int index = 0; index < nodeList.size(); index++) {
			ValuedNode node = nodeList.get(index);
			node.setRank(index+1);
		}
	}
	
	/**
	 * Display the node list in the manager for debugging
	 */
	public void display(PrintStream out) {
		for (ValuedNode node : nodeList) {
			out.println(node.toFullString());
		}
	}

}

class ValueComparator implements Comparator<ValuedNode> {
	private boolean order = true;
	
	/**
	 * If order == true, we sort the node as ascending order, if order == false, we sort the node as descending order
	 */
	public ValueComparator(boolean order) {
		this.order = order;
	}
	
	@Override
	public int compare(ValuedNode one, ValuedNode two) {
		if (order == true) return (int)Math.signum(one.value - two.value);
		else return (int)Math.signum(two.value - one.value);
	}
}


