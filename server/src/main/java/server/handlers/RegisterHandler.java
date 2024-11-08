package server.handlers;

import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

public class RegisterHandler extends ParentHandler {

    public String handleRequest(Request req, Response res) {

        String gsonString;

        try{

            UserData userData = gson.fromJson(req.body(), UserData.class);
            AuthData authData = this.userService.register(userData);

            if(authData != null){
                res.status(200);
                responseMap.put("username", authData.getUsername());
                responseMap.put("authToken", authData.getAuthToken());

            }else{
                res.status(400);
                responseMap.put("message", "Error: bad request");
            }
        }
        catch (RuntimeException exception){
            this.parseException(exception, res);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        gsonString = gson.toJson(responseMap);
        return gsonString;

    }
}
