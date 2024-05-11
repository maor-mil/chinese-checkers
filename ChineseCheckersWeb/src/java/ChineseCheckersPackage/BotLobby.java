package ChineseCheckersPackage;

import java.util.Random;

public class BotLobby extends Lobby {
    // BotType array to represents different bots with different logics
    private final BotType[] botT;
    
    public BotLobby(String greenPlayerStr, String redPlayerStr) {
        super((byte)2);
        super.addPlayer(super.createGreenPlayer());
        super.addPlayer(super.createRedPlayer());
        this.botT = new BotType[2];
        
        addBot(greenPlayerStr, (byte)0);
        addBot(redPlayerStr, (byte)1);
        
    }
    
    private void addBot(String playerStr, byte index) { 
        byte randomOpeningMove = (byte)((new Random()).nextInt(2));
        switch (playerStr) {
            case "alphaBeta":
                this.botT[index] = new AlphaBetaBot(this,
                                                    randomOpeningMove,
                                                    index,
                                                    false);
                break;
            case "alphaBetaHard":
                this.botT[index] = new AlphaBetaBot(this,
                                                    randomOpeningMove,
                                                    index,
                                                    true);
                break;
            case "monteCarlo":
                this.botT[index] = new MCTS(this,
                                            randomOpeningMove);
                break;
            default:
                this.botT[index] = null;
                break;
        }
    }
    
    public String botMove() {
        Move botNextMove = this.botT[this.currPlayerIndex].botMove();
        updateMoveOfPlayer(botNextMove);
        return botNextMove.getMoveID();
    }
    
    public boolean didPlayerWin(byte playerIndex) {
        return this.gameBoard.getPlayer(playerIndex).didPlayerWin();
    }
    
    public void updateMoveOfPlayer(Move move) {
        this.gameBoard.getPlayer(this.currPlayerIndex).updateMove(move);
    }
    
    public void updateMoveOfPlayer(Location oldLoc,
                                   Location newLoc,
                                   byte playerIndex) {
        this.gameBoard.getPlayer(playerIndex).updateMove(oldLoc,
                                                         newLoc);
    }
}
