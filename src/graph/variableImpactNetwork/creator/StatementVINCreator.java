package graph.variableImpactNetwork.creator;

import graph.variableImpactNetwork.LocalVariableImpactNode;
import graph.variableImpactNetwork.ReturnValueImpactNode;
import graph.variableImpactNetwork.VariableImpactGraph;
import graph.variableImpactNetwork.VariableImpactNode;

import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.NameReference;
import nameTable.nameScope.LocalScope;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import util.SourceCodeLocation;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/2
 * @version 1.0
 */
public class StatementVINCreator {
	private NameTableManager table = null;
	private VariableImpactGraph currentNetwork = null;
	private MethodDefinition currentMethod = null;
	private ReturnValueImpactNode currentReturnValueNode = null;
	private List<VariableDefinition> localVariableDefinitions = null;
	private CompilationUnit root = null;
	private String unitFileName = null;
	
	private List<TypeDeclaration> typeDeclarationList = null; 
	
	public StatementVINCreator(NameTableManager table) {
		this.table = table;
	}

	public void setCurrentNetwork(VariableImpactGraph network) {
		this.currentNetwork = network;
	}
	
	public void setCurrentCompilationUnit(CompilationUnit root, String unitFileName) {
		this.root = root;
		this.unitFileName = unitFileName;
	}
	
	public void setCurrentMethod(MethodDefinition definition, ReturnValueImpactNode node) {
		this.currentMethod = definition;
		this.currentReturnValueNode = node;
		this.localVariableDefinitions = null;
		this.typeDeclarationList = null;
	}
	
	public List<TypeDeclaration> getLocalTypeDeclarationList() {
		return typeDeclarationList;
	}
	
