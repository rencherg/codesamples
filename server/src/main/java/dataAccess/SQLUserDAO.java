package dataAccess;

import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

public class SQLUserDAO implements UserDAO {

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserData getUser(String username) throws SQLException {

        if(DatabaseManager.rowCount("user_data") == 0){
            return null;
        }

        UserData foundData = null;
        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "SELECT * FROM user_data WHERE username = ?;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, username);
            resultSet = myPreparedStatement.executeQuery();

            if(resultSet.next()){
                foundData = new UserData(resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("email"));
            }

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{
            if(resultSet != null){
                resultSet.close();
            }
            myPreparedStatement.close();
            myConnection.close();

            return foundData;
        }
    }

    //for internal package use
    static String getUserID(String username) throws SQLException, DataAccessException {

        if(DatabaseManager.rowCount("user_data") == 0){
            return null;
        }

        String foundId = null;
        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "SELECT * FROM user_data WHERE username = ?;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, username);
            resultSet = myPreparedStatement.executeQuery();

            if(resultSet.next()){
                foundId = resultSet.getString("id");
            }

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{
            if(resultSet != null){
                resultSet.close();
            }
            myPreparedStatement.close();
            myConnection.close();

        }
        return foundId;
    }

    //Checks if username and password are correct
    public UserData checkUserData(String username, String password) throws SQLException {

        if(DatabaseManager.rowCount("user_data") == 0){
            return null;
        }

        UserData foundData = null;

        try {

            UserData userData = this.getUser(username);
            if((userData != null) && (encoder.matches(password, userData.getPassword()))){
                foundData = userData;
            }

        } catch (SQLException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        }
        return foundData;
    }

    public UserData createUser(UserData user) throws SQLException {

        //Reject if user already exists
        if(this.getUser(user.getUsername()) != null){
            throw new RuntimeException("Error: User already exists");
        }

        //password encryption
        String encryptedPassword = encoder.encode(user.getPassword());

        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        boolean wasSuccesful = false;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "INSERT INTO user_data\n" +
                    "(\n" +
                    "    username,\n" +
                    "    password,\n" +
                    "    email\n" +
                    ")\n" +
                    "VALUES\n" +
                    "(?, ?, ?);";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, user.getUsername());
            myPreparedStatement.setString(2, encryptedPassword);
            myPreparedStatement.setString(3, user.getEmail());
            myPreparedStatement.execute();
            wasSuccesful = true;

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

        if(wasSuccesful){
            return user;
        }else{
            return null;
        }
    }
}
