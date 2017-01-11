package executor.testexecution;

//import org.execution_monitor.main.zuTestendeKlassen.HalloWelt;
//import org.execution_monitor.main.zuTestendeKlassen.HalloWeltTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class TestsuitExecuter {

    private Class<?>[] testClasses;

    public TestsuitExecuter(Class<?>[] testClasses) {
        this.testClasses = testClasses;
    }


    //Parameter sind Namen von Klassen (diese müssen vollqualifiziert sein z.B. "org.execution_monitor.main.zuTestendeKlassen.HalloWeltTest)
    public static void main(String[] args) {
        Class<?>[] testClasses = new Class[args.length];

        if (args.length < 1) {
            throw new IllegalStateException("Man muss mindestens eine Testklasse angeben! --> TestsuitExecuter");
        }

        //Aus den Parametern, die Strings sind, werden '.class'-Files erstellt
        int i = 0;
        for (String each: args) {
            Class<?> testClass = null;
            try {
                testClass = Class.forName(each);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalStateException("Man muss voll qualifizierten Klassenamen angeben z.B. org.am.Classname");
            }

            testClasses[i] = testClass;
            i++;
        }

        TestsuitExecuter testsuitExecuter = new TestsuitExecuter(testClasses);
       // testsuitExecuter.executeTestClasses();

        testsuitExecuter.executeTestClassesMethodByMethod();
    }

    //alle Tests werden nacheinander ausgeführt und wir können nicht bestimmen was zwischen den Testausführungen passiert
    private void executeTestClasses() {
        Result result = JUnitCore.runClasses(testClasses);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        System.out.println(result.wasSuccessful());
    }

    //alle Tests werden ausgeführt aber wir können bestimmen was zwischen den Testausführungen passiert
    //--> wir verwenden hier 'Markierungsmethoden', diese geben unserem Debugger 'ExecutionMonitor' Metadaten z.B.
    //ob ein Test erflogreich war oder nicht
    private void executeTestClassesMethodByMethod() {
        List<Method> testMethods;
        Request request;
        JUnitCore jUnitCore = new JUnitCore();


        for (Class<?> testClass : testClasses) {
            testMethods = new ArrayList<>();

            //Alle TestMethoden pro Klasse werden gesammelt
            for (Method method : testClass.getMethods()) {
                Annotation[] annotations = method.getAnnotations();

                for (Annotation annotation : annotations) {

                    if (annotation.toString().contains("@org.junit.Test")) {
                        testMethods.add(method);
                    }
                }

            }

            //Testmethoden werden ausgeführt
            for (Method testMethod : testMethods) {
                request = Request.method(testClass, testMethod.getName());
                Result result = jUnitCore.run(request);

                if (result.getFailureCount() >= 1) {
                    //TestCase hat gefailt


                    //unserer Debugger kann anhand dieser Methode feststellen, dass der gerade ausgefuehrte Testfall gefailt ist
                    testfallGefailt();

                } else {
                    //TestCase war erfolgreich

                    testfallErfolgreich();
                }
            }


        }
    }

    private void testfallErfolgreich() {

    }

    private void testfallGefailt() {

    }


}
