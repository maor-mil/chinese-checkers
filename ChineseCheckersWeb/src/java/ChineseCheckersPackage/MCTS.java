package ChineseCheckersPackage;

public class MCTS extends BotType {
    // IN_PROGRESS defention (for Monte-Carlo)
    public static final byte IN_PROGRESS = -2;
    
    /**
    * Builds new MCTS Object
    * @param botLob        The given botLobby
    * @param randomOpening The given random opening number it will execute
    */
    public MCTS(BotLobby botLob,
                byte randomOpening) {
        this.botLob = botLob;
        this.randomOpeningMove = randomOpening;
    }
    
    // Overrides the bot move for Monte Carlo logic
    @Override
    public Move botMove() {
        if (this.botLob.currGameTurn < 5) {
            if (this.botLob.getCurrColor() == 'g')
                return makeGreenKnownOpenningMove();
            else
                return makeRedKnownOpenningMove();
        }
        else {
           return findNextMove();
        }
    }
    
    /** The main Monte Carlo function that plays the game for set amount
    * of time and returns the best move it found
    * @return The best move the Monte Carlo algorithm found.
    */
    public Move findNextMove()
    {
        byte playOutResult;
        
        Board board = this.botLob.getGameBoard();
        byte playerNo = this.botLob.getCurrPlayerIndex();
        
        State boardState = new State(board);
        Node rootNode = new Node(boardState);
        byte opponent = (byte)(1-playerNo);
        //set the player to the opponent
        rootNode.getState().setPlayerNo(playerNo);
        // current time
        long startingTime = System.currentTimeMillis(); 
      
        while(System.currentTimeMillis() - startingTime < 2000) {
            //stage 1 - selecting
            // promising Node is the leaf node we are going to explore
            Node promisingNode = selectPromisingNode(rootNode);
            
            //stage 2 - expand
            if(promisingNode.getState().getBoard().checkStatus() == MCTS.IN_PROGRESS && 
               promisingNode.getChildArray().isEmpty()) {
                 promisingNode.expandNode();
            }
            
            //stage 3 - simulation           
            Node NodeToExplore = promisingNode;
            if(!NodeToExplore.getChildArray().isEmpty()) {
                // Node to Explore holds one of the just expanded nodes
                NodeToExplore = promisingNode.getRandomChildNode(); 

                playOutResult = simulateRandomPlayout(NodeToExplore);
            }
            else {
                playOutResult = NodeToExplore.getState().getBoard().checkStatus();
            }
            
            // stage 4 - backpropagetion
            // back propagte and update all the visited nodes
            backPropagation(NodeToExplore, playOutResult);      
        }
        
        // the winner node is the max value child (the backpropagation)
        Node winnerNode = rootNode.getChildWithMaxScore(); 
        return winnerNode.getState().getMove();
    }
    
    /**
    * The method gets a root node of a tree and find the best leaf to explore
    * @param root root node of a tree
    * @return best leaf to explore
    */
    private Node selectPromisingNode(Node root)
    {
        // this function 
        Node node = root;

        while(!node.getChildArray().isEmpty())
        {
            node = UCT.findPromisingNodeUsingUCT(node);
        }
        return node;      
    }
    
    /**
    * The method gets a leaf node and run a random game from his state
    * @param node given node
    * @return the board status at the end of the simulation
    */
    private byte simulateRandomPlayout(Node node) {
        Node tempNode = new Node(node);
        State tempState = tempNode.getState();
        byte boardStatus = tempState.getBoard().checkStatus();
        long startingTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startingTime < 100 && 
               boardStatus == MCTS.IN_PROGRESS)
        {
            // move the turn to the other player
            tempState.togglePlayer();
            // play a random move from the current
            tempState.randomPlay(); 
            // check if the game is over, if it is return the one who won
            boardStatus = tempState.getBoard().checkStatus(); 
        }
        //System.out.println("boardStatus: " + boardStatus);
        return boardStatus;   
    }
    
    /**
    * The method updates the current move sequence with the simulation result.
    * @param NodeToExplore the given leaf node
    * @param result the result of the game
    */
    private void backPropagation(Node NodeToExplore , int result) {
        Node tempNode = NodeToExplore;
        while(tempNode != null)
        {
            tempNode.getState().incrementVisit();
            tempNode.getState().setWinScore(result * tempNode.getState().getPlayerNo());
            tempNode = tempNode.getParent();
        }
    }
}
