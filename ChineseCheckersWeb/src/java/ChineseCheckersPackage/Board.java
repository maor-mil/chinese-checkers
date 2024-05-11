package ChineseCheckersPackage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    // =====================================================================
    // Consts Board details:
    // =====================================================================
    // The CELLS_COUNT represents the ammount of bits in the boardMask.
    // The last 9 bits are 0 so I didn't wrote them to the board mask, so we
    // have to sub them from the CELLLS_COUNT.
    private final static short CELLS_COUNT = 425-9;
    // There are 17 rows in the board
    public final static byte ROWS = 17;
    // There are 25 cols in the board
    public final static byte COLS = 25;
    
    // The boardMash the represents by bits the real cells in the Board.
    private final static byte[] boardMask = {
    0x00, 0x08, 0x00, 0x00,
    0x0A, 0x00, 0x00, 0x0A,
    (byte)0x80, 0x00, 0x0A, (byte) 0xA0,
    0x0A, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    (byte)0xAA, (byte)0xAA, (byte)0xA8, (byte)0xAA,
    (byte)0xAA, (byte)0xA8, 0x2A, (byte)0xAA,
    (byte)0xA8, 0x0A, (byte)0xAA, (byte)0xA8,
    0x0A, (byte)0xAA, (byte)0xAA, 0x0A, 
    (byte)0xAA, (byte)0xAA, (byte)0x8A, (byte)0xAA,
    (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    (byte)0xA8, 0x02, (byte)0xA8, 0x00,
    0x00, (byte)0xA8, 0x00, 0x00,
    0x28, 0x00, 0x00, 0x08};
    
    // The diraction array.
    public final static byte[][] dirArr = {
        {-1, -1}, // Top-Left
        {-1, 1}, // Top-Right
        {0, 2}, // Right
        {1, 1}, // Buttom-Right
        {1, -1}, // Buttom-Left
        {0, -2} // Left
    };
    
    // The players array
    private final Player[] playersArr;
    
    
    
    public Board(byte countOfPlayers) {
        this.playersArr = new Player[countOfPlayers];
    }
    
    public Board(Board other) {
        this.playersArr = new Player[other.getPlayersArr().length];
        
        for (byte i = 0; i < this.playersArr.length; i++) {
            this.playersArr[i] = new Player(other.getPlayersArr()[i]);
        }
    }
    
    public Player getPlayer(byte playerIndex) {
        return this.playersArr[playerIndex];
    }
    public void setPlayer(Player p, byte playerIndex) {
        this.playersArr[playerIndex] = p;
    }
    public byte getPlayersCount() {
        return (byte)this.playersArr.length;
    }
    
    public Player[] getPlayersArr() {
        return playersArr;
    }
    
    public void performMove(byte playerIndex, Move move) {
        this.playersArr[playerIndex].updateMove(move);
    }
    
    /**
    * This function returns if the any player of the lobby won the game.
    * @return the index of the player that one. If no one won, return 
    * IN_PROGRESS ( = -2)
    */
    public byte checkStatus() {
        for(byte i = 0; i < this.playersArr.length; i++) {
            if(didPlayerWin(i))
                return i;
        }
        return MCTS.IN_PROGRESS;
    }
    
    /**
    * This function returns if the current player of the lobby won the game.
    * @return true if the player won, otherwise returns false.
    */
    private boolean didPlayerWin(byte index) {
        return this.playersArr[index].didPlayerWin();
    }
    
    public List<Move> getAllPossibleMoves(byte playerIndex) {
        List<Move> allPossibleMoves = getAllPossibleMoves(playerIndex,
                                                          true, false);
        if (allPossibleMoves.isEmpty())
            allPossibleMoves = getAllPossibleMoves(playerIndex,
                                                   true, true);
        if (allPossibleMoves.isEmpty())
            allPossibleMoves = getAllPossibleMoves(playerIndex,
                                                   false, false);
        return allPossibleMoves;
    }
    
    private List<Move> getAllPossibleMoves(byte playerIndex,
                                           boolean shouldLookForLogicalMoves,
                                           boolean isEndGame) {
        
        List<Move> allPossibleMoves = new ArrayList<>();
        char playerColor = this.playersArr[playerIndex].getColor();
        Marble[] marblesArr = this.playersArr[playerIndex].getCurrMarblesPos();
        
        for (byte i = 0; i < marblesArr.length; i++) {
            String possibleMoves = allNextMovesStr(marblesArr[i].getLoc());
            
            for (byte j = 0; j < possibleMoves.length() / 4; j++) {
                Location newLoc = new Location(possibleMoves.substring(j*4, j*4 + 4));
                Move tempMove = new Move(marblesArr[i].getLoc(), newLoc);
                
                if (shouldLookForLogicalMoves) {
                    if (isNewLocationMakesSense(marblesArr[i].getRow(),
                                                newLoc.getRow(), newLoc.getCol(),
                                                playerColor,
                                                isEndGame)) {
                        allPossibleMoves.add(tempMove);
                    }
                }
                else {
                    allPossibleMoves.add(tempMove);
                }
            }
        }
        return allPossibleMoves;
    }
    
    private boolean isNewLocationMakesSense(byte oldRow,
                                            byte newRow, byte newCol,
                                            char playerColor,
                                            boolean isEndingGame) {
        boolean isLocationOnMiddleAndAdvance = false;
        
        if (playerColor == 'g') {
            if (newRow > oldRow || (isEndingGame && newRow == oldRow && newRow >= 12)) {
                if(checkIfMarbleOnMiddleBoard(newRow, newCol) ||
                   checkIfMarbleOnStartOrTarget(newRow))
                    isLocationOnMiddleAndAdvance = true;
            }
        }
        else if (playerColor == 'r')  {
            if (newRow < oldRow || (isEndingGame && newRow == oldRow && newRow <= 4)) {
                if(checkIfMarbleOnMiddleBoard(newRow, newCol) ||
                   checkIfMarbleOnStartOrTarget(newRow))
                    isLocationOnMiddleAndAdvance = true;
            }
        }
        return isLocationOnMiddleAndAdvance;
    }
    
    private boolean checkIfMarbleOnMiddleBoard(byte newRow, byte newCol) {
        boolean isOnMiddleBoard = false;
        
        byte [][] matLocs = {
            {4, 12, 17, 7},
            {5, 11, 18, 6},
            {6, 10, 19, 5},
            {7, 9, 20, 4}
        };
        
        if (newRow == 8)
            isOnMiddleBoard = true;
        
        for (byte i = 0; !isOnMiddleBoard && i < 4; i++) {
            if ((newRow == matLocs[i][0] || newRow == matLocs[i][1]) &&
                (newCol < matLocs[i][2] && newCol > matLocs[i][3]))
                isOnMiddleBoard = true;
        }
        
        return isOnMiddleBoard;
    }
    
    private boolean checkIfMarbleOnGreenHouse(byte newRow) {
        return newRow == 0 || newRow == 1 || newRow == 2 || newRow == 3;
    }
    private boolean checkIfMarbleOnRedHouse(byte newRow) {
        return newRow == 13 || newRow == 14 || newRow == 15 || newRow == 16;
    }
    
    private boolean checkIfMarbleOnStartOrTarget(byte newRow) {
        return checkIfMarbleOnGreenHouse(newRow) ||
               checkIfMarbleOnRedHouse(newRow);
    }
    
    
    public String allNextMovesStr(Location loc) {
        Set<String> possiblePosLst = new HashSet<>();
        String allPossibleMoves = "";
        String currMarbleStrID = loc.getLocStrID();
        
        // Add the given cell so we later remove it
        possiblePosLst.add(currMarbleStrID);
        
        // Go all over the possibilities of the given cell
        goAllOverPossibilities(loc, possiblePosLst, true);
        // Remove the given cell from the list
        possiblePosLst.remove(currMarbleStrID);
        
        // Go all over the returned possible positions
        for(String tempStr : possiblePosLst) {
            allPossibleMoves += tempStr;
        }
        
        return allPossibleMoves;
    }
    
    private void goAllOverPossibilities(Location loc,
                                        Set<String> possiblePosLst,
                                        boolean checkForComplexMove) {
        for (int i = 0; i < Board.dirArr.length; i++) {
            goOverOneSidePossibilities(loc, 
                                       Board.dirArr[i][0], 
                                       Board.dirArr[i][1],
                                       possiblePosLst,
                                       checkForComplexMove);
        }
    }
    
    
    private void goOverOneSidePossibilities(Location loc,
                                            byte rowDif, byte colDif,
                                            Set<String> possiblePosLst,
                                            boolean checkForComplexMove) {
        
        Location currLoc = new Location((byte)(loc.getRow() + rowDif),
                                        (byte)(loc.getCol() + colDif));
        Location nextLoc = new Location((byte)(loc.getRow() + (rowDif*2)),
                                        (byte)(loc.getCol() + (colDif*2)));
        
        String currStr = currLoc.getLocStrID();
        String nextStr = nextLoc.getLocStrID();
        
        // Check if the cell exists on the board and wasn't already checked
        if (Board.isCellExists(currLoc) 
            && !possiblePosLst.contains(currStr)) {
            // Check if there is a marble on the cell
            if (isThereMarble(currLoc)) {
                // Check if the next cell is in the board borders,
                // AND there isn't a marble on the next cell
                // AND it wasn't already checked
                if (Board.isCellExists(nextLoc) && 
                    !isThereMarble(nextLoc) &&
                    !possiblePosLst.contains(nextStr)) {
                    
                    // If the second top-left is clear, do a recurrsion with
                    // the cell due to complex move
                    possiblePosLst.add(nextStr);
                    goAllOverPossibilities(nextLoc,
                                           possiblePosLst,
                                           false);
                }
            }
            // Otherwise move the empty space to the moves options
            else if (checkForComplexMove){
                possiblePosLst.add(currStr);
            }
        }
    }
    
    
    /**
    * The function returns true if given location exists in the game board,
    * otherwise returns false.
    * @param  loc the given location
    * @return true if given location exists in the game board,
    *         otherwise returns false.
    */
    public static boolean isCellExists(Location loc) {
        byte row = loc.getRow();
        byte col = loc.getCol();
        
        if (row < 0 || col < 0)
            return false;
        if (col >= COLS  || row >= ROWS)
            return false;
        if (row*COLS + col > CELLS_COUNT)
            return false;
        return ((((boardMask[(row*COLS + col) / 8]) << (row*COLS + col) % 8) & 0x80) != 0);
    }
    
    
    /**
    * This method gets marble that is represented by row and col
    * and checks if the marble belongs to one of the players in the lobby.
    * @param  loc the location of the marble
    * @return the method returns true if the marble belongs to one of the 
    *         players in the lobby, otherwise returns false.
    */
    public boolean isThereMarble(Location loc) {
        // Flag to continue the method as long as we didn't find a marble
        boolean isThereMarble = false;
        // Go all over the players
        for (byte i = 0; !isThereMarble && i < this.playersArr.length; i++) {
            // Check if the marble belong to the player
            isThereMarble = this.playersArr[i].isMarbleBelongsToPlayer(loc);
        }
        
        return isThereMarble;
    }
}