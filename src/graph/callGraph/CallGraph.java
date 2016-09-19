package graph.callGraph;

import graph.basic.AbstractGraph;

public class CallGraph extends AbstractGraph {
	public CallGraph(String id) {
		super(id);
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in the format defined by complex network analysis tool Pajek.
	 * @param out : the output text file, which should be opened
	 */
/*	@Override
	public void simplyWriteToNetFile(PrintWriter output) throws IOException {
		
		output.println("*Vertices " + nodes.size());
		for (GraphNode node : nodes) {
			String id = node.getId();
			int index = nodes.indexOf(node) + 1;
			output.println(index + " \"" + id + "\"");
			output.println(id.hashCode());
		}
		//output.println("*Arcs " + edges.size());
		output.println("*Arcs");
		for (GraphEdge edge : edges) {
			String startId = edge.getStartNode().getId();
			String endId = edge.getEndNode().getId();
			output.println(startId.hashCode() + " " + endId.hashCode());
		}

		output.println();
		output.flush();
	}*/
}
