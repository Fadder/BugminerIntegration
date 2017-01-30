package plugin.graph;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import plugin.graphviz.GraphViz;

public class Graph {

	// A pointer to all of the nodes.
	public Instruction firstinst;

	
	private String filename; // name of your output files, default is graph
	private String type; // Type of the picture file eg  png, pdf. Default is png.
	private String path; /*System.getProperty("user.dir");*/ // A general path to the working directory.
	
	
	public Graph() {
		this.firstinst = null;
		filename = "graph";
		type = "png";
		path = "";
	}

	// Simply add a new edge to the graph or updates the counter.
	// If the graph doesn't have the necessary nodes yet, they are created.
	public void addEdge(int from, int to, int count) {
		boolean debugprint = false;
		if (firstinst == null) { // There are no nodes yet.
			if (debugprint)
				System.out.println("Adding new edge " + to
						+ " to new graph's new instr " + from);
			firstinst = new Instruction(from, to, count);
		} else {
			Instruction instr = searchInstr(from);
			if (instr == null) { // This node doesn't yet exist.
				if (debugprint)
					System.out.println("Adding new edge " + to
							+ " to new instr " + from);
				addInstr(from, to, count);
				return;
			} else { // The node exists, add edge to it.
				if (debugprint) System.out.println("Adding new edge " + to + " to instr " + from);
				instr.addEdge(to, count);
			}
		}
	}

	/*
	 * Search for the Instruction (node) with the ID id. Returns a pointer to
	 * it, if it exists. Otherways returns null.
	 */
	public Instruction searchInstr(int id) {
		if (firstinst == null)
			return null;
		Instruction instr = firstinst;
		while (instr != null) {
			if (instr.getID() == id) {
				return instr;
			}
			instr = instr.getNextInst();
		}

		return instr;
	}

	/*
	 * Adds a new instruction (node) to the graph.
	 * 
	 * @param id The id of the instruction.
	 * 
	 * @param to The id of the instruction to which the edge leads.
	 */
	public Instruction addInstr(int id, int to, int count) {
		Instruction instr = new Instruction(id, to, count);
		if (this.firstinst == null) {
			firstinst = instr;
			return firstinst;
		}
		Instruction iterator = firstinst;

		while (iterator.getNextInst() != null) {
			if (iterator.getNextInst().getID() > id) {
				Instruction temp = iterator.getNextInst();
				iterator.setNextInst(instr);
				instr.setNextInst(temp);
				return instr;
			}
			iterator = iterator.getNextInst();
		}
		iterator.setNextInst(instr);
		return instr;
	}

	// Creates a dot file from the graph and saves it.
	// It replaces the old file if this dot file already exists.
	// Return the path of the saved file as String.
	public String saveAsDot() {

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		Instruction inst = firstinst;

		// horizontal iteration through the different instructions
		while (inst != null) {
			Edge edge = inst.getEdge();
			int id = inst.getID();

			// on every instruction vertical iteration through all the edges
			// from that instr.
			while (edge != null) {
				int edgeTo = edge.getID(); // The instruction to which the edge
											// leads.
				int label = edge.getNum();
				String out = Integer.toString(id) + " -> " + edgeTo
						+ " [label=\"" + label + "\"];";

				gv.addln(out);
				edge = edge.getNext();
			}
			inst = inst.getNextInst();
		}

		gv.addln(gv.end_graph());
		System.out.println(gv.getDotSource());

		/*
		 * try{ BufferedWriter writer = new BufferedWriter( new
		 * OutputStreamWriter(new FileOutputStream("graph.dot"), "utf-8") );
		 */
		try {
			FileWriter writer = new FileWriter(path + filename + ".dot");
			writer.write(gv.getDotSource());
			writer.close();
		} catch (IOException e) {
			System.out.println("Error during creation of dot file: ");
			e.printStackTrace();
		}
		return path + filename + ".dot";
	}

	// Draw a picture on the screen, with scrollbars.
	public void pictureToScreen(String sourcePath) {
		// If the file format is not png, we call an extern program to open the
		// picture.
		if (!type.equals("png")) {
			pdfToPicture(sourcePath);
			return;
		}

		//System.out.println("Picture file read in. Path is: " + sourcePath);
		File out = new File(sourcePath);
		BufferedImage img = null;
		try {
			img = ImageIO.read(out);
		} catch (IOException e) {
			System.out.println("Couldn't read input picture file.");
			e.printStackTrace();
		}

		JFrame frame = new JFrame("And here is  the Graph...");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		if (img == null) {
			// here some error message
			frame.setVisible(true);
			return;
		}
		ImageIcon icon = new ImageIcon(img);
		frame.setLayout(new FlowLayout());

		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);

		frame.setLayout(new BorderLayout());

		JScrollBar hbar = new JScrollBar(JScrollBar.HORIZONTAL, 30, 20, 0, 300);
		JScrollBar vbar = new JScrollBar(JScrollBar.VERTICAL, 30, 40, 0, 300);

		// vbar.setUnitIncrement(100); // doesn't work right now
		lbl.add(hbar, BorderLayout.SOUTH);
		lbl.add(vbar, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane(lbl);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// scrollPane.setBounds(50, 30, 300, 50);

		frame.add(scrollPane);
		frame.pack(); // sizes the window to the size of the picture
		// window will appear in the middle of the screen
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);
	}

	private void pdfToPicture(String sourcePath) {
		try {
			File pdfFile = new File(sourcePath);
			Desktop.getDesktop().open(pdfFile);
		} catch (IOException ex) {
			System.out.println("No application found for reading pdf.");
			// no application registered for PDFs
		}

	}

	// Creates picture file from the dot file.
	// Returns the path of the picture as a String.
	// @param input String representation of the path of the file.
	// If the parameter is null, reads the default input.
	public String dotToImage(String input) {

		// Make a new Graph and create it from the input.
		GraphViz gv = new GraphViz();
		if (input == null)	{
			input = path + filename + ".dot";
		}
		gv.readSource(input);

		// Write graph to picture file.
		File out = new File(path + filename + "." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, "dot"), out);
		
		return path + filename + "." + type;
	}
	
	public void setFilename(String name) {
		filename = name;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setType(String new_type) {
		type = new_type;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String newPath) {
		path = newPath;
	}
	
	public void reset() {
		firstinst = null;
	}

}
