package plugin.graph;

import java.io.IOException;
import java.io.PipedReader;

/*
 * This class reads info from the pipe and creates the graph.
 */
public class pipeGraphThread extends Thread {

	private Graph g;
	private PipedReader pr;

	public pipeGraphThread(PipedReader pr) {
		this.pr = pr;
	}

	public void run() {
		g = new Graph();
		int input;
		System.out.println("pipeGraphThread is reading input...");
		try {
			// reads the input ascii characterwise, expected format
			// "number-number;" other characters are ignored
			int from = 0, to = 0;

			// to differentiate if we are reading the numbers of the first or
			// second note
			boolean firstNode = true;
			while ((input = pr.read()) != -1) {
				switch (input) {
				// the numbers
				case 48:
				case 49:
				case 50:
				case 51:
				case 52:
				case 53:
				case 54:
				case 55:
				case 56:
				case 57:
					// as long as numbers are read the input is a node
					if (firstNode) {
						from = from * 10 + (input - 48);
					} else {
						to = to * 10 + (input - 48);
					}
					break;

				case 45:
					// - marks the edge between first and second node
					firstNode = false;
					break;
				case 59:
					// ; marks the end of
					g.addEdge(from, to);
					firstNode = true;
					from = 0;
					to = 0;
					break;
				default:
					// Error handling
					System.out.print("unexpected character: " + (char) input
							+ " from input stream is ignored after reading ");
					if (firstNode)
						System.out.println(from);
					else
						System.out.println(to);
				}
			}

			pr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setPipedReader(PipedReader pr) {
		this.pr = pr;
	}

	public Graph getGraph() {
		return g;
	}

	public void resetGraph() {
		g = new Graph();
	}
}
