package graphBuilder;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import executor.monitoring.Edge;

/**
 * Class that defines a Controller who react to new inputs
 */
public final class Controller implements Runnable{

    private LinkedBlockingQueue<Edge> inputStream = new LinkedBlockingQueue<>();

    private HashMap<String,MethodGraph> methodGraphs = new HashMap<>();
    
    // TODO outputstream
    public Controller(LinkedBlockingQueue<Edge> inputStream){
    	this.inputStream = inputStream;
    }

    private void simpleTest() {
        Edge e11 = new Edge("m1",1,2);
        Edge e12 = new Edge("m1",2,3);
        Edge e13 = new Edge("m1",3,4);
        Edge e14 = new Edge("m1",1,2);
        Edge e21 = new Edge("m2",1,2);
        Edge e22 = new Edge("m2",1,2);
        Edge e23 = new Edge("m2",1,2);
        Edge e0  = Edge.LAST_EDGE;

        try {
            inputStream.put(e11);
            inputStream.put(e12);
            inputStream.put(e13);
            inputStream.put(e14);
            inputStream.put(e21);
            inputStream.put(e22);
            inputStream.put(e23);
            inputStream.put(e0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(update()){
            // do nothing
            System.out.print(".");
        }
        System.out.println();
        System.out.println("FINISHED EXECUTION");
        System.out.println();

        printAllTransitions();
        System.out.println();
    }
    
    public void run(){
    	while(update()){
    		// TODO
    	}
    }

    private boolean update(){
        try {
            Edge currentEdge = inputStream.take();
            if(currentEdge.isFinished()){
                return false;
            }
            MethodGraph methodGraph = methodGraphs.computeIfAbsent(currentEdge.getMethod(), k -> new MethodGraph(currentEdge.getMethod()));
            methodGraph.update(currentEdge);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void printAllTransitions(){
        for(MethodGraph methodGraph: methodGraphs.values()){
            System.out.println("Method: " + methodGraph.methodId);
            methodGraph.printAllTransitions();
            System.out.println();
        }
    }
}
