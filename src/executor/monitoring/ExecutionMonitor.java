package executor.monitoring;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;


public class ExecutionMonitor {

    private String classpath;
    private BlockingQueue<Edge> queue;
    private String projectPackageNameToMonitor;//Whitelist
    private String[] testClasses;
    private JVMConnectionCreator jvmConnectionCreator;
    private MethodExecutionLogger methodExecutionLogger;

    private static String testSuitExecuterMainClassName = "executor.testexecution.TestsuitExecuter";

    //hier muss man die Markierungsmethoden von dem Testsuitexecuter aufnehmen
    private static HashMap<String, String> metaMethodenTestsuitExecuter;

    //Packeges, die rausgefiltert werden sollen
    private static String[] blackList = {"java.*", "sun.*"};


    /**
     * hier werden alle MetaMethoden aus dem TestsuitExecuter aufgenommen
     */
    static {
        metaMethodenTestsuitExecuter = new HashMap<>();

        metaMethodenTestsuitExecuter.put("executor.testexecution.TestsuitExecuter.testfallErfolgreich()", "Erfolgreich");
        metaMethodenTestsuitExecuter.put("executor.testexecution.TestsuitExecuter.testfallGefailt()", "Testfall ist gefailt");
    }

    /**
     * Constructor kann mit Testklassen vom Typ Class umgehen
     */
    public ExecutionMonitor(String classpath, BlockingQueue<Edge> queue,
                            String projectPackageNameToMonitor, Class<?>... testClasses) {
        this.classpath = classpath;
        this.queue = queue;
        this.projectPackageNameToMonitor = projectPackageNameToMonitor;

        this.testClasses = new String[testClasses.length];
        //Testklassen werden in Strings umgewandelt, da der TestuitExecuter als Parameter nur Strings haben kann
        int i = 0;
        for (Class<?> each: testClasses) {
            this.testClasses[i] = each.getName();
            i++;
        }
        this.jvmConnectionCreator = new JVMConnectionCreator(testSuitExecuterMainClassName, this.testClasses, this.classpath);

        methodExecutionLogger = new MethodExecutionLogger();
    }

    /**
     * dafür da, falls die Testklassen als String uebergeben werden
     * Testklassen müssen vollqualifiziert sein z.B. org.execution_monitor.main.zuTestendeKlassen.HalloWeltTest
     */
    public ExecutionMonitor(String classpath, BlockingQueue<Edge> queue,
                            String projectPackageNameToMonitor, String... testClasses) {
        this.classpath = classpath;
        this.queue = queue;
        this.projectPackageNameToMonitor = projectPackageNameToMonitor;
        this.testClasses = testClasses;
        this.jvmConnectionCreator = new JVMConnectionCreator(testSuitExecuterMainClassName, this.testClasses, this.classpath);

        methodExecutionLogger = new MethodExecutionLogger();
    }

    /**
     * startet den ganzen Debugging Prozess
     */
    public void startMonitoring() {
        VirtualMachine vm = jvmConnectionCreator.launchAndConnectToTestsuitExecuter();

        //TODO Input und Output umleiten, da sonst VM abstürzen kann
        Process proc = vm.process();

        defineMonitoringRequests(vm);

        handleEvents(vm);
    }

