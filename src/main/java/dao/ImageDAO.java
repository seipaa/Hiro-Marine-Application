package dao;

import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.util.UUID;

public class ImageDAO extends BaseDAO {
    private static final Logger LOGGER = Logger.getLogger(ImageDAO.class.getName());
    private static final String UPLOAD_DIRECTORY = "src/main/resources/images/uploads/";

    public ImageDAO() {
        // Buat direktori upload jika belum ada
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    public String saveImage(File imageFile, int speciesId) {
        String fileName = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Generate unique filename
            String originalFileName = imageFile.getName();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            fileName = UUID.randomUUID().toString() + extension;

            // Copy file ke direktori upload
            File destinationFile = new File(UPLOAD_DIRECTORY + fileName);
            Files.copy(imageFile.toPath(), destinationFile.toPath());

            // Simpan informasi file ke database
            conn = getConnection();
            String sql = "INSERT INTO images (species_id, image_name, image_date, created_at) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, speciesId);
            stmt.setString(2, fileName);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();

            LOGGER.info("Successfully saved image: " + fileName);
            return fileName;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving image", e);
            return null;
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    public List<String> getImages() {
        List<String> images = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT image_name FROM images ORDER BY created_at DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                images.add(rs.getString("image_name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting images", e);
        } finally {
            closeResources(rs, stmt, conn);
        }

        return images;
    }

    public List<String> getImagesBySpeciesId(int speciesId) {
        List<String> images = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT image_name FROM images WHERE species_id = ? ORDER BY created_at DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, speciesId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                images.add(rs.getString("image_name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting images for species: " + speciesId, e);
        } finally {
            closeResources(rs, stmt, conn);
        }

        return images;
    }

    public boolean deleteImage(String imageName) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Hapus file fisik
            File file = new File(UPLOAD_DIRECTORY + imageName);
            if (file.exists()) {
                file.delete();
            }

            // Hapus record dari database
            conn = getConnection();
            String sql = "DELETE FROM images WHERE image_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, imageName);
            stmt.executeUpdate();

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting image", e);
            return false;
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    public String getImageName(int imageId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT image_name FROM images WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, imageId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("image_name");
            }
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting image name", e);
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources", e);
        }
    }
}
