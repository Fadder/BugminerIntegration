package plugin.graph;

/**
 * An Edge object contains a number, which identifies the node (vertex) 
 * 	to which the edge leads.  
 * 
 * @param id identification of the node corresponds to the line number of the handled instruction
 * @param num stores how many times an edge has been run
 * @param next pointer of the next Edge object
 */
public class Edge {

	private int id;
	private int num;
	private Edge next;

	public Edge(int n) {
		this.id = n;
		this.num = 1; // at first we ran this edge only once
		this.next = null;
	}
	
	public Edge(int n, int count) {
		this.id = n;
		this.num = count;
		this.next = null;
	}

	public void incNum() {
		num++;
	}
	
	public void incNum(int count) {
		num += count;
	}

	public int getID() {
		return id;
	}

	public Edge getNext() {
		return next;
	}

	public int getNum() {
		return this.num;
	}

	/** Inserts a new Edge after this Edge
	 * 
	 * @param newEdge
	 */
	public void setNext(Edge newEdge) {
		Edge temp = this.next;
		this.next = newEdge;
		newEdge.next = temp;
	}

}
