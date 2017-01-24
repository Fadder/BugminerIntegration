package graphBuilder;

import java.util.HashMap;
import java.util.LinkedList;

import executor.monitoring.Edge;

/**
 * Class that defines a MethodGraph object
 */
public class MethodGraph {

    String methodId;

    private int methodCalls;

   private HashMap<Integer,Transition> transitions = new HashMap<>();

    MethodGraph(String methodId){
        this.methodId = methodId;
        this.methodCalls = 0;
    }

    void update(Edge edge){
        if(edge.isFirstLine()){
            methodCalls++;
        }
        int pairedHash = hashSourceAndTarget(edge.getLineFrom(), edge.getLineTo());
        Transition transition = transitions.computeIfAbsent(pairedHash, k -> new Transition(edge.getLineFrom(), edge.getLineTo()));
        transition.update();
    }

    @Override
    public int hashCode(){
        return methodId.hashCode();
    }

    private int hashSourceAndTarget(int source, int target){
        // implements Cantor's pairing function
        int sum = source + target;
        return ((sum)*(sum + 1)/2) + target;
    }

    Transition getTransition(int source, int target){
        int pairedHash = hashSourceAndTarget(source, target);
        return transitions.get(pairedHash);
    }
    
    public LinkedList<Transition> getListOfTransition(){
    	return new LinkedList<>(transitions.values());
    }

    void printAllTransitions(){
        for(Transition transition: transitions.values()){
            System.out.print(transition.getSource() + "->" + transition.getTarget() + " : ");
            System.out.println(transition.getCount());
        }
    }
    
    public String getMethodId() {
		return methodId;
	}

	public int getMethodCalls() {
		return methodCalls;
	}

	
}
