package ChineseCheckersPackage;

import java.util.List;

public class AlphaBetaBot extends BotType {
    // =====================================================================
    // Consts:
    // =====================================================================
    private final static short MAX = Short.MAX_VALUE;
    private final static short MIN = Short.MIN_VALUE;
    private final static byte INITIAL_DEPTH = 3;
    
    // =====================================================================
    // Referrences and indexers to the players:
    // =====================================================================
    private final Player botP;
    private final byte botIndex;
    private final Player enemyP;
    private final byte enemyIndex;
    
    // =====================================================================
    // Best move saved information:
    // =====================================================================
    private Move globalBestMove;
    private final Move bestMove;
    private byte currentDepth;
    private long start;
    private boolean timeout;
    
    
    // =====================================================================
    // Is on hard mode
    // =====================================================================
    private final boolean isHardMode;
    
    /**
    * The method builds new AlphaBetaBot object
    * @param botLob        the botLobby that contains this AlphaBetaBot.
    * @param randomOpening index for random known opening.
    * @param botIndex      the bot index on the players array.
    * @param isHardMode    boolean for indicating if this alpha beta bot
    *                      should do advanced evaluation function
    */
    public AlphaBetaBot(BotLobby botLob,
                        byte randomOpening,
                        byte botIndex,
                        boolean isHardMode) {
        this.botLob = botLob;
        this.randomOpeningMove = randomOpening;
        
        this.botIndex = botIndex;
        this.enemyIndex = (byte)(1 - botIndex);
        
        
        this.botP = this.botLob.getGameBoard().getPlayer(this.botIndex);
        this.enemyP = this.botLob.getGameBoard().getPlayer(this.enemyIndex);
        
        this.bestMove = new Move();
        
        this.isHardMode = isHardMode;
    }
    
    /**
    * The method return Move object of the next move that the AI found as best.
    * @return the next move that the AI found as best.
    */
    @Override
    public Move botMove() {
        if (this.botLob.currGameTurn < 5) {
            if (this.botP.getColor() == 'g')
                return super.makeGreenKnownOpenningMove();
            else
                return super.makeRedKnownOpenningMove();
        }
        else {
           return findBestGlobalMove();
        }
    }
    
    /**
    * The method returns optimal value for current player, and updates the
    * bestMove variable when found best move.
    * @param depth             The number of steps into the game the AI
    *                          will search for.
    * @param maximizingPlayer  True - if the player is this player AI,
    *                          False - if the player is the enemy player.
    * @param alpha             Alpha is the best value that the maximizer 
    *                          currently can guarantee at that level or above.
    * @param beta              Beta is the best value that the minimizer 
    *                          currently can guarantee at that level or above.
    * @return optimal value for current player.
    */
    public short minimax(byte depth,  
                         Boolean maximizingPlayer,
                         short alpha, short beta) {
        if (this.globalBestMove != null && 
            System.currentTimeMillis() - this.start > TIMEOUT_MILISECONDS)
        {
            this.timeout = true;
            return MIN;
        }
        
        if(this.botLob.didPlayerWin(this.botIndex))
            return MAX;
        
        
        // Terminating condition
        if (depth == 0) 
            return evaluate();
        
        if (maximizingPlayer) {
        
            // Get all possible moves
            List<Move> allPossMoves = this.botLob.getGameBoard().
                                      getAllPossibleMoves(this.botIndex);
            
            for (byte i = 0 ; i < allPossMoves.size(); i++) {
                Location oldLoc = allPossMoves.get(i).getStartingLoc();
                Location newLoc = allPossMoves.get(i).getEndingLoc();
                
                // Make new move
                this.botLob.updateMoveOfPlayer(oldLoc,
                                               newLoc,
                                               this.botIndex);

                // compute evaluation function for this move. 
                short moveVal = minimax((byte)(depth - 1), 
                                  !maximizingPlayer, 
                                  alpha, beta);

                // Undo the move 
                this.botLob.updateMoveOfPlayer(newLoc,
                                               oldLoc,
                                               this.botIndex);
                if (depth == this.currentDepth) {
                    System.out.println("(" + oldLoc.getRow() + ", " 
                                       + oldLoc.getCol() + ") -> " +
                                       "(" + newLoc.getRow() + ", " 
                                       + newLoc.getCol() + ")" + " MoveVal: " + moveVal);
                }
                if (moveVal > alpha) {
                    alpha = moveVal;

                    if (depth == this.currentDepth) {
                        bestMove.setMove(oldLoc, newLoc);
                    }
                }
                /*else if (depth == this.currentDepth && moveVal == alpha) {
                    if (isGivenDistanceBetterThanBestDistance(this.botP.getColor(),
                                                             oldLoc.getRow(),
                                                             newLoc.getRow())) {
                        bestMove.setMove(oldLoc, newLoc);
                    }
                }*/

                // Alpha Beta Pruning 
                if (beta <= alpha) 
                    return alpha;
            }    
            
            return alpha;
        } 
        else {
            // Get all possible moves
            List<Move> allPossMoves = this.botLob.getGameBoard().
                                      getAllPossibleMoves(this.botIndex);
            
            for (byte i = 0 ; i < allPossMoves.size(); i++) {
                Location oldLoc = allPossMoves.get(i).getStartingLoc();
                Location newLoc = allPossMoves.get(i).getEndingLoc();
                
                // Make new move
                this.botLob.updateMoveOfPlayer(oldLoc,
                                               newLoc,
                                               this.enemyIndex);

                // compute evaluation function for this move. 
                short moveVal = minimax((byte)(depth - 1),
                                  !maximizingPlayer,
                                  alpha, beta);


                // Undo the move 
                this.botLob.updateMoveOfPlayer(newLoc,
                                               oldLoc,
                                               this.enemyIndex);

                // Alpha Beta Pruning 
                if (moveVal <= beta) {
                    beta = moveVal;
                }
                if (alpha >= beta) {
                    return beta;
                }
            }
            return beta;
        }
    }
    
