package ChineseCheckersPackage;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnOpen;
import javax.websocket.OnMessage;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
* Chinese WebSocket server. Handle the connection and communication between
* every client and other clients or to the game bot.
*
* @author  Maor Milakandov
* @version 1.0
* @since   2019-12-01 
*/

@ServerEndpoint("/endpoint")
public class WebSocketServer {
    // Each Session is belonged to a lobby
    private static final Map<Session, Lobby> sessionsDict = new HashMap<>();
    
    // Save Online Lobbys that are waiting for more players
    private static final Map<String, OnlineLobby> onlineLobbiesList = new HashMap<>();
    
    private boolean isConnectionValid = true;
    
    // ----------------------------------------------------------------------
    // Server Endpoint methods (onOpen, onMessage, onClose, onError):
    
    /**
    * This method handles all the new connections to the server by adding the
    * connected session to the sessions dictionary with no signed lobby.
    * @param s the connected session.
    */
    @OnOpen
    public void onOpen(Session s) {
        // Print log message that session joined to the server
        System.out.println(s.getId() + " Log into the server");
        
        // Add the session to the session dictionary with no lobby
        sessionsDict.put(s, null);
    }
    
    /**
    * This method handles all the communication between the clients and the
    * server by receiving a JSON message and checking the type of the request 
    * by the client - and then calling the right function to handle the request.
    * @param message receive JSON object as a string.
    * @param s the session that sent the message.
    */
    @OnMessage
    public void onMessage(String message, 
                          Session s) {
        // Make the message a json object
        JsonReader reader = Json.createReader(new StringReader(message));
        JsonObject jsonMessage = reader.readObject();
        
        // Get the type of the message
        String msgType = jsonMessage.getString("type");
        
        switch (msgType) {
            case "createLobby":
                handleCreateLobby(jsonMessage, s);
                break;
            case "getLobbiesList":
                handleGetLobbiesList(s);
                break;
            case "joinLobby":
                handleJoinLobby(jsonMessage, s);
                break;
            case "exitLobby":
                handleExitLobby(s);
                break;
            case "firstClick":
                handleFirstClick(jsonMessage, s);
                break;
            case "secondClick":
                handleSecondClick(jsonMessage, s);
                break;
            default:
                break;
        }
    }
    
    /**
    * This method get a session and removes it from the sessions dictionary
    * @param s the session of the disconnected client.
    */
    @OnClose
    public void onClose(Session s) {
        // If the session was part of online game, remove the session from the
        // online game lobby
        Lobby lob = sessionsDict.get(s);
        
        // Remove the session to the sessions dictionary
        sessionsDict.remove(s);
        
        if (lob instanceof OnlineLobby) {
            removeSessionFromLobbyOnline(s, (OnlineLobby)lob);
        }
        
        
    }

    /**
    * This method handle errors that happen on the server.
    * @param s the session of the disconnected client.
    * @param e the throwable error.
    */
    @OnError
    public void onError(Session s, Throwable e) {
        System.out.println("Error Happened: " + e.toString());

        // get StackTraceElements 
        // using getStackTrace() 
        StackTraceElement[] stktrace 
            = e.getStackTrace(); 

        // print element of stktrace 
        for (int i = 0; i < stktrace.length; i++) { 

            System.out.println("Index " + i 
                               + " of stack trace"
                               + " array conatins = "
                               + stktrace[i].toString()); 
        } 
        this.isConnectionValid = false;
    }
    
    // ----------------------------------------------------------------------
    
    // ----------------------------------------------------------------------
    // Handle methods for each type of communication between the clients:
    
    /**
    * This method handle createLobby request by given client by checking
    * the type of the lobby that the client wants to create, and according
    * to the lobby type the method creates the lobby.
    * @param jsonMessage the given JSON message that contains the gameType
    *                    that the client wish to create.
    * @param s           the given session.
    */
    public void handleCreateLobby(JsonObject jsonMessage, 
                                  Session s) { 
        // Get the type of the lobby - bot lobby, lan lobby or online lobby
        String gameType = jsonMessage.getString("gameType");
        
        switch (gameType) {
            case "bot":
                createBotLobby(jsonMessage, s);
                break;
            case "online":
                createOnlineLobby(jsonMessage, s);
                break;
            case "local":
                createLocalLobby(jsonMessage, s);
                break;
            default:
                break;
        }
    }
    
    
    /**
    * This method handle getLobbiesList request by given client by checking
    * sending the client all the online lobbies that need to be filled with
    * more players to start the game
    * @param s           the given session.
    */
    public void handleGetLobbiesList(Session s) {
        for (OnlineLobby tempLob : onlineLobbiesList.values()) {
            sendToSession(s, 
                          buildLobDetailsJsonObject(tempLob));
        }
    }
    
