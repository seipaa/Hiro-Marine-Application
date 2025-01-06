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
                MarineSpecies species = new MarineSpecies(
                    rs.getInt("id"),
                    "",  // imageUrl tidak diperlukan lagi
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

    public byte[] getSpeciesImage(int speciesId) throws SQLException {
        String query = "SELECT image_data FROM images WHERE species_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, speciesId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("image_data");
                }
            }
        }
        return null;
    }

    public void updateSpecies(MarineSpecies species, byte[] imageData) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update species data
            String updateSpecies = "UPDATE species SET name=?, latin_name=?, description=?, type=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSpecies)) {
                stmt.setString(1, species.getName());
                stmt.setString(2, species.getLatinName());
                stmt.setString(3, species.getDescription());
                stmt.setString(4, species.getType());
                stmt.setInt(5, species.getId());
                stmt.executeUpdate();
            }

            // Update image if provided
            if (imageData != null) {
                String updateImage = "UPDATE images SET image_data=? WHERE species_id=?";
                try (PreparedStatement stmt = conn.prepareStatement(updateImage)) {
                    stmt.setBytes(1, imageData);
                    stmt.setInt(2, species.getId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
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

    public void deleteSpecies(int id) throws SQLException {
        String query = "DELETE FROM species WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public ObservableList<MarineSpecies> searchSpecies(String searchText) {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT s.*, i.image_data FROM species s " +
                      "LEFT JOIN images i ON s.id = i.species_id " +
                      "WHERE LOWER(s.name) LIKE LOWER(?) OR LOWER(s.latin_name) LIKE LOWER(?) " +
                      "ORDER BY s.name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
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
            System.err.println("Error searching species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public byte[] loadSpeciesImage(int speciesId) {
        String query = "SELECT image_data FROM images WHERE species_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, speciesId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("image_data");
            }
        } catch (SQLException e) {
            System.err.println("Error loading species image: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void addSpecies(MarineSpecies species, byte[] imageData) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert species
            String insertSpecies = "INSERT INTO species (name, latin_name, description, type) VALUES (?, ?, ?, ?)";
            int speciesId;
            try (PreparedStatement stmt = conn.prepareStatement(insertSpecies, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, species.getName());
                stmt.setString(2, species.getLatinName());
                stmt.setString(3, species.getDescription());
                stmt.setString(4, species.getType());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    speciesId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated species ID");
                }
            }

            // Insert image if provided
            if (imageData != null) {
                String insertImage = "INSERT INTO images (species_id, image_data) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertImage)) {
                    stmt.setInt(1, speciesId);
                    stmt.setBytes(2, imageData);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
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
}