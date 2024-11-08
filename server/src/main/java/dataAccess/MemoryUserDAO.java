package dataAccess;

import model.UserData;

import java.util.Iterator;

public class MemoryUserDAO implements UserDAO {

    public MemoryUserDAO(){

    }

    //Checks if a user exists with the given username
    public UserData getUser(String username){
        Iterator<UserData> dataIterator = TempDB.userSet.iterator();

        UserData iteratorData;

        while (dataIterator.hasNext()) {

            iteratorData = dataIterator.next();

            if(iteratorData.getUsername().equals(username)){
                return iteratorData;
            }
        }
        return null;
    }

    //Checks if username and password are correct
    public UserData checkUserData(String username, String password){
        Iterator<UserData> dataIterator = TempDB.userSet.iterator();

        UserData iteratorData;

        while (dataIterator.hasNext()) {

            iteratorData = dataIterator.next();

            if((iteratorData.getUsername().equals(username)) && (iteratorData.getPassword().equals(password))){
                return iteratorData;
            }
        }
        return null;
    }

    public UserData createUser(UserData user){

        TempDB.userSet.add(user);

        return user;
    }

}
