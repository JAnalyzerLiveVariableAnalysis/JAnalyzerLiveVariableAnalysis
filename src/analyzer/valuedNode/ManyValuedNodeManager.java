package analyzer.valuedNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import nameTable.nameDefinition.NameDefinition;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ18ÈÕ
 * @version 1.0
 */
public class ManyValuedNodeManager {

	private List<ManyValuedNode> nodeList = null;
	private boolean hasSorted = false;
	
	/**
	 * Set name definition in the valued node to null so that we have chance to release the memory 
	 * occupied by the name table.
	 */
	public void clearNodeNameDefinition() {
		for (ManyValuedNode node: nodeList) {
			node.setDefinition(null);
		}
	}
	
	public void addValuedNode(ManyValuedNode node) {
		if (nodeList == null) nodeList = new ArrayList<ManyValuedNode>();
		nodeList.add(node);
	}
	
	public List<ManyValuedNode> getNodeList() {
		return nodeList;
	}

	public List<ManyValuedNode> getNodeListCopy(double valueThreshold, int valueIndex) {
		if (nodeList == null) return null;
		
		List<ManyValuedNode> result = new ArrayList<ManyValuedNode>(nodeList.size());
		for (ManyValuedNode node : nodeList) {
			if (node.getValue(valueIndex) >= valueThreshold) result.add(node);
		}
		
		return result;
	}
	
	public List<ManyValuedNode> getNodeListCopy() {
		if (nodeList == null) return null;
		
		List<ManyValuedNode> result = new ArrayList<ManyValuedNode>(nodeList.size());
		for (ManyValuedNode node : nodeList) result.add(node);
		
		return result;
	}

	public ManyValuedNodeManager getManagerCopy(double valueThreshold, int valueIndex) {
		ManyValuedNodeManager result = new ManyValuedNodeManager();
		result.nodeList = new ArrayList<ManyValuedNode>();
		for (ManyValuedNode node : nodeList) {
			if (node.getValue(valueIndex) >= valueThreshold) result.nodeList.add(node);
		}
		
		return result;
	}

	/**
	 * Read a list of node from a file which combines a node and its value.  
	 * @param file: Each line of the file gives a node and its value as "id\tlabel\tvalue\tvalue...." 
	 * @param kind: give the kind of nodes in the .net file, i.e. a node in the .net file should correspond to a class, a method or a variable? 
	 * @param hasTitle: the first line is the title or the node? 
	 */
	public void read(String file, ValuedNodeKind kind, boolean hasTitle) throws IOException {
		final String splitter = "\t";		// the table char is used as splitter in the file
		int infoNumber = -1;
		
		nodeList = new ArrayList<ManyValuedNode>();
		
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
				if (infoNumber < 0) {
					// When we read the first line, we set the information number
					infoNumber = infoStrings.length;
					if (infoNumber < 3) {
						scanner.close();
						throw new AssertionError("File [" + file + "] should at least have 3 columns!"); 
					}
				}
				if (infoStrings.length < infoNumber) {
					scanner.close();
					throw new AssertionError("In file [" + file + "], illegal line [" + line + "]"); 
				}
				
				String idString = infoStrings[0];
				String labelString = infoStrings[1];
				if (labelString.contains("\"")) {
					// Clear the quota character (i.e. '\"') in the label string!
					labelString = labelString.replace("\"", "");
				}
				ManyValuedNode node = ManyValuedNode.createManyValuedNodeInstance(idString, labelString, kind, infoNumber-2);
				for (int i = 2; i < infoNumber; i++) {
					if (!infoStrings[i].equals("NA")) {
						// The value of this position is Available
						try {
							double value = Double.parseDouble(infoStrings[i]);
							node.setValue(i-2, value);
						} catch (NumberFormatException exc) {
							// The value of this position is not available
						}
					}
				}
				nodeList.add(node);
				lineCounter = lineCounter + 1;
			}
		}
		scanner.close();
	}
	
	/**
	 * Write the node list to the given text file.
	 * @param file: each line of the file gives a node and its value as "id\tlabel\tvalue\tvalue....", which can be read by the above method
	 * 		read(String file, ValuedNodeKind kind, boolean hasTitle)
	 * @param title: if it is not a null, the write it to the file as the title.
	 */
	public void write(String file, String title, boolean includeDefinition) throws IOException {
		if (nodeList == null) return;
		
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		if (title != null) writer.println(title);
		
		for (ManyValuedNode node : nodeList) {
			String message = node.getId() + "\t" + node.getLabel();
			for (int i = 0; i < node.valueLength(); i++) {
				if (node.hasUsableValue(i)) message = message + "\t" + node.getValue(i);
				else message = message + "\tNA";		// write "NA" for not usable value (not available value) 
			}
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
	public void sortNodeByValue(boolean order, int valueIndex) {
		if (nodeList == null) return;
		if (hasSorted == true) return;

		Collections.sort(nodeList, new ManyValueComparator(order, valueIndex));
	}
	
	/**
	 * Display the node list in the manager for debugging
	 */
	public void display(PrintStream out) {
		for (ManyValuedNode node : nodeList) {
			out.println(node.toFullString());
		}
	}

}

class ManyValueComparator implements Comparator<ManyValuedNode> {
	private boolean order = true;
	private int valueIndex = 0;
	
	/**
	 * If order == true, we sort the node as ascending order, if order == false, we sort the node as descending order
	 */
	public ManyValueComparator(boolean order, int valueIndex) {
		this.order = order;
		this.valueIndex = valueIndex;
	}
	
	@Override
	public int compare(ManyValuedNode one, ManyValuedNode two) {
		if (order == true) return (int)Math.signum(one.values[valueIndex] - two.values[valueIndex]);
		else return (int)Math.signum(two.values[valueIndex] - one.values[valueIndex]);
	}
}

