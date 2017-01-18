package graphBuilder;

import java.util.concurrent.BlockingQueue;
import executor.monitoring.Edge;

/**
 * Class that defines a Controller who react to new inputs
 */
public final class Controller implements Runnable{

	private BlockingQueue<Edge> inputStream;
	private BlockingQueue<TestCase> outputStream;

    public Controller(BlockingQueue<Edge> inputStream, BlockingQueue<TestCase> outputStream){
    	this.inputStream = inputStream;
    	this.outputStream = outputStream;
    }

    public void run(){
    	boolean isProcessing = true;
    	while(isProcessing){
    		isProcessing = processTestCase();
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
 
}
