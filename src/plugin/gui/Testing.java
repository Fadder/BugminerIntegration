package plugin.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.sun.codemodel.internal.JLabel;

import plugin.graphviz.GraphViz;

public class Testing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GraphViz gv = new GraphViz();
		System.out.println(gv.getTempDir());
		System.out.println(gv.getExecutable());
		//gv.setTempDir("hello");
		//gv.setExecutable("hello");
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		//JLabel temDirLabel = new JLabel("GraphViz Temp-Dir:");
		JTextField tempDirField = new JTextField(gv.getTempDir());
		JTextField exeField = new JTextField(gv.getExecutable());
		
		panel.add(tempDirField);
		panel.add(exeField);
		frame.add(panel, 0);
		frame.setVisible(true);
	}

}
