package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseDAO {

    // Konfigurasi database
    private static final String DB_NAME = "hiros_marine";
    private static final String DB_HOST = "34.44.81.201";
    private static final String DB_USER = "hiro";
    private static final String DB_PASS = "hiro";

    private static final Logger LOGGER = Logger.getLogger(BaseDAO.class.getName());

    // Proses mendapatkan koneksi ke database
    protected Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME + "?useUnicode=true&characterEncoding=UTF-8";
        String user = DB_USER;
        String password = DB_PASS;
        // Mengembalikan objek Connection
        return DriverManager.getConnection(url, user, password);
    }

    // Proses menutup koneksi database
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
