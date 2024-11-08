package dataAccess;

import model.AuthData;

import java.sql.SQLException;
import java.util.Iterator;

import java.security.SecureRandom;

public class MemoryAuthDAO implements AuthDAO{

    public AuthData createAuth(String username) throws SQLException {

        AuthData authData = new AuthData(this.getUniqueToken(), username);
        TempDB.authSet.add(authData);
        return authData;
    }

    public boolean deleteAuth(String authToken){

        Iterator<AuthData> dataIterator = TempDB.authSet.iterator();

        AuthData iteratorData;

        while (dataIterator.hasNext()) {

            iteratorData = dataIterator.next();

            if(iteratorData.getAuthToken().equals(authToken)){
                TempDB.authSet.remove(iteratorData);
                return true;
            }
        }
        return false;

    }

    public AuthData getAuth(String authToken){

        Iterator<AuthData> dataIterator = TempDB.authSet.iterator();

        AuthData iteratorData;

        while (dataIterator.hasNext()) {

            iteratorData = dataIterator.next();

            if(iteratorData.getAuthToken().equals(authToken)){
                return iteratorData;
            }
        }
        return null;

    }
}
