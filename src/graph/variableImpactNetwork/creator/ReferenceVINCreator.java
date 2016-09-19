package graph.variableImpactNetwork.creator;

import graph.variableImpactNetwork.ArrayImpactNode;
import graph.variableImpactNetwork.FieldImpactNode;
import graph.variableImpactNetwork.LocalVariableImpactNode;
import graph.variableImpactNetwork.ObjectImpactNode;
import graph.variableImpactNetwork.ReturnValueImpactNode;
import graph.variableImpactNetwork.VariableImpactGraph;
import graph.variableImpactNetwork.VariableImpactNode;

import java.util.ArrayList;
import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.MethodReference;
import nameTable.nameReference.NameReference;
import nameTable.nameReference.NameReferenceKind;
import nameTable.nameReference.referenceGroup.NRGArrayAccess;
import nameTable.nameReference.referenceGroup.NRGArrayCreation;
import nameTable.nameReference.referenceGroup.NRGAssignment;
import nameTable.nameReference.referenceGroup.NRGClassInstanceCreation;
import nameTable.nameReference.referenceGroup.NRGMethodInvocation;
import nameTable.nameReference.referenceGroup.NameReferenceGroup;
import nameTable.nameReference.referenceGroup.NameReferenceGroupKind;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/1
 * @version 1.0
 */
