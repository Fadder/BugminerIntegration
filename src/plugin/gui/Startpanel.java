/**
 * 
 */
package plugin.gui;

import javax.swing.BoxLayout;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author jan
 *
 */
public class Startpanel extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Startpanel startpanel;
	
	private String outputFormat;

	JMenuBar menuBar;
	JMenu mnNewMenu;
	JMenuItem mntmBeedtruiea;
	JLabel lblNewLabel;
	JButton btnChooseFile;
	JButton startButton;
	ActionListener l;
	ActionListener l2;
	JPanel panel1, panel2, panel3;
	StartSettingDialog startSettingDialog= new StartSettingDialog(this);

	private Startpanel() {
		
		outputFormat = "png";
		getContentPane().setLayout(new BorderLayout(0, 0));

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();

		ActionListener listenerSelectOutputType = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//System.out.println("Fileformat set to: " + e.getActionCommand());
				outputFormat = e.getActionCommand();
			}
		};

		// Create the radio buttons for the output type selection

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
		
		group.add(pdf);
		group.add(png);
		group.add(ps);
		group.add(jpg);

		// Register a listener for the radio buttons.
		pdf.addActionListener(listenerSelectOutputType);
		png.addActionListener(listenerSelectOutputType);
		ps.addActionListener(listenerSelectOutputType);
		jpg.addActionListener(listenerSelectOutputType);
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel FileTypePanel = new JPanel();
		FileTypePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		FileTypePanel.add(pdf);
		FileTypePanel.add(png);
		FileTypePanel.add(ps);
		FileTypePanel.add(jpg);

		panel1.add(FileTypePanel);

		// Start Button added
		startButton = new JButton("START");
		panel1.add(startButton);
		l2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startSettingDialog.setOutputType(outputFormat); // Change output format
				startSettingDialog.refreshTestClasses();
				startSettingDialog.setVisible(true);
			}
		};
		startButton.addActionListener(l2);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(panel1);
		this.add(container);
		
		this.setSize(1000, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent evt){
				startpanel=null;
				System.out.println("Startpanel destroyed");
			}
		});

	}

	/**
	 * @param args
	 */
	
	 public static void main(String[] args) {
		 System.out.println("Hello world");
		 openPanel();
		}
	

	public static void openPanel() {
		if(startpanel == null){
			startpanel = new Startpanel();
			System.out.println("Startpanel generated");
		}
		startpanel.setVisible(true);
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
		System.out.println("Chosen path: " + inputPath);
		return inputPath;
	}
	
}
