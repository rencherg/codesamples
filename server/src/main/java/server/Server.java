package server;

import server.handlers.*;
import spark.Spark;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.webSocket("/connect", WSHandler.class);

        Spark.staticFiles.location("web");

        Spark.post("/user", (req, res) ->
                (new RegisterHandler()).handleRequest(req,
                        res));

        Spark.post("/session", (req, res) ->
                (new LoginHandler()).handleRequest(req,
                        res));

        Spark.delete("/session", (req, res) ->
                (new LogoutHandler()).handleRequest(req,
                        res));

        Spark.post("/game", (req, res) ->
                (new CreateGameHandler()).handleRequest(req,
                        res));

        Spark.get("/game", (req, res) ->
                (new ListGameHandler()).handleRequest(req,
                        res));

        Spark.put("/game", (req, res) ->
                (new JoinGameHandler()).handleRequest(req,
                        res));

        Spark.delete("/db", (req, res) ->
                (new DeleteHandler()).handleRequest(req,
                        res));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}