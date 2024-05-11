package ChineseCheckersPackage;

import java.util.ArrayList;
import java.util.List;


public class State {
    private Board board;
    private Move move;
    private byte playerNo;
    private int visitCount;
    private int winScore;

    public State(byte playersCount) {
        this.board = new Board(playersCount);
    }

    public State(State state) {
        this.board = new Board(state.getBoard());
        if(state.getMove() != null)
            this.move = new Move(state.getMove());
        this.playerNo = state.getPlayerNo();
        this.visitCount = state.getVisitCount();
        this.winScore = state.getWinScore();
    }
      
    public State(Board board) {
        this.board = new Board(board);
        this.winScore = 0;
    }

    public Move getMove() {
        return move;
    }
    public void setMove(Move move) {
        this.move = move;
    }
    
    
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public byte getPlayerNo() {
        return playerNo;
    }

    public void setPlayerNo(byte playerNo) {
        this.playerNo = playerNo;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getWinScore() {
        return winScore;
    }

    public void setWinScore(int winScore) {
        this.winScore += winScore;
    }
    
    /**
    * The method constructs a list of all possible states from current state
    * @return list with all the possible states
    */
    public List<State> getAllPossibleStates() 
    {
        
        List<State> possibleStates = new ArrayList<>();
        List<Move> availablePositions = this.board.getAllPossibleMoves(this.playerNo);
        availablePositions.forEach(p -> {
            State newState = new State(this.board);
            newState.setPlayerNo((byte)(1 - this.playerNo));
            newState.getBoard().performMove(newState.getPlayerNo(), p);
            newState.setMove(p);
            possibleStates.add(newState);
        });
        return possibleStates;
    }
    
    /**
    * The method makes a list of all possible positions on the board and 
    * plays a random move 
    */
    public void randomPlay() 
    {
        
        List<Move> availablePositions = this.board.getAllPossibleMoves(this.playerNo);
        int totalPossibilities = availablePositions.size();
        int selectRandom = (int) (Math.random() * totalPossibilities);        
        this.board.performMove(this.playerNo, availablePositions.get(selectRandom));
    }
    
    void incrementVisit() {
        this.visitCount++;
    }  
        
    void togglePlayer() {
        this.playerNo = (byte)(1 - this.playerNo);
    }
    
    int getOpponent() {
        return 1 - playerNo;
    }
}
