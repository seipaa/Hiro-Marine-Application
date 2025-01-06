package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.MarineSpecies;
import utils.DatabaseConnection;
import utils.DatabaseHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MarineSpeciesDAO extends BaseDAO {
    private ImageDAO imageDAO = new ImageDAO();

    public ObservableList<MarineSpecies> getAllMarineSpecies() {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT s.*, i.image_data, i.image_name FROM species s " +
                      "LEFT JOIN images i ON s.id = i.species_id " +
                      "ORDER BY s.name";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                byte[] imageData = rs.getBytes("image_data");
                String imageName = rs.getString("image_name");
                
                // Buat URL gambar yang sesuai jika ada data gambar
                String imageUrl;
                if (imageData != null && imageData.length > 0) {
                    // Simpan gambar ke file temporary dan gunakan path-nya
                    imageUrl = saveImageToTemp(imageData, imageName);
                } else {
                    // Jika tidak ada gambar, gunakan string kosong
                    imageUrl = "";
                }

                MarineSpecies species = new MarineSpecies(
                    rs.getInt("id"),
                    imageUrl,
                    rs.getString("name"),
                    rs.getString("latin_name"),
                    rs.getString("description"),
                    rs.getString("type")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    private String saveImageToTemp(byte[] imageData, String imageName) {
        try {
            // Buat direktori temporary jika belum ada
            Path tempDir = Paths.get("src/main/resources/images/temp");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // Simpan gambar ke file temporary
            String fileName = "species_" + System.currentTimeMillis() + "_" + imageName;
            Path tempFile = tempDir.resolve(fileName);
            Files.write(tempFile, imageData);

            return "/images/temp/" + fileName;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return "/images/default_species.jpg";
        }
    }

    public void addMarineSpecies(MarineSpecies species) {
        String query = "INSERT INTO species (name, latin_name, description, type, status) VALUES (?, ?, ?, ?, ?, 'pending')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, species.getName());
            stmt.setString(2, species.getLatinName());
            stmt.setString(3, species.getDescription());
            stmt.setString(4, species.getImageUrl());
            stmt.setString(5, species.getType());

            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to insert species");
            }

        } catch (SQLException e) {
            System.err.println("Error adding species: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add species", e);
        }
    }

    public void updateMarineSpecies(MarineSpecies species) {
        String query = "UPDATE species SET name=?, latin_name=?, description=?, type=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, species.getName());
            stmt.setString(2, species.getLatinName());
            stmt.setString(3, species.getDescription());
            stmt.setString(4, species.getImageUrl());
            stmt.setString(5, species.getType());
            stmt.setInt(6, species.getId());

            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to update species");
            }

        } catch (SQLException e) {
            System.err.println("Error updating species: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update species", e);
        }
    }

    public void deleteMarineSpecies(int id) {
        String query = "DELETE FROM species WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to delete species");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting species: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete species", e);
        }
    }

    public int addPendingSpecies(MarineSpecies species) throws SQLException {
        String sql = "INSERT INTO species (name, latin_name, description, type, status) VALUES (?, ?, ?, ?, 'pending')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, species.getName());
            stmt.setString(2, species.getLatinName());
            stmt.setString(3, species.getDescription());
            stmt.setString(4, species.getType());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Gagal mendapatkan ID species yang baru dibuat");
        }
    }

    public void approveSpecies(int id) {
        String query = "UPDATE species SET status = 'approved' WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Species with ID " + id + " has been approved");
            } else {
                throw new SQLException("No species found with ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error approving species: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to approve species", e);
        }
    }

    public ObservableList<MarineSpecies> getAllApprovedSpecies() {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT s.id, s.name, s.latin_name, s.description, s.type " +
                      "FROM species s " +
                      "WHERE s.status = 'approved' " +
                      "ORDER BY s.name";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                    rs.getInt("id"),
                    "",
                    rs.getString("name"),
                    rs.getString("latin_name"),
                    rs.getString("description"),
                    rs.getString("type")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            System.err.println("Error loading approved species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public ObservableList<MarineSpecies> getAllPendingSpecies() {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT s.*, i.image_name FROM species s " +
                      "LEFT JOIN images i ON s.id = i.species_id " +
                      "WHERE s.status = 'pending' " +
                      "ORDER BY s.name";

        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                    rs.getInt("id"),
                    rs.getString("image_name"),
                    rs.getString("name"),
                    rs.getString("latin_name"),
                    rs.getString("description"),
                    rs.getString("type")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    private ObservableList<MarineSpecies> getSpeciesByStatus(String status) {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT * FROM species WHERE status = ? ORDER BY name";

        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                        rs.getInt("id"),
                        rs.getString("image_url"),
                        rs.getString("name"),
                        rs.getString("latin_name"),
                        rs.getString("description"),
                        rs.getString("type")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public void updateSpecies(MarineSpecies species) throws SQLException {
        updateSpecies(species, null);
    }

    public void updateSpecies(MarineSpecies species, byte[] imageData) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update species data
            String speciesQuery = "UPDATE species SET name=?, latin_name=?, description=?, type=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(speciesQuery)) {
                stmt.setString(1, species.getName());
                stmt.setString(2, species.getLatinName());
                stmt.setString(3, species.getDescription());
                stmt.setString(4, species.getType());
                stmt.setInt(5, species.getId());
                stmt.executeUpdate();
            }

            // Update image jika ada
            if (imageData != null && imageData.length > 0) {
                String checkImageQuery = "SELECT COUNT(*) FROM images WHERE species_id = ?";
                boolean imageExists = false;
                
                try (PreparedStatement checkStmt = conn.prepareStatement(checkImageQuery)) {
                    checkStmt.setInt(1, species.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        imageExists = rs.getInt(1) > 0;
                    }
                }

                if (imageExists) {
                    // Update existing image
                    String updateImageQuery = "UPDATE images SET image_data=?, image_name=? WHERE species_id=?";
                    try (PreparedStatement imgStmt = conn.prepareStatement(updateImageQuery)) {
                        imgStmt.setBlob(1, new javax.sql.rowset.serial.SerialBlob(imageData));
                        String imageName = species.getImageUrl().substring(species.getImageUrl().lastIndexOf('/') + 1);
                        imgStmt.setString(2, imageName);
                        imgStmt.setInt(3, species.getId());
                        imgStmt.executeUpdate();
                    }
                } else {
                    // Insert new image
                    String insertImageQuery = "INSERT INTO images (species_id, image_name, image_data, created_at) VALUES (?, ?, ?, NOW())";
                    try (PreparedStatement imgStmt = conn.prepareStatement(insertImageQuery)) {
                        imgStmt.setInt(1, species.getId());
                        String imageName = species.getImageUrl().substring(species.getImageUrl().lastIndexOf('/') + 1);
                        imgStmt.setString(2, imageName);
                        imgStmt.setBlob(3, new javax.sql.rowset.serial.SerialBlob(imageData));
                        imgStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    public void addSpecies(MarineSpecies species) throws SQLException {
        addSpecies(species, null);
    }

    public void addSpecies(MarineSpecies species, byte[] imageData) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert species dengan status 'approved'
            String speciesQuery = "INSERT INTO species (name, latin_name, description, type, status) VALUES (?, ?, ?, ?, 'approved')";
            try (PreparedStatement stmt = conn.prepareStatement(speciesQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, species.getName());
                stmt.setString(2, species.getLatinName());
                stmt.setString(3, species.getDescription());
                stmt.setString(4, species.getType());
                
                stmt.executeUpdate();
                
                // Dapatkan ID species yang baru dibuat
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int speciesId = rs.getInt(1);
                    
                    // Insert image jika ada
                    if (imageData != null && imageData.length > 0) {
                        String imageQuery = "INSERT INTO images (species_id, image_name, image_data, created_at) VALUES (?, ?, ?, NOW())";
                        try (PreparedStatement imgStmt = conn.prepareStatement(imageQuery)) {
                            imgStmt.setInt(1, speciesId);
                            String imageName = species.getImageUrl().substring(species.getImageUrl().lastIndexOf('/') + 1);
                            imgStmt.setString(2, imageName);
                            imgStmt.setBlob(3, new javax.sql.rowset.serial.SerialBlob(imageData));
                            imgStmt.executeUpdate();
                        }
                    }
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    public void deleteSpecies(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // Hapus dari tabel images terlebih dahulu
            String deleteImageQuery = "DELETE FROM images WHERE species_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteImageQuery)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Kemudian hapus dari tabel species
            String deleteSpeciesQuery = "DELETE FROM species WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSpeciesQuery)) {
                stmt.setInt(1, id);
                int result = stmt.executeUpdate();
                
                if (result == 0) {
                    throw new SQLException("Species dengan ID " + id + " tidak ditemukan");
                }
            }

            // Commit transaksi jika berhasil
            conn.commit();

        } catch (SQLException e) {
            // Rollback jika terjadi error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Gagal menghapus species: " + e.getMessage());
        } finally {
            // Kembalikan autocommit ke true dan tutup koneksi
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void rejectSpecies(int id) throws SQLException {
        String query = "DELETE FROM species WHERE id = ? AND status = 'pending'";
        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public ObservableList<MarineSpecies> searchSpecies(String searchText) {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT * FROM species WHERE status = 'approved' AND " +
                "(LOWER(name) LIKE LOWER(?) OR LOWER(latin_name) LIKE LOWER(?)) " +
                "ORDER BY name";

        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                        rs.getInt("id"),
                        rs.getString("image_url"),
                        rs.getString("name"),
                        rs.getString("latin_name"),
                        rs.getString("description"),
                        rs.getString("type")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            System.err.println("Error searching species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public byte[] getSpeciesImage(int speciesId) throws SQLException {
        String query = "SELECT image_data FROM images WHERE species_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, speciesId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Blob blob = rs.getBlob("image_data");
                    if (blob != null) {
                        return blob.getBytes(1, (int) blob.length());
                    }
                }
            }
        }
        return null;
    }

    public byte[] loadSpeciesImage(int speciesId) {
        String query = "SELECT image_data FROM images WHERE species_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, speciesId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("image_data");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading species image: " + e.getMessage());
        }
        return null;
    }

    // ...
}