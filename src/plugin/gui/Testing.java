package plugin.gui;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import plugin.graphviz.GraphViz;

public class Testing {

	public void makeSettingsPanel() {

		GraphViz gv = new GraphViz();
		// StartTabbedPane stp = new StartTabbedPane();
		//System.out.println(StartTabbedPane.getOsname());
		//System.out.println(StartTabbedPane.getTempDir());
		// gv.setTempDir("hello");
		// gv.setExecutable("hello");

		JFrame frame = new JFrame();
		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(/*3*/ 0, 2, 6, 3));
		// Set border for the panel
		panel2.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));     
		
		JRadioButton pdf = new JRadioButton("pdf");
		pdf.setMnemonic(KeyEvent.VK_B);
		pdf.setActionCommand("pdf");
		pdf.setSelected(false);

		JRadioButton png = new JRadioButton("png");
		png.setMnemonic(KeyEvent.VK_B);
		png.setActionCommand("png");
		png.setSelected(true);

		JRadioButton ps = new JRadioButton("ps");
		ps.setMnemonic(KeyEvent.VK_B);
		ps.setActionCommand("ps");
		ps.setSelected(false);

		JRadioButton jpg = new JRadioButton("jpg");
		jpg.setMnemonic(KeyEvent.VK_B);
		jpg.setActionCommand("jpg");
		jpg.setSelected(false);

		ButtonGroup group = new ButtonGroup();
		group.add(pdf);
		group.add(png);
		group.add(ps);
		group.add(jpg);

		/*
		 * Creates two labels and corresponding textfields, when these are changed and lose focus the input is set
		 */
		JPanel FileTypePanel = new JPanel();
		JLabel fileTypeLabel = new JLabel();
		fileTypeLabel.setText("Output file type:");
		FileTypePanel.add(pdf);
		FileTypePanel.add(png);
		FileTypePanel.add(ps);
		FileTypePanel.add(jpg);
		
		JLabel tempDirLabel = new JLabel("Temporary directory:");
		JLabel exeFieldLabel = new JLabel("Path to GraphViz:");

		JTextField tempDirField = new JTextField(StartTabbedPane.getTempDir());
		JTextField exeField = new JTextField(StartTabbedPane.getExecutable());
		

		tempDirField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				StartTabbedPane.setTempDir((String) tempDirField.getText());
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		exeField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				StartTabbedPane.setExecutable((String) exeField.getText());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
			}
		});

		/*
		 * Set icon to frame
		 */
		String iconPath = System.getProperty("user.dir") + "/src/images/cfg-drawer.gif";
		String iconDescription = "";
		if (new File(iconPath).exists()) {
			ImageIcon icon = new ImageIcon(iconPath, iconDescription);
			frame.setIconImage(icon.getImage());
		}

		panel2.add(tempDirLabel);
		panel2.add(tempDirField);
		panel2.add(exeFieldLabel);
		panel2.add(exeField);
		panel2.add(fileTypeLabel);
		panel2.add(FileTypePanel);

		//panel2.addTab("Settings", icon, panel2, "Set the application paths and output files");

		frame.add(panel2, 0);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Testing testframe = new Testing();
		testframe.makeSettingsPanel();

	}

}
