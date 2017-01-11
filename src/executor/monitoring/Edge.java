package executor.monitoring;

import java.util.Objects;

// the edges are handed over in a java.util.concurrent.BlockingQueue<Edge>
// someone creates a queue and gives it to the graph builder and executor
// in their constructors. When the executor observes that the program
// terminates, a special invalid edge with isFinished() returning true is
// returned.

public class Edge {
	// the method this edge is in --> package + classname + methodname
	private String method;

	// the last line we executed in this method
	private int lineFrom;

	// the line we reached from the last line
	private int lineTo;

	private String enteredFromMethod;
	private boolean failure;

	private Edge(Builder builder) {
		this.method = Objects.requireNonNull(builder.method);
		this.lineFrom = builder.lineFrom;
		this.lineTo = builder.lineTo;
		this.enteredFromMethod = builder.enteredFromMethod;
		this.failure = builder.failure;
	}
	//
	// public Edge(String method, int lineFrom, int lineTo) {
	// this.method = method;
	// this.lineFrom = lineFrom;
	// this.lineTo = lineTo;
	// }
	//
	// public Edge(String method, int lineFrom, int lineTo, String
	// enteredFromMethod) {
	// this.method = method;
	// this.lineFrom = lineFrom;
	// this.lineTo = lineTo;
	// this.enteredFromMethod = enteredFromMethod;
	// }

	public boolean isFinished() {
		return method.equals("") && lineTo == -1;
	}

	public boolean isTestCaseFinished() {
		return method.equals("") && lineTo == 0;
	}

	public boolean isFailure() {
		return this.failure;
	}

	public boolean isFirstLine() {
		return lineFrom == -1;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getLineFrom() {
		return lineFrom;
	}

	public void setLineFrom(int lineFrom) {
		this.lineFrom = lineFrom;
	}

	public int getLineTo() {
		return lineTo;
	}

	public void setLineTo(int lineTo) {
		this.lineTo = lineTo;
	}

	public String getEnteredFromMethod() {
		return enteredFromMethod;
	}

	public void setEnteredFromMethod(String enteredFromMethod) {
		this.enteredFromMethod = enteredFromMethod;
	}

	public static class Builder {
		private String method;
		private int lineFrom;
		private int lineTo;
		private String enteredFromMethod;
		private boolean failure;

		public Builder() {

		}

		public Builder method(String methodName) {
			this.method = methodName;
			return this;
		}

		public Builder lineFrom(int lineFrom) {
			this.lineFrom = lineFrom;
			return this;
		}

		public Builder lineTo(int lineTo) {
			this.lineFrom = lineTo;
			return this;
		}

		public Builder enteredFrom(String enteredFrom) {
			this.enteredFromMethod = enteredFrom;
			return this;
		}

		public Edge successfulTestCase() {
			this.method = "";
			this.failure = false;
			this.lineFrom = 0;
			this.lineTo = 0;
			return new Edge(this);
		}

		public Edge failedTestCase() {
			this.method = "";
			this.failure = true;
			this.lineFrom = 0;
			this.lineTo = 0;
			return new Edge(this);
		}

		public Edge lastEdge() {
			this.method = "";
			this.lineFrom = -1;
			this.lineTo = -1;
			return new Edge(this);
		}
		
		public Edge buildEdge(){
			return new Edge(this);
		}

	}
}