    /**
    * This method handle createLobby request by given client by checking
    * the type of the lobby that the client wants to create, and according
    * to the lobby type the method creates the lobby.
    * @param jsonMessage the given JSON message that contains the gameType
    *                    that the client wish to create.
    * @param s           the given session.
    */
    public void handleJoinLobby(JsonObject jsonMessage, 
                                Session s) {
        String lobName = jsonMessage.getString("lobName");
        OnlineLobby lob = (OnlineLobby)onlineLobbiesList.get(lobName);
        if(lob != null) {
            sessionsDict.replace(s, lob);
            sendToSession(s, buildJoinedOnlineGameJsonObject());
            if (lob.addPlayerToOnlineLobby(s)) {
                sendToSessionsArray(((OnlineLobby)lob).getSessions(), 
                                     buildOnlineGameStartedJsonObject());
                sendCurrGameDetails(s, lob);
                onlineLobbiesList.remove(lobName);
            }
        }
        else
            sendToSession(s, buildFailedJoinOnlineGameJsonObject());
    }
    
    /**
    * This method sends the details of the given OnlineLobby to all it's players
    * @param lob the given OnlineLobby.
    */
    public void sendOnlineLobDetails(OnlineLobby lob) {
        sendToSessionsArray(lob.getSessions(), buildLobDetailsJsonObject(lob));
    }
    
    /**
    * This method handle exit lobby request from given session.
    * @param s the given session.
    */
    public void handleExitLobby(Session s) {
        // Get the lobby of the session
        Lobby lob = sessionsDict.get(s);
        // Exit the lobby
        exitLobby(s, lob);
        // Send exitedLobby message successfully to the player
        sendToSession(s, buildExitedLobbyJsonObject());
    }
    
    /**
    * This method exit lobby of given session from given lobby.
    * @param s  the given session.
    * @param lob the given lobby
    */
    public void exitLobby(Session s, Lobby lob) {
        // remove the lobby from the sessionDict
        sessionsDict.replace(s, null);
        
        // If the lobby belongs to online lobby, remove the session from the
        // session aray of the online lobby
        if (lob instanceof OnlineLobby) {
            removeSessionFromLobbyOnline(s, (OnlineLobby)lob);
        }
    }
    
    /**
    * This method handle first click of user by checking if the clicked marble
    * is of the current player - If so, the method will check what moves are
    * available and will send the coordinates to the player, otherwise will send
    * invalidMarble to the player.
    * @param jsonMessage the given JSON message that contains first click 
    *                    details.
    * @param s           the given session.
    */
    public void handleFirstClick(JsonObject jsonMessage, 
                                 Session s) { 
        // Get the first clicked location
        String startLoc = jsonMessage.getString("startLoc");
        
        // Get the player lobby
        Lobby lob = sessionsDict.get(s);

        // Find all the empty cells that can be reached
        // from this location
        Location loc = new Location(startLoc);
        
        if(lob instanceof OnlineLobby && 
           !((OnlineLobby)lob).isSessionTurn(s, loc)) {
            sendToSession(s, buildInvalidMarbleJsonObject());
        }
        else {
            // Get all the possible positions
            String allPossMoves = lob.getAllPossibleMovesOfClickedMarble(loc);
            if (allPossMoves.equals("invalidMarble")) {
                // Tell the client this is not his marble!
                sendToSession(s, buildInvalidMarbleJsonObject());
            }
            else {
                // Return all the positions to the session
                sendToSession(s, buildPossibleMovesJsonObject(allPossMoves));
            }  
        }
    }
    
