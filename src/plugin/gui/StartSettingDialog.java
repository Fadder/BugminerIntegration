package plugin.gui;

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

import executor.monitoring.Edge;
import executor.monitoring.ExecutionMonitor;
//import executor.monitoring.TestConsumer; for simple testing concerns
import graphBuilder.Controller;
import graphBuilder.TestCase;
import plugin.utility.ClasspathResolver;
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
	private Map<IProject, Collection<String>> testClassCache=new HashMap<>();

	/**
	 * 
	 */
	public StartSettingDialog() {

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

		// ========== Scope TextField ==========

		JTextField scopeTextField = new JTextField();
		scopeTextField.setText("(e.g. org.apache.commons.lang3. )");

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
				BlockingQueue<TestCase> TestCaseStream = new LinkedBlockingQueue<>(100000);
				String scope = scopeTextField.getText().replace("*", "");

				System.out.println("Classpath: " + classpath);
				System.out.println("Test classes: " + testClasses);
				System.out.println("Scope: " + scope);
				
				Job compilingJob = new Job("Compiling project"){
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
						ExecutionMonitor execMon = new ExecutionMonitor(classpath, edgeStream, scope, testClasses);
						execMon.startMonitoring(monitor);
						return Status.OK_STATUS;
					}

				};

				Job consumerJob = new Job("Processing output") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						//TestConsumer consumer = new TestConsumer(edgeStream);
						//consumer.consume(arg0);
						Controller graphBuilder = new Controller(edgeStream, TestCaseStream);
						graphBuilder.run();
						// Thread graphBuilderThr = new Thread(graphBuilder);
						
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
				progressGroup.beginTask("Debugging " +selectedProject.getName(), 100);
				executionJob.setProgressGroup(progressGroup, 99);
				consumerJob.setProgressGroup(progressGroup, 1);
				executionJob.schedule();
				consumerJob.schedule();

				// graphBuilderThr.start();
				setVisible(false);
			}
		};
		startButton.addActionListener(listener2);

		// ========== Cancel Button ==========

		JButton cancelButton = new JButton("Cancel");
		ActionListener listener3 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(listener3);

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
						.addComponent(scopeTextField)
						.addGroup(layout.createSequentialGroup().addComponent(startButton).addComponent(cancelButton)));

		verticalGroup
				.addGroup(layout.createParallelGroup().addComponent(projectLabel).addComponent(projectBox, 0,
						GroupLayout.DEFAULT_SIZE, 25))
				.addGroup(layout.createParallelGroup().addComponent(testClassLabel).addComponent(testClassScrollPane))
				.addGroup(layout.createParallelGroup().addComponent(scopeLabel).addComponent(scopeTextField, 0,
						GroupLayout.DEFAULT_SIZE, 25))
				.addGroup(layout.createParallelGroup().addComponent(startButton).addComponent(cancelButton));

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
		setSize(400, 300);
		setLocationRelativeTo(null);
		getContentPane().setLayout(layout);
		
		
		Job backgroundPreLoading=new Job("Preloading test classes"){

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				for(IProject project: projects){
					testClassCache.computeIfAbsent(project,value-> TestLocator.findTestClasses(project));
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
			Collection<String> testClasses=testClassCache.computeIfAbsent(selectedProject,value-> TestLocator.findTestClasses(selectedProject));
			for (String testClass : testClasses) {
				testClassListModel.addElement(testClass);
			}
		}
	}

}
