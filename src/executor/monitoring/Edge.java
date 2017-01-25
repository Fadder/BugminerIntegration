package executor.monitoring;
import java.util.Objects;

// the edges are handed over in a java.util.concurrent.BlockingQueue<Edge>
// someone creates a queue and gives it to the graph builder and executor
// in their constructors. When the executor observes that the program
// terminates, a special invalid edge with isFinished() returning true is
// returned.

public class Edge {
	private String method;
	private int lineFrom;
	private int lineTo;
	private String enteredFrom;
	private boolean failure;
	private String testcase;

	private Edge(String method, String enteredFrom, int lineFrom, int lineTo, String testcase, boolean failure) {
		this.method = Objects.requireNonNull(method);
		this.lineFrom = lineFrom;
		this.lineTo = lineTo;
		this.enteredFrom = enteredFrom;
		this.failure = failure;
		this.testcase=testcase;
	}
	
	//TODO: mit den ganzen zugefügten Informationen wirds langsam ziemlich hässlich, vielleicht doch in klassen aufspalten? wäre aber interfaceänderung :/
	
	static Edge createStepEdge(String method, int lineFrom, int lineTo){
		return new Edge(method,null,lineFrom,lineTo,null,false);
	}
	
	static Edge createMethodEntryEdge(String method, String enteredFrom, int entryLine){
		return new Edge(method,enteredFrom,-1,entryLine,null, false);
	}
	
	static Edge createSuccessfulTestcaseEdge(String testcase){
		return new Edge("",null,-2,-2,testcase, false);
	}
	
	static Edge createfailedTestcaseEdge(String testcase){
		return new Edge("",null,-2,-2,testcase, true);
	}
	
    static Edge createLastEdge(){
		return new Edge("",null,-1,-1,null,false);
	}
	
    
    /**
     * Gibt den namen des beendeten TestCase zurück.
     * Nur gültig, wenn isTestCaseFinished true ist
     * 
     * @return name der testmethode
     */
	public String getTestcase(){
		return testcase;
	}

	/**
	 * Gibt zurück, ob diese Edge die letzte ist.
	 * Wenn true, sind alle restlichen Werte dieser Kante ungültig
	 */
	public boolean isFinished() {
		return method.equals("") && lineTo == -1 && lineFrom==-1;
	}

	/**
	 * Gibt zurück, ob ein Testcase beendet wurde.
	 * Wenn true, gibt nur getTestcase() und isFailure()
	 * ein gültiges Ergebnis zurück
	 * @return
	 */
	public boolean isTestCaseFinished() {
		return testcase!=null;
	}

	/**
	 * Gibt zurück, ob der Testcase ein Fehlschlag war.
	 * Nur gültig, wenn isTestCaseFinished() true ergibt.
	 * @return
	 */
	public boolean isFailure() {
		return failure;
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

	public String getEnteredFrom() {
		return enteredFrom;
	}

}