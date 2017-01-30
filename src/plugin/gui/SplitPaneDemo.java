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

import plugin.graph.Graph;

import java.util.*;
 
//SplitPaneDemo itself is not a visible component.
public class SplitPaneDemo extends JPanel
                          implements ListSelectionListener {
    private JLabel picture;
    private JLabel imageCanvas;
    private JList<?> list;
    private JSplitPane splitPane;
    private String[] fileNames;
    private String path;

    
    public SplitPaneDemo() {
    	path = setPath();
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
        //updateLabel(fileNames[list.getSelectedIndex()]);
        
    }
     
    
    //Listens to the list
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList)e.getSource();
        updateLabel(fileNames[list.getSelectedIndex()]);
    }
    
    //Renders the selected image
    protected void updateLabel (String name) {
    	
        BufferedImage image = null;
        ImageIcon icon;
		try {
			image = ImageIO.read(new File(path + name));
			System.out.println("bi created for " + name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		icon = new ImageIcon(image);
		if  (icon != null) {
            picture.setText(null);
        } else {
            picture.setText(name + " not a picture or file not found.");
        }
			
		
		picture.setIcon(icon);
    	
		/*
        ImageIcon icon = createImageIcon(path + name);
         System.out.println("Icon not created, why??");
        Graph g = new Graph();
        g.pictureToScreen(path + name);
        picture.setIcon(icon);
        
        */
    }


    public JSplitPane getSplitPane() {
        return splitPane;
    }

   
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
       java.net.URL imgURL = SplitPaneDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
 

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
    
    public static String setPath(){
    	JFrame frame = null;
    	
    	// make sure that plugin.graph.Settings.getPath(); has been set
    	String path = plugin.graph.Settings.getPath();
    	
    	// for testing
    	path = "C:/Users/Misi HP/Documents/Iskola/Humboldt/programok/BugminerIntegration/testprojekt";
    	
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
    private void createAndShowGUI() {
 
        //Create and set up the window.
        JFrame frame = new JFrame("CFG Drawer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SplitPaneDemo splitPaneDemo = new SplitPaneDemo();
        frame.getContentPane().add(splitPaneDemo.getSplitPane());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
 
       
}
