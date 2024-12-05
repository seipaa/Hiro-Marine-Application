package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.News;

public class NewsDAO extends BaseDAO {
    private static final Logger LOGGER = Logger.getLogger(NewsDAO.class.getName());

    public News getNewsById(int id) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        News news = null;
        try {
            con = getConnection();
            String query = "SELECT * FROM news WHERE id = ?";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                news = new News(rs.getInt("id"), rs.getInt("adminId"), rs.getString("title"), rs.getString("description"), rs.getString("imageUrl"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return news;
    }

    public News getNewsByTitle(String title) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        News news = null;
        try {
            con = getConnection();
            if (con != null) {
                System.out.println("Database connection established.");
            } else {
                System.out.println("Failed to establish database connection.");
            }
            String query = "SELECT * FROM news WHERE title LIKE ?";
            stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + title.trim() + "%");
            System.out.println("Executing query: " + stmt.toString());
            rs = stmt.executeQuery();
            if (rs.next()) {
                news = new News(rs.getInt("id"), rs.getInt("admin_id"), rs.getString("title"), rs.getString("description"), rs.getString("image_url"));
                System.out.println("News found: " + news.getTitle());
            } else {
                System.out.println("No news found with title: " + title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return news;
    }

    // Metode untuk menambahkan berita
    public void addNews(News news) {
        String query = "INSERT INTO news (adminId, title, description, imageUrl) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, news.getAdminId());
            stmt.setString(2, news.getTitle());
            stmt.setString(3, news.getDescription());
            stmt.setString(4, news.getImageUrl());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metode untuk mendapatkan semua berita
    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<>();
        String query = "SELECT * FROM news ORDER BY id DESC";
        
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                News news = new News(
                    rs.getInt("id"),
                    rs.getInt("admin_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("image_url")
                );
                newsList.add(news);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all news: " + e.getMessage(), e);
        }
        return newsList;
    }
}
