package graphBuilder;

import java.util.Collection;
import java.util.HashMap;

import executor.monitoring.Edge;

public class TestCase {
	
	private String id = "ID not set yet";
	private HashMap<String,MethodGraph> methodGraphs = new HashMap<>();
    private boolean isSuccessful;
    
    public static TestCase newLastTestCase(){
    	TestCase last = new TestCase();
    	last.setId("FINISHED");
    	return last;
    }
    
    public boolean isLast(){
    	return this.id.equals("FINISHED");
    }
    
    public void update(Edge edge){
    	MethodGraph methodGraph = methodGraphs.computeIfAbsent(edge.getMethod(), k -> new MethodGraph(edge.getMethod()));
        methodGraph.update(edge);
    }
    
	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
	
	public HashMap<String,MethodGraph> getMethodGraphs() {
		return methodGraphs;
	}
	
	public Collection<MethodGraph> getCollectionMethodGraphs() {
		return methodGraphs.values();
	}
		
}
