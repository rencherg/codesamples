package service;

import chess.ChessGame;
import dataAccess.*;
import model.AuthData;
import model.GameData;

import java.sql.SQLException;

public class GameService {
    private final SQLGameDAO sqlGameDAO;
    private final SQLAuthDAO sqlAuthDAO;

    private boolean checkInfo(String data){
        return((data != "") && (data != null) && (data.length() > 0));
    }

    public GameService() {
        this.sqlGameDAO = new SQLGameDAO();
        this.sqlAuthDAO = new SQLAuthDAO();
    }

    public GameData[] getGame(String authToken) throws RuntimeException, SQLException {
        if(this.sqlAuthDAO.getAuth(authToken) != null){
            return this.sqlGameDAO.listGames();
        }else{
            throw new RuntimeException("Error: unauthorized");
        }
    }

    public int createGame(String authToken, String gameName) throws SQLException {
        if(this.sqlAuthDAO.getAuth(authToken) == null){
            throw new RuntimeException("Error: unauthorized");
        }if((this.checkInfo(gameName))){
            GameData newGameData = this.sqlGameDAO.createGame(new ChessGame(), null, null, gameName);
            return newGameData.getGameID();
        }else{
            throw new RuntimeException("Error: bad request");
        }
    }

    public boolean joinGame(String authToken, String clientColor, int gameID) throws SQLException {
        if(clientColor != null){
            clientColor = clientColor.toUpperCase();
        }
        AuthData userAuthData = this.sqlAuthDAO.getAuth(authToken);
        GameData gameData = this.sqlGameDAO.getGame(gameID);
        if(userAuthData == null){
            throw new RuntimeException("Error: unauthorized");
        } else if(gameData == null) {
            throw new RuntimeException("Error: bad request");

        //game observer
        }else if(clientColor == null){
            return true;
        }else if(((clientColor.equals("BLACK")) && (gameData.getBlackUsername()!=null) || ((clientColor.equals("WHITE")) && (gameData.getWhiteUsername()!=null)))){
            throw new RuntimeException("Error: already taken");
        }else{
            if(clientColor.equals("WHITE") && gameData.getWhiteUsername()==null){
                gameData.setWhiteUsername(userAuthData.getUsername());
                this.sqlGameDAO.updateGame(gameData);
                return true;
            } else if(clientColor.equals("BLACK") && gameData.getBlackUsername()==null){
                gameData.setBlackUsername(userAuthData.getUsername());
                this.sqlGameDAO.updateGame(gameData);
                return true;
            }else{
                throw new RuntimeException("Error: bad request");
            }
        }
    }
}
