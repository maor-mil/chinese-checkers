package ChineseCheckersPackage;

public abstract class BotType {
    public final static int TIMEOUT_MILISECONDS = 1000;
    protected BotLobby botLob;
    protected byte randomOpeningMove;
    
    public abstract Move botMove();
    
    protected Move makeGreenKnownOpenningMove() {
        Move[][] knownOpeningMoves = {
            {
                new Move(new byte[]{3, 9, 4, 10}),
                new Move(new byte[]{3, 15, 4, 14}),
                new Move(new byte[]{1, 11, 5, 11}),
                new Move(new byte[]{1, 13, 5, 13}),
                new Move(new byte[]{0, 12, 1, 11})
            },
            {
                new Move(new byte[]{3, 15, 4, 14}),
                new Move(new byte[]{3, 9, 4, 10}),
                new Move(new byte[]{1, 13, 5, 13}),
                new Move(new byte[]{1, 11, 5, 11}),
                new Move(new byte[]{0, 12, 1, 13})
            }
        };
        
        return knownOpeningMoves[this.randomOpeningMove][botLob.currGameTurn];
    }
    
    protected Move makeRedKnownOpenningMove() {
        Move[][] knownOpeningMoves = {
            {
                new Move(new byte[]{13, 9, 12, 10}),
                new Move(new byte[]{13, 15, 12, 14}),
                new Move(new byte[]{15, 11, 11, 11}),
                new Move(new byte[]{15, 13, 11, 13}),
                new Move(new byte[]{16, 12, 15, 11})
            },
            {
                new Move(new byte[]{13, 15, 12, 14}),
                new Move(new byte[]{13, 9, 12, 10}),
                new Move(new byte[]{15, 13, 11, 13}),
                new Move(new byte[]{15, 11, 11, 11}),
                new Move(new byte[]{16, 12, 15, 13})
            }
        };
        
        return knownOpeningMoves[this.randomOpeningMove][botLob.currGameTurn];
    }
}