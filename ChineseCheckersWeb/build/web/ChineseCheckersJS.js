let selectedBall = null;
let selectedBallOrignalHerf = "";
let infoMessage = document.getElementById("infoMessage");
let currColor = document.getElementById("currPlayerColor");
let currTurn = document.getElementById("currGameTurn");
let nextMoves = [];
let originalGameUI;
let originalOnlineGamesListTbl;
let isLocalGame = false;

let socket = new WebSocket("ws://localhost:8080/ChineseCheckersWeb/endpoint");
socket.onopen = onOpen;
socket.onclose = onClose;
socket.onmessage = onMessage;
socket.onerror = onError;
socket.onclose = onClose;

// Add all cells to onclick function when the window is loaded
window.addEventListener("load", function() {
    originalGameUI = document.getElementById("gameUI").innerHTML;
    originalOnlineGamesListTbl = document.getElementById("onlineGamesListTbl")
                                 .innerHTML;
    makeCellsClickable();
}, false);


function makeCellsClickable() {
    // Get all the use elements (the cells)
    let useElementsCollection = document.getElementById("ClickableElements").children;
    // Go all over the use elements (the cells)
    for(let i = 0; i < 121; i++) {
        // Get current cell
        let tempUseElement = useElementsCollection[i];
        
        // Add curr cell to onclick function
        tempUseElement.addEventListener('click', function() {
            // Herf is the type of the cell (cell = ball and it's color OR 
            // empty cell)
            let tempHerf = tempUseElement.getAttribute('xlink:href').toString();

            // If this is the first click of the user so target the 
            // selected ball
            if (selectedBall === null && tempHerf !== "#WhiteHole")
            {
                updateSelectedBall(tempUseElement, tempHerf);
                sendFirstClickToServer();
            }
            // If this is the second click of the user
            else if (selectedBall !== null)
            {
                // If the second click is on empty cell, send the cordinates 
                // to the server
                if (tempHerf === "#WhiteHoleClickable")
                {
                    // Send details of move to server
                    sendSecondClickToServer(tempUseElement);
                }
                // If the user clicked on the same ball, cancel the click
                else if (tempHerf.includes('OnClick'))
                {
                    selectedBall.setAttribute('xlink:href', selectedBallOrignalHerf);
                    makeWhiteHolesNotClickable();
                    cancelSelectedBall();
                }
            }
        }, false);
    }
}

function onOpen(event) {
    infoMessage.innerHTML = "Please pick up a game mode!";
}

function onClose(event) {
    infoMessage.innerHTML = "Signed out of server!";
}

function addLobToLobList(lobName, pCount, requiredCount, colorsStr) {
    let table = document.getElementById("onlineGamesListTbl");
    var row = table.insertRow(-1);
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    var cell3 = row.insertCell(2);
    var cell4 = row.insertCell(3);
    cell1.innerHTML = lobName;
    cell2.innerHTML = pCount;
    cell3.innerHTML = requiredCount;
    cell4.appendChild(createNewJoinLobbyBtn(lobName, colorsStr));
}

function createNewJoinLobbyBtn(lobName, colorsStr) {
    var btn = document.createElement("BUTTON");
    btn.innerHTML = "Join Now!";
    btn.value = lobName;
    btn.style.width = "50%";
    btn.onclick = function() { 
        sendJoinLobbyToServer(lobName);
        document.getElementById("onlineGamesListTbl").innerHTML = originalOnlineGamesListTbl;

        if (!colorsStr.includes("g")) {
            cancelPlayer("GroupGreen");
        }
        if (!colorsStr.includes("r")) {
            cancelPlayer("GroupRed");
        }
        if (!colorsStr.includes("y")) {
            cancelPlayer("GroupYellow");
        }
        if (!colorsStr.includes("p")) {
            cancelPlayer("GroupPurple");
        }
        if (!colorsStr.includes("o")) {
            cancelPlayer("GroupOrange");
        }
        if (!colorsStr.includes("b")) {
            cancelPlayer("GroupBlue");
        }

        waitForMorePlayersInOnlineGame();
        };
  return btn;
}


