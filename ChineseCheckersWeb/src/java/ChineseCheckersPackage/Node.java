package ChineseCheckersPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Node {
    State state;
    Node parent;
    List<Node> childArray;
    
    public Node(byte playersCount)
    {
        state = new State(playersCount);
        parent = null;
        childArray = new ArrayList<>();
    }
    
    public Node(Node node)
    {
        this.state = new State(node.state);
        parent = node.parent;
        childArray = node.childArray;
    }
    
    public Node(State state)
    {
        this.state = new State(state);
        parent = new Node(state.getBoard().getPlayersCount());
        childArray = new ArrayList<>();
    }
    
    public Node(State state , Node Parent)
    {
        this.state = new State(state);
        this.parent = Parent;
        childArray = new ArrayList<>();
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = new State(state);
    }
    public Node getParent() {
        return parent;
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }
    public List<Node> getChildArray() {
        return childArray;
    }
    public void setChildArray(List<Node> childArray) {
        this.childArray = childArray;
    }
    
    /**
    * @return The method returns a random node from the children
    */
    public Node getRandomChildNode()
    {
        
        int selectRandom = (int) (Math.random() * this.childArray.size());
        return this.childArray.get(selectRandom);
    }
        
    /**
     * The method finds the child with max value
     * @return The method returns the child with max value
    */
    public Node getChildWithMaxScore() {
        return Collections.max(this.childArray, Comparator.comparing(c -> {
            return c.getState().getVisitCount();
        }));
    }
    
    /**
    * The method is used to a leaf node and add to his children array all
    * the possible states from this node state
    * available states holds all the possible states from that
    * certain state
    */
    public void expandNode()
    {
        List<State> availableStates = this.state.getAllPossibleStates(); 
        for(int i =0 ; i < availableStates.size(); i++)
        {
            Node newNode = new Node(availableStates.get(i),this);
            this.childArray.add(newNode);
        }      
    }
}
