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
	private String scope;
	private JVMConnectionCreator jvmConnectionCreator;
	private MethodExecutionLogger methodExecutionLogger;
	private EventRequestManager eventRequestManager;

	private List<String> testClasses;
	private int currentTestClass = 0;

	private IProgressMonitor monitor;

	// die test ausführende Klasse und ihre beobachtete Methoden
	private static final String executorClassName = "executor.testexecution.TestsuitExecuter";
	private static final String executorSuccessMethod = executorClassName + ".testfallErfolgreich(java.lang.String)";
	private static final String executorFailureMethod = executorClassName + ".testfallGefailt(java.lang.String)";
	private static final String executorNewTestMethod = executorClassName
			+ ".executeTestClassMethodByMethod(java.lang.Class)";

	private ClassPrepareRequest classPrepareRequest;

	/**
	 * Erstellt einen neuen ExecutionMonitor
	 * 
	 * @param testClasses
	 *            Testklassen die untersucht werden sollen
	 * @param classpath
	 *            der kombinierte classpath für alle Testklassen und dem
	 *            TestSuitExecutor selbst
	 * @param scope
	 *            der scope der Untersuchung, also welche Klassen bzw Packete
	 *            untersucht werden sollen z.b. org.apache.*
	 * @param queue
	 *            die queue in die die Ergebnisse ausgegeben werden
	 */
	public ExecutionMonitor(List<String> testClasses, String classpath, String scope, BlockingQueue<Edge> queue) {
		this.classpath = classpath;
		this.queue = queue;
		this.scope = scope;
		this.testClasses = testClasses;
		this.jvmConnectionCreator = new JVMConnectionCreator(executorClassName, this.testClasses, this.classpath);

		methodExecutionLogger = new MethodExecutionLogger();
	}

	/**
	 * Startet den Debugging Prozess
	 * 
	 * @param monitor
	 *            Falls Abbruchmoeglichkeit und Fortschrittsberichte erwuenscht
	 *            sind, sonst null
	 */
	public void startMonitoring(IProgressMonitor monitor) {
		if (monitor != null) {
			this.monitor = SubMonitor.convert(monitor, testClasses.size());
			this.monitor.setTaskName("Debugger");
			this.monitor.subTask("Start-up phase");
		}
		VirtualMachine vm = jvmConnectionCreator.launchAndConnectToTestsuitExecuter();

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
		eventRequestManager = vm.eventRequestManager();
		ThreadReference mainThread = getMainThreadReferenceFrom(vm);
		MethodEntryRequest methodEntryRequest = eventRequestManager.createMethodEntryRequest();
		MethodExitRequest methodExitRequest = eventRequestManager.createMethodExitRequest();
		StepRequest stepRequest = eventRequestManager.createStepRequest(mainThread, StepRequest.STEP_LINE,
				StepRequest.STEP_INTO);
		ExceptionRequest exceptionRequest = eventRequestManager.createExceptionRequest(null, true, false);
		classPrepareRequest = eventRequestManager.createClassPrepareRequest();

		methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);

		// Filtert die Testklassen an sich aus
		for (String testClass : testClasses) {
			methodEntryRequest.addClassExclusionFilter(testClass);
			methodExitRequest.addClassExclusionFilter(testClass);
			stepRequest.addClassExclusionFilter(testClass);
			exceptionRequest.addClassExclusionFilter(testClass);
		}
		if (scope != null && !scope.isEmpty()) {
			// whitelist ansatz, betrachte nur events in unserem scope
			methodEntryRequest.addClassFilter(scope);
			methodExitRequest.addClassFilter(scope);
			stepRequest.addClassFilter(scope);
			exceptionRequest.addClassFilter(scope);
		}
		// der ClassPrepareRequest wird nur verwendet um das erste Laden des
		// TestsuitExecutor zu verarbeiten
		classPrepareRequest.addClassFilter(executorClassName);

		// Betrachte nur den mainThread
		methodEntryRequest.addThreadFilter(mainThread);
		methodExitRequest.addThreadFilter(mainThread);
		exceptionRequest.addThreadFilter(mainThread);

		methodEntryRequest.enable();
		methodExitRequest.enable();
		stepRequest.enable();
		exceptionRequest.enable();
		classPrepareRequest.enable();
	}

	private void handleEvents(VirtualMachine vm) {
		boolean running = true;
		EventQueue eventQ = vm.eventQueue();

		while (running) {
			if (monitor != null && monitor.isCanceled()) {
				vm.exit(-1);
				try {
					queue.put(Edge.createLastEdge());
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

				if (event instanceof StepEvent) {
					handleStepEvent((StepEvent) event);
				} else if (event instanceof MethodEntryEvent) {
					try {
						handleMethodEntryEvent((MethodEntryEvent) event);
					} catch (AbsentInformationException e) {
						e.printStackTrace();
					}
				} else if (event instanceof MethodExitEvent) {
					handleMethodExitEvent((MethodExitEvent) event);
				} else if (event instanceof ExceptionEvent) {
					handleExceptionEvent((ExceptionEvent) event);
				} else if (event instanceof BreakpointEvent) {
					try {
						handleBreakpointEvent((BreakpointEvent) event);
					} catch (IncompatibleThreadStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (event instanceof ClassPrepareEvent) {
					handleClassPrepareEvent((ClassPrepareEvent) event);
				}
				// vm.resume(); //an dieser Position wird manchmal eine
				// VMDisconnectedException verursacht
			}
			eventSet.resume();// besser als vm.resume(), da keine
								// VMDisconnectedException geworfen wird
		}

		try {
			queue.put(Edge.createLastEdge());
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
	 * Unser TestsuitExecutor wird hier zum ersten mal geladen, wir setzen
	 * breakpoints bei den meta methoden um deren Aufruf zu registrieren.
	 * Dadurch brauchen wir keine MethodEntryEvents bei unserer Klasse und
	 * können so einen whitelist benutzen.
	 */
	private void handleClassPrepareEvent(ClassPrepareEvent event) {
		ReferenceType testsuitExecutorClass = event.referenceType();

		for (Method method : testsuitExecutorClass.allMethods()) {
			String methodName = method.toString();
			if (methodName.equals(executorSuccessMethod) || methodName.equals(executorFailureMethod)
					|| methodName.equals(executorNewTestMethod)) {
				try {
					List<Location> lines = method.allLineLocations();
					// setze breakpoint bei der ersten ausführbaren linie
					BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(lines.get(0));
					breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					breakpointRequest.enable();
				} catch (AbsentInformationException e) {
					e.printStackTrace();
				}

			}
		}
		classPrepareRequest.disable();
	}

	/**
	 * Das sind die pseudo MethodEntryEvents für unsere TestsuitExecutor klasse
	 * für die meta methoden
	 */
	private void handleBreakpointEvent(BreakpointEvent event) throws IncompatibleThreadStateException {
		Method method = event.location().method();
		String methodName = method.toString();

		if (methodName.equals(executorNewTestMethod)) {
			// Signalisierung das eine neue Testklasse bearbeitet wird
			if (monitor == null) {
				return;
			}
			if (currentTestClass != 0) {
				monitor.worked(1);
			}
			monitor.subTask(
					testClasses.get(currentTestClass++) + " (" + currentTestClass + "/" + testClasses.size() + ")");
			return;
		}

		// Testcase ist erledigt, argument ist der name der testcase methode
		String testcase = ((StringReference) event.thread().frame(0).getArgumentValues().get(0)).value();
		try {
			if (methodName.equals(executorSuccessMethod)) {
				queue.put(Edge.createSuccessfulTestcaseEdge(testcase));
			} else {
				queue.put(Edge.createfailedTestcaseEdge(testcase));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
				queue.add(Edge.createStepEdge(method.toString(), methodExecutionLogger.getLastLineNumber(),
						location.lineNumber()));
				methodExecutionLogger.stepEvent(location.lineNumber());
			}
		}
	}

	private void handleMethodExitEvent(MethodExitEvent event) {
		Method method = event.method();
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
		Method method = event.method();
		if (!method.isConstructor() && !method.isStaticInitializer()) {
			String methodname = method.toString();

			methodExecutionLogger.setMethodEntered(true);

			int lineNumber = event.location().lineNumber();
			queue.add(Edge.createMethodEntryEdge(method.toString(), methodExecutionLogger.getLastMethodname(),
					lineNumber));

			methodExecutionLogger.enterNewMethod(methodname, lineNumber);
		}
	}

	private void handleExceptionEvent(ExceptionEvent event) {
		Method method = event.location().method();
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

}
