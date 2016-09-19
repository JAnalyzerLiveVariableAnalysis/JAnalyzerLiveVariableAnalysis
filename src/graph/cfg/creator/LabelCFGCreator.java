package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for labeled statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class LabelCFGCreator implements StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList) {

		LabeledStatement labeledStatement = (LabeledStatement)astNode;
		// 1 Create a virtual start node for the statement, 
		ExecutionPoint startNode = ExecutionPointFactory.createVirtualStart(labeledStatement);
		currentCFG.addNode(startNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    startNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, startNode);	
		
		// 3 Create a precede node list labelPrecedeNodeList, which only contains the node startNode, since the precede node of the labeled 
		//   body is startNode
		List<PossiblePrecedeNode> labelPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		labelPrecedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		// 4 Create CFG for the body of the labeled statement, and get new precede node list labelBodyPrecedeNodeList
		Statement labelBody = labeledStatement.getBody();
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(labelBody);
		List<PossiblePrecedeNode> labelBodyPrecedeNodeList = creator.create(currentCFG, labelBody, labelPrecedeNodeList);
		
		// 5 Create a virtual end node for the statement, 
		ExecutionPoint endNode = ExecutionPointFactory.createVirtualEnd(labeledStatement);
		currentCFG.addNode(endNode);
		
		// 6 Traverse labelBodyPrecedeNodeList, for each node labelBodyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge
		//   <labelBodyPrecedeNode, endNode> to currentCFG, if it is a PPR_BREAK node with the same label, add edge <labelBodyPrecedeNode, endNode>
		//   to currentCFG, if it is a PPR_CONTINUE node with the same label, add edge <labelBodyPrecedeNode, startNode> to currentCFG, 
		//   otherwise add it to precedeNodeList in the above 1
		String statementLabel = labeledStatement.getLabel().getIdentifier();
		for (PossiblePrecedeNode labelBodyPrecedeNode : labelBodyPrecedeNodeList) {
			PossiblePrecedeReasonType reason = labelBodyPrecedeNode.getReason();
			String label = labelBodyPrecedeNode.getLabel();
			
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				currentCFG.addEdge(new CFGEdge(labelBodyPrecedeNode.getNode(), endNode, label));
			} else if (reason == PossiblePrecedeReasonType.PPR_BREAK && label != null) {
				if (label.equals(statementLabel)) currentCFG.addEdge(new CFGEdge(labelBodyPrecedeNode.getNode(), endNode, null));
			} else if (reason == PossiblePrecedeReasonType.PPR_CONTINUE && label != null)  {
				if (label.equals(statementLabel)) currentCFG.addEdge(new CFGEdge(labelBodyPrecedeNode.getNode(), startNode, null));
			} else precedeNodeList.add(labelBodyPrecedeNode);
		}

		// 7 Add endNode to the precedeNodeList
		precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