function waitForMorePlayersInOnlineGame() {
    isLocalGame = false;
    document.getElementById("boardSvg").style.pointerEvents = "none";
    document.getElementById("gameUI").style.display = "block";
    document.getElementById("onlineGamesList").style.display = "none";
    document.getElementById("pickPlayersColors").style.display = "none";
    infoMessage.innerHTML = "Please wait for game to start!";
}

function onMessage(event) {
    // Get the json message
    let jsonMsg = JSON.parse(event.data);
    // Get the type of the message
    let msgType = jsonMsg.type.toString();
    
    if (msgType === "startedGame") {
        startGame();
    }
    else if (msgType === "onlineGameStarted") {
        document.getElementById("exitMatchBtn").style.display = "none";
        document.getElementById("boardSvg").style.pointerEvents = "auto";
        infoMessage.innerHTML = "Game Started!";
    }
    else if (msgType === "lobDetails") {
        let lobName = jsonMsg.lobName.toString();
        let pCount = jsonMsg.lobCount;
        let requiredCount = jsonMsg.lobMaxPlayers;
        let colorsStr = jsonMsg.colorsStr;
        addLobToLobList(lobName, pCount, requiredCount, colorsStr);
    }
    else if (msgType === "joinedOnlineGame") {
        waitForMorePlayersInOnlineGame();
    }
    else if (msgType === "failedJoinOnlineGame") {
        infoMessage.innerHTML = "Couldn't join to the server! Please return\n\
                                 to the lobby!";
    }
    else if (msgType === "nameTaken") {
        informClientNameTaken();
    }
    else if (msgType === "gameCanceled") {
       document.getElementById("boardSvg").style.pointerEvents = "none";
       document.getElementById("exitMatchBtn").style.display = "block";
       infoMessage.innerHTML = "The game is canceled because on of the\n\
                                players left the lobby. Please leave!";
    }
    else if (msgType === "possibleMoves") {
        let possibleMoves = jsonMsg.possibleMoves.toString();
        for (let i = 0; i < possibleMoves.length / 4; i++) {
            let tempName = possibleMoves.substring(4*i, 4*i + 4);
            nextMoves.push(document.getElementById(tempName));
        }
        makeWhiteHolesClickable();
        infoMessage.innerHTML = "";
    }
    else if (msgType === "invalidMarble") {
        selectedBall.setAttribute('xlink:href', selectedBallOrignalHerf);
        cancelSelectedBall();
        infoMessage.innerHTML = "You can't select this ball!";
    }
    else if (msgType === "secondClick") {
        //selectedBall.setAttribute('xlink:href', selectedBallOrignalHerf);
        makeWhiteHolesNotClickable();
        switchBalls(jsonMsg.startLoc, jsonMsg.endLoc);
    }
    else if (msgType === "exitedLobby") {
        exitLobby();
    }
    else if (msgType === "currGameDetails") {
        let currC = String.fromCharCode(jsonMsg.currColor);
        currColor.innerHTML = "Current Player: " + getColor(currC);
        currTurn.innerHTML = "Current Game Turn: " + (jsonMsg.currTurn + 1);
    }
    else if (msgType === "gameWon") {
        let currC = String.fromCharCode(jsonMsg.winningColor);
        currColor.innerHTML = "Player " + getColor(currC) + " WON!";
        //currTurn.innerHTML = "Game over! Go back to the menu to play another one!";
        document.getElementById("boardSvg").style.pointerEvents = "none";
        document.getElementById("exitMatchBtn").style.display = "block";
    }
}

function onError(event) {
    infoMessage.innerHTML = "ERROR! :(";
}

function informClientNameTaken() {
    infoMessage.innerHTML = "Lobby name is taken! Please pick another one!";
}

