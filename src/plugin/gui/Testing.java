package plugin.gui;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1));
		// 3 rows, 1 column Panel having Grid Layout

		/*
		 * Creates two labels and corresponding textfields, when these are changed and lose focus the input is set
		 */
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

		panel.add(tempDirLabel);
		panel.add(tempDirField);
		panel.add(exeFieldLabel);
		panel.add(exeField);

		frame.add(panel, 0);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Testing testframe = new Testing();
		testframe.makeSettingsPanel();

	}

}
