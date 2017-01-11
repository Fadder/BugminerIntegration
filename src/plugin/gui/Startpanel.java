/**
 * 
 */
package plugin.gui;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


import java.awt.BorderLayout;

import java.awt.FlowLayout;


import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author jan
 *
 */
public class Startpanel extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JMenuBar menuBar;
	JMenu mnNewMenu;
	JMenuItem mntmBeedtruiea;
	JLabel lblNewLabel;
	JButton btnChooseFile;
	JButton startButton;
	ActionListener l;
	ActionListener l2;
	JPanel panel1, panel2, panel3;
	JDialog startSettingDialog= new StartSettingDialog();

	public Startpanel() {

		panel1 = new JPanel();

		// getContentPane().add(panel2, 1);
		// getContentPane().add(panel3, 1);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnNewMenu = new JMenu("Configure");
		menuBar.add(mnNewMenu);

		mntmBeedtruiea = new JMenuItem("Select");
		mnNewMenu.add(mntmBeedtruiea);
		getContentPane().setLayout(new BorderLayout(0, 0));

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();

		ActionListener listenerSelectOutputType = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Fileformat set to: " + e.getActionCommand());
			}
		};

		// Create the radio buttons for the output type selection

		JRadioButton pdf = new JRadioButton("pdf");
		pdf.setMnemonic(KeyEvent.VK_B);
		pdf.setActionCommand("pdf");
		pdf.setSelected(true);

		JRadioButton png = new JRadioButton("png");
		png.setMnemonic(KeyEvent.VK_B);
		png.setActionCommand("png");
		png.setSelected(false);

		JRadioButton ps = new JRadioButton("ps");
		ps.setMnemonic(KeyEvent.VK_B);
		ps.setActionCommand("ps");
		ps.setSelected(false);
		group.add(pdf);
		group.add(png);
		group.add(ps);

		// Register a listener for the radio buttons.
		pdf.addActionListener(listenerSelectOutputType);
		png.addActionListener(listenerSelectOutputType);
		ps.addActionListener(listenerSelectOutputType);
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel FileTypePanel = new JPanel();
		FileTypePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		FileTypePanel.add(pdf);
		FileTypePanel.add(png);
		FileTypePanel.add(ps);

		// getContentPane().add(FileTypePanel);

		panel1.add(FileTypePanel);
		// lblNewLabel = new JLabel("New label");
		// getContentPane().add(lblNewLabel);

		btnChooseFile = new JButton("Choose file");

		panel1.add(btnChooseFile);
		l = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(chooseFile());
			}
		};

		btnChooseFile.addActionListener(l);

		startButton = new JButton("START");

		panel1.add(startButton);
		l2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startSettingDialog.setVisible(true);
			}
		};
		startButton.addActionListener(l2);

		getContentPane().add(panel1, 0);

	}

	/**
	 * @param args
	 */
	/*
	 * public static void main(String[] args) { // TODO Auto-generated method
	 * stub createAndStartPanel(); }
	 */

	public void createAndStartPanel() {
		Startpanel startpanel = new Startpanel();
		startpanel.setSize(500, 300);
		startpanel.setLocationRelativeTo(null);
		startpanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		startpanel.setVisible(true);

	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	

	public String chooseFile() {
		JFileChooser chooser = new JFileChooser();
		getContentPane().add(chooser);
		String inputPath = "";

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			inputPath = chooser.getSelectedFile().getAbsolutePath();
		} else {
			// Exception
			inputPath = "";
		}
		return inputPath;
	}

}
