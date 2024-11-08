import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();

        try{
            server.run(8080);
        }catch (Exception e){
            server.stop();
        }

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}