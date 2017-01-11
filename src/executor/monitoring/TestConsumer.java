package executor.monitoring;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class TestConsumer {
	private BlockingQueue<Edge> input;

	public TestConsumer(BlockingQueue<Edge> input) {
		this.input = input;
	}

	public void consume(IProgressMonitor monitor) {
		while (true) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			Edge transition = null;
			try {
				transition = input.poll(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (transition != null) {
				if(transition.isFinished()){break;}
				
				if(transition.isTestCaseFinished()){
					if(transition.isFailure()){
						System.out.println("Test failed");
					}else{
						System.out.println("Test success");
					}
					continue;
				}
				System.out.println("----------------------------------");
				System.out.println("CalledFrom: " + transition.getEnteredFromMethod());
				System.out.println("Methodenname: " + transition.getMethod());
				System.out.println("lineFrom: " + transition.getLineFrom());
				System.out.println("lineTo: " + transition.getLineTo());
			}
		}
	}

}
