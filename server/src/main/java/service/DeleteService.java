package service;

import dataAccess.TestDAO;

import java.sql.SQLException;

//FOR TESTING ONLY
public class DeleteService {

    private final TestDAO testDAO = new TestDAO();

    public boolean clear() throws SQLException {
        return this.testDAO.clearDB();
    }
}