public class ReferenceVINCreator {

	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NameReference reference, boolean usingAsEndNode) {
		if (!reference.isGroupReference()) {
			List<VariableImpactNode> nodeList = new ArrayList<VariableImpactNode>();
			VariableImpactNode node = findNodeInSingleReference(currentNetwork, reference, usingAsEndNode);
			if (node != null) nodeList.add(node);
			return nodeList;
		}
		
		NameReferenceGroup referenceGroup = (NameReferenceGroup)reference;
		NameReferenceGroupKind groupKind = referenceGroup.getGroupKind();
		
		List<VariableImpactNode> resultNodeList = null;
		if (groupKind == NameReferenceGroupKind.NRGK_ARRAY_ACCESS) {
			NRGArrayAccess tempReference = (NRGArrayAccess)referenceGroup;
			return findImpactsInExpressionReference(currentNetwork, tempReference, usingAsEndNode);
		} else if (groupKind == NameReferenceGroupKind.NRGK_ARRAY_CREATION) {
			NRGArrayCreation tempReference = (NRGArrayCreation)referenceGroup;
			return findImpactsInExpressionReference(currentNetwork, tempReference, usingAsEndNode);
		} else if (groupKind == NameReferenceGroupKind.NRGK_ASSIGNMENT) {
			NRGAssignment tempReference = (NRGAssignment)referenceGroup;
			return findImpactsInExpressionReference(currentNetwork, tempReference, usingAsEndNode);
		} else if (groupKind == NameReferenceGroupKind.NRGK_CLASS_INSTANCE_CREATION) {
			NRGClassInstanceCreation tempReference = (NRGClassInstanceCreation)referenceGroup;
			return findImpactsInExpressionReference(currentNetwork, tempReference, usingAsEndNode);
		} else if (groupKind == NameReferenceGroupKind.NRGK_METHOD_INVOCATION) {
			NRGMethodInvocation tempReference = (NRGMethodInvocation)referenceGroup;
			return findImpactsInExpressionReference(currentNetwork, tempReference, usingAsEndNode);
		} else {
			resultNodeList = new ArrayList<VariableImpactNode>();
			List<NameReference> subreferenceList = referenceGroup.getSubReference();
			if (subreferenceList != null) {
				for (NameReference subreference : subreferenceList) {
					List<VariableImpactNode> subNodeList = findImpactsInExpressionReference(currentNetwork, subreference, usingAsEndNode);
					resultNodeList.addAll(subNodeList);
				}
			}
			return resultNodeList;
		}
	}
	
	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NRGArrayAccess reference, boolean usingAsEndNode) {
		List<NameReference> subreferenceList = reference.getSubReference();
		NameReference firstReference = subreferenceList.get(0);
		List<VariableImpactNode> firstNodeList = findImpactsInExpressionReference(currentNetwork, firstReference, usingAsEndNode);
		if (usingAsEndNode == true) return firstNodeList;
		
		NameReference secondReference = subreferenceList.get(1);
		List<VariableImpactNode> secondNodeList = findImpactsInExpressionReference(currentNetwork, secondReference, usingAsEndNode);
		firstNodeList.addAll(secondNodeList);
		return firstNodeList;
	}
	
	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NRGArrayCreation reference, boolean usingAsEndNode) {
		ArrayImpactNode node = currentNetwork.createAndAddNodeForReference(reference);

		List<VariableImpactNode> resultNodeList = new ArrayList<VariableImpactNode>();
		if (usingAsEndNode == true) return resultNodeList;
		resultNodeList.add(node);
		
		List<NameReference> subreferenceList = reference.getSubReference();
		for (int index = 1; index < subreferenceList.size(); index++) {
			NameReference subreference = subreferenceList.get(index);
			List<VariableImpactNode> subNodeList = findImpactsInExpressionReference(currentNetwork, subreference, false);
			resultNodeList.addAll(subNodeList);
		}
		return resultNodeList;
	}
	
	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NRGAssignment reference, boolean usingAsEndNode) {
		List<NameReference> subreferenceList = reference.getSubReference();
		NameReference firstReference = subreferenceList.get(0);
		List<VariableImpactNode> firstNodeList = findImpactsInExpressionReference(currentNetwork, firstReference, true);
		
		NameReference secondReference = subreferenceList.get(1);
		List<VariableImpactNode> secondNodeList = findImpactsInExpressionReference(currentNetwork, secondReference, false);
		
		for (VariableImpactNode endNode : firstNodeList) {
			for (VariableImpactNode startNode : secondNodeList) {
				currentNetwork.checkAndAddAssignImpact(startNode, endNode);
			}
		}

		if (usingAsEndNode == false) firstNodeList.addAll(secondNodeList);
		return firstNodeList;
	}

	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NRGClassInstanceCreation reference, boolean usingAsEndNode) {
		ObjectImpactNode node = currentNetwork.createAndAddNodeForReference(reference);
		if (reference.isResolved()) {
			NameDefinition definition = reference.getDefinition();
			if (definition.isDetailedType()) {
				DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
				List<FieldDefinition> fieldList = type.getAllFieldList();
				if (fieldList != null) {
					for (FieldDefinition field : fieldList) {
						FieldImpactNode fNode = currentNetwork.createAndAddNodeForDefinition(type, field);
						currentNetwork.createAndAddComponentImpact(fNode, node);
					}
				}
			}
		}

		List<VariableImpactNode> resultNodeList = new ArrayList<VariableImpactNode>();
		if (usingAsEndNode == true) return resultNodeList;
		
		resultNodeList.add(node);
		
		List<NameReference> subreferenceList = reference.getSubReference();
		for (int index = 0; index < subreferenceList.size(); index++) {
			NameReference subreference = subreferenceList.get(index);
			List<VariableImpactNode> subNodeList = findImpactsInExpressionReference(currentNetwork, subreference, false);
			resultNodeList.addAll(subNodeList);
		}
		return resultNodeList;
	}
	
	protected static List<VariableImpactNode> findImpactsInExpressionReference(VariableImpactGraph currentNetwork, NRGMethodInvocation reference, boolean usingAsEndNode) {
		List<VariableImpactNode> resultNodeList = new ArrayList<VariableImpactNode>();
		
		List<NameReference> subreferenceList = reference.getSubReference();
		NameReference firstReference = subreferenceList.get(0);
		MethodReference methodReference = null;
		if (firstReference.getReferenceKind() != NameReferenceKind.NRK_METHOD) {
			List<VariableImpactNode> firstNodeList = findImpactsInExpressionReference(currentNetwork, firstReference, usingAsEndNode);
			resultNodeList.addAll(firstNodeList);
			methodReference = (MethodReference)subreferenceList.get(1);
		} else methodReference = (MethodReference)firstReference;
		
		List<NameReference> argumentList = methodReference.getArguments();
		Object[] argumentNodeListArray = null;
		boolean hasArguments = false;
		if (argumentList != null) {
			if (argumentList.size() > 0) hasArguments = true;
		}
		if (hasArguments) {
			argumentNodeListArray = new Object[argumentList.size()];
			
			for (int index = 0; index < argumentList.size(); index++) {
				NameReference argumentReference = argumentList.get(index);
				List<VariableImpactNode> nodeList = findImpactsInExpressionReference(currentNetwork, argumentReference, false);
				argumentNodeListArray[index] = nodeList;
			}

			List<MethodDefinition> methodDefinitionList = methodReference.getAlternatives();
			if (methodDefinitionList != null) {
				for (MethodDefinition methodDefinition : methodDefinitionList) {
					List<VariableDefinition> parameterList = methodDefinition.getParameters();
					for (int index = 0; index < parameterList.size(); index++) {
						VariableDefinition parameter = parameterList.get(index);
						LocalVariableImpactNode variableNode = currentNetwork.createAndAddNodeForDefinition(parameter);
						@SuppressWarnings("unchecked")
						List<VariableImpactNode> nodeList = (List<VariableImpactNode>)argumentNodeListArray[index];
						for (VariableImpactNode startNode : nodeList) {
							currentNetwork.checkAndAddAssignImpact(startNode, variableNode);
						}
					}
				}
			}
		}
		
		List<MethodDefinition> methodDefinitionList = methodReference.getAlternatives();
		if (methodDefinitionList != null) {
			for (MethodDefinition methodDefinition : methodDefinitionList) {
				ReturnValueImpactNode rNode = currentNetwork.createAndAddNodeForDefinition(methodDefinition);
				resultNodeList.add(rNode);
			}
		}
		
		if (usingAsEndNode == true) return resultNodeList;
		
		if (hasArguments) {
			for (int index = 0; index < argumentNodeListArray.length; index++) {
				@SuppressWarnings("unchecked")
				List<VariableImpactNode> nodeList = (List<VariableImpactNode>)argumentNodeListArray[index];
				resultNodeList.addAll(nodeList);
			}
		}
		return resultNodeList;
	}
	
	/**
	 * Find  node in a single reference (i.e. it is not a reference group). If the reference bind to 
	 * <p>(1) a method definition, we return an instance of ReturnValueImpactNode corresponding to its return value;
	 * <p>(2) a field definition, we return an instance of FieldImpactNode corresponding to this field;
	 * <p>(3) a variable definition, we return an instance of LocalVariableImpactNode corresponding to this variable (or parameter)
	 * <p>(4) otherwise (including the reference can not be resolved), we return null!  
	 * @param currentNetwork : we find the node in this network. If the reference bind to a method definition, a field definition or a variable 
	 * definition, we find the corresponding node in this network. When we can not find the corresponding node, we create a node, and add it to 
	 * the network! 
	 * @param reference : the given reference. It must not be a reference group!
	 * @return If the reference corresponding to a node in the network, we return the node, else return null
	 */
	protected static VariableImpactNode findNodeInSingleReference(VariableImpactGraph currentNetwork, NameReference reference, boolean usingAsEndNode) {
		if (!reference.isResolved()) return null;
		if (usingAsEndNode == true && !reference.isLeftValue()) return null;
		
		NameDefinition definition = reference.getDefinition();
		if (definition.getDefinitionKind() == NameDefinitionKind.NDK_FIELD) {
			FieldDefinition fieldDefinition = (FieldDefinition)definition;
			DetailedTypeDefinition type = (DetailedTypeDefinition)fieldDefinition.getScope();
			FieldImpactNode fNode = currentNetwork.createAndAddNodeForDefinition(type, fieldDefinition);
			return fNode;
		} else if (definition.getDefinitionKind() == NameDefinitionKind.NDK_METHOD) {
			MethodDefinition methodDefinition = (MethodDefinition)definition;
			ReturnValueImpactNode rNode = currentNetwork.createAndAddNodeForDefinition(methodDefinition);
			return rNode;
		} else if (definition.getDefinitionKind() == NameDefinitionKind.NDK_PARAMETER || definition.getDefinitionKind() == NameDefinitionKind.NDK_VARIABLE) {
			VariableDefinition variableDefinition = (VariableDefinition)definition;
			LocalVariableImpactNode vNode = currentNetwork.createAndAddNodeForDefinition(variableDefinition);
			return vNode;
		}
		return null;
	}
	
}
