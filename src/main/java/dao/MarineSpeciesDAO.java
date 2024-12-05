package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.MarineSpecies;

public class MarineSpeciesDAO extends BaseDAO {
    private static final Logger LOGGER = Logger.getLogger(MarineSpeciesDAO.class.getName());

    public MarineSpecies getMarineSpeciesById(int id) {
        String query = "SELECT * FROM marine_species WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MarineSpecies(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("imageUrl")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching marine species by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<MarineSpecies> getAllMarineSpecies() {
        List<MarineSpecies> speciesList = new ArrayList<>();
        String query = "SELECT * FROM marine_species";
        
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                MarineSpecies species = new MarineSpecies(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("imageUrl")
                );
                speciesList.add(species);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all marine species: " + e.getMessage(), e);
        }
        return speciesList;
    }

    public void addMarineSpecies(MarineSpecies species) {
        String query = "INSERT INTO marine_species (name, description, image_url) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, species.getName());
            stmt.setString(2, species.getDescription());
            stmt.setString(3, species.getImageUrl());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMarineSpecies(MarineSpecies species) {
        String query = "UPDATE marine_species SET name = ?, description = ?, image_url = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, species.getName());
            stmt.setString(2, species.getDescription());
            stmt.setString(3, species.getImageUrl());
            stmt.setInt(4, species.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMarineSpecies(int id) {
        String query = "DELETE FROM marine_species WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
