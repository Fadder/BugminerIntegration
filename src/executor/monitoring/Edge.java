package executor.monitoring;

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


    public Edge(String method, int lineFrom, int lineTo) {
        this.method = method;
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
    }

    public Edge(String method, int lineFrom, int lineTo, String enteredFromMethod) {
        this.method = method;
        this.lineFrom = lineFrom;
        this.lineTo = lineTo;
        this.enteredFromMethod = enteredFromMethod;
    }

    public boolean isFinished() {
        return method.equals("");
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
}
