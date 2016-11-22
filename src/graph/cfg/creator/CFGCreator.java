package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import sourceCodeAST.SourceCodeLocation;

/**
 * Create CFG for java source code file
 * @author Zhou Xiaocong
 * @since 2012/12/29
 * @version 1.0
 * 
 * @update 2013/12/30 Zhou Xiaocong
 * 		Add a method ControlFlowGraph create(MethodDeclaration methodDec, String className) for create CFG for a given method!
 * 		Add a method ASTNode matchASTNode(ExecutionPoint point) to match the AST node for an execution point.
 * 			Add a class MatchExecutionPointASTVisitor to support matching AST node for an execution point.
 *
 */
public class CFGCreator {
	private String sourceFileName = null;
	private CompilationUnit astRoot = null;

	public CFGCreator(String sourceFileName, CompilationUnit astRoot) {
		this.sourceFileName = sourceFileName;
		this.astRoot = astRoot;
	}

	/**
	 * <p>After give the AST root and the file name of the compilation unit, create CFG for the file.
	 * Note that the source file name is used to generate the source code location objects for CFG nodes, and
	 * the AST root node is used to find the row and column number of the AST node corresponding to a CFG node.
	 * 
	 * <p>Moreover, we only create CFG for the methods defined in those class declared in the root of the package, 
	 * that is, we do not create CFG for the methods defined in those member classes, local classes, interfaces, and
	 * anonymous classes 
	 */
	@SuppressWarnings("unchecked")
	public List<ControlFlowGraph> create() {
		if (astRoot == null || sourceFileName == null) return null;
		
		// Set ExecutionPointFactory's sourceFileName and root node
		ExecutionPointFactory.setCompilationUnit(sourceFileName, astRoot);
		List<ControlFlowGraph> resultCFGList = new ArrayList<ControlFlowGraph>();
		
		List<AbstractTypeDeclaration> typeList = astRoot.types();
		for (AbstractTypeDeclaration type : typeList) {
			if (type.getNodeType() != ASTNode.TYPE_DECLARATION) continue;
			
			TypeDeclaration classDeclaration = (TypeDeclaration)type;
			if (classDeclaration.isInterface()) continue;
			if (!classDeclaration.isPackageMemberTypeDeclaration()) continue;
			
			String className = classDeclaration.getName().getIdentifier();
			// We only create CFG for the methods defined in those class declared in the root of the package!
			MethodDeclaration[] methodDeclarationArray = classDeclaration.getMethods();
			for (MethodDeclaration methodDeclaration : methodDeclarationArray) {
				String methodName = methodDeclaration.getName().getIdentifier();
				String id = className + "." + methodName + "@" + SourceCodeLocation.getStartLocation(methodDeclaration, astRoot, sourceFileName);
				String label = className + "." + methodName;
				String description = label;

				Block methodBody = methodDeclaration.getBody();
				if (methodBody == null) {
					// This method is an abstract method, we can not create CFG for it
					continue;
				}
				
				// Create a ControFlowGraph object
				ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
				currentCFG.setSourceFile(sourceFileName, astRoot);
				currentCFG.setMethod(className, methodName, methodDeclaration);
				
				// Create the start node for the CFG of the method
				ExecutionPoint startNode = ExecutionPointFactory.createStart(methodDeclaration);
				currentCFG.setAndAddStartNode(startNode);
				
				// Create a precede node list precedeNodeList, which only contains the node startNode
				List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
				precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

				// Create CFG for the body of the method and get new precedeNodeList
				StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(methodBody);
				precedeNodeList = creator.create(currentCFG, methodBody, precedeNodeList);
				
				// Create end and abnormal end node for the entire method
				ExecutionPoint endNode = ExecutionPointFactory.createEnd(methodDeclaration);
				currentCFG.setAndAddEndNode(endNode);
				
				ExecutionPoint abnormalEndNode = null;
				// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
				// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
				for (PossiblePrecedeNode precedeNode : precedeNodeList) {
					PossiblePrecedeReasonType reason = precedeNode.getReason();
					String edgeLabel = precedeNode.getLabel();
					if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
						currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
					} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
						if (abnormalEndNode == null) {
							abnormalEndNode = ExecutionPointFactory.createAbnormalEnd(methodDeclaration);
							currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
						}
						currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
					} else {
						assert false : "After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList";
					} 
				}
				
				// Add the currentCFG to the resultCFGList
				resultCFGList.add(currentCFG);
			}
		}
		return resultCFGList;
	}
	
	/**
	 * create CFG for a given method! Note that the caller should give the name (i.e. the parameter className) of the class defined the method!
	 */
	public ControlFlowGraph create(MethodDeclaration methodDeclaration, String className) {
		// Set ExecutionPointFactory's sourceFileName and root node
		ExecutionPointFactory.setCompilationUnit(sourceFileName, astRoot);

		String methodName = methodDeclaration.getName().getIdentifier();
		String id = className + "." + methodName + "@" + SourceCodeLocation.getStartLocation(methodDeclaration, astRoot, sourceFileName);
		String label = className + "." + methodName;
		String description = label;

		Block methodBody = methodDeclaration.getBody();
		if (methodBody == null) return null;
		
		// Create a ControFlowGraph object
		ControlFlowGraph currentCFG = new ControlFlowGraph(id, label, description);
		currentCFG.setSourceFile(sourceFileName, astRoot);
		currentCFG.setMethod(className, methodName, methodDeclaration);
		
		// Create the start node for the CFG of the method
		ExecutionPoint startNode = ExecutionPointFactory.createStart(methodDeclaration);
		currentCFG.setAndAddStartNode(startNode);
		
		// Create a precede node list precedeNodeList, which only contains the node startNode
		List<PossiblePrecedeNode> precedeNodeList = new LinkedList<PossiblePrecedeNode>();
		precedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));

		// Create CFG for the body of the method and get new precedeNodeList
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(methodBody);
		precedeNodeList = creator.create(currentCFG, methodBody, precedeNodeList);
		
		// Create end and abnormal end node for the entire method
		ExecutionPoint endNode = ExecutionPointFactory.createEnd(methodDeclaration);
		currentCFG.setAndAddEndNode(endNode);
		
		ExecutionPoint abnormalEndNode = null;
		// Traverse precedeNodeList, for each precedeNode in the list, if it is a PPR_RETURN or PPR_SEQUENCE, add edge <precedeNode, endNode>,
		// if it is a PPR_THROW, create abnormalEndNode, and add edge <precedeNode, abnormalEndNode>
		for (PossiblePrecedeNode precedeNode : precedeNodeList) {
			PossiblePrecedeReasonType reason = precedeNode.getReason();
			String edgeLabel = precedeNode.getLabel();
			if (reason == PossiblePrecedeReasonType.PPR_SEQUENCE || reason == PossiblePrecedeReasonType.PPR_RETURN) {
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), endNode, edgeLabel));
			} else if (reason == PossiblePrecedeReasonType.PPR_THROW) {
				if (abnormalEndNode == null) {
					abnormalEndNode = ExecutionPointFactory.createAbnormalEnd(methodDeclaration);
					currentCFG.setAndAddAbnormalEndNode(abnormalEndNode);
				}
				currentCFG.addEdge(new CFGEdge(precedeNode.getNode(), abnormalEndNode, edgeLabel));
			} else {
				throw new AssertionError("After create CFG for the entire method, there are unexpected precede nodes in the precedeNodeList");
			} 
		}
		
		return currentCFG;
	}
	
	/**
	 * Match an AST node for a given execution point without using the AST node stored in the point, since we may restore a CFG from 
	 * a file which can not save the AST node of the execution points in the future, and then we may need match the execution point with
	 * an abstract syntax tree build by the same source file.  
	 */
	public ASTNode matchASTNode(ExecutionPoint point) {
		SourceCodeLocation start = point.getStartLocation();
		SourceCodeLocation end = point.getEndLocation();
		
		int position = astRoot.getPosition(start.getLineNumber(), start.getColumn());
		int length = astRoot.getPosition(end.getLineNumber(), end.getColumn()) - position;
		
		MatchExecutionPointASTVisitor visitor = new MatchExecutionPointASTVisitor(position, length);
		astRoot.accept(visitor);
		ASTNode result = visitor.getMatchedNode();
		
		return result;
	}
}

class MatchExecutionPointASTVisitor extends ASTVisitor {
	private int nodePosition = 0;
	private int nodeLength = 0;
	
	private ASTNode matchedNode = null;
	
	public MatchExecutionPointASTVisitor(int position, int length) {
		nodePosition = position;
		nodeLength = length;
	}
	
	public ASTNode getMatchedNode() {
		return matchedNode;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		if (matchedNode != null) return false;
		
		int position = node.getStartPosition();
		int length = node.getLength();
		
		// So far, we match the AST node according to its position and its length!!!
		if (position == nodePosition && length == nodeLength) matchedNode = node;
		
		if (matchedNode == null) return true;		// Do not match the AST node, we need to visit its children.
		else return false;							// We have matched the AST node, we DO NOT need to visit its children.
	}
	
}