    /**
    * This method returns the best global possible move for the player in the
    * given time.
    * @return The best move the that the alpha-beta algorithm found in the 
    *         given time.
    */
    public Move findBestGlobalMove() {
        this.timeout = false;
        this.start = System.currentTimeMillis();
        
        for (byte d = 0;; d++)
        {
            if (d == 0) {
                this.globalBestMove = null;
            }
            else {
                this.globalBestMove = new Move(this.bestMove);
                System.out.println("Completed search with depth " + this.currentDepth);            
                System.out.println("time: " + (System.currentTimeMillis() - this.start));
            }
            this.currentDepth = (byte) (INITIAL_DEPTH + d);
            if (minimax(this.currentDepth, true, MIN, MAX) == MAX)
                return this.bestMove;
            if (this.timeout)
            {
                System.out.println("timeout!");
                System.out.println("bestMove: " + this.bestMove.getMoveID());
                System.out.println("globalBestMove: " + this.globalBestMove.getMoveID());
                return globalBestMove;
            }
        }
    }
    
    
    public boolean isGivenDistanceBetterThanBestDistance(char pColor,
                                                         byte oldRow,
                                                         byte newRow){
        if (this.botP.getColor() == 'g') {
            if(newRow - oldRow > 
               bestMove.getEndingLoc().getRow() - bestMove.getStartingLoc().getRow() && 
               oldRow < bestMove.getStartingLoc().getRow())
                return true;
        }
        else if (this.botP.getColor() == 'r') {
            if(oldRow - newRow > 
               bestMove.getStartingLoc().getRow() - bestMove.getEndingLoc().getRow() &&
                oldRow > bestMove.getStartingLoc().getRow())
                return true;
        }
        return false;
    }
    
    /**
    * This method returns the current board score for our bot.
    * @return the current board score for our bot.
    */
    private short evaluate() {
        if (this.botP.getColor() == 'g') {
            return (short)(evaluateGreen(botP, enemyP) - evaluateRed(enemyP, botP));
        }
        else {
            return (short)(evaluateRed(botP, enemyP) - evaluateGreen(enemyP, botP));
        }
    }
    
    /**
    * This method returns the current board score for the green player.
    * @return the current board score for the green player.
    */
    private short evaluateGreen(Player greenP, Player redP) {
        short score = 0;
        Marble[] marblesArr = greenP.getCurrMarblesPos();
        
        for (byte i = 0; i < marblesArr.length; i++) {
            score += marblesArr[i].getRow();
            if (checkIfMarbleAlone(marblesArr[i], greenP))
                score -= 10;
            if (marblesArr[i].getIsOnStartingSpot())
                score -= 30;
            
            if (this.botLob.currGameTurn < 40 && this.isHardMode) {
                score += checkShapesForGivenMarble(marblesArr[i], 
                                                   greenP, redP,
                                                   (byte)1);
            }
        }
        
        return score;
    }
    
    /**
    * This method returns the current board score for the red player.
    * @return the current board score for the red player.
    */
    private short evaluateRed(Player redP, Player greenP) {
        short score = 0;
        Marble[] marblesArr = redP.getCurrMarblesPos();
        
        for (byte i = 0; i < marblesArr.length; i++) {
            score += (Board.ROWS - marblesArr[i].getRow() - 1);
            if (checkIfMarbleAlone(marblesArr[i], redP))
                score -= 10;
            if (marblesArr[i].getIsOnStartingSpot())
                score -= 30;
            
            if (this.botLob.currGameTurn < 40 && this.isHardMode) {
                score += checkShapesForGivenMarble(marblesArr[i], 
                                                   redP, greenP, 
                                                   (byte)-1);
            }
        }
        
        return score;
    }
    
