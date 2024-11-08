package model;

import chess.ChessGame;

public class GameData {
    private int gameID;
    private String blackUsername;
    private String whiteUsername;
    private String gameName;
    private ChessGame game;

    public GameData(int gameID, String blackUsername, String whiteUsername, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.blackUsername = blackUsername;
        this.whiteUsername = whiteUsername;
        this.gameName = gameName;
        this.game = game;
    }

    public int getGameID() {
        return gameID;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "GameData{" +
                "gameID=" + gameID +
                ", blackUsername='" + blackUsername + '\'' +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", gameName='" + gameName + '\'' +
                ", game=" + game.toString() +
                '}';
    }
}
