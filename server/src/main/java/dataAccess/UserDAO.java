package dataAccess;

import model.UserData;

import java.sql.SQLException;

public interface UserDAO {

    //Checks if a user exists with the given username
    public UserData getUser(String username) throws SQLException;

    //Checks if username and password are correct
    public UserData checkUserData(String username, String password) throws SQLException;

    public UserData createUser(UserData user) throws SQLException;

}
