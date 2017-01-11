package executor.monitoring;


// the edges are handed over in a java.util.concurrent.BlockingQueue<Edge>
// someone creates a queue and gives it to the graph builder and executor
// in their constructors. When the executor observes that the program
// terminates, a special invalid edge with isFinished() returning true is
// returned.

public class Edge {
	public static final Edge LAST_EDGE= new Edge("",-1,-1);
	public static final Edge TESTCASE_SUCCESS= new Edge("",1 ,-2);
	public static final Edge TESTCASE_FAILURE= new Edge("",0,-2);
	// the method this edge is in --> package + classname + methodname
	private String method;

	// the last line we executed in this method
	private int lineFrom;

	// the line we reached from the last line
	private int lineTo;

	private String enteredFromMethod;
	
	 public Edge(String method, int lineFrom, int lineTo) {
	 this.method = method;
	 this.lineFrom = lineFrom;
	 this.lineTo = lineTo;
	 }
	
	 public Edge(String method, int lineFrom, int lineTo, String
	 enteredFromMethod) {
	 this.method = method;
	 this.lineFrom = lineFrom;
	 this.lineTo = lineTo;
	 this.enteredFromMethod = enteredFromMethod;
	 }

	public boolean isFinished() {
		return method.equals("") && lineTo == -1;
	}

	public boolean isTestCaseFinished() {
		return method.equals("") && lineTo == -2;
	}

	public boolean isFailure() {
		return this.lineFrom==0;
	}

	public boolean isFirstLine() {
		return lineFrom == -1;
	}

	public String getMethod() {
		return method;
	}

	public int getLineFrom() {
		return lineFrom;
	}

	public int getLineTo() {
		return lineTo;
	}

	public String getEnteredFromMethod() {
		return enteredFromMethod;
	}
}
