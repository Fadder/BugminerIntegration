package plugin.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Random;

public class ImageViewer {

    JPanel gui;
    /** Displays the image. */
    JLabel imageCanvas;

    /** Set the image as icon of the image canvas (display it). */
    public void setImage(Image image) {
        imageCanvas.setIcon(new ImageIcon(image));
    }

    public void initComponents() {
        if (gui==null) { 
            gui = new JPanel(new BorderLayout());
            gui.setBorder(new EmptyBorder(5,5,5,5));
            imageCanvas = new JLabel();

            JPanel imageCenter = new JPanel(new GridBagLayout());
            imageCenter.add(imageCanvas);
            JScrollPane imageScroll = new JScrollPane(imageCenter);
            imageScroll.setPreferredSize(new Dimension(300,100));
            gui.add(imageScroll, BorderLayout.CENTER);
        }
    }

    public Container getGui() {
        initComponents();
        return gui;
    }

    public static Image getImage() {

        BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File("/home/jan/Dropbox/SemesterprojectBugMining/workspace/BugminerIntegration/src/Testclasses/graph1.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		}
		return bi;
    }

    public static void main(String[] args) throws Exception {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame("Image Viewer");
                // TODO Fix kludge to kill the Timer
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                final ImageViewer viewer = new ImageViewer();
                f.setContentPane(viewer.getGui());

                f.pack();
                f.setLocationByPlatform(true);
                f.setVisible(true);

                ActionListener animate = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        viewer.setImage(getImage());
                    }
                };
                Timer timer = new Timer(1500,animate);
                timer.start();
            }
        };
        SwingUtilities.invokeLater(r);
    }
}