function setBotGame() {
    document.getElementById("pickBots").style.display = "block";
    document.getElementById("selectGameMode").style.display = "none";
    infoMessage.innerHTML = "Select wanted bots!";
}

function setOnlineGame() {
    setPickingPlayersColors();
    document.getElementById("lobName").style.display = "block";
    document.getElementById("onlineBtn").style.display = "block";
    document.getElementById("localBtn").style.display = "none";
} 

function setLocalGame() {
    setPickingPlayersColors();
    document.getElementById("lobName").style.display = "none";
    document.getElementById("onlineBtn").style.display = "none";
    document.getElementById("localBtn").style.display = "block";
}

function setPickingPlayersColors() {
    document.getElementById("pickPlayersColors").style.display = "block";
    document.getElementById("selectGameMode").style.display = "none";
    infoMessage.innerHTML = "Select wanted colors!";
}

/**
* Starts a game vs AI and sends the server the information
*/
function startBotGame() {
    document.getElementById("selectGameMode").style.display = "block";
    document.getElementById("pickBots").style.display = "none";
    
    cancelPlayer("GroupYellow");
    cancelPlayer("GroupOrange");
    cancelPlayer("GroupPurple");
    cancelPlayer("GroupBlue");
    isLocalGame = false;
    
    greenP = document.getElementById("greenPlayer").value;
    redP = document.getElementById("redPlayer").value;
    
    if (greenP !== "human" && redP !== "human") {
        document.getElementById("exitMatchBtn").style.display = "none";
        document.getElementById("boardSvg").style.pointerEvents = "none";
    }
    
    let objToSend = null;
    if (greenP === "human" && redP === "human") {
        objToSend = { type:     "createLobby",
                      gameType: "local",
                      pColors:  "gr" };
        isLocalGame = true;
    }
    else {
        objToSend = { type:     "createLobby",
                      gameType: "bot",
                      greenP:    greenP,
                      redP:     redP };
    }
    sendJsonObjToServer(objToSend);
    //startGame();
}

/**
* Starts a local game and sends the server the information
*/
function startLocalGame() {
    let cStr = selectColors();
    
    if(cStr !== "") {
        let objToSend = { type:     "createLobby", 
                          gameType: "local" };
        objToSend["pColors"] = cStr;
        isLocalGame = true;
        sendJsonObjToServer(objToSend);
    }
    
}

function startOnlineGame() {
    let cStr = selectColors();
    
    if(cStr !== "") {
        let objToSend = { type:     "createLobby", 
                          gameType: "online" };
        objToSend["pColors"] = cStr;
        objToSend["lobName"] = document.getElementById("lobName").value;
        sendJsonObjToServer(objToSend);
    }
}

function selectColors() {
    let cStr = "";
    
    let c1 = document.getElementById("greenCB");
    let c2 = document.getElementById("redCB");
    let c3 = document.getElementById("yellowCB");
    let c4 = document.getElementById("purpleCB");
    let c5 = document.getElementById("orangeCB");
    let c6 = document.getElementById("blueCB");
    let playersCount = c1.checked + c2.checked + c3.checked +
                       c4.checked + c5.checked + c6.checked;
    
    if (playersCount >= 2) {
        cStr += checkIfCheckboxWasChecked(c1);
        cStr += checkIfCheckboxWasChecked(c2);
        cStr += checkIfCheckboxWasChecked(c3);
        cStr += checkIfCheckboxWasChecked(c4);
        cStr += checkIfCheckboxWasChecked(c5);
        cStr += checkIfCheckboxWasChecked(c6);
    }
    else {
        infoMessage.innerHTML = "Please select at least 2 colors!";
    }
    return cStr;
}


function checkIfCheckboxWasChecked(cb) {
    if (cb.checked)
        return cb.value;
    else {
        cancelPlayer(getGroupColor(cb.value));
        return "";
    }
}

