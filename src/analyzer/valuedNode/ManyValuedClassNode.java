package analyzer.valuedNode;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import softwareChange.NameTableComparator;
import util.SourceCodeLocation;

/**
 * A node corresponding to a detailed type definition (i.e. a class definition), and many values (authority, hub, or other 
 * value, such as the measures of the class) attached to the node. 
 * @author Zhou Xiaocong
 * @since 2015/9/18
 * @version 1.0
 */
public class ManyValuedClassNode extends ManyValuedNode {
	/**
	 * Create a valued class node. In general, the id of the node are unique number string, or the full qualified 
	 * name of a detailed type definition; and the label of the node are as "simpleName@location", where simpleName 
	 * is the simple name of a detailed type definition, and location is the full string of the start location of 
	 * the detailed type definition. 
	 */
	public ManyValuedClassNode(String id, String label, int valueLength) {
		super(id, label, valueLength);
	}
	
	@Override
	public NameDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(DetailedTypeDefinition definition) {
		this.definition = definition;
	}
	
	/**
	 * Bind the node to a detailed type definition in the given table. We extract the class name and location string
	 * from the label of the node, and then use the name and location to find the definition in the table  
	 */
   	public void bindDefinition(NameTableManager table) {
		String simpleName = parseClassSimpleName();
		String locationString = parseLocationString();
		SourceCodeLocation location = SourceCodeLocation.getLocation(locationString);
		
		definition = table.findDetailedTypeDefinitionByLocation(simpleName, location);
	}
	
	/**
	 * Test if the node has been bind to the same class as another class node 
	 */
	@Override
	public boolean hasBindToSameDefinition(ValuedNode anotherNode) {
		if (!(anotherNode instanceof ValuedClassNode)) return false;
		
		ValuedClassNode another = (ValuedClassNode)anotherNode;
		if (definition == null || another.definition == null) return false;
		if (definition == another.definition) return true;
		
		DetailedTypeDefinition typeDef = (DetailedTypeDefinition)definition;
		DetailedTypeDefinition otherTypeDef = (DetailedTypeDefinition)another.definition;
		
		final double threshold = 0.90;
		if (NameTableComparator.calculateDetailedTypeDefinitionSimilarity(typeDef, otherTypeDef, true) >= threshold) return true;
		return false;
	}
	

	/**
	 * parse the simple name of the class corresponding to this node. We assume that the label of the node has the format 
	 * as "classSimpleName@locationString" 
	 */
	private String parseClassSimpleName() {
		int index = label.indexOf('@');
		if (index < 0) {
			throw new AssertionError("Can not find charater @ at the label of node, label = [" + label + "]");
		}
		return label.substring(0, index);
	}

	/**
	 * parse the location string of the class corresponding to this node. We assume that the label of the node has the format 
	 * as "classSimpleName@locationString" 
	 */
	private String parseLocationString() {
		int index = label.indexOf('@');
		if (index < 0) {
			throw new AssertionError("Can not find charater @ at the label of node, label = [" + label + "]");
		}
		return label.substring(index+1, label.length());
	}

}
