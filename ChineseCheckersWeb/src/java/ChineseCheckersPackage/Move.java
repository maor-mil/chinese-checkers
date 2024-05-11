package ChineseCheckersPackage;

public class Move {
    private Location startingLoc;
    private Location endingLoc;
    
    public Move() {
    }
    
    public Move(Move other) {
        this.startingLoc = new Location(other.getStartingLoc());
        this.endingLoc = new Location(other.getEndingLoc());
    }
    
    public Move(Location startingLoc, Location endingLoc) {
        this.startingLoc = new Location(startingLoc);
        this.endingLoc = new Location(endingLoc);
    }
    
    public Move(byte[] cordinatesArr) {
        this.startingLoc = new Location(cordinatesArr[0], cordinatesArr[1]);
        this.endingLoc = new Location(cordinatesArr[2], cordinatesArr[3]);
    }
    
    public void setMove(Location startingLoc, Location endingLoc) {
        this.startingLoc = startingLoc;
        this.endingLoc = endingLoc;
    }
    
    public Location getStartingLoc() {
        return this.startingLoc;
    }
    
    public Location getEndingLoc() {
        return this.endingLoc;
    }
    
    public String getMoveID() {
        return this.startingLoc.getLocStrID() + this.endingLoc.getLocStrID();
    }
}