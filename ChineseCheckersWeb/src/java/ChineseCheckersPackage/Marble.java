package ChineseCheckersPackage;

public class Marble {
    // The location of the Marble (contains row and col)
    private final Location loc;
    // Flag to indicate if this marble is on the starting spot of the player
    // it belongs to
    private boolean isOnStartingSpot;
    // Flag to indicate if this marble is on the winning spot of the player
    // it belongs to
    private boolean isOnWinningSpot;
    
    public Marble(byte row, byte col) {
        this.loc = new Location(row, col);
        this.isOnStartingSpot = true;
        this.isOnWinningSpot = false;
    }
    
    public Marble(Marble other) {
        this.loc = new Location(other.getLoc());
    }

    public Location getLoc() {
        return this.loc;
    }
    public boolean getIsOnStartingSpot() {
        return this.isOnStartingSpot;
    }
    public boolean getIsOnWinningSpot() {
        return this.isOnWinningSpot;
    }
    public byte getRow() {
        return this.loc.getRow();
    }
    public byte getCol() {
        return this.loc.getCol();
    }
    public void setRow(byte row) {
        this.loc.setRow(row);
    }
    public void setCol(byte col) {
        this.loc.setCol(col);
    }
    public void setMarbleLocation(byte row, byte col) {
        this.setRow(row);
        this.setCol(col);
    }
    public void setMarbleLocation(Location other) {
        this.loc.setRow(other.getRow());
        this.loc.setCol(other.getCol());
    }
    public void setIsOnStartingSpot(boolean isOnStartingSpot) {
        this.isOnStartingSpot = isOnStartingSpot; 
    }
    public void setIsOnWinningSpot(boolean isOnWinningSpot) {
        this.isOnWinningSpot = isOnWinningSpot;
    }
    
}