    /**
     * pro VM gibt es einen EventRequestManager und eine EventQueue
     * beim EventRequestManager muss man registrieren welche Events in die EventQueue aufgenommen werden
     */
    private void defineMonitoringRequests(VirtualMachine vm) {
        EventRequestManager eventRequestManager = vm.eventRequestManager();

        //TODO Suspend-Policy angeben?
        MethodEntryRequest methodEntryRequest = eventRequestManager.createMethodEntryRequest();
        MethodExitRequest methodExitRequest = eventRequestManager.createMethodExitRequest();
        StepRequest stepRequest = eventRequestManager.createStepRequest(getMainThreadReferenceFrom(vm), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
        ExceptionRequest exceptionRequest = eventRequestManager.createExceptionRequest(null, true, false);

        excludeClassesFromMonitoring(methodEntryRequest, methodExitRequest, stepRequest, exceptionRequest, vm);

        methodEntryRequest.enable();
        methodExitRequest.enable();
        stepRequest.enable();
        exceptionRequest.enable();
    }

    /**
     * wir müssen alle bekannten Packages ausschließen, die wir nicht verwenden wollen --> TODO: blacklist erweitern
     * dadurch kann die Performence gesteigert werden
     * der eigene Filter --> filterEvent(Event event) , filtert aus den restlichen Events nurnoch die nötigsten Events raus
     * dies ist nötig, da wir Klassen aus zwei Packages überwachen wollen --> Testprojekt-Package und Packge von dem TestsuitExecuter
     */
    private void excludeClassesFromMonitoring(MethodEntryRequest methodEntryRequest, MethodExitRequest methodExitRequest, StepRequest stepRequest, ExceptionRequest exceptionRequest, VirtualMachine vm) {
        excludeListOfPackages(blackList, methodEntryRequest, methodExitRequest, stepRequest, exceptionRequest);
        //JUnit Testklassen sollen auch nicht ueberwacht werden
        excludeListOfPackages(testClasses, methodEntryRequest, methodExitRequest, stepRequest, exceptionRequest);
    }

    private void excludeListOfPackages(String[] excludeList, MethodEntryRequest methodEntryRequest, MethodExitRequest methodExitRequest, StepRequest stepRequest, ExceptionRequest exceptionRequest) {
        for (String each : excludeList) {
            methodEntryRequest.addClassExclusionFilter(each);
            methodExitRequest.addClassExclusionFilter(each);
            stepRequest.addClassExclusionFilter(each);
            exceptionRequest.addClassExclusionFilter(each);
        }
    }

    private void handleEvents(VirtualMachine vm) {
        boolean running = true;
        EventQueue eventQ = vm.eventQueue();

        while (running) {
            EventSet eventSet = null;
            try {
                eventSet = eventQ.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (VMDisconnectedException e) {
                System.out.println("TestsuitExecuter hat sich beendet");//TODO Fehlerausgaben von TestsuitExecuter sollen angezeigt werden
                break;
            }

            EventIterator eventIterator = eventSet.eventIterator();
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();

                //Events werden rausgefiltert
                if (filterEvent(event)) {
                    continue;
                }

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
                //vm.resume(); //an dieser Position wird manchmal eine VMDisconnectedException verursacht
            }
            eventSet.resume();//besser als vm.resume(), da keine VMDisconnectedException geworfen wird

            printProtokoll(); //das verwenden, falls man die Events "live" ausgeben möchte
        }
        //printProtokoll2(); //das verwenden, falls man die Events erst nach Beendigung von TestsuitExecuter ausgeben möchte

    }

    private void printProtokoll2() {
        while (!queue.isEmpty()) {
            Edge transition = queue.remove();

            System.out.println("----------------------------------");
            System.out.println("CalledFrom: " + transition.getEnteredFromMethod());
            System.out.println("Methodenname: " + transition.getMethod());
            System.out.println("lineFrom: " + transition.getLineFrom());
            System.out.println("lineTo: " + transition.getLineTo());
        }
    }

    private void printProtokoll() {
        if (queue.isEmpty()) {
            return;
        }
        Edge transition = queue.remove();

        System.out.println("----------------------------------");
        System.out.println("CalledFrom: " + transition.getEnteredFromMethod());
        System.out.println("Methodenname: " + transition.getMethod());
        System.out.println("lineFrom: " + transition.getLineFrom());
        System.out.println("lineTo: " + transition.getLineTo());
    }

    /**
     * True --> Event soll ignoriert werden
     * False --> bei Events aus dem Testprojekt und TestsuitExecuter
     */
    private boolean filterEvent(Event event) {
        if (event.toString().contains(testSuitExecuterMainClassName) || event.toString().contains(projectPackageNameToMonitor)) {
            return false;
        }
        return true;
    }

    private void handleStepEvent(StepEvent event) {
        if (!event.location().method().isConstructor() && !event.location().method().isStaticInitializer()) {
           if (event.location().method().toString().startsWith(testSuitExecuterMainClassName)) {
                //es handelt sich um den TestsuitExecuter --> hier soll alles ignoriert werden
               return;
            }

            if (methodExecutionLogger.isMethodEntered()) {
               //diese Konstrukt ist nötig da bei einem Methodeneintritt ein MethodentryEvent und Stepevent generiert werden
                methodExecutionLogger.setMethodEntered(false);
            }
            else {
                addTransition(event.location().method().toString(), methodExecutionLogger.getLastLineNumber(), event.location().lineNumber());
                methodExecutionLogger.stepEvent(event.location().lineNumber());
            }
        }
    }

    private void handleMethodExitEvent(MethodExitEvent event) {
        if (!event.method().isConstructor() && !event.location().method().isStaticInitializer()) {
            //beim TestsuitExecuter wird nix an dem abstrakten Callstack gemacht
            if (event.location().method().toString().startsWith(testSuitExecuterMainClassName)) {
                return;
            }

            methodExecutionLogger.setMethodEntered(false);//nötig, da bei Methoden, die nur einen return Statement haben kein StepEvent generiert wird
            methodExecutionLogger.exitCurrentMethod();
        }
    }

    private void handleMethodEntryEvent(MethodEntryEvent event) throws AbsentInformationException {
        if (!event.method().isConstructor() && !event.location().method().isStaticInitializer()) {

            //--------------TestsuitExecuterMethoden-------
            if (event.location().method().toString().startsWith(testSuitExecuterMainClassName)) {
                //es handelt sich um den TestsuitExecuter
                //an dem abstrakten Callstack wird nix gemacht!!

                //wurde gerade eine MetaMethode aufgerufen?
                if (metaMethodenTestsuitExecuter.containsKey(event.location().method().toString())) {
                    //Markierungsmethode wurde aufgerufen

                    //Testfall abgelaufen --> sicherheitshalber wird der abstrakte Callstack resetet
                    //sonst koennten Exceptions Probleme verursachen
                    methodExecutionLogger.reset();

                    if (metaMethodenTestsuitExecuter.get(event.location().method().toString()).equals("Erfolgreich")) {
                        System.out.println("!!!!!!!!!!!!!!!!!Testfall war erfolgreich!!!!!!!!!!!!!!!!!!!!!!!");
                        return;
                    } else if (metaMethodenTestsuitExecuter.get(event.location().method().toString()).equals("Testfall ist gefailt")) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!Testfall war nicht erfolgreich!!!!!!!!!!!!!!!!!!!");
                        return;
                    } else {
                        throw new IllegalStateException("Irgendwas stimmt mit der Hashmap nicht");
                    }

                } else {
                    //sonst mit naechstem Event weitermachen
                    return;
                }
            }
            //---------------------------------------------

            //ab hier Events aus dem Testprojekt
            methodExecutionLogger.setMethodEntered(true);

            String newMethodname = event.location().method().toString();
            int lineNumber = event.location().lineNumber();
            addTransitionMethodEntered(newMethodname, lineNumber, methodExecutionLogger.getLastMethodname());

            methodExecutionLogger.enterNewMethod(newMethodname, lineNumber);
        }
    }

    private void handleExceptionEvent(ExceptionEvent event) {
        if (!event.location().method().isConstructor() && !event.location().method().isStaticInitializer()) {
            String catchLocation = event.catchLocation().method().toString();
            methodExecutionLogger.repairAfterException(catchLocation);//der abstrakte Callstack wird mit dem aktuellen Callstack synchronisiert
        }
    }

    //gibt den Main-Thread vom TestsuitExecuter zurueck
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
        //addTransition(fullyQualifiedMethodname, -1, lineTo);
        Edge transition = new Edge(fullyQualifiedMethodname, -1, lineTo, enteredFromMethod);

        queue.add(transition);
    }

    private void addTransitionMethodExit(String fullyQualifiedMethodname, int lineTo) {
        addTransition(fullyQualifiedMethodname, -9, -9);
    }

    private void addTransition(String fullyQualifiedMethodname, int lineFrom, int lineTo) {
        Edge transition = new Edge(fullyQualifiedMethodname, lineFrom, lineTo);

        queue.add(transition);
    }




}


