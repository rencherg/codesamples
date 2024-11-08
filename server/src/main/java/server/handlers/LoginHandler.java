package server.handlers;

import com.google.gson.JsonObject;
import dataAccess.DataAccessException;
import model.AuthData;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

public class LoginHandler extends ParentHandler {

    public String handleRequest(Request req, Response res) {

        String gsonString = "";

        try{

            JsonObject jobj = gson.fromJson(req.body(), JsonObject.class);
            String username = jobj.get("username").getAsString();
            String password = jobj.get("password").getAsString();

            AuthData authData = this.userService.login(username, password);

            if(authData != null){
                res.status(200);
                responseMap.put("authToken", authData.getAuthToken());
                responseMap.put("username", authData.getUsername());
            }else{
                res.status(400);
                responseMap.put("message", "Error: bad request");
            }
        }
        catch (RuntimeException exception){
            this.parseException(exception, res);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        gsonString = gson.toJson(responseMap);
        return gsonString;

    }
}
