package executor.monitoring;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;



import java.io.IOException;
import java.util.List;
import java.util.Map;


public class JVMConnectionCreator {

    private String testsuitExecuterMainClass;//das ist die Klasse: TestsuitExecuter
    private String[] testClasses; //sind die Ausfuehrungsparameter von TestsuitExecuter
    private String classpath;

    public JVMConnectionCreator(String testsuitExecuterMainClass, String[] testClasses, String classpath) {
        this.testsuitExecuterMainClass = testsuitExecuterMainClass;
        this.testClasses = testClasses;
        this.classpath = classpath;
    }

    public VirtualMachine launchAndConnectToTestsuitExecuter() {
        VirtualMachine ret = null;

        try {
            ret = launchVM();
        } catch (VMStartException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private VirtualMachine launchVM() throws VMStartException, IllegalConnectorArgumentsException, IOException {
        LaunchingConnector connector = getCommandLineLaunchingConnector();

        Map<String, Connector.Argument> connectionProperties = setConnectionProperties(connector);

        return connector.launch(connectionProperties);
    }

    private LaunchingConnector getCommandLineLaunchingConnector() {
        List<LaunchingConnector> connectors = Bootstrap.virtualMachineManager().launchingConnectors();

        LaunchingConnector launchingConnector = null;

        for (LaunchingConnector each : connectors) {
            if (each.name().equals("com.sun.jdi.CommandLineLaunch")) {
                launchingConnector = each;
                break;
            }
        }

        return launchingConnector;
    }

    private Map<String, Connector.Argument> setConnectionProperties(LaunchingConnector connector) {
        Map<String, Connector.Argument> connectionProperties = connector.defaultArguments();

        Connector.Argument mainClass = connectionProperties.get("main");
        String applicationNameWithParameters = testsuitExecuterMainClass + " ";
        //Testklassen werden als Parameter uebergeben
        for(int i = 0; i < testClasses.length; i++) {
            applicationNameWithParameters = applicationNameWithParameters + testClasses[i] + " ";
        }
        mainClass.setValue(applicationNameWithParameters);

        Connector.Argument options = connectionProperties.get("options");
        options.setValue("-cp " + classpath);

        return connectionProperties;
    }


}
