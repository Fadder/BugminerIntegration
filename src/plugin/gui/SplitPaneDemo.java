/**
 * 
 */
package plugin.gui;

/**
 * @author jan
 *
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
 
//SplitPaneDemo itself is not a visible component.
public class SplitPaneDemo extends JPanel
                          implements ListSelectionListener {
    private JLabel picture;
    private JLabel imageCanvas;
    private JList<?> list;
    private JSplitPane splitPane;
    private String[] fileNames;
    private static String path = setPath();

    
    public SplitPaneDemo() {
    	fileNames = readDirectory(); 
    	
        //Create the list of images and put it in a scroll pane.
         
        list = new JList(fileNames);
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

       // pictureScrollPane.add(imageCanvas);
 
        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(800, 400));
      //  updateLabel(fileNames[list.getSelectedIndex()]);
        
      
    }
     
    
    public JSplitPane getSplitPane() {
        return splitPane;
    }
    
    //Listens to the list
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList)e.getSource();
        ActionListener loadImage = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
            	setImage(getImage());
            }
        };
        //updateLabel(path +fileNames[list.getSelectedIndex()]);
    }
     
    //Renders the selected image
/*     protected void updateLabel (String name) {
    	BufferedImage wPic;
		try {
			wPic = ImageIO.read(this.getClass().getResource(name));
			JLabel wIcon = new JLabel(new ImageIcon(wPic));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
   protected void updateLabel (String fullPath) {
        ImageIcon icon = createImageIcon(fullPath);
        picture.add(imageCanvas);
        if  (icon != null) {
            picture.setText(null);
        } else {
            picture.setText("Image not found");
        }
    }
   
  */
 
    /** Set the image as icon of the image canvas (display it). */
    public void setImage(Image image) {
        imageCanvas.setIcon(new ImageIcon(image));
    }
    
    public static Image getImage() {

        BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File("/home/jan/Dropbox/SemesterprojectBugMining/workspace/BugminerIntegration/src/Testclasses/graph1.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		}if (bi == null) System.out.println("hier Fehler");
		return bi;
    }
    /** Returns an ImageIcon, or null if the path was invalid. 
    protected static ImageIcon createImageIcon(String fullPath) {
    	
    	System.out.println(fullPath); 
       java.net.URL imgURL = null;
	try {
		imgURL = new java.net.URL(fullPath);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       System.out.println("create image " + imgURL);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + fullPath);
            return null;
        }
    }*/
 
    private static String[] readDirectory() {
 	   	System.out.println("readDirectory "+ path);
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
    
    private static String setPath(){
    	JFrame frame = null;
    	
    	// make sure that plugin.graph.Settings.getPath(); has been set
    	String path = plugin.graph.Settings.getPath();
    	
    	// for testing
    	path = "/home/jan/Dropbox/SemesterprojectBugMining/workspace/BugminerIntegration/src/Testclasses/";
    	
    	if (path == null) {
    		path = (String)JOptionPane.showInputDialog(
    				frame,
    				"Path for graph image files not found or empty.\n Please choose path:",
    				"CFG: Error",
    				JOptionPane.QUESTION_MESSAGE
    				//icon
    				);
    	}
    	
    	return path;
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
 
        //Create and set up the window.
        JFrame frame = new JFrame("CFG Drawer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SplitPaneDemo splitPaneDemo = new SplitPaneDemo();
        frame.getContentPane().add(splitPaneDemo.getSplitPane());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
 
       
}
