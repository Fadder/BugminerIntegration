package plugin.graph;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import graphBuilder.MethodGraph;
import graphBuilder.TestCase;
import graphBuilder.Transition;

public class GraphDrawer implements Runnable {
	
	private final boolean DEBUG = false;
	private Graph graph; // The own representation of the graph which will be converted into a dot file and then to a picture.
	private BlockingQueue<TestCase> inputStream; // The input stream where the full testcases come.
	private IProgressMonitor monitor;
	private File projectFolder;
	
	public GraphDrawer(BlockingQueue<TestCase> inputStream, IProject project, IProgressMonitor monitor) {
		graph = new Graph();
		this.inputStream = inputStream;
		this.monitor=SubMonitor.convert(monitor);
		projectFolder=new File(project.getLocationURI());
		System.out.println(projectFolder+ " ----- " + projectFolder.exists());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		/*// Both of these give the path to eclipse but not to the project's working directory.
		if (DEBUG) System.out.println("Path of image: "+graph.getPath());
		System.out.println(new java.io.File("").getAbsolutePath());*/
		
		/*
		File f = new File("simple.dot");
		System.out.println("The absolute path is: "+f.getAbsolutePath()+"\nThe relative path is: "+f.getPath());*/
		
		//graph.setPath("");
		
		while (true) { // Maybe some break condition?
			try {
				TestCase tc;
				while((tc = inputStream.poll(100, TimeUnit.MILLISECONDS))==null){
					if(monitor.isCanceled()){
						throw new OperationCanceledException();
					}
				};
				if (DEBUG) System.out.println("TestCase: "+tc);
				
				// Build graph here.
				testcaseToGraph(tc);
				graph.pictureToScreen(graph.dotToImage(graph.saveAsDot()));
				if (DEBUG) System.out.println("One graph drawn.");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (DEBUG) System.out.println("Drawing finished.");
			
			//New TestCase, new Graph
			resetGraph();
		}
	}
	
	
	// Converts the TestCase objects into the plugin's Graph format.
	private void testcaseToGraph(TestCase tc) {
		if (tc.getMethodGraphs().isEmpty()){
			System.out.println("Error! Given MethodGraph is empty.");
		}
		if (DEBUG) System.out.println("Testcase to Graph.");
		//Collection<MethodGraph> collectmg = tc.getMethodGraphs().values();
		Collection<MethodGraph> collectmg = tc.getCollectionMethodGraphs();
		if (collectmg.isEmpty()){
			System.out.println("Error! Given Collection is empty.");
		}
		
		
		for (MethodGraph mg : collectmg) {
			if (DEBUG) System.out.println("    A methodgraph.");
			Collection<Transition> collectT = mg.getListOfTransition();
			for (Transition t: collectT) {
				if (DEBUG) System.out.println("        A transition.");
				int a = t.getSource();
				int b = t.getTarget();
				int c = t.getCount();
				if (DEBUG) System.out.println("Transition gave the edge: "+a+"->"+b+", "+c+" times.");
				
				graph.addEdge(a, b, c);
			}
		}
		
	}
	
	// Deletes old graph.
	private void resetGraph() {
		graph = new Graph();
	}

}