function getColor(ch) {
    let color = "";
    if (ch === 'g')
        color = "Green";
    else if (ch === 'y')
        color = "Yellow";
    else if (ch === 'o')
        color = "Orange";
    else if (ch === 'r')
        color = "Red";
    else if (ch === 'p')
        color = "Purple";
    else if (ch === 'b')
        color = "Blue";
    return color;
}

function getColorChar(str) {
    let colorCh = "";
    if (str.includes("Green"))
        colorCh = 'g';
    else if (str.includes("Yellow"))
        colorCh = 'y';
    else if (str.includes("Orange"))
        colorCh = 'o';
    else if (str.includes("Red"))
        colorCh = 'r';
    else if (str.includes("Purple"))
        colorCh = 'p';
    else if (str.includes("Blue"))
        colorCh = 'b';
    return colorCh;
}

function getGroupColor(ch) {
    let color = "";
    if (ch === 'g')
        color = "GroupGreen";
    else if (ch === 'y')
        color = "GroupYellow";
    else if (ch === 'o')
        color = "GroupOrange";
    else if (ch === 'r')
        color = "GroupRed";
    else if (ch === 'p')
        color = "GroupPurple";
    else if (ch === 'b')
        color = "GroupBlue";
    return color;
}

/**
* Starting the game by hiding all the selectGameMode and showing the
* svg board game
*/
function startGame() {
    document.getElementById("gameUI").style.display = "block";
    document.getElementById("selectGameMode").style.display = "none";
    document.getElementById("pickPlayersColors").style.display = "none";
    document.getElementById("onlineGamesList").style.display = "none";
    infoMessage.innerHTML = "Game Started!";
}

function updateOnlineGameStats(nPlayersInLobby) {
    infoMessage.innerHTML = "Players in lobby: " + nPlayersInLobby;
}

function startPlayableOnlineGame() {
    document.getElementById("boardSvg").style.pointerEvents = "none";
    infoMessage.innerHTML = "All players joined! Game started!";
}

function exitMatch() {
    let objToSend = { type: "exitLobby" };
    sendJsonObjToServer(objToSend);
}

function returnToMenuFromPickingColors() {
    document.getElementById("pickPlayersColors").style.display = "none";
    document.getElementById("selectGameMode").style.display = "block";
    infoMessage.innerHTML = "Please pick up a game mode!";
}

function returnToMenuFromLobbiesList() {
    document.getElementById("onlineGamesList").style.display = "none";
    document.getElementById("selectGameMode").style.display = "block";
    document.getElementById("onlineGamesListTbl").innerHTML = originalOnlineGamesListTbl;
    infoMessage.innerHTML = "Please pick up a game mode!";
}

function exitLobby() {
    if(selectedBall !== null) {
        nextMoves = [];
        cancelSelectedBall();
    }
    document.getElementById("gameUI").innerHTML = originalGameUI;
    makeCellsClickable();
    document.getElementById("gameUI").style.display = "none";
    document.getElementById("selectGameMode").style.display = "block";
    currColor.innerHTML = "";
    currTurn.innerHTML = "";
    infoMessage.innerHTML = "Please pick up a game mode!";
}

/**
* Removes group color from the game by changing the herf of all the balls
* of the selected color to "#WhiteHole"
* @param  {String} groupColor The class name of color that need to be removed
*                             from the game
*/
function cancelPlayer(groupColor) {
    let team = document.getElementsByClassName(groupColor);
    for(let i = 0; i < 10; i++) {
        team[i].setAttribute('xlink:href', "#WhiteHole");
    }
}

/**
* Make the white holes on the nextMoves global array clickable by changing 
* their herf
*/
function makeWhiteHolesClickable() {
   for(let i = 0; i < nextMoves.length; i++) {
       nextMoves[i].setAttribute('xlink:href', "#WhiteHoleClickable");
   }
}

