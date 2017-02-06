package plugin.gui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import plugin.graphviz.GraphViz;

public class Testing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GraphViz gv = new GraphViz();
		//StartTabbedPane stp = new StartTabbedPane();
		System.out.println(StartTabbedPane.getOsname());
		System.out.println(StartTabbedPane.getTempDir());
		//gv.setTempDir("hello");
		//gv.setExecutable("hello");
		
		
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1));
		// 3 rows, 1 column Panel having Grid Layout
				
		//JLabel tempDirLabel = new JLabel("text");
		JLabel label = new JLabel("This is a basic label");
		JTextField tempDirField = new JTextField(StartTabbedPane.getTempDir());
		JTextField exeField = new JTextField(StartTabbedPane.getExecutable());
		
		panel.add(label);
		panel.add(tempDirField);
		panel.add(exeField);
		frame.add(panel, 0);
		
		frame.pack();
		frame.setVisible(true);
	}

}
