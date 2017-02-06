package plugin.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import plugin.graphviz.GraphViz;

public class GraphDrawer implements Runnable {
	
	private final boolean DEBUG = false;
	private Graph graph; // The own representation of the graph which will be converted into a dot file and then to a picture.
	private BlockingQueue<TestCase> inputStream; // The input stream where the full testcases come.
	private IProgressMonitor monitor;
	private File projectFolder;
	private String path;
	
	public GraphDrawer(BlockingQueue<TestCase> inputStream, IProject project, IProgressMonitor monitor) {
		graph = new Graph();
		this.inputStream = inputStream;
		this.monitor=SubMonitor.convert(monitor);
		projectFolder=new File(project.getLocationURI());
		path = projectFolder.getAbsolutePath();
		// System.out.println(projectFolder+ " ----- " + projectFolder.exists());
	}

	@Override  
	public void run() {
		
		long startTime = System.currentTimeMillis();
		
		graph.setPath(path);
		while (true) { // Maybe some break condition?
			try {
				TestCase tc;
				while((tc = inputStream.poll(100, TimeUnit.MILLISECONDS))==null){
					if(monitor.isCanceled()){
						throw new OperationCanceledException();
					}
				};
				if(tc.isLast()) return;
				if (DEBUG) System.out.println("TestCase: "+tc);
				
				// Build graph here.
				for(MethodGraph methodgraph: tc.getCollectionMethodGraphs()){
					String filename = "/" + tc.getId() + "_" + methodgraph.getMethodId();
					graph.reset(); // Delete previous data
					graph.setFilename(filename);
					
					/* // This is the old method, with our own Graph object.
					testcaseToGraph(methodgraph);
					graph.dotToImage(graph.saveAsDot());*/  
					
					// New method without Graph object, faster.
					graph.dotToImage(testcaseToDot(methodgraph, filename));
				}
				long endTime = System.currentTimeMillis();
				System.out.println("That took " + (endTime - startTime) + " milliseconds");
				
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
	private void testcaseToGraph(MethodGraph mg) {

		if (DEBUG) System.out.println("    A methodgraph.");
		Collection<Transition> collectT = mg.getListOfTransition();
		for (Transition t : collectT) {
			if (DEBUG) System.out.println("        A transition.");
			int a = t.getSource();
			int b = t.getTarget();
			int c = t.getCount();
			if (DEBUG) System.out.println("Transition gave the edge: " + a + "->" + b + ", " + c + " times.");
			graph.addEdge(a, b, c);
		}
	}
	
	
	//Writes the MethodGraph directly into the dot file, saving time on our Graph object. 
	private String testcaseToDot(MethodGraph mg, String filename) {
		
		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.addln("labelloc = \"t\";");
		gv.addln("label = \"" + filename + "\";");
		gv.addln("node [shape = box];");
		
		Collection<Transition> collectT = mg.getListOfTransition();
		for (Transition t : collectT) {
			int a = t.getSource();
			int b = t.getTarget();
			int c = t.getCount();
			//Add edge to dot.
			String out;
			if (a == -1) {
				out = "Start -> " + b + " [label=\"" + c + "\"];";
			} else {
				out = Integer.toString(a) + " -> " + b + " [label=\"" + c + "\"];";
			}
			
			gv.addln(out);
		}
		
		gv.addln(gv.end_graph());
		System.out.println(gv.getDotSource());
		
		try { // Save GraphViz as a dot file.
			FileWriter writer = new FileWriter(path + filename + ".dot");
			writer.write(gv.getDotSource());
			writer.close();
		} catch (IOException e) {
			System.out.println("Error during creation of dot file: ");
			e.printStackTrace();
		}
		return path + filename + ".dot";
	}
	
	public void setType(String newType) {
		graph.setType(newType);
	}
	
	// Deletes old graph.
	private void resetGraph() {
		graph.reset();
	}

}
