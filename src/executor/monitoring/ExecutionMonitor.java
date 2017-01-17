package executor.monitoring;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

public class ExecutionMonitor {

	private String classpath;
	private BlockingQueue<Edge> queue;
	private String projectPackageNameToMonitor;// Whitelist
	private List<String> testClasses;
	private JVMConnectionCreator jvmConnectionCreator;
	private MethodExecutionLogger methodExecutionLogger;
	private IProgressMonitor monitor;
	private int currentTestCase = 0;

	private static final String executorClassName = "executor.testexecution.TestsuitExecuter";

	// Requests, die gefiltert werden sollen
	private MethodEntryRequest methodEntryRequest;


	/**
	 * dafür da, falls die Testklassen als String uebergeben werden Testklassen
	 * müssen vollqualifiziert sein z.B.
	 * org.execution_monitor.main.zuTestendeKlassen.HalloWeltTest
	 */
	public ExecutionMonitor(String classpath, BlockingQueue<Edge> queue, String projectPackageNameToMonitor,
			List<String> testClasses) {
		this.classpath = classpath;
		this.queue = queue;
		this.projectPackageNameToMonitor = projectPackageNameToMonitor;
		this.testClasses = testClasses;
		this.jvmConnectionCreator = new JVMConnectionCreator(executorClassName, this.testClasses, this.classpath);

		methodExecutionLogger = new MethodExecutionLogger();
	}

	/**
	 * startet den ganzen Debugging Prozess
	 */
	public void startMonitoring(IProgressMonitor monitor) {
		this.monitor = SubMonitor.convert(monitor, testClasses.size());
		if (monitor != null) {
			this.monitor.setTaskName("Debugger");
			this.monitor.subTask("Start-up phase");
		}
		VirtualMachine vm = jvmConnectionCreator.launchAndConnectToTestsuitExecuter();

		// TODO Input und Output umleiten, da sonst VM abstürzen kann
		Process proc = vm.process();
		new Thread(new IORedirecter(proc.getInputStream(), System.out)).start();
		new Thread(new IORedirecter(proc.getErrorStream(), System.err)).start();

		defineMonitoringRequests(vm);

		handleEvents(vm);
	}

	/**
	 * pro VM gibt es einen EventRequestManager und eine EventQueue beim
	 * EventRequestManager muss man registrieren welche Events in die EventQueue
	 * aufgenommen werden
	 */
	private void defineMonitoringRequests(VirtualMachine vm) {
		EventRequestManager eventRequestManager = vm.eventRequestManager();
		ThreadReference mainThread=getMainThreadReferenceFrom(vm);
		methodEntryRequest = eventRequestManager.createMethodEntryRequest();
		MethodExitRequest methodExitRequest = eventRequestManager.createMethodExitRequest();
		StepRequest stepRequest = eventRequestManager.createStepRequest(mainThread, StepRequest.STEP_LINE,
				StepRequest.STEP_INTO);
		ExceptionRequest exceptionRequest = eventRequestManager.createExceptionRequest(null, true, false);

		methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);

		for (String testClass : testClasses) {
			methodEntryRequest.addClassExclusionFilter(testClass);
			methodExitRequest.addClassExclusionFilter(testClass);
			stepRequest.addClassExclusionFilter(testClass);
			exceptionRequest.addClassExclusionFilter(testClass);
		}
		methodExitRequest.addClassFilter(projectPackageNameToMonitor+"*");
		stepRequest.addClassFilter(projectPackageNameToMonitor+"*");
		exceptionRequest.addClassFilter(projectPackageNameToMonitor+"*");
		
		methodEntryRequest.addThreadFilter(mainThread);
		methodExitRequest.addThreadFilter(mainThread);
		exceptionRequest.addThreadFilter(mainThread);
		