    private boolean checkIfMarbleAlone(Marble m, Player p) {
        boolean isMarbleAlone = true;
        
        // Check if the marble is alone ONLY on the side color side of the 
        // board
        /*if (p.getColor() == 'g' && m.getRow() > 8)
            isMarbleAlone = false;
        else if (p.getColor() == 'r' && m.getRow() < 8)
            isMarbleAlone = false;
        */
        for (int i = 0; isMarbleAlone && i < Board.dirArr.length; i++) {
            Location newLoc = new Location((byte)(m.getRow() + Board.dirArr[i][0]),
                                           (byte)(m.getCol() + Board.dirArr[i][1]));
            if (p.isMarbleBelongsToPlayer(newLoc))
                isMarbleAlone = false;
        }
        return isMarbleAlone;
    }
    
    
    private short checkShapesForGivenMarble(Marble m,
                                            Player p, Player enemyP,
                                            byte checkingFromButtomToUpper) {
        return (short) (checkPairAndTriple(m, p, checkingFromButtomToUpper) +
                        checkQuad(m, p, enemyP, checkingFromButtomToUpper));
    }
    
    // Check the following shapes:
    // --*----
    // ---*---
    // OR:
    // ----*--
    // ---*---
    // Also add bonus if both exists (meaning):
    // --*-*--
    // ---*---
    private short checkPairAndTriple(Marble m, Player p, 
                                     byte checkingFromButtomToUpper) {
        short score = 0;
        
        Location tempLocation;
        boolean isRightPair = false;

        tempLocation = new Location((byte)(m.getRow() + (1 * checkingFromButtomToUpper)),
                                    (byte)(m.getCol() + 1));

        if (Board.isCellExists(tempLocation) &&
            p.isMarbleBelongsToPlayer(tempLocation)) {
            tempLocation.setRow((byte)(m.getRow() + (2 * checkingFromButtomToUpper)));
            tempLocation.setCol((byte)(m.getCol() + 2));

            if (Board.isCellExists(tempLocation) &&
                !this.botLob.getGameBoard().isThereMarble(tempLocation)) {
                    score++;
                    isRightPair = true;
            }
        }

        // Check left
        tempLocation.setRow((byte)(m.getRow() + (1 * checkingFromButtomToUpper)));
        tempLocation.setCol((byte)(m.getCol() - 1));

        if (Board.isCellExists(tempLocation) &&
            p.isMarbleBelongsToPlayer(tempLocation)) {
            tempLocation.setRow((byte)(m.getRow() + (1 * checkingFromButtomToUpper)));
            tempLocation.setCol((byte)(m.getCol() - 2));

            if (Board.isCellExists(tempLocation) &&
                !this.botLob.getGameBoard().isThereMarble(tempLocation)) {
                    score++;
                    
                    // If both pairs exists, add bonus to the score
                    // because it is triangle shape
                    if (isRightPair)
                        score += 10;
            }
        }
        
        return score;
    }
    
    
    // Check the following shapes:
    // --*----
    // ---*---
    // --*----
    // ---*---
    // OR:
    // ----*--
    // ---*---
    // ----*--
    // ---*---
    private short checkQuad(Marble m, Player p, Player enemyP,
                            byte checkingFromButtomToUpper) {
        short score = 0;
        
        score += checkOneSideQuad(m, p, enemyP, 
                                  checkingFromButtomToUpper,
                                  (byte)1);
        score += checkOneSideQuad(m, p, enemyP, 
                                  checkingFromButtomToUpper,
                                  (byte)-1);
        return score;
    }
    
    private short checkOneSideQuad(Marble m, Player p, Player enemyP,
                                   byte checkingFromButtomToUpper,
                                   byte sideDiff) { 
        short score = 0;
        
        Location tempLocation;
        // Check first progression
        tempLocation = new Location((byte)(m.getRow() + (1 * checkingFromButtomToUpper)),
                                    (byte)(m.getCol() + (1 * sideDiff)));

        if (Board.isCellExists(tempLocation) &&
            p.isMarbleBelongsToPlayer(tempLocation)) {
            tempLocation.setRow((byte)(m.getRow() + (2 * checkingFromButtomToUpper)));
            tempLocation.setCol((byte)(m.getCol()));

            if (Board.isCellExists(tempLocation) &&
                p.isMarbleBelongsToPlayer(tempLocation)) {
                // Check second progression
                tempLocation.setRow((byte)(m.getRow() + (3 * checkingFromButtomToUpper)));
                tempLocation.setCol((byte)(m.getCol() + (1 * sideDiff)));

                if (Board.isCellExists(tempLocation) &&
                    p.isMarbleBelongsToPlayer(tempLocation)) {
                    tempLocation.setRow((byte)(m.getRow() + (4 * checkingFromButtomToUpper)));
                    tempLocation.setCol((byte)(m.getCol()));

                    if (Board.isCellExists(tempLocation) &&
                        !this.botLob.getGameBoard().isThereMarble(tempLocation)) {
                        
                        tempLocation.setRow((byte)(m.getRow() + (2 * checkingFromButtomToUpper)));
                        tempLocation.setCol((byte)(m.getCol() + (2 * sideDiff)));

                        if (Board.isCellExists(tempLocation)) {
                            if(enemyP.isMarbleBelongsToPlayer(tempLocation)) {
                                score -= 30;
                            }
                            else if (!p.isMarbleBelongsToPlayer(tempLocation)) {
                                score += 20;
                            }
                        }
                    }
                }
            }
        }
        return score;
    }
}