	public void findImpactsInStatement(Statement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		int type = statement.getNodeType();
		
		if (type == ASTNode.BLOCK) {
			Block block = (Block)statement;
			findImpactsInStatement(block, conditionExpressionNodeList);
		} else if (type == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			Expression expression = expressionStatement.getExpression();
			findImpactsInExpression(expression, conditionExpressionNodeList);
		} else if (type == ASTNode.DO_STATEMENT) {
			DoStatement doStatement = (DoStatement)statement;
			findImpactsInStatement(doStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.ENHANCED_FOR_STATEMENT) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			findImpactsInStatement(enhancedForStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.FOR_STATEMENT) {
			ForStatement forStatement = (ForStatement)statement;
			findImpactsInStatement(forStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.WHILE_STATEMENT) {
			WhileStatement whileStatement = (WhileStatement)statement;
			findImpactsInStatement(whileStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.IF_STATEMENT) {
			IfStatement ifStatement = (IfStatement)statement;
			findImpactsInStatement(ifStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.LABELED_STATEMENT) {
			LabeledStatement labeledStatement = (LabeledStatement)statement;
			findImpactsInStatement(labeledStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.RETURN_STATEMENT) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
			findImpactsInStatement(returnStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.SWITCH_STATEMENT) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			findImpactsInStatement(switchStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.SYNCHRONIZED_STATEMENT) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			findImpactsInStatement(synchronizedStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.TRY_STATEMENT) {
			TryStatement tryStatement = (TryStatement)statement;
			findImpactsInStatement(tryStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
			findImpactsInStatement(variableDeclarationStatement, conditionExpressionNodeList);
		} else if (type == ASTNode.TYPE_DECLARATION_STATEMENT) {
			TypeDeclarationStatement typeStatement = (TypeDeclarationStatement)statement;
			AbstractTypeDeclaration abstractTypeDeclaration = typeStatement.getDeclaration();
			if (abstractTypeDeclaration.isLocalTypeDeclaration()) {
				TypeDeclaration typeDeclaration = (TypeDeclaration)abstractTypeDeclaration;
				if (typeDeclarationList == null) typeDeclarationList = new ArrayList<TypeDeclaration>();
				typeDeclarationList.add(typeDeclaration);
			}
//		} else if (type == ASTNode.EMPTY_STATEMENT) {
//			// Do nothing!
//		} else if (type == ASTNode.ASSERT_STATEMENT) {
//			// Do nothing!
//		} else if (type == ASTNode.CONSTRUCTOR_INVOCATION) {
//			// Do nothing!
//		} else if (type == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
//			// Do nothing!
		} 
		
	}
	
	public void findImpactsInStatement(Block statement, List<VariableImpactNode> conditionExpressionNodeList) {
		// Get the statements in the block. The element type of the list returned by Block.statements() should be Statement
		@SuppressWarnings("unchecked")
		List<Statement> statements = statement.statements();
		if (statements == null) return;
		
		// Find impacts for the statements in the block
		for (Statement statementInBlock : statements) {
			findImpactsInStatement(statementInBlock, conditionExpressionNodeList);
		}
	}
	
	public void findImpactsInStatement(DoStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement loopBody = statement.getBody();
		if (loopBody != null) {
			List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
			
			findImpactsInStatement(loopBody, newConditionExpressionNodeList);
		}
	}
	
	public void findImpactsInStatement(EnhancedForStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement loopBody = statement.getBody();
		if (loopBody != null) {
			List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
			
			findImpactsInStatement(loopBody, newConditionExpressionNodeList);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void findImpactsInStatement(ForStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		List<Expression> initExpressionList = statement.initializers();
		if (initExpressionList != null) {
			for (Expression expression : initExpressionList) findImpactsInExpression(expression, conditionExpressionNodeList);
		}
		
		Statement loopBody = statement.getBody();
		List<Expression> updateExpressionList = statement.updaters();
		List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
		boolean hasUpdater = false;
		if (updateExpressionList != null) {
			if (updateExpressionList.size() > 0) hasUpdater = true;
		}
		if (loopBody != null || hasUpdater == true) {
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
		}
		if (loopBody != null) {
			findImpactsInStatement(loopBody, newConditionExpressionNodeList);
		}
		if (hasUpdater == true) {
			for (Expression expression : updateExpressionList) findImpactsInExpression(expression, newConditionExpressionNodeList);
		}
	}

	public void findImpactsInStatement(WhileStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement loopBody = statement.getBody();
		if (loopBody != null) {
			List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
			
			findImpactsInStatement(loopBody, newConditionExpressionNodeList);
		}
	}

	public void findImpactsInStatement(IfStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement thenStatement = statement.getThenStatement();
		Statement elseStatement = statement.getElseStatement();

		List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
		if (thenStatement != null || elseStatement != null) {
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
		}

		if (thenStatement != null) {
			findImpactsInStatement(thenStatement, newConditionExpressionNodeList);
		}
		if (elseStatement != null) {
			findImpactsInStatement(elseStatement, newConditionExpressionNodeList);
		}
	}

	public void findImpactsInStatement(LabeledStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement labeledStatement = statement.getBody();
		findImpactsInStatement(labeledStatement, conditionExpressionNodeList);
	}

	public void findImpactsInStatement(ReturnStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		for (VariableImpactNode startNode : conditionExpressionNodeList) {
			currentNetwork.checkAndAddControlImpact(startNode, currentReturnValueNode);
		}

		Expression expression = statement.getExpression();
		if (expression == null) return;
		NameReference reference = table.createReferenceForASTNode(expression, root, unitFileName);
		if (reference != null) {
			reference.resolveBinding();
			List<VariableImpactNode> nodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
			for (VariableImpactNode node : nodeList) {
				currentNetwork.checkAndAddAssignImpact(node, currentReturnValueNode);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void findImpactsInStatement(SwitchStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		List<Statement> statementList = statement.statements();
		if (statementList != null) {
			if (statementList.size() > 0) {
				List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
				Expression conditionExpression = statement.getExpression();
				if (conditionExpression != null) {
					NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
					if (reference != null) {
						reference.resolveBinding();
						newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
						newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
					}
				}
				
				for (Statement statementInSwitch : statementList) {
					findImpactsInStatement(statementInSwitch, newConditionExpressionNodeList);
				}
			}
		}
	}

	public void findImpactsInStatement(SynchronizedStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement loopBody = statement.getBody();
		if (loopBody != null) {
			List<VariableImpactNode> newConditionExpressionNodeList = conditionExpressionNodeList;
			Expression conditionExpression = statement.getExpression();
			if (conditionExpression != null) {
				NameReference reference = table.createReferenceForASTNode(conditionExpression, root, unitFileName);
				if (reference != null) {
					reference.resolveBinding();
					newConditionExpressionNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, false);
					newConditionExpressionNodeList.addAll(conditionExpressionNodeList);
				}
			}
			
			findImpactsInStatement(loopBody, newConditionExpressionNodeList);
		}
	}

	@SuppressWarnings("unchecked")
	public void findImpactsInStatement(TryStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		Statement tryBody = statement.getBody();
		if (tryBody != null) findImpactsInStatement(tryBody, conditionExpressionNodeList);
		List<CatchClause> catchList = statement.catchClauses();
		if (catchList != null) {
			for (CatchClause clause : catchList) {
				Statement clauseBody = clause.getBody();
				findImpactsInStatement(clauseBody, conditionExpressionNodeList);
			}
		}
		Statement finallyBody = statement.getFinally();
		if (finallyBody != null) findImpactsInStatement(finallyBody, conditionExpressionNodeList);
	}
	
	@SuppressWarnings("unchecked")
	public void findImpactsInStatement(VariableDeclarationStatement statement, List<VariableImpactNode> conditionExpressionNodeList) {
		if (localVariableDefinitions == null) {
			LocalScope bodyScope = currentMethod.getBodyScope();
			localVariableDefinitions = bodyScope.getAllLocalVaraibleDefinitions();
		}
		
		List<VariableDeclarationFragment> fragments = statement.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(varNode, root, unitFileName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(varNode, root, unitFileName);
			
			// Find the field definition corresponding the field declaration
			String varName = varNode.getName().getIdentifier();
			VariableDefinition variableDefinition = null;
			for (VariableDefinition variableInBody : localVariableDefinitions) {
				if (variableInBody.getSimpleName().equals(varName) && variableInBody.getLocation().isBetween(start, end)) {
					variableDefinition = variableInBody;
					break;
				}
			}
			if (variableDefinition == null) throw new AssertionError("Internal error, can not find definition for variable declaration: " + varNode.toString());
			LocalVariableImpactNode vNode = currentNetwork.createAndAddNodeForDefinition(variableDefinition);
			for (VariableImpactNode startNode : conditionExpressionNodeList) {
				currentNetwork.checkAndAddControlImpact(startNode, vNode);
			}
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				NameReference initExpRef = table.createReferenceForASTNode(initializer, root, unitFileName);
				if (initExpRef != null) {
					initExpRef.resolveBinding();
					List<VariableImpactNode> startNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, initExpRef, false);
					for (VariableImpactNode startNode : startNodeList) {
						currentNetwork.checkAndAddAssignImpact(startNode, vNode);
					}
				}
			}
		}
	}

	public void findImpactsInExpression(Expression expression, List<VariableImpactNode> conditionExpressionNodeList) {
		if (expression.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
			VariableDeclarationExpression variableDeclarations = (VariableDeclarationExpression)expression;
			findImpactsInExpression(variableDeclarations, conditionExpressionNodeList);
		} else {
			NameReference reference = table.createReferenceForASTNode(expression, root, unitFileName);
			if (reference != null) {
				reference.resolveBinding();
				List<VariableImpactNode> nodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, reference, true);
				for (VariableImpactNode endNode : nodeList) {
					for (VariableImpactNode startNode : conditionExpressionNodeList) {
						currentNetwork.checkAndAddControlImpact(startNode, endNode);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void findImpactsInExpression(VariableDeclarationExpression variableDeclarations, List<VariableImpactNode> conditionExpressionNodeList) {
		if (localVariableDefinitions == null) {
			LocalScope bodyScope = currentMethod.getBodyScope();
			localVariableDefinitions = bodyScope.getAllLocalVaraibleDefinitions();
		}
		
		List<VariableDeclarationFragment> fragments = variableDeclarations.fragments();
		for (VariableDeclarationFragment varNode : fragments) {
			SourceCodeLocation start = SourceCodeLocation.getStartLocation(varNode, root, unitFileName);
			SourceCodeLocation end = SourceCodeLocation.getEndLocation(varNode, root, unitFileName);
			
			// Find the field definition corresponding the field declaration
			String varName = varNode.getName().getIdentifier();
			VariableDefinition variableDefinition = null;
			for (VariableDefinition variableInBody : localVariableDefinitions) {
				if (variableInBody.getSimpleName().equals(varName) && variableInBody.getLocation().isBetween(start, end)) {
					variableDefinition = variableInBody;
					break;
				}
			}
			if (variableDefinition == null) throw new AssertionError("Internal error, can not find definition for variable declaration: " + varNode.toString());
			LocalVariableImpactNode vNode = currentNetwork.createAndAddNodeForDefinition(variableDefinition);
			for (VariableImpactNode startNode : conditionExpressionNodeList) {
				currentNetwork.checkAndAddControlImpact(startNode, vNode);
			}
			
			// Visit the initializer in the variable declaration
			Expression initializer = varNode.getInitializer();
			if (initializer != null) {
				NameReference initExpRef = table.createReferenceForASTNode(initializer, root, unitFileName);
				if (initExpRef != null) {
					initExpRef.resolveBinding();
					List<VariableImpactNode> startNodeList = ReferenceVINCreator.findImpactsInExpressionReference(currentNetwork, initExpRef, false);
					for (VariableImpactNode startNode : startNodeList) {
						currentNetwork.checkAndAddAssignImpact(startNode, vNode);
					}
				}
			}
		}
	}
	
}
