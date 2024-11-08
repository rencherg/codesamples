package dataAccess;

import model.AuthData;

import java.security.SecureRandom;
import java.sql.SQLException;

//Auth Dao
public interface AuthDAO {

    AuthData createAuth(String username) throws SQLException, DataAccessException;

    boolean deleteAuth(String authToken) throws SQLException, DataAccessException;

    AuthData getAuth(String authToken) throws SQLException;

    //Generates a unique token
    default String getUniqueToken() throws SQLException {

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        String token = "";

        boolean foundValidToken = false;

        while(foundValidToken == false){
            random.nextBytes(bytes);
            token = bytes.toString();
            if(this.getAuth(token) == null){
                foundValidToken = true;
            }
        }

        return token;
    }

}