		methodEntryRequest.enable();
		methodExitRequest.enable();
		stepRequest.enable();
		exceptionRequest.enable();
	}

	private void handleEvents(VirtualMachine vm) {
		boolean running = true;
		EventQueue eventQ = vm.eventQueue();

		while (running) {
			if (monitor!=null&& monitor.isCanceled()) {
				vm.exit(-1);
				try {
					queue.put(Edge.LAST_EDGE);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new OperationCanceledException();
			}
			EventSet eventSet = null;
			try {
				eventSet = eventQ.remove();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (VMDisconnectedException e) {
				System.out.println("TestsuitExecuter hat sich beendet");// TODO
																		// Fehlerausgaben
																		// von
																		// TestsuitExecuter
																		// sollen
																		// angezeigt
																		// werden
				break;
			}

			EventIterator eventIterator = eventSet.eventIterator();
			while (eventIterator.hasNext()) {
				Event event = eventIterator.nextEvent();

				if (event instanceof MethodEntryEvent) {
					try {
						handleMethodEntryEvent((MethodEntryEvent) event);
					} catch (AbsentInformationException e) {
						e.printStackTrace();
					}
				} else if (event instanceof MethodExitEvent) {
					handleMethodExitEvent((MethodExitEvent) event);
				} else if (event instanceof StepEvent) {
					handleStepEvent((StepEvent) event);
				} else if (event instanceof ExceptionEvent) {
					handleExceptionEvent((ExceptionEvent) event);
				}
				// vm.resume(); //an dieser Position wird manchmal eine
				// VMDisconnectedException verursacht
			}
			eventSet.resume();// besser als vm.resume(), da keine
								// VMDisconnectedException geworfen wird
		}

		try {
			queue.put(Edge.LAST_EDGE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (monitor != null) {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * True --> Event soll ignoriert werden False --> bei Events aus dem
	 * Testprojekt und TestsuitExecuter
	 */
	private boolean filterEvent(MethodEntryEvent event) {
		String eventClass = event.location().declaringType().name();
		if (eventClass.equals(executorClassName) || eventClass.startsWith(projectPackageNameToMonitor)) {
			return false;
		}
		
		int oldPos = -1;
		int newPos = eventClass.indexOf('.');
		while (newPos != -1) {
			String subString = eventClass.substring(0, newPos + 1);

			if (!projectPackageNameToMonitor.startsWith(subString) && !executorClassName.startsWith(subString)) {
				methodEntryRequest.disable();		

				String newFilter = subString + "*";
				methodEntryRequest.addClassExclusionFilter(newFilter);

				methodEntryRequest.enable();
				return true;
			}
			oldPos = newPos;
			newPos = eventClass.indexOf('.', oldPos + 1);
		}

		return true;
	}

	private void handleStepEvent(StepEvent event) {
		Location location = event.location();
		Method method = location.method();
		if (!method.isConstructor() && !method.isStaticInitializer()) {

			if (methodExecutionLogger.isMethodEntered()) {
				// diese Konstrukt ist nötig da bei einem Methodeneintritt ein
				// MethodentryEvent und Stepevent generiert werden
				methodExecutionLogger.setMethodEntered(false);
			} else {
				addTransition(method.toString(), methodExecutionLogger.getLastLineNumber(), location.lineNumber());
				methodExecutionLogger.stepEvent(location.lineNumber());
			}
		}
	}

	private void handleMethodExitEvent(MethodExitEvent event) {
		Method method= event.method();
		if (!method.isConstructor() && !method.isStaticInitializer()) {
			methodExecutionLogger.setMethodEntered(false);// nötig, da bei
															// Methoden, die nur
															// einen return
															// Statement haben
															// kein StepEvent
															// generiert wird
			methodExecutionLogger.exitCurrentMethod();
		}
	}

	private void handleMethodEntryEvent(MethodEntryEvent event) throws AbsentInformationException {
		if(filterEvent(event)){
			return;
		}
		Method method= event.method();
		if (!method.isConstructor() && !method.isStaticInitializer()) {
			String methodname = method.toString();
			// --------------TestsuitExecuterMethoden-------
			if (methodname.startsWith(executorClassName)) {
				// es handelt sich um den TestsuitExecuter
				// an dem abstrakten Callstack wird nix gemacht!!

				// wurde gerade eine MetaMethode aufgerufen?
				try {
					switch (methodname) {
					case executorClassName + ".testfallErfolgreich()":
						queue.put(Edge.TESTCASE_SUCCESS);
						break;
					case executorClassName + ".testfallGefailt()":
						queue.put(Edge.TESTCASE_FAILURE);
						break;
					case executorClassName + ".executeTestClassMethodByMethod(java.lang.Class)":
						if (monitor == null) {
							break;
						}
						if (currentTestCase != 0) {
							monitor.worked(1);
						}
						monitor.subTask(testClasses.get(currentTestCase++) + " (" + currentTestCase + "/"
								+ testClasses.size() + ")");
						break;
					default:
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;

			}
			// ---------------------------------------------

			// ab hier Events aus dem Testprojekt
			methodExecutionLogger.setMethodEntered(true);

			int lineNumber = event.location().lineNumber();
			addTransitionMethodEntered(methodname, lineNumber, methodExecutionLogger.getLastMethodname());

			methodExecutionLogger.enterNewMethod(methodname, lineNumber);
		}
	}

	private void handleExceptionEvent(ExceptionEvent event) {
		Method method= event.location().method();
		if (!method.isConstructor() && !method.isStaticInitializer()) {
			String catchLocation = event.catchLocation().method().toString();
			methodExecutionLogger.repairAfterException(catchLocation);// der
																		// abstrakte
																		// Callstack
																		// wird
																		// mit
																		// dem
																		// aktuellen
																		// Callstack
																		// synchronisiert
		}
	}

	// gibt den Main-Thread vom TestsuitExecuter zurueck
	private ThreadReference getMainThreadReferenceFrom(VirtualMachine vm) {
		ThreadReference mainThreadInVM = null;
		for (ThreadReference each : vm.allThreads()) {
			if (each.name().equals("main")) {
				mainThreadInVM = each;
				break;
			}
		}

		if (mainThreadInVM == null) {
			throw new IllegalStateException("TestsuitExecuter hat kein 'main' Thread");
		} else {
			return mainThreadInVM;
		}

	}

	private void addTransitionMethodEntered(String fullyQualifiedMethodname, int lineTo, String enteredFromMethod) {
		// addTransition(fullyQualifiedMethodname, -1, lineTo);
		Edge transition = new Edge(fullyQualifiedMethodname, -1, lineTo, enteredFromMethod);

		queue.add(transition);
	}

	private void addTransition(String fullyQualifiedMethodname, int lineFrom, int lineTo) {
		Edge transition = new Edge(fullyQualifiedMethodname, lineFrom, lineTo);

		queue.add(transition);
	}

}
