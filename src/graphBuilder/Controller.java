package graphBuilder;

import java.util.Collection;
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
			
			Collection<MethodGraph> collectmg = currentTestCase.getMethodGraphs().values();
			for (MethodGraph mg : collectmg) {
				Collection<Transition> collectT = mg.getListOfTransition();
				for (Transition t: collectT) {
					int a = t.getSource();
					int b = t.getTarget();
					int c = t.getCount();
					System.out.println("Transition gave the edge: "+a+"->"+b+", "+c+" times.");
				}
			System.out.println("TestCase sent.");
			} // This part is needed for the debugging. 
		} catch (InterruptedException e) {
			System.out.println("InterruptedException while putting test case.");
            e.printStackTrace();
		}
        return true;
    }
 
}
