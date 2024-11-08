package service;

import dataAccess.DataAccessException;
import dataAccess.SQLAuthDAO;
import dataAccess.SQLUserDAO;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;

public class UserService {

    private final SQLUserDAO sqlUserDAO;
    private final SQLAuthDAO sqlAuthDAO;


    public UserService() {
        this.sqlUserDAO = new SQLUserDAO();
        this.sqlAuthDAO = new SQLAuthDAO();
    }

    //checks if not null or empty
    private boolean checkInfo(String data){
        return((!data.equals("")) && (data.length() > 0));
    }

    public AuthData register(UserData userData) throws RuntimeException, SQLException, DataAccessException {
        if(sqlUserDAO.getUser(userData.getUsername()) != null){
            throw new RuntimeException("Error: already taken");
        } else if(checkInfo(userData.getUsername()) && checkInfo(userData.getPassword()) && checkInfo(userData.getEmail()) && (this.sqlUserDAO.getUser(userData.getUsername())==null)){
            UserData newUser = new UserData(userData.getUsername(), userData.getPassword(), userData.getEmail());
            this.sqlUserDAO.createUser(newUser);
            return this.sqlAuthDAO.createAuth(userData.getUsername());
        }else{
            throw new RuntimeException("Error: bad request");
        }
    }

    public AuthData login(String username, String password) throws SQLException, DataAccessException {
        if ((this.sqlUserDAO.checkUserData(username, password) == null)) {
            throw new RuntimeException("Error: unauthorized");
        } else if(checkInfo(username) && checkInfo(password)){// && (this.memoryAuthDAO.getAuthUsername(username) == null) This part may need to be included to prevent a user from logging in again
            return this.sqlAuthDAO.createAuth(username);
        }else{
            throw new RuntimeException("Error: bad request");
        }
    }

    public boolean logout(String authToken) throws SQLException, DataAccessException {
        if(this.sqlAuthDAO.getAuth(authToken) == null){
            throw new RuntimeException("Error: unauthorized");
        }else if(this.sqlAuthDAO.deleteAuth(authToken)){
            return true;
        }else{
            throw new RuntimeException("Error: unauthorized");
        }
    }

}
