package dao;

import utils.DatabaseHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseDAO {

    private static final String URL = "jdbc:mysql://34.44.81.201:3306/hiros_marine";
    private static final String USER = "hiro";
    private static final String PASSWORD = "hiro";

    private static final Logger LOGGER = Logger.getLogger(BaseDAO.class.getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseHelper.getConnection();
    }

    protected void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                // Log kesalahan kalo koneksinya gagal buat ditutup
                LOGGER.log(Level.SEVERE, "Error closing connection: " + e.getMessage(), e);
            }
        }
    }
}
