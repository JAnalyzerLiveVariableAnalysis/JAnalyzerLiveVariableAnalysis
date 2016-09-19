package ui.structureBrowser.tree;

public class FieldNode extends ProjectTreeNode {

	String type;

	public FieldNode(NodeKind kind, String label, String type, String sizeMetrics) {
		super(kind, label, sizeMetrics);
		this.type = type;
	}

	public String toString() {
		return getSimpleName() + " : " + type;
	}

	@Override
	public String getItemString(int index) {
		return null;
	}

}
