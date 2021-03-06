package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Create CFG for while statement
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 *
 */
public class EnhancedForCFGCreator extends StatementCFGCreator {

	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList, String nodeLabel) {

		ExecutionPointFactory factory = currentCFG.getExecutionPointFactory();
		// 1 Create a conditionNode for the parameter:expression of the enhanced for statement (it is also the entry of the while statement), and add 
		//   it to the currentCFG, then call StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNodetraverse() to to use conditionNode to 
		//   substitute PPR_SEQUENCE nodes in precedeNodeList, and add suitable edges to currentCFG, and get new precedeNodeList
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement)astNode;
		ExecutionPoint enhancedForExpNode = factory.createPredicate(enhancedForStatement);
		currentCFG.addNode(enhancedForExpNode);
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, enhancedForExpNode);
		
		// 2 Create a precede node list loopPrecedeNodeList, which only contains the node conditionNode, since the precede node of the loop body is conditionNode
		List<PossiblePrecedeNode> loopPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		loopPrecedeNodeList.add(new PossiblePrecedeNode(enhancedForExpNode, PossiblePrecedeReasonType.PPR_SEQUENCE, CFGEdge.LABEL_TRUE));
		
		// 3 Create CFG for the body of the loop statement, and get new precede node list loopBodyPrecedeNode
		List<PossiblePrecedeNode> loopBodyPrecedeNodeList;
		Statement loopBody = enhancedForStatement.getBody();
		if (loopBody != null) {
			StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(loopBody);
			loopBodyPrecedeNodeList = creator.create(currentCFG, loopBody, loopPrecedeNodeList, null);
		} else {
			// if the body of the loop is null, then the precede node of the succeed node should still be conditionNode
			loopBodyPrecedeNodeList = loopPrecedeNodeList;		
		}
		
		
		// 4 Create a virtual end node endNode for the while statement, add it to currentCFG, and add edge <conditionNode, endNode> to currentCFG
		ExecutionPoint endNode = factory.createVirtualEnd(enhancedForStatement);
		currentCFG.addNode(endNode);
		currentCFG.addEdge(new CFGEdge(enhancedForExpNode, endNode, CFGEdge.LABEL_FALSE));
		
		// 5 Traverse loopBodyPrecedeNodeList, for each node loopBodyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge
		//   <loopBodyPrecedeNode, conditionNode> to currentCFG, if it is a PPR_BREAK node without label, add edge <loopBodyPrecedeNode, endNode>
		//   to currentCFG, if it is a PPR_CONTINUE node without label, add edge <loopBodyPrecedeNode, conditionNode> to currentCFG, 
		//   otherwise add it to precedeNodeList in the above 1
		for (PossiblePrecedeNode loopBodyPrecedeNode : loopBodyPrecedeNodeList) {
			PossiblePrecedeReasonType reason = loopBodyPrecedeNode.getReason();
			String label = loopBodyPrecedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), enhancedForExpNode, label));
			} else if (reason == PossiblePrecedeReasonType.PPR_BREAK && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), endNode, null)); 
			} else if (reason == PossiblePrecedeReasonType.PPR_CONTINUE  && StatementCFGCreatorHelper.needAddEdgeByMatchLabel(label, nodeLabel)) {
				currentCFG.addEdge(new CFGEdge(loopBodyPrecedeNode.getNode(), enhancedForExpNode, null));
			} else precedeNodeList.add(loopBodyPrecedeNode);
		}

		// 6 Add endNode to the precedeNodeList
		precedeNodeList.add(new PossiblePrecedeNode(endNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		return precedeNodeList;
	}

}
