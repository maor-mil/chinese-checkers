package ChineseCheckersPackage;

import java.util.Collections;
import java.util.Comparator;


public class UCT {
    
    /**
     * The method going over all the children of given node and 
     * find out the one with the highest UCT value
     * @param node given node
     * @return The method returns the node with the highest UCT value
    */
    public static Node findPromisingNodeUsingUCT(Node node)      
    
    {
        // the T for the equation
        int parentVisit = node.getState().getVisitCount();
        // going over all the nodes in the child array and find which has
        // the highest UCT value
        return Collections.max( 
          node.getChildArray(),
          Comparator.comparing(c -> UCTequ(parentVisit, c.getState().getVisitCount(), c.getState().getWinScore())));     
    }
    
    
    public static double UCTequ(int totalVisit , int nodeVisit , double nodeWinScore)
    {
        /*wi = number of wins after the i-th move
        ni = number of simulations after the i-th move
        c = exploration parameter (theoretically equal to √2)
        t = total number of simulations for the parent node
        */
        return (nodeWinScore / (double) nodeVisit) + (1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit));
    }
}
