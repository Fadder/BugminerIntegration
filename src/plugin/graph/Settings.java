package plugin.graph;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class Settings {
	// outputfile typ, eg png, pdf, ps
	// png is displayed with eclips, pdf and ps with external viewer
	private static String type;

	public static String getType() {
		return type;
	}

	public static void setType(String type) {
		Settings.type = type;
	}

	private static String filename;

	public static String getFilename() {
		return filename;
	}

	public static void setFilename(String filename) {
		Settings.filename = filename;
	}

	public static String getPicturePath() {
		return picturePath;
	}

	public static String getPath() {
		return workPath;
	}

	private static String workPath; // working directory
	private static String picturePath;

	public static void setPicturePath(String path) {
		Settings.picturePath = path;
	}

	public static void setPath(String path) {
		Settings.workPath = path;
	}

	private static final String pathJan = "/home/jan/Dropbox/SemesterprojectBugMining/workspace/cfgdrawer/Semesterprojekt_Uebung/";
	private static final String picturePathJan = pathJan + filename + "."
			+ type;

	private static final String pathMihaly = "C:/Users/Misi HP/Documents/Iskola/Humboldt/programok/cfgdrawer/Semesterprojekt_Uebung/";
	private static final String picturePathMihaly = pathMihaly + filename + "."
			+ type;

	/*
	 * main for testing, set your path, set your filename, set your export type,
	 * then a graph is drawn
	 */
	public static void main() {

		setPath(pathJan);
		setPath(pathMihaly);
		setPicturePath(picturePathJan);
		setPicturePath(picturePathMihaly);

		// outputfile typ, eg png, pdf, ps
		// png is displayed with eclips, pdf and ps with external viewer
		setType("png");

		// chose the name of your file
		setFilename("graph");

		// If we test the Threads or not. They still require some work...
		boolean decision = true;

		if (decision) {
			testThreads();
			return;
		}

		Graph g = new Graph();
		g.addEdge(5, 1);

		/*
		 * for (int i = 0; i < 1000; i++) { //Extrem case. g.addEdge(i, (i+1));
		 * }
		 */
		/*
		 * int n = 50; for (int i = 0; i < n; i++) { int a = (int)
		 * (Math.random()*n); int b = (int) (Math.random()*n); g.addEdge(a, b);
		 * }
		 */
		/*
		 * for (int i = 0; i < 10; i++) { for (int j = i; j < 10; j++) {
		 * g.addEdge(i, j); } }
		 */
		g.pictureToScreen(g.dotToImage(g.saveAsDot()));
	}

	// A function to test the new Threads, so that we don't spam the main
	// function.
	private static void testThreads() {
		Graph g = new Graph();

		// Trying the new threads for input.
		PipedWriter pw = new PipedWriter();
		PipedReader pr;
		try {
			pr = new PipedReader(pw);
			// System.out.println("Pipe successfully created.");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		pipeInputThread pit = new pipeInputThread(pw);
		pipeGraphThread pgt = new pipeGraphThread(pr);
		pit.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Errorerror!");
			e.printStackTrace();
		}
		System.out.println("PGT starts immediately.");
		pgt.run();
		System.out.println("PGT started.");
		g = pgt.getGraph();
		System.out.println("Graph received.");
		g.pictureToScreen(g.dotToImage(g.saveAsDot()));
	}

}
