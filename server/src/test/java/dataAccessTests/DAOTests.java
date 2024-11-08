package dataAccessTests;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import dataAccess.*;
import model.GameData;
import org.junit.jupiter.api.*;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

//For testing the DAO classes
public class DAOTests {

    private final MemoryUserDAO memoryUserDAO = new MemoryUserDAO();
    private final MemoryAuthDAO memoryAuthDAO = new MemoryAuthDAO();
    SQLUserDAO sqlUserDAO = new SQLUserDAO();
    SQLAuthDAO sqlAuthDAO = new SQLAuthDAO();
    SQLGameDAO sqlGameDAO = new SQLGameDAO();

    @BeforeAll
    public static void init(){
    }

    //Old memory DAO test
    @Test
    @Order(1)
    public void userDAOTest(){
        UserData myUser1 = new UserData("rencherg", "password", "rencher.grant@gmail.com");
        UserData myUser2 = new UserData("fmulder", "TrustNo1", "f.mulder@gmail.com");

        this.memoryUserDAO.createUser(myUser1);
        this.memoryUserDAO.createUser(myUser2);

        Assertions.assertEquals(this.memoryUserDAO.getUser("rencherg"), myUser1);
        Assertions.assertEquals(this.memoryUserDAO.checkUserData("fmulder", "TrustNo1"), myUser2);
        Assertions.assertNotEquals(this.memoryUserDAO.getUser("fmulder"), myUser1);
        Assertions.assertNotEquals(this.memoryUserDAO.checkUserData("rencherg", "password"), myUser2);
    }

    //Old memory DAO test
    @Test
    @Order(2)
    public void authDAOTest() throws SQLException {

        AuthData myAuthData = this.memoryAuthDAO.createAuth("rencherg");

        Assertions.assertEquals(this.memoryAuthDAO.getAuth(myAuthData.getAuthToken()), myAuthData);
        Assertions.assertNull(this.memoryAuthDAO.getAuth("sample"));

        boolean deleteBoolean = this.memoryAuthDAO.deleteAuth(myAuthData.getAuthToken());

        Assertions.assertTrue(deleteBoolean);

        Assertions.assertNull(this.memoryAuthDAO.getAuth(myAuthData.getAuthToken()));

    }

    //Old memory DAO test
    @Test
    public void getUserSQL() throws SQLException{

        sqlUserDAO.createUser(new UserData("dread pirate roberts", "test123", "d.pr@fbi.gov"));

        Assertions.assertEquals("d.pr@fbi.gov", sqlUserDAO.getUser("dread pirate roberts").getEmail());

    }

    @Test
    public void getCount() throws SQLException{

        //Need to change
        Assertions.assertEquals(0, DatabaseManager.rowCount("game_data"));

    }

    @Test
    public void getUserSQLPositive() throws SQLException {
        sqlUserDAO.createUser(new UserData("don", "trustno1", "don@fbi.gov"));
        sqlUserDAO.createUser(new UserData("carlos", "trustno1", "carlos@fbi.gov"));
        sqlUserDAO.createUser(new UserData("smith", "trustno1", "smith@fbi.gov"));

        Assertions.assertEquals("carlos@fbi.gov", sqlUserDAO.getUser("carlos").getEmail());

    }

    @Test
    public void getUserSQLNegative() throws SQLException {
        sqlUserDAO.createUser(new UserData("don", "trustno1", "don@fbi.gov"));
        sqlUserDAO.createUser(new UserData("carlos", "trustno1", "carlos@fbi.gov"));
        sqlUserDAO.createUser(new UserData("smith", "trustno1", "smith@fbi.gov"));

        TestDAO.clearDB();

        Assertions.assertNull(sqlUserDAO.getUser("carlos"));

    }

