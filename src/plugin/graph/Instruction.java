package plugin.graph;

public class Instruction {

	private final int id; // ID of the vertex
	private Instruction next; // pointer to the next vertex in the list (has
								// nothing to do with the graph)
	private Edge edges;
	private String type;
	private String error;

	// By initializing a new Instruction we already received an edge
	// that leads from it to an other one.
	public Instruction(int from, int to) {
		this.id = from;
		this.next = null;
		this.edges = new Edge(to);
		this.type = "";
		this.error = "";
	}

	public void addEdge(int to) {
		// This part should never occur, but who knows.
		if (edges == null) { // There are no edges yet.
			edges = new Edge(to);
			return;
		}

		Edge edge = searchEdge(to);
		if (edge == null) { // Not found. Add new edge.
			if (edges.getID() > to) { // New edge will be the first.
				Edge temp = edges;
				edges = new Edge(to);
				edges.setNext(temp);
				return;
			}
			edge = edges;
			while (edge.getNext() != null) {
				if (edge.getNext().getID() > to) {
					// Now insert a new Edge after the Edge "edge".
					Edge temp = edge.getNext();
					Edge newEdge = new Edge(to);
					edge.setNext(newEdge);
					newEdge.setNext(temp);
					return;
				}
				edge = edge.getNext();
			}
			// The new edge will be the last one.
			Edge newEdge = new Edge(to);
			edge.setNext(newEdge);

		} else {
			edge.incNum();
		}
	}

	// Returns the searched Edge or null, if it doesn't exist.
	public Edge searchEdge(int id) {
		if (edges == null)
			return null;
		Edge edge = edges;
		while (edge != null) {
			if (edge.getID() == id)
				return edge; // Edge found.
			if (edge.getID() > id)
				return null; // Id is too big: no such edge.
			edge = edge.getNext();
		}
		return null; // There is no edge to this node (yet).
	}

	public void setNextInst(Instruction instr) {
		this.next = instr;
	}

	public Edge getEdge() {
		return this.edges;
	}

	public Instruction getNextInst() {
		return this.next;
	}

	public int getID() {
		return this.id;
	}

	public String getType() {
		return this.type;
	}

	public String getError() {
		return this.error;
	}

}
