package server.handlers;

import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CreateGameHandler extends ParentHandler{

    private final Map<String, Integer> gameResponseMap = new HashMap<>();

    public String handleRequest(Request req, Response res) {

        String gsonString;

        try{

            String authToken = req.headers("authorization");

            JsonObject jobj = gson.fromJson(req.body(), JsonObject.class);
            String gameName = jobj.get("gameName").getAsString();

            int gameID = this.gameService.createGame(authToken, gameName);

            gameResponseMap.put("gameID", gameID);
            gsonString = gson.toJson(gameResponseMap);
        }
        catch (RuntimeException exception){
            this.parseException(exception, res);
            gsonString = gson.toJson(responseMap);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return gsonString;

    }
}