/**
* Make the white holes on the nextMoves global variable NOT clickable
* by changing their herf AND make reset the nextMoves global array
*/
function makeWhiteHolesNotClickable() {
   for(let i = 0; i < nextMoves.length; i++) {
       nextMoves[i].setAttribute('xlink:href', "#WhiteHole");
   }
   nextMoves = [];
}

/**
* Switches the herfs between 2 use elements that their id are startLoc and 
* endLoc and if necessary - cancel the selected ball
* @param  {String} startLoc The id of the first cell (colored ball)
* @param  {String} endLoc   The id of the second cell (white clickable hole)
*/
function switchBalls(startLoc, endLoc) {
    // Get the starting and ending use elements
    let coloredBall = document.getElementById(startLoc);
    let whiteHole = document.getElementById(endLoc);
    
    // Get the color of the colored ball
    let coloredBallHerf = coloredBall.getAttribute('xlink:href').toString();
    // Cut the 'OnClick' from the herf
    coloredBallHerf = coloredBallHerf.replace("OnClick", "");
    
    // Switch between their herf attributes
    whiteHole.setAttribute('xlink:href', coloredBallHerf);
    coloredBall.setAttribute('xlink:href', "#WhiteHole");

    // If the selected ball is not null, this means the updated ball was of
    // this player so reset the selected ball global variables
    if (isLocalGame) {
        if (selectedBall !== null)
            cancelSelectedBall();
    }
    else if(coloredBallHerf.localeCompare(selectedBallOrignalHerf)) {
        cancelSelectedBall();
    }
}

/**
* Saves the new clicked ball to the global variables selectedBall and
* selectedBallOrignalHerf and update the selected ball by updating
* its herf by changing it to 'OnClick' herf
* @param  {SVGUseElement} tempUseElement The new selected use element 
*                                        ( = the new selected ball)
* @param  {String}        tempHerf       The herf of the new selected use 
*                                        element ( = the new selected ball)
*/
function updateSelectedBall(tempUseElement, tempHerf){
    // Save the clicked ball
    selectedBall = tempUseElement;
    selectedBallOrignalHerf = tempHerf;

    // Update the selected ball
    selectedBall.setAttribute('xlink:href', tempHerf + 'OnClick');
}

/**
* Reset the selectedBall and the selectedBallOrignalHerf global variables
*/
function cancelSelectedBall() {
    selectedBall = null;
    selectedBallOrignalHerf = "";
}

function sendGetLobbiesListToServer() {
    document.getElementById("selectGameMode").style.display = "none";
    document.getElementById("onlineGamesList").style.display = "block";
    infoMessage.innerHTML = "Join to any game you wish!";
    
    let objToSend = { type: "getLobbiesList" };
    sendJsonObjToServer(objToSend);
}

/**
* Creates JSON object of firstClick type that contains the clicked ball,
* and sends the data to the server
*/
function sendFirstClickToServer() {
    let objToSend = { type: "firstClick",
                      startLoc: selectedBall.getAttribute("id") };
    sendJsonObjToServer(objToSend);
}

/**
* Creates JSON object of firstClick type that contains the clicked ball,
* and sends the data to the server
*/
function sendJoinLobbyToServer(lobName) {
    let objToSend = { type: "joinLobby",
                      lobName: lobName };
    sendJsonObjToServer(objToSend);
}

/**
* Creates JSON object of secondClick type that contains the starting position
* and the ending position, and sends the data to the server
* @param  {SVGUseElement} endCellName Use element of the second cell
*/
function sendSecondClickToServer(endCellName) {
    let objToSend = { type: "secondClick", 
                      startLoc: selectedBall.getAttribute("id"), 
                      endLoc: endCellName.getAttribute("id") };
    sendJsonObjToServer(objToSend);
}

/**
* Convert given JSON object to string and send it to the server
* @param  {Object} objToSend JSON object that needed to be sent
*                            to the server
*/
function sendJsonObjToServer(objToSend){
    socket.send(JSON.stringify(objToSend));
}