    /**
    * This method handle second click of user by updating the game board
    * and sending the information to the session(s) in the lobby - if
    * the session is on BotLobby, this method will order the bot to make a move
    * and inform the player about it.
    * @param jsonMessage the given JSON message that contains second click 
    *                    details.
    * @param s           the given session.
    */
    public void handleSecondClick(JsonObject jsonMessage, 
                                  Session s) {
        // Get the clicked locations
        String startLocStr = jsonMessage.getString("startLoc");
        String endLocStr = jsonMessage.getString("endLoc");
        
        Location startLoc = new Location(startLocStr);
        Location endLoc = new Location(endLocStr);
        
        // Get the player lobby
        Lobby lob = sessionsDict.get(s);
        
        makeMoveWithPlayer(s, lob, startLoc, endLoc, jsonMessage);
        
    }
    
    
    /**
    * This method make a move with a player and sends the info about the move
    * to the clients.
    * @param s           the given session (client).
    * @param lob         the given bot lobby.
    * @param startLoc    the starting location.
    * @param endLoc      the ending location
    * @param jsonMessage the given jsonMessage.
    */
    public void makeMoveWithPlayer(Session s,
                                   Lobby lob,
                                   Location startLoc,
                                   Location endLoc,
                                   JsonObject jsonMessage) {
        
        // Update the move of the player
        lob.updateMoveOfPlayer(startLoc,
                               endLoc);
        
        // Check if the game turn is above 25 and if the player have a piece 
        // at his home
        /*if (lob.getCurrGameTurn() > 25 && lob.didPlayerStayInHome()) {
                ;
        }*/
        
        // Send second click to player(s) in lobby
        sendToSessionsInLobby(s, lob, jsonMessage);
        
        // If the player didn't won
        if(!checkIfPlayerWon(s, lob)) {
            // Update the curr player index
            lob.updateGameDetailsOfLobby();
            // send the curr game details
            sendCurrGameDetails(s, lob);
            
            // Do move with bot if lobby is BotLobby
            if (lob instanceof BotLobby) {
                makeMoveWithBot(s, (BotLobby)lob);
            }
                
        }
    }
    
    /**
    * This method returns true if the current player in the lobby won and inform 
    * all the sessions in the lobby about it if he did.
    * @param s   the given session (player).
    * @param lob the given lobby.
    * @return This method returns true if given player won and inform all the 
    * sessions in the lobby about it if he did.
    */
    public boolean checkIfPlayerWon(Session s, 
                                    Lobby lob) {
        // Check if player win
        if (lob.didPlayerWin()) {
            // Send game won message to player(s) in lobby
            sendToSessionsInLobby(s, lob, 
                                  buildGameWonJsonObject(lob.getCurrColor()));
            // Exit the lobby
            exitLobby(s, lob);
            return true;
        }
        return false;
    }
    
    // ----------------------------------------------------------------------
    
    // ----------------------------------------------------------------------
    // Auxiliary methods:
    
    /**
    * This method handle new bot lobby request from given session.
    * @param jsonMessage the given request.
    * @param s           the given session.
    */
    public void createBotLobby(JsonObject jsonMessage, Session s) {
        String greenPlayerStr = jsonMessage.getString("greenP");
        String redPlayerStr = jsonMessage.getString("redP");
        BotLobby newLobby = new BotLobby(greenPlayerStr, redPlayerStr);
        joinLobbyToSessionsDictAndInformTheClient(s, newLobby);
        
        if(!redPlayerStr.equals("human") && !greenPlayerStr.equals("human")) {
            simulateTwoBotsGame(s, newLobby);
        }
        else if (!greenPlayerStr.equals("human"))
            makeMoveWithBot(s, newLobby);
    }
    
    /**
    * This method simulate game between 2 bots and inform the client about every
    * move.
    * @param s   the given session (client).
    * @param lob the given bot lobby.
    */
    public void simulateTwoBotsGame(Session s,
                                    BotLobby lob) {
        boolean didPlayerTwoWin = false;
        
        // Make moves with bots as long as either won
        while(isConnectionValid && !didPlayerTwoWin && !makeMoveWithBot(s, lob))
            if(makeMoveWithBot(s, lob))
                didPlayerTwoWin = true;
    }

    /**
    * This method handle new local lobby request from given session.
    * @param jsonMessage the given request.
    * @param s           the given session.
    */
    public void createLocalLobby(JsonObject jsonMessage,
                                 Session s) {
        String playersColors = jsonMessage.getString("pColors");
        Lobby newLobby = new LocalLobby(playersColors);
        joinLobbyToSessionsDictAndInformTheClient(s, newLobby);
    }
    
    /**
    * This method handle new online lobby request from given session.
    * @param jsonMessage the given request.
    * @param s           the given session.
    */
    public void createOnlineLobby(JsonObject jsonMessage,
                                  Session s) {
        String pColors = jsonMessage.getString("pColors");
        byte pCount = (byte)(pColors.length());
        String lobName = jsonMessage.getString("lobName");
        
        if (onlineLobbiesList.containsKey(lobName))
            sendToSession(s, buildNameTakenJsonObject());
        else
        {
            OnlineLobby newLobby = new OnlineLobby(pColors, lobName);
            onlineLobbiesList.put(lobName, newLobby);
            newLobby.addPlayerToOnlineLobby(s);
            // Put the lobby to the session on the sessionsDict
            sessionsDict.replace(s, newLobby);
            // Inform the client he joined the lobby
            sendToSession(s, buildJoinedOnlineGameJsonObject());
        }
    }
    
