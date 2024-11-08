package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public interface GameDAO {

    public GameData createGame(ChessGame game, String blackUsername, String whiteUsername, String gameName) throws SQLException;

    public GameData getGame(int gameID) throws SQLException;

    public GameData[] listGames() throws SQLException;

    public GameData updateGame(GameData gameData) throws SQLException;

    default int getUniqueID() throws SQLException {

        int id = 0;

        boolean foundValidToken = false;

        while(foundValidToken == false){
            id = ThreadLocalRandom.current().nextInt(1000, 10000);
            if(this.getGame(id) == null){
                foundValidToken = true;
            }
        }

        return id;
    }

}
