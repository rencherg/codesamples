package server.handlers;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.SQLAuthDAO;
import dataAccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerError;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@WebSocket
public class WSHandler {

    // List to store connected sessions
    private static final CopyOnWriteArrayList<Session> sessions = new CopyOnWriteArrayList<>();
    private List<UserSession> userSessionListData = new ArrayList<>();
    SQLGameDAO sqlGameDAO = new SQLGameDAO();
    SQLAuthDAO sqlAuthDAO = new SQLAuthDAO();
    WSHelper wsHelper = new WSHelper();

    Gson gson = new Gson();

    public enum ClientRole {
        OBSERVER,
        WHITE,
        BLACK

    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        sessions.add(session);
        userSessionListData.add(new UserSession(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand receivedCommandClass = gson.fromJson(message, UserGameCommand.class);
        UserGameCommand.CommandType receivedCommand = receivedCommandClass.getCommandType();

        switch(receivedCommand){
            case JOIN_OBSERVER:
                JoinObserver joinObserver = gson.fromJson(message, JoinObserver.class);
                handleJoinObserver(session, joinObserver);
                break;
            case JOIN_PLAYER:
                JoinPlayer joinPlayer = gson.fromJson(message, JoinPlayer.class);
                handleJoinPlayer(session, joinPlayer);
                break;
            case MAKE_MOVE:
                MakeMove makeMove = gson.fromJson(message, MakeMove.class);
                handleMakeMove(session, makeMove);
                break;
            case LEAVE:
                Leave leave = gson.fromJson(message, Leave.class);
                handleLeave(session, leave);
                break;
            case RESIGN:
                Resign resign = gson.fromJson(message, Resign.class);
                handleResign(session, resign);
                break;
        }

    }

    private void handleJoinObserver(Session session, JoinObserver joinObserver){

        String authToken = joinObserver.getAuthString();
        int gameId = joinObserver.getGameID();
        ChessGame chessGame = null;
        String username = checkAuthToken(authToken, session);

        if(username == null){
            return;
        }

        //verify game id
        try {

            GameData gameToJoin = sqlGameDAO.getGame(gameId);

            if(gameToJoin == null){
                ServerError serverError = new ServerError("Error: Bad game ID");
                wsHelper.sendMessage(serverError, session);
                return;
            }

            chessGame = gameToJoin.getGame();

            if(chessGame.isGameOver()){
                ServerError serverError = new ServerError("Error: The game is over");
                wsHelper.sendMessage(serverError, session);
                return;
            }
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Bad game ID or other SQL issue");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        for(UserSession userSession: userSessionListData){
            if(userSession.getUserSession().equals(session)){
                userSession.setGameId(gameId);
                userSession.setClientRole(ClientRole.OBSERVER);
            }
        }

        LoadGame loadGame = new LoadGame(chessGame);
        wsHelper.sendMessage(loadGame, session);
        Notification notification = new Notification("Notification: " + username + " has joined as an observer.");
        wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
    }

    private void handleJoinPlayer(Session session, JoinPlayer joinPlayer){
        String authToken = joinPlayer.getAuthString();
        int gameId = joinPlayer.getGameID();
        ChessGame chessGame = null;
        ChessGame.TeamColor teamColor = joinPlayer.getPlayerColor();
        String joinedTeam = null;

        if(teamColor == null){
            ServerError serverError = new ServerError("Error: Incorrect Color");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        String username = checkAuthToken(authToken, session);

        if(username == null){
            return;
        }

        //verify game id
        try {
            GameData gameToJoin = sqlGameDAO.getGame(gameId);
            String whiteUsername = gameToJoin.getWhiteUsername();
            String blackUsername = gameToJoin.getBlackUsername();

            if(gameToJoin == null){
                ServerError serverError = new ServerError("Error: Bad game ID");
                wsHelper.sendMessage(serverError, session);
                return;
            }

            chessGame = gameToJoin.getGame();

            if(chessGame.isGameOver()){
                ServerError serverError = new ServerError("Error: Game is over");
                wsHelper.sendMessage(serverError, session);
                return;
            }else if(!((whiteUsername != null && whiteUsername.equals(username) && teamColor.equals(ChessGame.TeamColor.WHITE)) || (blackUsername != null && blackUsername.equals(username) && teamColor.equals(ChessGame.TeamColor.BLACK)))){
                ServerError serverError = new ServerError("Error: Incorrect Color");
                wsHelper.sendMessage(serverError, session);
                return;
            }
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Bad game ID");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        for(UserSession userSession: userSessionListData){
            if(userSession.getUserSession().equals(session)){
                userSession.setGameId(gameId);
                if(teamColor.equals(ChessGame.TeamColor.BLACK)){
                    userSession.setClientRole(ClientRole.BLACK);
                    joinedTeam = "black";
                }else if(teamColor.equals(ChessGame.TeamColor.WHITE)){
                    userSession.setClientRole(ClientRole.WHITE);
                    joinedTeam = "white";
                }

            }
        }

        LoadGame loadGame = new LoadGame(chessGame);
        wsHelper.sendMessage(loadGame, session);
        Notification notification = new Notification("Notification: " + username + " has joined the " + joinedTeam + " team.");
        wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
    }

    private void handleMakeMove(Session session, MakeMove makeMove){
        String authToken = makeMove.getAuthString();
        int gameId = makeMove.getGameID();


        ChessMove chessMove = makeMove.getMove();

        String username = checkAuthToken(authToken, session);
        if(username == null){
            return;
        }

        GameData gameData = checkGameIdHelper(gameId, session);
        if(gameData == null){
            return;
        }

        ChessGame chessGame = gameData.getGame();

        for(UserSession userSession: userSessionListData){
            if(userSession.getUserSession().equals(session)){
                //Check that the participant making a move is a player

                ClientRole clientRole = userSession.getClientRole();

                //Check that the participant resigning is a player
                if(clientRole == null || clientRole.equals(ClientRole.OBSERVER)){
                    ServerError serverError = new ServerError("Error: Only players can make moves");
                    wsHelper.sendMessage(serverError, session);
                    return;

                //Check that its the players turn that wants to make a move
                }else if(!((clientRole.equals(ClientRole.BLACK) && chessGame.getTeamTurn().equals(ChessGame.TeamColor.BLACK))||(clientRole.equals(ClientRole.WHITE) && chessGame.getTeamTurn().equals(ChessGame.TeamColor.WHITE)))){
                    ServerError serverError = new ServerError("Error: It's not your turn");
                    wsHelper.sendMessage(serverError, session);
                    return;
                }
            }
        }

        //try to execute move - If it doesn't work then return error
        try{
            chessGame.makeMove(chessMove);
        }catch(InvalidMoveException e){
            ServerError serverError = new ServerError("Error: Invalid move");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        //This may need to change to a set function if the chessgame object in the gameData object is not changed automatically
        try {
            sqlGameDAO.updateGame(gameData);
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Problem with the move");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        //Check if the game is in checkmate or stalemate
        if(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)){
            markGameOver(gameData, session);

            Notification notification = new Notification("Black has won the game by checkmate!");
            wsHelper.sendToAll(notification, session, userSessionListData);
        }else if(chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)){
            markGameOver(gameData, session);

            Notification notification = new Notification("White has won the game by checkmate!");
            wsHelper.sendToAll(notification, session, userSessionListData);
        }else if(chessGame.isInStalemate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)){
            chessGame.setGameOver(true);

            markGameOver(gameData, session);

            Notification notification = new Notification("The game has reached stalemate!");
            wsHelper.sendToAll(notification, session, userSessionListData);
        }

        LoadGame loadGame = new LoadGame(chessGame);
        wsHelper.sendToAll(loadGame, session, userSessionListData);

        //This may need to change
        Notification notification = new Notification(username + " has made a move!");
        wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
    }

    private void handleResign(Session session, Resign resign){
        String authToken = resign.getAuthString();
        int gameId = resign.getGameID();

        String username = checkAuthToken(authToken, session);
        if(username == null){
            return;
        }

        GameData gameData = checkGameIdHelper(gameId, session);
        if(gameData == null){
            return;
        }

        ChessGame chessGame = gameData.getGame();

        for(UserSession userSession: userSessionListData){
            if(userSession.getUserSession().equals(session)){

                ClientRole clientRole = userSession.getClientRole();

                //Check that the participant resigning is a player
                if(clientRole == null || clientRole.equals(ClientRole.OBSERVER)){
                    ServerError serverError = new ServerError("Error: You must be a player to resign.");
                    wsHelper.sendMessage(serverError, session);
                    return;
                }else{

                    if(clientRole.equals(ClientRole.WHITE)){
                        Notification notification = new Notification("Notification: " + username + ", the white player has resigned! Black wins the game!");
                        wsHelper.sendToAll(notification, session, userSessionListData);
                    }else if(clientRole.equals(ClientRole.BLACK)) {
                        Notification notification = new Notification("Notification: " + username + ", the black player has resigned! White wins the game!");
                        wsHelper.sendToAll(notification, session, userSessionListData);
                    }

                    markGameOver(gameData, session);

                    break;
                }

            }
        }
    }

    private void handleLeave(Session session, Leave leave){
        String authToken = leave.getAuthString();
        int gameId = leave.getGameID();
        GameData joinedGame;

        String username = checkAuthToken(authToken, session);
        if(username == null){
            return;
        }

        //verify game id
        try {
            joinedGame = sqlGameDAO.getGame(gameId);
            if(joinedGame == null){
                ServerError serverError = new ServerError("Error: Bad game ID");
                wsHelper.sendMessage(serverError, session);
                return;
            }

        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Bad game ID");
            wsHelper.sendMessage(serverError, session);
            return;
        }

        for(UserSession userSession: userSessionListData){
            if(userSession.getUserSession().equals(session)){

                ClientRole clientRole = userSession.getClientRole();

                if(clientRole != null && clientRole.equals(ClientRole.BLACK)){
                    joinedGame.setBlackUsername(null);
                    Notification notification = new Notification("Notification: " + username + ", the black player has left the game.");
                    wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
                }else if(clientRole != null && clientRole.equals(ClientRole.WHITE)){
                    joinedGame.setWhiteUsername(null);
                    Notification notification = new Notification("Notification: " + username + ", the white player has left the game.");
                    wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
                }else if(clientRole != null && clientRole.equals(ClientRole.OBSERVER)){
                    Notification notification = new Notification("Notification: " + username + ", an observer has left the game.");
                    wsHelper.sendToAllExceptRoot(notification, session, userSessionListData);
                }

                try {
                    sqlGameDAO.updateGame(joinedGame);
                } catch (SQLException e) {
                    ServerError serverError = new ServerError("Error: Problem with leaving the game");
                    wsHelper.sendMessage(serverError, session);
                }

                userSession.setClientRole(null);
                userSession.setGameId(-1);

            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {

        // Remove the closed session from the list of sessions
        UserSession userSession = wsHelper.getUserSession(session, userSessionListData);
        userSessionListData.remove(userSession);

        sessions.remove(session);
    }

    private void markGameOver(GameData gameData, Session session){

        ChessGame chessGame = gameData.getGame();
        chessGame.setGameOver(true);

        try {
            sqlGameDAO.updateGame(gameData);
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Problem with the move");
            wsHelper.sendMessage(serverError, session);
        }
    }

    private String checkAuthToken(String authToken, Session session){
        String username = null;
        //Check authToken
        try {
            AuthData authData = sqlAuthDAO.getAuth(authToken);
            if(authData == null){
                ServerError serverError = new ServerError("Error: Failed authentication");
                wsHelper.sendMessage(serverError, session);
                return null;
            }else{
                username = authData.getUsername();
            }
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Failed authentication");
            wsHelper.sendMessage(serverError, session);
            return null;
        }
        return username;
    }

    //verify game id
    private GameData checkGameIdHelper(int gameId, Session session){
        GameData gameData;
        try {
            gameData = sqlGameDAO.getGame(gameId);
            if(gameData == null){
                ServerError serverError = new ServerError("Error: Bad game ID");
                wsHelper.sendMessage(serverError, session);
                return null;
            }

            ChessGame chessGame = gameData.getGame();

            if(chessGame.isGameOver()){
                ServerError serverError = new ServerError("Error: The game is over");
                wsHelper.sendMessage(serverError, session);
                return null;
            }
        } catch (SQLException e) {
            ServerError serverError = new ServerError("Error: Bad game ID");
            wsHelper.sendMessage(serverError, session);
            return null;
        }
        return gameData;
    }
}