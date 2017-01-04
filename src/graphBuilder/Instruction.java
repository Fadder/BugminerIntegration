package graphBuilder;

import java.util.List;

/**
 * Class that defines a Instruction object
 */
public class Instruction {

    private int id;

    public List<Transition> transitionList;

    public Instruction(int id) {
        this.id = id;
    }

    public void updateTransition(int transitionId){

    }
}
