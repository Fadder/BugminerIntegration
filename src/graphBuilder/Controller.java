package graphBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import executor.monitoring.Edge;

/**
 * Class that defines a Controller who react to new inputs
 */
public final class Controller implements Runnable{

	private BlockingQueue<Edge> inputStream = new LinkedBlockingQueue<>();
	private BlockingQueue<TestCase> outputStream = new LinkedBlockingQueue<>();

    public Controller(BlockingQueue<Edge> inputStream, BlockingQueue<TestCase> outputStream){
    	this.inputStream = inputStream;
    	this.outputStream = outputStream;
    }

    public void run(){
    	while(processTestCase()){
	    	//do nothing
	    }
    }
    
    private boolean processTestCase(){
    	TestCase currentTestCase = new TestCase();
    	while(true){
	        try {
	            Edge currentEdge = inputStream.take();
	            if(currentEdge.isFinished()){
	            	return false;
	            }
	            if(currentEdge.isTestCaseFinished()){
	            	currentTestCase.setSuccessful(!currentEdge.isFailure());
	            	break;
	            }
	        } catch (InterruptedException e) {
	        	System.out.println("InterruptedException while processing test case.");
	            e.printStackTrace();
	        }
    	}
    	try {
			outputStream.put(currentTestCase);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException while putting test case.");
            e.printStackTrace();
		}
        return true;
    }
    
    private void simpleTest() {
	    Edge e11 = new Edge("m1",1,2);
	    Edge e12 = new Edge("m1",2,3);
	    Edge e13 = new Edge("m1",3,4);
	    Edge e14 = new Edge("m1",1,2);
	    Edge e21 = new Edge("m2",1,2);
	    Edge e22 = new Edge("m2",1,2);
	    Edge e23 = new Edge("m2",1,2);
	    Edge finishTC  = new Edge("", 0, 0);
	    Edge finish  = new Edge("", 0, -1);
	    	
	    try {
	        inputStream.put(e11);
	        inputStream.put(e12);
	        inputStream.put(e13);
	        inputStream.put(e14);
	        inputStream.put(e21);
	        inputStream.put(e22);
	        inputStream.put(e23);
	        inputStream.put(finishTC);
	        inputStream.put(finish);
	
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	
	    while(processTestCase()){
	        // do nothing
	        System.out.print(".");
	    }
	    System.out.println();
	    System.out.println("FINISHED EXECUTION");
	    System.out.println();
	
	    //printAllTransitions();
	    System.out.println();
	}

    
}
