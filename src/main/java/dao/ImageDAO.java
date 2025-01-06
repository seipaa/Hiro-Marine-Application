package dao;

import utils.DatabaseConnection;
import java.sql.*;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageDAO {

    public static ArrayList<byte[]> getImages() {
        ArrayList<byte[]> result = new ArrayList<>();
        String sql = "SELECT image_data FROM images";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Blob blob = rs.getBlob("image_data");
                result.add(blob.getBytes(1, (int) blob.length()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    // Menyimpan gambar ke database
    public void saveImage(int speciesId, String imageName, InputStream imageData) throws SQLException {
        String sql = "INSERT INTO images (species_id, image_name, image_data) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, speciesId);
            stmt.setString(2, imageName);      // VARCHAR(255) - untuk nama file asli
            stmt.setBlob(3, imageData);        // LONGBLOB - untuk data binary gambar
            
            stmt.executeUpdate();
        }
    }
    
    // Mengambil data gambar dari database
    public byte[] getImageData(int speciesId) throws SQLException {
        String sql = "SELECT image_data FROM images WHERE species_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, speciesId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Blob blob = rs.getBlob("image_data");
                return blob.getBytes(1, (int) blob.length());
            }
        }
        return null;
    }
    
    // Mengambil nama gambar
    public String getImageName(int speciesId) throws SQLException {
        String sql = "SELECT image_name FROM images WHERE species_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, speciesId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("image_name");
            }
        }
        return null;
    }
}
