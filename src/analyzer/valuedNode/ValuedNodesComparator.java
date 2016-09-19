package analyzer.valuedNode;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import analyzer.correlation.RankPairManager;
import analyzer.correlation.ValuePairManager;
import softwareChange.NodeChangeIndicator;

/**
 * For comparing two valued node lists (stored in ValuedNodeManager), and write some reports
 * @author Zhou Xiaocong
 * @since 2014/1/20
 * @version 1.0
 */
public class ValuedNodesComparator {
	
	private static NodeChangeIndicator indicator = null;

	/**
	 * Set an indicator for testing if two nodes can be binded to the same name definition
	 */
	public static void setNodeChangerIndicator(NodeChangeIndicator indicator) {
		ValuedNodesComparator.indicator = indicator;
	}
	
	/**
	 * Compare two valued node list by its original rank. If a node in list one binds to the same definition as a node in list two, then we write
	 * this node and its original ranks in the two list to the reports. Those nodes do not bind to the same definition will be written at the end 
	 * of the report file.  
	 * <p> We assume that the node lists stored in the managers have been sorted by descending order of their values 
	 */
	public static void compareByOriginalRank(ValuedNodeManager one, ValuedNodeManager two, PrintWriter report) {
		List<ValuedNode> oneNodeList = one.getNodeListCopy();
		List<ValuedNode> twoNodeList = two.getNodeListCopy();
		
		boolean[] twoFound = new boolean[twoNodeList.size()];
		for (int index = 0; index < twoFound.length; index++) twoFound[index] = false;
		
		List<ValuedNode> oneRemains = new ArrayList<ValuedNode>();
		
		String reportString = "Node label\tRank One\tRank Two";
		report.println(reportString);
		
		for (int oneIndex = 0; oneIndex < oneNodeList.size(); oneIndex++) {
			ValuedNode oneNode = oneNodeList.get(oneIndex);
			String oneNodeLabel = oneNode.getLabel();
			
			boolean found = false;
			for (int twoIndex = 0; twoIndex < twoNodeList.size(); twoIndex++) {
				ValuedNode twoNode = twoNodeList.get(twoIndex);
				String twoNodeLabel = twoNode.getLabel();

				System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "]");

				boolean sameNode = false;
				if (indicator != null) sameNode = indicator.canBindToSameDefinition(oneNodeLabel, twoNodeLabel);
				else sameNode = oneNode.hasBindToSameDefinition(twoNode);
				
				if (sameNode == true) {
					System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "], equal, one rank = " + oneNode.getRank() + ", two rank = " + twoNode.getRank());
					
					reportString = oneNode.getLabel() + "\t" + oneNode.getRank() + "\t" + twoNode.getRank();
					report.println(reportString);
					
					twoFound[twoIndex] = true;
					found = true;
					break;
				}
			}
			if (found == false) oneRemains.add(oneNode);
		}
		
		for (ValuedNode node : oneRemains) {
			reportString = node.getLabel() + "\t" + node.getRank() + "\t" + "0";
			report.println(reportString);
		}
		for (int index = 0; index < twoFound.length; index++) {
			if (twoFound[index] == false) {
				ValuedNode node = twoNodeList.get(index);
				reportString = node.getLabel() + "\t" + "0" + "\t" + node.getRank();
				report.println(reportString);
			}
		}
		report.flush();
	}
	
	/**
	 * Compare two valued node list by its related rank. If a node in list one binds to the same definition as a node in list two, then we compare and 
	 * write this node and its related ranks in the two list to the reports. Those nodes do not bind to the same definition will not be compared and written 
	 * to the report
	 */
	public static void compareByRelatedRank(ValuedNodeManager one, ValuedNodeManager two, PrintWriter report) {
		List<ValuedNode> oneNodeList = one.getNodeListCopy();
		List<ValuedNode> twoNodeList = two.getNodeListCopy();
		
		List<ValuedNode> oneFoundNode = new ArrayList<ValuedNode>();
		int[] twoFoundRank = new int[twoNodeList.size()];
		
		int foundIndex = 0;
		for (int oneIndex = 0; oneIndex < oneNodeList.size(); oneIndex++) {
			ValuedNode oneNode = oneNodeList.get(oneIndex);
			String oneNodeLabel = oneNode.getLabel();
			
			for (int twoIndex = 0; twoIndex < twoNodeList.size(); twoIndex++) {
				ValuedNode twoNode = twoNodeList.get(twoIndex);
				String twoNodeLabel = twoNode.getLabel();

				System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "]");

				boolean sameNode = false;
				if (indicator != null) sameNode = indicator.canBindToSameDefinition(oneNodeLabel, twoNodeLabel);
				else sameNode = oneNode.hasBindToSameDefinition(twoNode);
				
				if (sameNode == true) {
					oneFoundNode.add(oneNode);
					twoFoundRank[foundIndex] = (int)twoNode.getRank();
					foundIndex = foundIndex + 1;
					break;
				}
			}
		}
		
		int foundNodeNumber = foundIndex;			// The number of node in one list which have been binded to same definition as a node in two list 
		int nextRank = 1;							// For calculate the related rank of founded node in the two node list, begin with 1
		while (nextRank <= foundNodeNumber) {
			int scanIndex = 0;
			// Find the first rank which greater than or equal to nextRank 
			while (twoFoundRank[scanIndex] < nextRank && scanIndex < foundNodeNumber) scanIndex++;
			
			if (scanIndex >= foundNodeNumber) {
				// All related rank are less than nextRank, and so there is no node to calculate!
				// Originally, if all related rank are mutually different, scanIndex can not be less than nextRank. 
				// However, if two different nodes in the one list are corresponding to the same node in the two list, and then 
				// these two nodes may be have the same related rank!
				break;
			}

			// Find the minimal rank which greater than or equal to nextRank, and then it should be ranked to nextRank!
			int minRankIndex = scanIndex;
			scanIndex = scanIndex + 1;
			while (scanIndex < foundNodeNumber) {
				if (twoFoundRank[scanIndex] < twoFoundRank[minRankIndex] && twoFoundRank[scanIndex] >= nextRank) {
					minRankIndex = scanIndex;
				}
				scanIndex = scanIndex + 1;
			}

			if (twoFoundRank[minRankIndex] >= nextRank) {
				// Ok, this node should be ranked to nextRank!
				twoFoundRank[minRankIndex] = nextRank;
				// For find the next related rank!
				nextRank = nextRank + 1;
			} else {
				// Since nextRank <= foundNodeNumber, so we must find a rank greater than or equal to nextRank!
				throw new AssertionError("Internal error, twoFoundRank[" + minRankIndex + "] = " + twoFoundRank[minRankIndex] + " should greater than nextRank = " + nextRank + ", and foundNodeNumber = " + foundNodeNumber);
			}
		}
		
		String reportString = "Node label\tRank One\tRank Two";
		report.println(reportString);
		
		for (int oneIndex = 0; oneIndex < oneFoundNode.size(); oneIndex++) {
			int oneRank = oneIndex + 1;
			reportString = oneFoundNode.get(oneIndex).getLabel() + "\t" + oneRank + "\t" + twoFoundRank[oneIndex];
			report.println(reportString);
		}
		report.flush();
	}

	/**
	 * Compare two valued node list by its related rank and generate a rank pair manage. If a node in list one binds to the same 
	 * definition as a node in list two, then we compare and save this node and its related ranks in the two list to the rank pair 
	 * manager. Those nodes do not bind to the same definition will not be compared and saved to the manager
	 */
	public static RankPairManager generateRankPairsByRelatedRank(String id, ValuedNodeManager one, ValuedNodeManager two) {
		RankPairManager resultManager = new RankPairManager(id);
		
		List<ValuedNode> oneNodeList = one.getNodeListCopy();
		List<ValuedNode> twoNodeList = two.getNodeListCopy();
		
		List<ValuedNode> oneFoundNode = new ArrayList<ValuedNode>();
		int[] twoFoundRank = new int[twoNodeList.size()];
		
		int foundIndex = 0;
		for (int oneIndex = 0; oneIndex < oneNodeList.size(); oneIndex++) {
			ValuedNode oneNode = oneNodeList.get(oneIndex);
			String oneNodeLabel = oneNode.getLabel();
			
			for (int twoIndex = 0; twoIndex < twoNodeList.size(); twoIndex++) {
				ValuedNode twoNode = twoNodeList.get(twoIndex);
				String twoNodeLabel = twoNode.getLabel();

				System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "]");

				boolean sameNode = false;
				if (indicator != null) sameNode = indicator.canBindToSameDefinition(oneNodeLabel, twoNodeLabel);
				else sameNode = oneNode.hasBindToSameDefinition(twoNode);
				
				if (sameNode == true) {
					oneFoundNode.add(oneNode);
					twoFoundRank[foundIndex] = (int)twoNode.getRank();
					foundIndex = foundIndex + 1;
					break;
				}
			}
		}
		
		int foundNodeNumber = foundIndex;			// The number of node in one list which have been binded to same definition as a node in two list 
		int nextRank = 1;							// For calculate the related rank of founded node in the two node list, begin with 1
		while (nextRank <= foundNodeNumber) {
			int scanIndex = 0;
			// Find the first rank which greater than or equal to nextRank 
			while (twoFoundRank[scanIndex] < nextRank && scanIndex < foundNodeNumber) scanIndex++;
			
			if (scanIndex >= foundNodeNumber) {
				// All related rank are less than nextRank, and so there is no node to calculate!
				// Originally, if all related rank are mutually different, scanIndex can not be less than nextRank. 
				// However, if two different nodes in the one list are corresponding to the same node in the two list, and then 
				// these two nodes may be have the same related rank!
				break;
			}

			// Find the minimal rank which greater than or equal to nextRank, and then it should be ranked to nextRank!
			int minRankIndex = scanIndex;
			scanIndex = scanIndex + 1;
			while (scanIndex < foundNodeNumber) {
				if (twoFoundRank[scanIndex] < twoFoundRank[minRankIndex] && twoFoundRank[scanIndex] >= nextRank) {
					minRankIndex = scanIndex;
				}
				scanIndex = scanIndex + 1;
			}

			if (twoFoundRank[minRankIndex] >= nextRank) {
				// Ok, this node should be ranked to nextRank!
				twoFoundRank[minRankIndex] = nextRank;
				// For find the next related rank!
				nextRank = nextRank + 1;
			} else {
				// Since nextRank <= foundNodeNumber, so we must find a rank greater than or equal to nextRank!
				throw new AssertionError("Internal error, twoFoundRank[" + minRankIndex + "] = " + twoFoundRank[minRankIndex] + " should greater than nextRank = " + nextRank + ", and foundNodeNumber = " + foundNodeNumber);
			}
		}
		
		for (int oneIndex = 0; oneIndex < oneFoundNode.size(); oneIndex++) {
			String label = oneFoundNode.get(oneIndex).getLabel();
			int oneRank = oneIndex + 1;
			int twoRank = twoFoundRank[oneIndex];
			resultManager.addRankPair(label, oneRank, twoRank);
		}
		return resultManager;
	}

	/**
	 * Compare two valued node list by its label and generate a value pair manage. If a node in list one binds to the same 
	 * definition as a node in list two, then we compare and save this node and its value in the two list to the value pair 
	 * manager. Those nodes do not bind to the same definition will not be compared and saved to the manager
	 */
	public static ValuePairManager generateValuePairs(String id, ValuedNodeManager one, ValuedNodeManager two) {
		ValuePairManager resultManager = new ValuePairManager(id);
		
		List<ValuedNode> oneNodeList = one.getNodeListCopy();
		List<ValuedNode> twoNodeList = two.getNodeListCopy();
		
		for (int oneIndex = 0; oneIndex < oneNodeList.size(); oneIndex++) {
			ValuedNode oneNode = oneNodeList.get(oneIndex);
			String oneNodeLabel = oneNode.getLabel();
			
			for (int twoIndex = 0; twoIndex < twoNodeList.size(); twoIndex++) {
				ValuedNode twoNode = twoNodeList.get(twoIndex);
				String twoNodeLabel = twoNode.getLabel();

				System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "]");

				boolean sameNode = false;
				if (indicator != null) sameNode = indicator.canBindToSameDefinition(oneNodeLabel, twoNodeLabel);
				else sameNode = oneNode.hasBindToSameDefinition(twoNode);
				
				if (sameNode == true) {
					double oneValue = oneNode.getValue();
					double twoValue = twoNode.getValue();
					
					resultManager.addValuePair(oneNodeLabel, oneValue, twoValue);
					break;
				}
			}
		}
		
		return resultManager;
	}

	/**
	 * Compare two valued node list by its label and generate a report. If a node in list one binds to the same 
	 * definition as a node in list two, then we compare and write this node and its value in the two list to the value pair 
	 * manager. Those nodes do not bind to the same definition will not be compared and saved to the report
	 */
	public static void generateValuePairs(String title, ValuedNodeManager one, ValuedNodeManager two, PrintWriter report) {
		report.println(title);
		
		List<ValuedNode> oneNodeList = one.getNodeListCopy();
		List<ValuedNode> twoNodeList = two.getNodeListCopy();
		
		for (int oneIndex = 0; oneIndex < oneNodeList.size(); oneIndex++) {
			ValuedNode oneNode = oneNodeList.get(oneIndex);
			String oneNodeLabel = oneNode.getLabel();
			
			for (int twoIndex = 0; twoIndex < twoNodeList.size(); twoIndex++) {
				ValuedNode twoNode = twoNodeList.get(twoIndex);
				String twoNodeLabel = twoNode.getLabel();

				System.out.println("Compare one [" + oneNodeLabel + "] with two [" + twoNodeLabel + "]");

				boolean sameNode = false;
				if (indicator != null) sameNode = indicator.canBindToSameDefinition(oneNodeLabel, twoNodeLabel);
				else sameNode = oneNode.hasBindToSameDefinition(twoNode);
				
				if (sameNode == true) {
					double oneValue = oneNode.getValue();
					double twoValue = twoNode.getValue();
					
					String reportString = oneNodeLabel + "\t" + oneValue + "\t" + twoValue;
					report.println(reportString);
					break;
				}
			}
		}
	}
	
}
