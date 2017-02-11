/**
 * 
 */
package plugin.gui;

/**
 * SplitPaneDemo(directory) reads the directory and displays a window with the content of the directory on the right and rendered images on the left
 * TODO: where to get the path from? Displayed as a single frame or a panel?
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
 
//SplitPaneDemo itself is not a visible component.
public class SplitPaneDemo extends JPanel
                          implements ListSelectionListener {
    private JLabel picture;
    private JList<?> list;
    private JSplitPane splitPane;
    private String[] fileNames;
    private String path;// = setPath();

    public SplitPaneDemo(String path) {

    	this.path = path;

    	fileNames = readDirectory(); 
    	
        //Create the list of images and put it in a scroll pane.
        list = new JList<String>(fileNames);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        
        JScrollPane listScrollPane = new JScrollPane(list);
        picture = new JLabel();
        picture.setFont(picture.getFont().deriveFont(Font.ITALIC));
        picture.setHorizontalAlignment(JLabel.CENTER);
         
        JScrollPane pictureScrollPane = new JScrollPane(picture);
 
        //Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                   listScrollPane, pictureScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
 
        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        listScrollPane.setMinimumSize(minimumSize);
        pictureScrollPane.setMinimumSize(minimumSize);

        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(800, 400));
    }
     
    
    //Listens to the list
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList)e.getSource();
        updateLabel(fileNames[list.getSelectedIndex()]);
    }
    
    /**
     * Renders the selected image or displays an error message if not an image has been chosen
     * 
     * @param name full path of the selected image
     */
    protected void updateLabel (String name) {
    	
        BufferedImage image = null;
		try {
			image = ImageIO.read(new File(path + name));
		} catch (IOException e) {

			System.out.println("javax.imageio.IIOException: Can't read input file!");
		}
		try {
			picture.setIcon(new ImageIcon(image, name));
			picture.setText(null);
            
		} catch (NullPointerException e){
			picture.setText(name + " cannot be displayed.");
			picture.setIcon(null);
		}
    }


    public JSplitPane getSplitPane() {
        return splitPane;
    }

    
    /** reads the directory and puts all filenames in a list to be displayed*/
    private String[] readDirectory() {

    	ArrayList<String> files = new  ArrayList<String>();
    	File folder = new File(path);
    	File[] listOfGraphfiles = folder.listFiles();
    	
    	for (int i = 0; i < listOfGraphfiles.length; i++) {
    		if (listOfGraphfiles[i].isFile()) {
    			files.add(listOfGraphfiles[i].getName());
    		}
    	}    	
    	return (String[]) files.toArray(new String[1]);
     }


    /**
     * Create the GUI and show it for debugging.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI(String path) {
 
        //Create and set up the window.
        JFrame frame = new JFrame("CFG Drawer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // To not close the Startpanel.

        SplitPaneDemo splitPaneDemo = new SplitPaneDemo(path);

        frame.getContentPane().add(splitPaneDemo.getSplitPane());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
       
}
