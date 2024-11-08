package server.handlers;

import org.eclipse.jetty.websocket.api.Session;

public class UserSession {
    private int gameId = -1;
    private Session userSession;
    private WSHandler.ClientRole clientRole;

    public UserSession(Session userSession) {
//        this.gameId = gameId;
        this.userSession = userSession;
//        this.clientRole = clientRole;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setClientRole(WSHandler.ClientRole clientRole) {
        this.clientRole = clientRole;
    }

    public int getGameId() {
        return gameId;
    }

    public Session getUserSession() {
        return userSession;
    }

    public WSHandler.ClientRole getClientRole() {
        return clientRole;
    }
}
