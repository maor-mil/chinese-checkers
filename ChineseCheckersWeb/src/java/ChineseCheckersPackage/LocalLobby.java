package ChineseCheckersPackage;

public class LocalLobby extends Lobby {
    
    public LocalLobby(String playersColors) {
        super((byte)(playersColors.length()));
        super.addColorsToGame(playersColors);
    }
}
