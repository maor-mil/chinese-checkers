package ChineseCheckersPackage;


public class Player {
    // currMarblesPos have the marbles of the player
    private final Marble[] currMarblesPos;
    // startingMarblesPos is const and contains the starting locations of the
    // player's marbles
    private final Location[] startingMarblesPos;
    // The color of the player
    private final char color;
    
    /**
    * The method builds new Player object
    * @param startingLoc starting locations of marbles
    * @param color       the color of the player
    */
    public Player(byte[] startingLoc, char color) {
        this.currMarblesPos = new Marble[10];
        this.startingMarblesPos = new Location[10];
        this.color = color;
        
        for(byte i = 0; i < startingLoc.length; i+=2)
        {
           this.currMarblesPos[i/2] = new Marble(startingLoc[i], startingLoc[i+1]);
           this.startingMarblesPos[i/2] = new Location(startingLoc[i], startingLoc[i+1]);
        }
    }
    
    /**
    * Copy-Constructor.
    * @param other the other player.
    */
    public Player(Player other) {
        this.currMarblesPos = new Marble[10];
        this.startingMarblesPos = new Location[10];
        this.color = other.getColor();
        
        for (byte i = 0; i < this.currMarblesPos.length; i++) {
            this.currMarblesPos[i] = new Marble(other.getCurrMarblesPos()[i]);
            this.startingMarblesPos[i] = new Location(other.getStartingMarblesPos()[i]);
        }
    }
    
    public Marble[] getCurrMarblesPos() {
        return this.currMarblesPos;
    }
    
    public Location[] getStartingMarblesPos() {
        return this.startingMarblesPos;
    }
    
    public char getColor() {
        return this.color;
    }
    
    public boolean didPlayerStayAtHome() {
        boolean didPlayerStayAtHome = false;
        for (byte i = 0; !didPlayerStayAtHome && i < currMarblesPos.length; i++) {
            didPlayerStayAtHome = this.currMarblesPos[i].getIsOnWinningSpot();
        }
        return didPlayerStayAtHome;
    }
    
    public boolean didPlayerWin() {
        boolean didPlayerWinFlag = true;
        for (byte i = 0; didPlayerWinFlag && i < currMarblesPos.length; i++) {
            didPlayerWinFlag = this.currMarblesPos[i].getIsOnWinningSpot();
        }
        return didPlayerWinFlag;
    }
    
    public void updateMove(Move move) {
        updateMove(move.getStartingLoc(), move.getEndingLoc());
    }
    
    
    public void updateMove(Location oldLoc,
                           Location newLoc) {
        boolean isUpdated = false;
        for (byte i = 0; !isUpdated && i < this.currMarblesPos.length; i++) {
            if (this.currMarblesPos[i].getRow() == oldLoc.getRow() &&
                this.currMarblesPos[i].getCol() == oldLoc.getCol()) {
                this.currMarblesPos[i].setMarbleLocation(newLoc);
                updateMarbleInfoPos(this.currMarblesPos[i]);
                isUpdated = true;
            }
        }
    }
    
    public void updateMarbleInfoPos(Marble m) {
        boolean isUpdated = false;
        
        m.setIsOnStartingSpot(false);
        for (byte i = 0; !isUpdated && i < this.startingMarblesPos.length; i++) {
            Location tempLoc = this.startingMarblesPos[i];
            if (m.getRow() == tempLoc.getRow() && m.getCol() == tempLoc.getCol()) {
                m.setIsOnStartingSpot(true);
                isUpdated = true;
            }
        }
        
        if(!isUpdated) {
            m.setIsOnWinningSpot(false);
            for (byte i = 0; !isUpdated && i < this.startingMarblesPos.length; i++) {
                Location tempLoc = this.startingMarblesPos[i];
                if ( (m.getRow() == (Board.ROWS - tempLoc.getRow() - 1)) &&
                     (m.getCol() == (Board.COLS - tempLoc.getCol() - 1)) ) {
                    m.setIsOnWinningSpot(true);
                    isUpdated = true;
                }
            }
        }
    }
    
    public boolean isMarbleBelongsToPlayer(Location loc) {
        boolean isBelong = false;
        for (byte i = 0; !isBelong && i < this.currMarblesPos.length; i++) {
            if (this.currMarblesPos[i].getRow() == loc.getRow() &&
                this.currMarblesPos[i].getCol() == loc.getCol()) {
                isBelong = true;
            }
        }
        return isBelong;
    }
}