package server.handlers;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.ServerMessage;

import java.util.List;

public class WSHelper {

    Gson gson = new Gson();

    //Sends message to all users in the same game
    public void sendToAll(ServerMessage serverMessage, Session rootSession, List<UserSession> userSessionListData) {

        UserSession rootUserSession = getUserSession(rootSession, userSessionListData);
        int id = rootUserSession.getGameId();

        if(rootUserSession == null){
            System.err.print("Error: Root user not found.");
            return;
        }

        userSessionListData.forEach(userSession -> {
            try {
                int userId = userSession.getGameId();
                if(userId == id){
                    final Session session = userSession.getUserSession();
                    sendMessage(serverMessage, session);
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        });
    }

    //Sends message to all users in the same game
    public void sendToAllExceptRoot(ServerMessage serverMessage, Session rootSession, List<UserSession> userSessionListData) {

        UserSession rootUserSession = getUserSession(rootSession, userSessionListData);
        int id = rootUserSession.getGameId();

        if(rootUserSession == null){
            System.err.print("Error: Root user not found.");
            return;
        }

        userSessionListData.forEach(userSession -> {
            try {
                int userId = userSession.getGameId();
                if(userId == id && !rootUserSession.equals(userSession)){
                    final Session session = userSession.getUserSession();
                    sendMessage(serverMessage, session);
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        });
    }

    //send a message to the given session
    public void sendMessage(ServerMessage serverMessage, Session session) {
        try {
            String message = gson.toJson(serverMessage);
            session.getRemote().sendString(message);
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }

    //Get the userSession object given a Session object
    public UserSession getUserSession(Session session, List<UserSession> userSessionListData) {
        for (UserSession userSession : userSessionListData) {
            if (userSession.getUserSession().equals(session)) {
                return userSession;
            }
        }
        return null;
    }
}


