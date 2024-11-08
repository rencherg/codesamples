package dataAccess;

import model.AuthData;
import model.UserData;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO{

    public AuthData createAuth(String username) throws SQLException, DataAccessException {

        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        String userID = SQLUserDAO.getUserID(username);
        String token = this.getUniqueToken();
        AuthData authData = null;

        if(userID == null){
            return null;
        }

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "INSERT INTO auth_data\n" +
                    "(\n" +
                    "    username,\n" +
                    "    token,\n" +
                    "    user_id\n" +
                    ")\n" +
                    "VALUES\n" +
                    "(?, ?, ?);";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, username);
            myPreparedStatement.setString(2, token);
            myPreparedStatement.setString(3, userID);
            myPreparedStatement.execute();
            authData = new AuthData(token, username);

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{

            if(myPreparedStatement!= null){
                myPreparedStatement.close();
            }
            if(myConnection != null){
                myConnection.close();
            }
        }
        return authData;
    }

    public boolean deleteAuth(String authToken) throws SQLException, DataAccessException {

        boolean success = false;

        if(DatabaseManager.rowCount("auth_data") == 0){
            return false;
        }

        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "DELETE FROM auth_data WHERE token = ?;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, authToken);
            myPreparedStatement.execute();

            success = true;

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{
            if(myPreparedStatement!= null){
                myPreparedStatement.close();
            }
            if(myConnection != null){
                myConnection.close();
            }

        }
        return success;

    }

    public AuthData getAuth(String authToken) throws SQLException {

        if(DatabaseManager.rowCount("auth_data") == 0){
            return null;
        }

        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;

        AuthData foundData = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "SELECT * FROM auth_data WHERE token = ?;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, authToken);
            resultSet = myPreparedStatement.executeQuery();

            if(resultSet.next()){
                foundData = new AuthData(resultSet.getString("token"), resultSet.getString("username"));
            }

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{
            if(resultSet != null){
                resultSet.close();
            }
            if(myPreparedStatement!= null){
                myPreparedStatement.close();
            }
            if(myConnection != null){
                myConnection.close();
            }

            return foundData;
        }

    }
}
