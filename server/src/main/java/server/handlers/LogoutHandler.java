package server.handlers;

import dataAccess.DataAccessException;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

public class LogoutHandler extends ParentHandler {

    public String handleRequest(Request req, Response res) {

        String gsonString = "";

        try{

            String authToken = req.headers("authorization");

            boolean successfulLogout = this.userService.logout(authToken);

            if(successfulLogout){
                res.status(200);
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