    @Test
    public void checkUserDataSQLPositive() throws SQLException {
        sqlUserDAO.createUser(new UserData("don", "trustno1", "don@fbi.gov"));
        sqlUserDAO.createUser(new UserData("carlos", "trustno1", "carlos@fbi.gov"));
        sqlUserDAO.createUser(new UserData("smith", "trustno1", "smith@fbi.gov"));

        Assertions.assertEquals("carlos@fbi.gov", sqlUserDAO.checkUserData("carlos", "trustno1").getEmail());

    }

    @Test
    public void checkUserDataSQLNegative() throws SQLException {
        sqlUserDAO.createUser(new UserData("don", "trustno1", "don@fbi.gov"));
        sqlUserDAO.createUser(new UserData("carlos", "trustno1", "carlos@fbi.gov"));
        sqlUserDAO.createUser(new UserData("smith", "trustno1", "smith@fbi.gov"));

        TestDAO.clearDB();

        Assertions.assertNull(sqlUserDAO.checkUserData("carlos", "trustno1"));

    }

    //Verifies that a new user was successfully created.
    @Test
    public void createUserSQLPositive() throws SQLException {
        sqlUserDAO.createUser(new UserData("john paul jones", "trustno1", "fmulder@fbi.gov"));

        Assertions.assertEquals("fmulder@fbi.gov", sqlUserDAO.getUser("john paul jones").getEmail());

    }

