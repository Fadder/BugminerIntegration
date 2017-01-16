package graphBuilder;

import java.util.HashMap;

import executor.monitoring.Edge;

public class TestCase {
	
	//TODO ID
	private HashMap<String,MethodGraph> methodGraphs = new HashMap<>();
    private boolean isSuccessful;
    
    public void update(Edge edge){
    	MethodGraph methodGraph = methodGraphs.computeIfAbsent(edge.getMethod(), k -> new MethodGraph(edge.getMethod()));
        methodGraph.update(edge);
    }
    
	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
	
	private void printAllTransitions(){
        for(MethodGraph methodGraph: methodGraphs.values()){
            System.out.println("Method: " + methodGraph.methodId);
            methodGraph.printAllTransitions();
            System.out.println();
        }
    }
	
}
