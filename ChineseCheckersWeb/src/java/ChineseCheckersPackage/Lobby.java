package ChineseCheckersPackage;

public abstract class Lobby {
    
    protected Board gameBoard;
    protected byte currPlayerIndex;
    protected short currGameTurn;
    
    /**
    * The method builds new Lobby.
    * @param countOfPlayers the count of the players in the lobby.
    */
    public Lobby (byte countOfPlayers) {
        this.gameBoard = new Board(countOfPlayers);
        this.currPlayerIndex = 0;
        this.currGameTurn = -1;
    }
    
    /**
    * The method returns the gameBoard of the lobby.
    * @return The game board of the lobby.
    */
    public Board getGameBoard() {
        return this.gameBoard;
    }
    /**
    * The method returns the getCurrPlayerIndex of the lobby.
    * @return The getCurrPlayerIndex.
    */
    public byte getCurrPlayerIndex() {
        return this.currPlayerIndex;
    }
    /**
    * The method returns the getCurrGameTurn of the lobby.
    * @return The getCurrGameTurn.
    */
    public short getCurrGameTurn() {
        return this.currGameTurn;
    }
    
    /**
    * The method adds new Player to the gameBoard and updates the lobby
    * game details.
    * @param p the given player.
    */
    public void addPlayer(Player p) {
        this.gameBoard.setPlayer(p, this.currPlayerIndex);
        updateGameDetailsOfLobby();
    } 
    
    /**
    * The method adds new Players to the gameBoard based on given playersColors
    * String
    * @param playersColors the given players colors String.
    */
    protected void addColorsToGame(String playersColors) {
        if (playersColors.indexOf('g') != -1)
            addPlayer(createGreenPlayer());
        if (playersColors.indexOf('y') != -1)
            addPlayer(createYellowPlayer());
        if (playersColors.indexOf('o') != -1)
            addPlayer(createOrangePlayer());
        if (playersColors.indexOf('r') != -1)
            addPlayer(createRedPlayer());
        if (playersColors.indexOf('p') != -1)
            addPlayer(createPurplePlayer());
        if (playersColors.indexOf('b') != -1)
            addPlayer(createBluePlayer());
    }
    
    /**
    * This method returns new green Player.
    * @return Player The new green Player.
    */
    protected Player createGreenPlayer() {
        return new Player(new byte[]{ 0, 12, 1, 13, 1, 11, 2, 14, 2, 12, 2, 10,
                                      3, 15, 3, 13, 3, 11, 3, 9 },
                          'g');
    }
    
    /**
    * This method returns new red Player.
    * @return Player The new red Player.
    */
    protected Player createRedPlayer() {
        return new Player(new byte[]{ 16, 12, 15, 11, 15, 13, 14, 10, 14, 12, 
                                      14, 14, 13, 9, 13, 11, 13, 13, 13, 15 },
                          'r');
    }
    
    /**
    * This method returns new yellow Player.
    * @return Player The new yellow Player.
    */
    protected Player createYellowPlayer() {
        return new Player(new byte[]{ 4, 24, 5, 23, 4, 22, 6, 22, 5, 21, 4, 20, 
                                      7, 21, 6, 20, 5, 19, 4, 18 },
                          'y');
    }
    
    /**
    * This method returns new purple Player.
    * @return Player The new purple Player.
    */
    protected Player createPurplePlayer() {
        return new Player(new byte[]{ 12, 0, 11, 1, 12, 2, 10, 2, 11, 3, 12, 4,
                                      9, 3, 10, 4, 11, 5, 12, 6},
                          'p');
    }
    
    /**
    * This method returns new orange Player.
    * @return Player The new orange Player.
    */
    protected Player createOrangePlayer() {
        return new Player(new byte[]{ 12, 24, 12, 22, 11, 23, 12, 20, 11, 21, 
                                      10, 22, 12, 18, 11, 19, 10, 20, 9, 21},
                          'o');
    }
    
    /**
    * This method returns new blue Player.
    * @return Player The new blue Player.
    */
    protected Player createBluePlayer() {
        return new Player(new byte[]{ 4, 0, 4, 2, 5, 1, 4, 4, 5, 3, 6, 2, 4, 6, 
                                      5, 5, 6, 4, 7, 3},
                          'b');
    }
    
    /**
    * This method updates the current player index and the current game turn
    * of this lobby.
    */
    public void updateGameDetailsOfLobby() {
        this.currPlayerIndex++;
        if (this.currPlayerIndex == this.gameBoard.getPlayersCount()) {
            this.currPlayerIndex = 0;
            this.currGameTurn++;
        }
    }

    
    /**
    * This function returns the current Player of the lobby.
    */
    public Player getCurrPlayer() {
        return this.gameBoard.getPlayer(this.currPlayerIndex);
    }
    
    /**
    * This function returns the color of the current Player of the lobby.
    * @return the color of the player
    */
    public char getCurrColor(){
       return getCurrPlayer().getColor();
    }
    
    /**
    * This function update move of given coordinates.
     * @param oldLoc the old location of the marble
     * @param newLoc the new location of the marble
    */
    public void updateMoveOfPlayer(Location oldLoc,
                                   Location newLoc) {
        this.gameBoard.getPlayer(this.currPlayerIndex).updateMove(oldLoc,
                                                                  newLoc);
    }
    
    /**
    * This function returns if the current player of the lobby won the game.
    * @return true if the player won, otherwise returns false.
    */
    public boolean didPlayerWin() {
        return this.gameBoard.getPlayer(this.currPlayerIndex).didPlayerWin();
    }
    
    public boolean didPlayerStayAtHome() {
        return this.gameBoard.getPlayer(this.currPlayerIndex).didPlayerStayAtHome();
    }
    
    /**
    * This function returns all possible moves of clicked marble as StringID
    * @param loc the given location of the marble
    * @return string that contains all the possible next moves
    */
    public String getAllPossibleMovesOfClickedMarble(Location loc) {
        String allPossibleMoves;
        
        if(!this.gameBoard.getPlayer(this.currPlayerIndex).isMarbleBelongsToPlayer(loc)) {
            allPossibleMoves = "invalidMarble";
        }
        else
            allPossibleMoves = this.gameBoard.allNextMovesStr(loc);
        return allPossibleMoves;
    }
}
