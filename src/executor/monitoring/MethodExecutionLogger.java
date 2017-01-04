package executor.monitoring;

import java.util.LinkedList;

/**
 * Created by work on 25.11.2016.
 */
public class MethodExecutionLogger {
    private LinkedList<String> methods;
    private LinkedList<Integer> lineNumbers;
    private int indexOfLineNumbersList;
    private boolean methodEntered;



    public MethodExecutionLogger() {
        methods = new LinkedList<>();
        lineNumbers = new LinkedList<>();
        indexOfLineNumbersList = -1;
        methodEntered = false;
    }

    public void enterNewMethod(String newMethod, int lineNumber) {
        methods.addLast(newMethod);
        lineNumbers.addLast(lineNumber);
        indexOfLineNumbersList++;
    }

    public void exitCurrentMethod() {
        methods.removeLast();
        lineNumbers.removeLast();
        indexOfLineNumbersList--;
    }

    /**
     * Update current linenumber
     */
    public void stepEvent(int lineNumber) {
        lineNumbers.set(indexOfLineNumbersList, lineNumber);
    }


    public String getLastMethodname() {
        if (indexOfLineNumbersList < 0) {
            return "TestCase";
        }
        return methods.getLast();
    }

    public int getLastLineNumber() {
        return lineNumbers.getLast();
    }

    public boolean isMethodEntered() {
        return methodEntered;
    }

    public void setMethodEntered(boolean methodEntered) {
        this.methodEntered = methodEntered;
    }

    /**
     * synchronize this Stack with the callstack after Exception occurred
     */
    public void repairAfterException(String catchLocation) {
        //methods Liste wird solange aufgeraemt, bis oben die Methode steht, die eine geworfene Exception fängt
        while (!methods.isEmpty()) {
            if(methods.getLast().equals(catchLocation)){
                return;
            }
            methods.removeLast();
            lineNumbers.removeLast();
            indexOfLineNumbersList--;
        }
        System.err.println("Checked Exception wurde nich vom eigenen ProjektKlassen gefangen?");

    }

    public void reset() {
        methods = new LinkedList<>();
        lineNumbers = new LinkedList<>();
        indexOfLineNumbersList = -1;
        methodEntered = false;
    }
}
