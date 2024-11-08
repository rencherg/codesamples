package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class MemoryGameDAO implements GameDAO {

    public MemoryGameDAO(){

    }

    public GameData createGame(ChessGame game, String blackUsername, String whiteUsername, String gameName) throws SQLException {
        GameData gameData = new GameData(this.getUniqueID(), blackUsername, whiteUsername, gameName, game);
        TempDB.gameSet.add(gameData);
        return gameData;
    }

    public GameData getGame(int gameID){

        Iterator<GameData> dataIterator = TempDB.gameSet.iterator();

        GameData iteratorData;

        while (dataIterator.hasNext()) {

            iteratorData = dataIterator.next();

            if(iteratorData.getGameID() == gameID){
                return iteratorData;
            }
        }
        return null;

    }

    public GameData[] listGames(){
        GameData gameDataArray[] = new GameData[TempDB.gameSet.size()];

        int i = 0;
        for (GameData game : TempDB.gameSet)
            gameDataArray[i++] = game;

        return gameDataArray;
    }

    public GameData updateGame(GameData gameData){
        GameData foundGameData = this.getGame(gameData.getGameID());
        foundGameData.setGame(gameData.getGame());
        return gameData;
    }
}
