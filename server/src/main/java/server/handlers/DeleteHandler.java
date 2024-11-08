package server.handlers;

import service.DeleteService;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

public class DeleteHandler extends ParentHandler {
    private DeleteService deleteService = new DeleteService();

    public String handleRequest(Request req, Response res) {

        String gsonString = "";

        try{

            deleteService.clear();

        }
        catch (RuntimeException exception){
            responseMap.put("message", "Error: bad request");
            res.status(400);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        gsonString = gson.toJson(responseMap);

        return gsonString;

    }
}
