package graphBuilder;

import java.util.HashMap;

import executor.monitoring.Edge;

public class TestCase {
	
	//TODO name ID
	static int instanceCounter = 0;
	private int id;
	private HashMap<String,MethodGraph> methodGraphs = new HashMap<>();
    private boolean isSuccessful;
    
    public TestCase(){
    	this.id = instanceCounter;
    	instanceCounter++;
    }
    
    public void update(Edge edge){
    	MethodGraph methodGraph = methodGraphs.computeIfAbsent(edge.getMethod(), k -> new MethodGraph(edge.getMethod()));
        methodGraph.update(edge);
    }
    
	public int getId() {
		return id;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
		
}
