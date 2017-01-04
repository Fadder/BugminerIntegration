package plugin.graph;

import java.io.IOException;
import java.io.PipedWriter;


/*
 * This is a class that generates information for a fake input graph.
 */
public class pipeInputThread extends Thread {
	private PipedWriter pw;
	
	public pipeInputThread(PipedWriter pw) {
		this.pw = pw;
	} 
	
	
	public void run() {
		try {
			System.out.println("Writing information.");
			pw.write ("1-21;2-21;");
			pw.write("2-2345;");
			pw.write("2-2345;");
			/*
			for (int i = 0; i < 100; i++) {
				for (int j = 0; j < 10; j++) {
					pw.write(i+"-"+j+";");
				}
				for (int k = 0; k < 3; k++) {
					int a = (int) (Math.random()*2);
					pw.write(i+"-"+(i+a)+";");	
				}
			}*/
			//pw.write ("2-3");
			pw.close ();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
