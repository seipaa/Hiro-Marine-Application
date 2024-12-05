package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.Admin;

public class AdminDAO extends BaseDAO {
    public Admin getAdminByUsernameAndPassword(String username, String password) throws SQLException {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getString("username"),
                        rs.getString("password")
                    );
                }
            }
        }
        return null;
    }
} 