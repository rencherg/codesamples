package passoffTests.ServiceTests;

//Non HTTP Service tests go here
import dataAccess.DataAccessException;
import dataAccess.TestDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.DeleteService;
import service.GameService;
import service.UserService;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    private final DeleteService deleteService = new DeleteService();
    private final GameService gameService = new GameService();
    private final UserService userService = new UserService();

    //Only 1 test for delete endpoint
    @Test
    @Order(1)
    @DisplayName("Delete positive")
    public void deleteTest() throws SQLException {
        Assertions.assertTrue(this.deleteService.clear());
    }

    @Test
    @Order(2)
    @DisplayName("register positive")
    public void registerTestPositive() throws SQLException, DataAccessException {
        Assertions.assertNotNull(this.userService.register(new UserData("cougarboy123", "password", "rencher.grant@gmail.com")));
    }

    @Test
    @Order(3)
    @DisplayName("register negative")
    public void registerTestNegative() {
        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            //Invalid information passed
            this.userService.register(new UserData("", "", ""));
        });

        String expectedMessage = "Error: bad request";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("login positive")
    public void loginTestPositive() throws SQLException, DataAccessException {
        AuthData authData = this.userService.register(new UserData("fmulder", "trustno1", "f.mulder@fbi.gov"));
        this.userService.logout(authData.getAuthToken());
        Assertions.assertNotNull(this.userService.login("fmulder", "trustno1"));
    }

    @Test
    @Order(5)
    @DisplayName("login negative")
    public void loginTestNegative() {

        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            this.userService.login("never used", "neverused");
        });

        String expectedMessage = "Error: unauthorized";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("logout positive")
    public void logoutTestPositive() throws SQLException, DataAccessException {
        AuthData authData = this.userService.register(new UserData("wskinner", "trustno1", "f.mulder@fbi.gov"));
        Assertions.assertTrue(this.userService.logout(authData.getAuthToken()));
    }

    @Test
    @Order(7)
    @DisplayName("logout negative")
    public void logoutTestNegative() throws SQLException, DataAccessException {
        this.userService.register(new UserData("dreadpirateroberts", "trustno1", "f.mulder@fbi.gov"));

        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            Assertions.assertFalse(this.userService.logout("invalid token"));
        });

        String expectedMessage = "Error: unauthorized";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());

    }

    @Test
    @Order(8)
    @DisplayName("createGame positive")
    public void createGamePositive() throws SQLException, DataAccessException {
        AuthData authData = this.userService.register(new UserData("johnpauljones", "trustno1", "f.mulder@fbi.gov"));
        int id = this.gameService.createGame(authData.getAuthToken(), "my game");
        Assertions.assertNotNull(id);
    }

    @Test
    @Order(9)
    @DisplayName("createGame negative")
    public void createGameNegative() {
        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            this.gameService.createGame("invalid token", "my game");
        });

        String expectedMessage = "Error: unauthorized";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    @Order(10)
    @DisplayName("joinGame positive")
    public void joinGamePositive() throws SQLException, DataAccessException {
        AuthData authData = this.userService.register(new UserData("akrycek", "trustno1", "f.mulder@fbi.gov"));
        int id = this.gameService.createGame(authData.getAuthToken(), "my game");
        Assertions.assertNotNull(this.gameService.joinGame(authData.getAuthToken(), "WHITE", id));
    }

    @Test
    @Order(11)
    @DisplayName("joinGame negative")
    public void joinGameNegative() throws SQLException, DataAccessException {
        AuthData authData1 = this.userService.register(new UserData("mlg", "trustno1", "f.mulder@fbi.gov"));
        AuthData authData2 = this.userService.register(new UserData("grantacus_", "password", "rencher.grant@gmail.com"));
        int id = this.gameService.createGame(authData1.getAuthToken(), "my game");
        this.gameService.joinGame(authData1.getAuthToken(), "WHITE", id);

        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            this.gameService.joinGame(authData2.getAuthToken(), "WHITE", id);
        });

        String expectedMessage = "Error: already taken";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("getGame positive")
    public void getGamePositive() throws SQLException, DataAccessException {
        AuthData authData = this.userService.register(new UserData("byu", "trustno1", "f.mulder@fbi.gov"));
        this.gameService.createGame(authData.getAuthToken(), "game1");
        this.gameService.createGame(authData.getAuthToken(), "game2");
        this.gameService.createGame(authData.getAuthToken(), "game3");
        GameData[] gameData = this.gameService.getGame(authData.getAuthToken());
        Assertions.assertEquals(3, gameData.length);
    }

    @Test
    @Order(13)
    @DisplayName("getGame negative")
    public void getGameNegative(){
        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            Assertions.assertNull(this.gameService.getGame("invalid token"));
        });

        String expectedMessage = "Error: unauthorized";
        Assertions.assertEquals(expectedMessage, thrownException.getMessage());
    }

    @AfterEach
    public void clearDb() throws SQLException {
        TestDAO.clearDB();
    }
}