    /**
    * This method make a move with a bot and sends the info about the move
    * to the client.
    * @param s   the given session (client).
    * @param lob the given bot lobby.
    * @return the method returns true if the bot won otherwise returns false.
    */
    public boolean makeMoveWithBot(Session s,
                                   BotLobby lob) {
        // Make move with bot
        String moveStrID = lob.botMove();
        // Send the move to the client
        sendToSession(s, buildSecondClickJsonObject(moveStrID));
        
        // If the bot didn't win
        if(!checkIfPlayerWon(s, lob)) {
            // Update the curr player index
            lob.updateGameDetailsOfLobby();
            // Send the curr game details
            sendCurrGameDetails(s, lob);
            
            // Return false if the bot didn't win
            return false;
        }
        
        // Return true if the bot win
        return true;
    }
    
    /**
    * This method removes given session from his online lobby.
    * @param s   the given session (client).
    * @param lob the given online lobby.
    */
    public void removeSessionFromLobbyOnline(Session s, 
                                             OnlineLobby lob) {
        if (lob.removeSessionFromSessionsArr(s)) {
            Session[] sessionsArr = lob.getSessions();
            for (Session session : sessionsArr) {
                if (session != null && session != s) {
                    sessionsDict.replace(session, null);
                    sendToSession(session, buildGameCanceledJsonObject());
                }
            }
            onlineLobbiesList.remove(lob.getLobName());
        }
    }
    
    /**
    * This method gets a new made up lobby and:
    *   - Puts the lobby to the session on the sessionsDict
    *   - Sends the client a message that he joined the lobby successfully
    *   - Sends the client the game details
    * @param s        The given session.
    * @param newLobby The new made up lobby.
    */
    public void joinLobbyToSessionsDictAndInformTheClient(Session s, 
                                                          Lobby newLobby) {
        // Put the lobby to the session on the sessionsDict
        sessionsDict.replace(s, newLobby);
        // Send the client a message that he joined the lobby successfully
        sendStartedGame(s);
        // Send the client the game details
        sendCurrGameDetails(s, newLobby);
    }
    
    /**
    * This method gets a session and sends the startedGame JSON message to
    * the client.
    * @param s The given session.
    */
    public void sendStartedGame(Session s) {
        sendToSession(s, buildStartedGameJsonObject());
    }
    
    /**
    * This method gets session and lobby and sends the current game details
    * to the player(s) in the lobby.
    * @param s   The given session.
    * @param lob The given lobby.
    */
    public void sendCurrGameDetails(Session s, 
                                    Lobby lob) {
        /*if (lob instanceof OnlineLobby && lob.getCurrGameTurn() == -1) {
            
        }
        else*/
            sendToSessionsInLobby(s, lob, buildCurrGameDetailsJsonObject(lob));
    }
    
    // ----------------------------------------------------------------------
    
    // ----------------------------------------------------------------------
    // Methods for building new JSON objects:
    
    /**
    * This method returns new startedGame typed JSON object.
    * @return JsonObject The new JSON of 'startedGame' type.
    */
    public JsonObject buildStartedGameJsonObject() {
        return Json.createObjectBuilder()
                   .add("type", "startedGame")
                   .build();
    }
    
    /**
    * This method gets a lobby and builds a new JSON Object that contains the
    * game details (the color of the current player, and the current game turn).
    * @param  lob        The given lobby.
    * @return JsonObject The new JSON object that contains the game details.
    */
    public JsonObject buildCurrGameDetailsJsonObject(Lobby lob) {
        return Json.createObjectBuilder().add("type", "currGameDetails")
                                         .add("currColor", lob.getCurrColor())
                                         .add("currTurn", lob.getCurrGameTurn())
                                         .build();
    }
    
    /**
    * This method returns new invalidMarble typed JSON object.
    * @return JsonObject The new JSON of 'invalidMarble' type.
    */
    public JsonObject buildInvalidMarbleJsonObject() {
        return Json.createObjectBuilder().add("type", "invalidMarble").build();
    }
    
    /**
    * This method returns new possibleMoves typed JSON object.
    * @param  allPossMoves The given all possible moves String.
    * @return JsonObject   The new JSON of 'possibleMoves' type.
    */
    public JsonObject buildPossibleMovesJsonObject(String allPossMoves) {
        return Json.createObjectBuilder().add("type", "possibleMoves")
                                         .add("possibleMoves", allPossMoves)
                                         .build();
    }
    
