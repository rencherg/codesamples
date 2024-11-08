package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;

public class SQLGameDAO implements GameDAO{

    Gson gson = new Gson();

    public GameData createGame(ChessGame game, String blackUsername, String whiteUsername, String gameName) throws SQLException {

        GameData returnData = null;
        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        String jsonString = gson.toJson(game);
        int generatedId;
        ResultSet resultSet = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "INSERT INTO game_data\n" +
                    "(\n" +
                    "    white_username,\n" +
                    "    black_username,\n" +
                    "    game_name,\n" +
                    "    game_data\n" +
                    ")\n" +
                    "VALUES\n" +
                    "(?, ?, ?, ?);";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            myPreparedStatement.setString(1, whiteUsername);
            myPreparedStatement.setString(2, blackUsername);
            myPreparedStatement.setString(3, gameName);
            myPreparedStatement.setString(4, jsonString);
            myPreparedStatement.executeUpdate();

            resultSet = myPreparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                generatedId = resultSet.getInt(1);
                returnData = new GameData(generatedId, blackUsername, whiteUsername, gameName, game);
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
        return returnData;
    }

    public GameData getGame(int gameID) throws SQLException {

        GameData foundData = null;
        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "SELECT * FROM game_data WHERE id = ?;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, String.valueOf(gameID));
            resultSet = myPreparedStatement.executeQuery();

            if (resultSet.next()) {
                ChessGame game = gson.fromJson(resultSet.getString("game_data"), ChessGame.class);
                foundData = new GameData(resultSet.getInt("id"), resultSet.getString("black_username"), resultSet.getString("white_username"), resultSet.getString("game_name"), game);
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

        return foundData;

    }

    public GameData[] listGames() throws SQLException {

        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;
        int rowCount = DatabaseManager.rowCount("game_data");
        GameData gameDataArray[] = new GameData[rowCount];
        int currentIndex = 0;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "SELECT * FROM game_data;";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            resultSet = myPreparedStatement.executeQuery();

            while (resultSet.next()) {
                ChessGame game = gson.fromJson(resultSet.getString("game_data"), ChessGame.class);
                gameDataArray[currentIndex] = new GameData(resultSet.getInt("id"), resultSet.getString("black_username"), resultSet.getString("white_username"), resultSet.getString("game_name"), game);
                currentIndex++;
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

        return gameDataArray;
    }

    public GameData updateGame(GameData gameData) throws SQLException {

        if(this.getGame(gameData.getGameID()) == null){
            return null;
        }

        GameData returnData = null;
        Connection myConnection = null;
        PreparedStatement myPreparedStatement = null;
        ResultSet resultSet = null;
        String jsonString = gson.toJson(gameData.getGame());
        int id;

        try {

            myConnection = DatabaseManager.getConnection();
            String sqlQuery = "UPDATE game_data\n" +
                    "SET game_data = ?, black_username = ?, white_username = ? \n" +
                    "WHERE id = ?";
            myPreparedStatement = myConnection.prepareStatement(sqlQuery);
            myPreparedStatement.setString(1, jsonString);
            myPreparedStatement.setString(2, gameData.getBlackUsername());
            myPreparedStatement.setString(3, gameData.getWhiteUsername());
            myPreparedStatement.setString(4, String.valueOf(gameData.getGameID()));
            myPreparedStatement.execute();

            returnData = gameData;

        } catch (SQLException | DataAccessException e) {
            throw(new RuntimeException("Error: bad SQL query"));
        } finally{
            if(resultSet != null){
                resultSet.close();
            }
            myPreparedStatement.close();
            myConnection.close();

        }
        return returnData;
    }
}


//    UPDATE game_data
//    SET game_data = 'test', SET black_username = 'new', SET white_username = 'new'
//        WHERE id = 24
