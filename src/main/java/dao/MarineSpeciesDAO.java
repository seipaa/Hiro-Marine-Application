package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.MarineSpecies;
import utils.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarineSpeciesDAO extends BaseDAO {
    // ...

    public ObservableList<MarineSpecies> getAllMarineSpecies() {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT * FROM species ORDER BY name";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("latin_name"),
                        rs.getString("description"),
                        rs.getString("image_url"),
                        rs.getString("type")
                );
                speciesList.add(species);
                System.out.println("Loaded species: " + species.getName()); // Debug log
            }
        } catch (SQLException e) {
            System.err.println("Error fetching species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public void addMarineSpecies(MarineSpecies species) {
        String query = "INSERT INTO species (name, latin_name, description, image_url, type, status) VALUES (?, ?, ?, ?, ?, 'pending')";

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
        String query = "UPDATE species SET name=?, latin_name=?, description=?, image_url=?, type=? WHERE id=?";

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

    public void addPendingSpecies(MarineSpecies species) throws SQLException {
        String query = "INSERT INTO species (image_url, name, latin_name, description, type, status) " +
                "VALUES (?, ?, ?, ?, ?, 'pending')";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            String imageUrl = species.getImageUrl();
            if (!imageUrl.startsWith("/images/")) {
                imageUrl = "/images/" + imageUrl;
            }
            stmt.setString(1, imageUrl);
            stmt.setString(2, species.getName());
            stmt.setString(3, species.getLatinName());
            stmt.setString(4, species.getDescription());
            stmt.setString(5, species.getType());

            int result = stmt.executeUpdate();
            if (result > 0) {
                System.out.println("Species added successfully: " + species.getName());
            } else {
                throw new SQLException("Failed to add species");
            }
        } catch (SQLException e) {
            System.err.println("Error adding species: " + e.getMessage());
            e.printStackTrace();
            throw e;
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
        String query = "SELECT * FROM species WHERE status = 'approved' ORDER BY name";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
                System.out.println("Loaded approved species: " + species.getName());
            }
        } catch (SQLException e) {
            System.err.println("Error loading approved species: " + e.getMessage());
            e.printStackTrace();
        }
        return speciesList;
    }

    public ObservableList<MarineSpecies> getAllPendingSpecies() {
        ObservableList<MarineSpecies> speciesList = FXCollections.observableArrayList();
        String query = "SELECT * FROM species WHERE status = 'pending' ORDER BY name";

        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

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
        String query = "UPDATE species SET image_url = ?, name = ?, latin_name = ?, " +
                "description = ?, type = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            String imageUrl = species.getImageUrl();
            if (!imageUrl.startsWith("/images/")) {
                imageUrl = "/images/" + imageUrl;
            }
            stmt.setString(1, imageUrl);
            stmt.setString(2, species.getName());
            stmt.setString(3, species.getLatinName());
            stmt.setString(4, species.getDescription());
            stmt.setString(5, species.getType());
            stmt.setInt(6, species.getId());

            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to update species");
            }
        } catch (SQLException e) {
            System.err.println("Error updating species: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void addSpecies(MarineSpecies newSpecies) throws SQLException {
        String query = "INSERT INTO species (image_url, name, latin_name, description, type, status) " +
                "VALUES (?, ?, ?, ?, ?, 'pending')";
        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            String imageUrl = newSpecies.getImageUrl();
            if (!imageUrl.startsWith("/images/")) {
                imageUrl = "/images/" + imageUrl;
            }

            stmt.setString(1, imageUrl);
            stmt.setString(2, newSpecies.getName());
            stmt.setString(3, newSpecies.getLatinName());
            stmt.setString(4, newSpecies.getDescription());
            stmt.setString(5, newSpecies.getType());

            stmt.executeUpdate();
        }
    }

    public void deleteSpecies(int id) throws SQLException {
        String query = "DELETE FROM species WHERE id = ?";
        try (Connection con = DatabaseHelper.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
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

    // ...
}