package ChineseCheckersPackage;

public class Location {
    private byte row;
    private byte col;
    
    public Location(String locationID) {
        this.row = (byte)((locationID.charAt(0) - '0') * 10 + 
                           locationID.charAt(1) - '0');
        this.col = (byte)((locationID.charAt(2) - '0') * 10 + 
                           locationID.charAt(3) - '0');
    }
    
    public Location(byte row, byte col) {
        this.row = row;
        this.col = col;
    }
    
    public Location(Location other) {
        this.row = other.getRow();
        this.col = other.getCol();
    }
    
    public byte getRow() {
        return this.row;
    }
    public byte getCol() {
        return this.col;
    }
    public void setRow(byte row) {
        this.row = row;
    }
    public void setCol(byte col) {
        this.col = col;
    }
    
    /**
    * This method gets marble that is represented by row and col
    * and convert the coordinates to string ID of the marble.
    * @return string ID of the marble.
    */
    public String getLocStrID() { 
        String locId;
        
        if (this.row < 10)
            locId = '0' + Byte.toString(this.row);
        else
            locId = Byte.toString(this.row);
        
        if (this.col < 10)
            locId += '0' + Byte.toString(this.col);
        else
            locId += Byte.toString(this.col);
        
        return locId;
    }
}