    /**
    * This method returns new exitedLobby typed JSON object.
    * @return JsonObject The new JSON of 'exitedLobby' type.
    */
    public JsonObject buildExitedLobbyJsonObject() {
        return Json.createObjectBuilder().add("type", "exitedLobby").build();
    }
    
    /**
    * This method returns new secondClick typed JSON object.
    * @param  moveStrID  The given moveID which contains the old position 
    *                    and the new position.
    * @return JsonObject The new JSON of 'secondClick' type.
    */
    private JsonObject buildSecondClickJsonObject(String moveStrID) {
        return Json.createObjectBuilder()
                .add("type", "secondClick")
                .add("startLoc", moveStrID.substring(0,4))
                .add("endLoc", moveStrID.substring(4,8))
                .build();
    }
    
    /**
    * This method returns new gameWon typed JSON object.
    * @param  color      The char of the player that won
    * @return JsonObject The new JSON of 'gameWon' type.
    */
    private JsonObject buildGameWonJsonObject(char color) {
        return Json.createObjectBuilder()
                .add("type", "gameWon")
                .add("winningColor", color)
                .build();
    }
    
    /**
    * This method returns new gameCanceled typed JSON object.
    * @return JsonObject The new JSON of 'gameCanceled' type.
    */
    private JsonObject buildGameCanceledJsonObject() {
        return Json.createObjectBuilder()
                .add("type", "gameCanceled")
                .build();
    }
    
    /**
    * This method returns new nameTaken typed JSON object.
    * @return JsonObject The new JSON of 'nameTaken' type.
    */
    private JsonObject buildNameTakenJsonObject() {
        return Json.createObjectBuilder()
                .add("type", "nameTaken")
                .build();
    }
    
    /**
    * This method returns new onlineGameStarted typed JSON object.
    * @return JsonObject The new JSON of 'onlineGameStarted' type.
    */
    private JsonObject buildOnlineGameStartedJsonObject() {
        return Json.createObjectBuilder()
                .add("type", "onlineGameStarted")
                .build();
    }
    
    
    /**
    * This method returns new lobDetails typed JSON object.
    * @return JsonObject The new JSON of 'lobDetails' type.
    */
    private JsonObject buildLobDetailsJsonObject(OnlineLobby lob) {
        return Json.createObjectBuilder()
                .add("type", "lobDetails")
                .add("lobName", lob.getLobName())
                .add("lobCount", lob.getCurrentPlayersCount())
                .add("lobMaxPlayers", lob.getSessions().length)
                .add("colorsStr", lob.getPlayersColors())
                .build();
    }
    
    /**
    * This method returns new joinedOnlineGame typed JSON object.
    * @return JsonObject The new JSON of 'joinedOnlineGame' type.
    */
    public JsonObject buildJoinedOnlineGameJsonObject() {
        return Json.createObjectBuilder()
                .add("type", "joinedOnlineGame")
                .build();
    }
    
    /**
    * This method returns new failedJoinOnlineGame typed JSON object.
    * @return JsonObject The new JSON of 'failedJoinOnlineGame' type.
    */
    public JsonObject buildFailedJoinOnlineGameJsonObject() {
        return Json.createObjectBuilder()
                .add("type", "failedJoinOnlineGame")
                .build();
    }
    
    // ----------------------------------------------------------------------
    
    
    // ----------------------------------------------------------------------
    // Methods for sending JSON message to the clients:
    
    /**
    * This method get a lobby and sends a JSON object as string to the sessions 
    * in the lobby.
    * @param s           The session that belong to the lobby.
    * @param lob         The given lobby.
    * @param jsonMessage The given JSON message.
    */
    public void sendToSessionsInLobby(Session s, 
                                      Lobby lob, 
                                      JsonObject jsonMessage) {
        if (lob instanceof OnlineLobby) {
            sendToSessionsArray(((OnlineLobby)lob).getSessions(), 
                                 jsonMessage);
        }
        else {
            sendToSession(s, jsonMessage);
        }
    }
    
    /**
    * This method sends a JSON object as string to given sessions array.
    * @param sessions The given sessions array.
    * @param message  The given JSON message.
    */
    private void sendToSessionsArray(Session[] sessions, 
                                     JsonObject message) {
        for (Session session : sessions) {
            if(session != null)
                sendToSession(session, message);
        }
    }
    
    /**
    * This method sends a JSON object as String to given session.
    * @param s       The given session.
    * @param message The given JSON message.
    */
    private void sendToSession(Session s, 
                               JsonObject message) {
        try {
            s.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            onError(s, ex);
        }
    }
    
    // ----------------------------------------------------------------------
}