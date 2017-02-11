package plugin.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;

import executor.monitoring.Edge;
import executor.monitoring.ExecutionMonitor;
import graphBuilder.Controller;
import graphBuilder.TestCase;
import plugin.graph.GraphDrawer;
import plugin.utility.ClasspathResolver;
import plugin.utility.SourceFormatter;
import plugin.utility.TestLocator;

public class StartSettingDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8893913147091486760L;
	private JComboBox<String> projectBox;
	private DefaultListModel<String> testClassListModel = new DefaultListModel<>();
	private JList<String> testClassList;
	private List<IProject> projects;
	private IProject selectedProject;
	private Map<IProject, Collection<String>> testClassCache = new HashMap<>();
	JTextField scopeTextField = new JTextField();;
	
	private String outputType; // Pictures will be saved in this format.
	private String exec_gv;
	
	/**
	 * 
	 */
	public StartSettingDialog(String exec_gv) {
		this.exec_gv = exec_gv;
		outputType = "png";
		
		// ========== Labels ==========

		JLabel projectLabel = new JLabel("Project:");
		JLabel testClassLabel = new JLabel("Test classes:");
		JLabel scopeLabel = new JLabel("Scope:");

		// ========== Project list ==========

		projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()).stream()
				.filter(x -> isJavaProject(x)).collect(Collectors.toList());

		String[] projectnames = new String[projects.size()];
		for (int i = 0; i < projects.size(); i++) {
			projectnames[i] = projects.get(i).getName();
		}
		projectBox = new JComboBox<String>(projectnames);
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshTestClasses();
			}
		};

		projectBox.addActionListener(listener);

		// ========== testClass List ==========

		testClassList = new JList<>(testClassListModel);

		testClassList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane testClassScrollPane = new JScrollPane(testClassList);

		// ========== Start Button ==========

		JButton startButton = new JButton("Start");

		ActionListener listener2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int[] testClassSelection = testClassList.getSelectedIndices();
				if (testClassSelection.length == 0 || selectedProject == null) {
					JOptionPane.showMessageDialog(null, "Select project and testClasses");
					return;
				}
				List<String> testClasses = new ArrayList<>();
				for (int i = 0; i < testClassSelection.length; i++) {
					testClasses.add(testClassListModel.get(testClassSelection[i]));
				}

				String classpath = ClasspathResolver.getClasspath(selectedProject);

				BlockingQueue<Edge> edgeStream = new LinkedBlockingQueue<>(100000);

				BlockingQueue<TestCase> testCaseStream = new LinkedBlockingQueue<>(100000);
				String scope = scopeTextField.getText();

				System.out.println("Classpath: " + classpath);
				System.out.println("Test classes: " + testClasses);
				System.out.println("Scope: " + scope);

				Job compilingJob = new Job("Compiling project") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							selectedProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
						} catch (CoreException e) {
							e.printStackTrace();
						}
						return Status.OK_STATUS;
					}

				};

				Job executionJob = new Job("Debugging Testcase") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						ExecutionMonitor execMon = new ExecutionMonitor(testClasses, classpath, scope, edgeStream);
						execMon.startMonitoring(monitor);
						return Status.OK_STATUS;
					}

				};

				Job consumerJob = new Job("Processing output") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// TestConsumer consumer = new TestConsumer(edgeStream);
						// consumer.consume(arg0);
						Controller graphBuilder = new Controller(edgeStream, testCaseStream);
						graphBuilder.run();
						// Thread graphBuilderThr = new Thread(graphBuilder);

						/*
						 * // Create Graph (plugin) from the TestCaseStream
						 * GraphDrawer gd = new GraphDrawer(TestCaseStream);
						 * gd.run();
						 */

						return Status.OK_STATUS;
					}

				};

				Job drawerJob = new Job("Putting graph to screen") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {

						// Create Graph (plugin) from the TestCaseStream
						GraphDrawer gd = new GraphDrawer(testCaseStream, selectedProject, monitor);
						gd.setType(outputType);
						gd.setExecutable_graphviz(exec_gv);
						gd.run();

						return Status.OK_STATUS;
					}

				};

				compilingJob.setPriority(Job.INTERACTIVE);
				compilingJob.setUser(true);
				compilingJob.schedule();
				try {
					compilingJob.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				IProgressMonitor progressGroup = Job.getJobManager().createProgressGroup();
				progressGroup.beginTask("Debugging " + selectedProject.getName(), 100);
				executionJob.setProgressGroup(progressGroup, 99);
				consumerJob.setProgressGroup(progressGroup, 1);
				executionJob.schedule();
				consumerJob.schedule();
				drawerJob.schedule();
				/*
				// What if the directory if loaded before the drawerJob is finished?
				File temp = new File(selectedProject.getLocationURI());
				// Show the image files in the directory of the project.
				startpanel.refreshSplitPane(temp.getAbsolutePath());*/

				// graphBuilderThr.start();
				setVisible(false);
			}
		};
		startButton.addActionListener(listener2);

		// ========== Cancel Button ==========

		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		// ========== Reformat Button ==========

		JButton reformatButton = new JButton("Reformat Code");

		reformatButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (selectedProject == null) {
					JOptionPane.showMessageDialog(null, "No project selected");
					return;
				}
				int reply = JOptionPane.showConfirmDialog(null, "This will modify all source files in project \""
						+ selectedProject.getName() + "\", are you sure?", "Code Reformatting",
						JOptionPane.YES_NO_OPTION);
				if (reply != JOptionPane.YES_OPTION) {
					return;
				}

				Job reformattingJob = new Job("Reformatting project") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						SourceFormatter.format(selectedProject, monitor);
						return Status.OK_STATUS;
					}

				};
				reformattingJob.setUser(true);
				reformattingJob.schedule();
				try {
					reformattingJob.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// ========== Layout ==========

		GroupLayout layout = new GroupLayout(getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		SequentialGroup verticalGroup = layout.createSequentialGroup();
		SequentialGroup horizontalGroup = layout.createSequentialGroup();

		horizontalGroup
				.addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(projectLabel)
						.addComponent(testClassLabel).addComponent(scopeLabel))
				.addGroup(layout.createParallelGroup().addComponent(projectBox).addComponent(testClassScrollPane)
						.addComponent(scopeTextField).addGroup(layout.createSequentialGroup().addComponent(startButton)
								.addComponent(reformatButton).addComponent(cancelButton)));

		verticalGroup
				.addGroup(layout.createParallelGroup().addComponent(projectLabel).addComponent(projectBox, 0,
						GroupLayout.DEFAULT_SIZE, 25))
				.addGroup(layout.createParallelGroup().addComponent(testClassLabel).addComponent(testClassScrollPane))
				.addGroup(layout.createParallelGroup().addComponent(scopeLabel).addComponent(scopeTextField, 0,
						GroupLayout.DEFAULT_SIZE, 25))
				.addGroup(layout.createParallelGroup().addComponent(startButton).addComponent(reformatButton)
						.addComponent(cancelButton));

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
		setSize(400, 300);
		setLocationRelativeTo(null);
		getContentPane().setLayout(layout);

		Job backgroundPreLoading = new Job("Preloading test classes") {

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				for (IProject project : projects) {
					testClassCache.computeIfAbsent(project, value -> TestLocator.findTestClasses(project));
				}
				return Status.OK_STATUS;
			}

		};
		backgroundPreLoading.setPriority(Job.DECORATE);
		backgroundPreLoading.schedule();

	}

	private boolean isJavaProject(IProject project) {
		try {
			if (project.getDescription().hasNature(JavaCore.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void refreshTestClasses() {
		testClassListModel.removeAllElements();
		int selection = projectBox.getSelectedIndex();
		if (selection >= 0) {
			selectedProject = projects.get(selection);
			Collection<String> testClasses = testClassCache.computeIfAbsent(selectedProject,
					value -> TestLocator.findTestClasses(selectedProject));
			for (String testClass : testClasses) {
				testClassListModel.addElement(testClass);
			}
			scopeTextField.setText(greatestCommonPackageFragment());
		}
	}

	private String greatestCommonPackageFragment() {
		int size = testClassListModel.size();
		if (size == 0) {
			return "";
		}

		String smallestString = testClassListModel.get(0);

		for (int i = 1; i < size; i++) {
			String current = testClassListModel.get(i);
			if (current.length() < smallestString.length()) {
				smallestString = current;
			}
		}

		char commonCharacter = '*';

		for (int characterIndex = 0; characterIndex < smallestString.length(); characterIndex++) {
			for (int stringIndex = 0; stringIndex < testClassListModel.size(); stringIndex++) {
				if (stringIndex == 0) {
					commonCharacter = testClassListModel.get(0).charAt(characterIndex);
					continue;
				}
				if (testClassListModel.get(stringIndex).charAt(characterIndex) != commonCharacter) {
					String commonPrefix = smallestString.substring(0, characterIndex);
					int lastDot = commonPrefix.lastIndexOf('.');
					if (lastDot == -1) {
						return "";
					}
					return commonPrefix.substring(0, lastDot + 1)+"*";
				}
			}
		}

		int lastDot = smallestString.lastIndexOf('.');
		
		if (lastDot == -1) {
			return "";
		}

		return smallestString.substring(0, lastDot + 1)+"*";
	}
	
	public void setOutputType(String newType) {
		outputType = newType;
	}

	public Container getPane() {
		return getContentPane();
	}
	
	public void setExecutable_graphviz(String newExec) {
		exec_gv = newExec;
	}
	
}