    @Test
    //Fix this
    public void createUserSQLNegative() throws SQLException {

        sqlUserDAO.createUser(new UserData("fmulder", "Trustno1", "fmulder@fbi.gov"));

        Exception thrownException = assertThrows(RuntimeException.class, () -> sqlUserDAO.createUser(new UserData("fmulder", "Trustno1", "fmulder@fbi.gov")));

        String expectedMessage = "Error: User already exists";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    //Verifies that a new user was successfully created.
    @Test
    public void createAuthSQLPositive() throws SQLException, DataAccessException {

        sqlUserDAO.createUser(new UserData("john paul jones", "password", "jp.j@gmail.com"));
        AuthData authData = sqlAuthDAO.createAuth("john paul jones");

        Assertions.assertEquals(authData.getUsername(), sqlAuthDAO.getAuth(authData.getAuthToken()).getUsername());

    }

    //Try to create auth data without registering a user
    @Test
    public void createAuthSQLNegative() throws SQLException, DataAccessException {

        Assertions.assertNull(sqlAuthDAO.createAuth("Not created"));

    }

    @Test
    public void getAuthSQLPositive() throws SQLException, DataAccessException {
        sqlUserDAO.createUser(new UserData("don", "trustno1", "don@fbi.gov"));
        sqlUserDAO.createUser(new UserData("carlos", "trustno1", "carlos@fbi.gov"));
        sqlUserDAO.createUser(new UserData("smith", "trustno1", "smith@fbi.gov"));

        sqlAuthDAO.createAuth("don");
        AuthData authData2 = sqlAuthDAO.createAuth("carlos");
        sqlAuthDAO.createAuth("smith");

        Assertions.assertEquals(authData2.getUsername(), sqlAuthDAO.getAuth(authData2.getAuthToken()).getUsername());

    }

    @Test
    public void getAuthSQLNegative() throws SQLException{

        Assertions.assertNull(sqlAuthDAO.getAuth("doesn't exist"));

    }

    //Assert that the record was successfully deleted
    @Test
    public void deleteAuthSQLPositive() throws SQLException, DataAccessException {

        sqlUserDAO.createUser(new UserData("grant", "password", "grant@fbi.gov"));

        AuthData authData = sqlAuthDAO.createAuth("grant");

        Assertions.assertEquals(authData.getUsername(), sqlAuthDAO.getAuth(authData.getAuthToken()).getUsername());

        sqlAuthDAO.deleteAuth(authData.getAuthToken());

        Assertions.assertNull(sqlAuthDAO.getAuth(authData.getAuthToken()));

    }

    @Test
    public void deleteAuthSQLNegative() throws SQLException, DataAccessException {

        Assertions.assertFalse(sqlAuthDAO.deleteAuth("doesnt exist"));

    }

    @Test
    public void createGameSQLPositive() throws SQLException{

        ChessGame game = new ChessGame();

        GameData gameData = sqlGameDAO.createGame(game, "scrappers", "rogue faction", "warscrap.io");

        Assertions.assertEquals(gameData.getGameName(), sqlGameDAO.getGame(gameData.getGameID()).getGameName());

    }

    @Test
    //Change this to be a better test
    public void createGameSQLNegative() throws SQLException{

        ChessGame game = new ChessGame();

        GameData gameData = sqlGameDAO.createGame(game, "BYU", "utah", "the holy war");

        Assertions.assertEquals(gameData.getGameName(), sqlGameDAO.getGame(gameData.getGameID()).getGameName());

    }

    @Test
    public void getGameSQLPositive() throws SQLException{

        ChessGame game = new ChessGame();

        GameData gameData1 = sqlGameDAO.createGame(game, "team1", "team2", "fbi");
        GameData gameData2 = sqlGameDAO.createGame(game, "washington", "washington state", "apple cup");
        GameData gameData3 = sqlGameDAO.createGame(game, "scrappers", "rogue faction", "warscrap.io");

        Assertions.assertEquals(gameData1.getGameName(), sqlGameDAO.getGame(gameData1.getGameID()).getGameName());
        Assertions.assertEquals(gameData2.getGameName(), sqlGameDAO.getGame(gameData2.getGameID()).getGameName());
        Assertions.assertEquals(gameData3.getGameName(), sqlGameDAO.getGame(gameData3.getGameID()).getGameName());

    }

    @Test
    public void getGameSQLNegative() throws SQLException{

        ChessGame game = new ChessGame();

        sqlGameDAO.createGame(game, "team1", "team2", "fbi");
        sqlGameDAO.createGame(game, "washington", "washington state", "apple cup");
        sqlGameDAO.createGame(game, "scrappers", "rogue faction", "warscrap.io");

        Assertions.assertNull(sqlGameDAO.getGame(-1));

    }

    @Test
    public void updateGameSQLPositive() throws SQLException, InvalidMoveException {

        ChessGame changedGame = new ChessGame();

        GameData gameData = sqlGameDAO.createGame(changedGame, "team1", "team2", "fbi");

        changedGame.makeMove(new ChessMove(new ChessPosition(2, 3), new ChessPosition(3, 3), null));
        gameData.setGame(changedGame);
        String changedBoard = changedGame.getBoard().toString();

        sqlGameDAO.updateGame(gameData);

        GameData changedGameData = sqlGameDAO.getGame(gameData.getGameID());

        Assertions.assertEquals(changedBoard, changedGameData.getGame().getBoard().toString());

    }

    @Test
    public void updateGameSQLNegative() throws SQLException{

        Assertions.assertNull(sqlGameDAO.updateGame(new GameData(-1, "test", "test", "test", new ChessGame())));

    }

    @Test
    public void listGamesSQLPositive() throws SQLException{

        sqlGameDAO.createGame(new ChessGame(), "scrappers", "rogue faction", "warscrap.io");
        sqlGameDAO.createGame(new ChessGame(), "BYU", "utah", "the holy war");
        sqlGameDAO.createGame(new ChessGame(), "fbi", "colonists", "The X-Files");

        GameData[] gameData = sqlGameDAO.listGames();

        Assertions.assertEquals(3, gameData.length);

    }

    @Test
    public void listGamesSQLNegative() throws SQLException{

        GameData[] gameData = sqlGameDAO.listGames();

        Assertions.assertEquals(0, gameData.length);

    }

    @Test
    public void clearDB() throws SQLException{

        Assertions.assertTrue(TestDAO.clearDB());
    }

    @Test
    @AfterEach
    public void clearDBAfterEach() throws SQLException{

        Assertions.assertTrue(TestDAO.clearDB());
    }
}