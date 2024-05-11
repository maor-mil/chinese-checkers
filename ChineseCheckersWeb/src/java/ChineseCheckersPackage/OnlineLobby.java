package ChineseCheckersPackage;

import javax.websocket.Session;

public class OnlineLobby extends Lobby {
    private final Session[] sessions;
    private final String lobName;
    private final String playersColorsStr;
    private byte currentPlayersCount;
    
    /**
     * The method is constructor that builds new OnlineLobby object
     * @param playersColors String that each char represents different color
     * @param lobName       The name of the lobby
    */
    public OnlineLobby(String playersColors,
                       String lobName) {
        super((byte)playersColors.length());
        super.addColorsToGame(playersColors);
        this.lobName = lobName;
        this.playersColorsStr = playersColors;
        this.sessions = new Session[playersColors.length()];
        this.currentPlayersCount = 0;
        this.currGameTurn = -1;
    }
    
    /**
     * The method checks if given player clicked on marble on his turn
     * @param s   The given session
     * @param loc The name of the lobby
     * @return true if it the player turn otherwise returns false
    */
    public boolean isSessionTurn(Session s, Location loc) {
        byte savedIndex = 0;
        for (byte i = 0; i < this.sessions.length; i++){
            if(sessions[i] == s)
                savedIndex = i;
        }
        return this.gameBoard.getPlayer(savedIndex).isMarbleBelongsToPlayer(loc);
    }
    
    public Session[] getSessions() {
        return this.sessions;
    }
    public String getLobName() {
        return this.lobName;
    }
    public byte getCurrentPlayersCount() {
        return this.currentPlayersCount;
    }
    public String getPlayersColors() {
        return this.playersColorsStr;
    }
    
    /**
     * The method adds a given player to the game and checks if the game can
     * start after he was added
     * @param s   The given session
     * @return    returns true if after adding the player the game can begin,
     *            otherwise returns false
    */
    public boolean addPlayerToOnlineLobby(Session s) {
        boolean isAdded = false;
        for (byte i = 0; !isAdded && i < this.sessions.length; i++) {
            if (this.sessions[i] == null) {
                this.sessions[i] = s;
                isAdded = true;
                this.currentPlayersCount++;
            }
        }
        super.updateGameDetailsOfLobby();
        
        if (this.currentPlayersCount == this.sessions.length) {
            return true;
        }
        return false;
    }   
    
    /**
     * The method removes given player from the lobby and checks if the game
     * need to be canceled because he left the lobby
     * start after he was added
     * @param s   The given session
     * @return    returns true if the game should close due to the player
     *            leaving the game, otherwise returns false
    */
    public boolean removeSessionFromSessionsArr(Session s) {
        if (this.currGameTurn == -1) {
            // Decrease the ammount of players in lobby by 1
            this.currentPlayersCount--;
            this.currPlayerIndex--;
            
            // Check if there are players in the room
            if (this.currentPlayersCount == 0) {
                return true;
            }
            else {
                removeSessionSafely(s);
                return false;
            }
        }
        else
            return true;
    }
    
    /**
     * The method removes given player from the lobby.
     * @param s   The given session
    */
    private void removeSessionSafely(Session s) {
        boolean isRemoved = false;
        for (byte i = 0; !isRemoved && i < this.sessions.length; i++) {
            if (this.sessions[i] == s) {
                this.sessions[i] = null;
                isRemoved = true;
            }
        }
    }
}
