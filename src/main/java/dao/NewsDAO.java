package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.News;

public class NewsDAO extends BaseDAO {
    private static final Logger LOGGER = Logger.getLogger(NewsDAO.class.getName());

    private static final int CACHE_SIZE = 20;
    private List<News> cachedNews = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 10000; // 10 detik

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection con) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources", e);
        }
    }

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
                news = createNewsFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting news by ID", e);
        } finally {
            closeResources(rs, stmt, con);
        }
        return news;
    }

    public List<News> getAllNews() {
        return getAllNews(null);
    }

    public List<News> getAllNews(String searchQuery) {
        // Jika ada query pencarian, bypass cache
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            return getAllNewsFromDB(searchQuery);
        }

        // Gunakan cache jika masih valid
        long currentTime = System.currentTimeMillis();
        if (!cachedNews.isEmpty() && (currentTime - lastCacheUpdate) < CACHE_DURATION) {
            return new ArrayList<>(cachedNews);
        }

        // Update cache
        cachedNews = getAllNewsFromDB(null);
        lastCacheUpdate = currentTime;
        return new ArrayList<>(cachedNews);
    }

    private List<News> getAllNewsFromDB(String searchQuery) {
        List<News> newsList = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            String query;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                query = "SELECT * FROM news WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ? ORDER BY created_at DESC";
                stmt = con.prepareStatement(query);
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                System.out.println("Executing search query: " + query + " with pattern: " + searchPattern);
            } else {
                query = "SELECT * FROM news ORDER BY created_at DESC";
                stmt = con.prepareStatement(query);
                System.out.println("Executing query: " + query);
            }
            
            rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                News news = createNewsFromResultSet(rs);
                newsList.add(news);
                count++;
                System.out.println("Loaded news: " + news.getTitle() + " (Breaking: " + news.isBreakingNews() + ")");
            }
            System.out.println("Total news loaded from database: " + count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error mendapatkan daftar berita", e);
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, con);
        }
        return newsList;
    }

    public List<News> getBreakingNews() {
        List<News> breakingNews = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            // Ubah query untuk mengambil breaking news terbaru
            String query = "SELECT * FROM news WHERE is_breaking_news = true ORDER BY created_at DESC";
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                News news = createNewsFromResultSet(rs);
                breakingNews.add(news);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting breaking news", e);
        } finally {
            closeResources(rs, stmt, con);
        }
        return breakingNews;
    }

    public boolean addNews(News news) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            // Jika ini adalah berita utama, perbarui posisi berita utama yang ada
            if (news.isBreakingNews()) {
                String updatePosisi = "UPDATE news SET position = position + 1 WHERE is_breaking_news = true";
                stmt = con.prepareStatement(updatePosisi);
                stmt.executeUpdate();
                stmt.close();
            }

            // Tambahkan berita baru
            String query = "INSERT INTO news (admin_id, title, description, image_url, created_at, is_breaking_news, position) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, news.getAdminId());
            stmt.setString(2, news.getTitle());
            stmt.setString(3, news.getDescription());
            stmt.setString(4, news.getImageUrl());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(6, news.isBreakingNews());
            stmt.setInt(7, news.isBreakingNews() ? 1 : ambilPosisiBerikutnya());

            int hasil = stmt.executeUpdate();
            if (hasil > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    news.setId(rs.getInt(1));
                }
            }

            con.commit();
            return hasil > 0;
        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error saat rollback transaksi", ex);
            }
            LOGGER.log(Level.SEVERE, "Error saat menambah berita", e);
            return false;
        } finally {
            closeResources(rs, stmt, con);
        }
    }

    private int ambilPosisiBerikutnya() {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            String query = "SELECT COALESCE(MAX(position), 0) + 1 FROM news WHERE is_breaking_news = false";
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saat mengambil posisi berikutnya", e);
            return 1;
        } finally {
            closeResources(rs, stmt, con);
        }
    }

    public boolean updateNews(News news) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            String query = "UPDATE news SET title = ?, description = ?, image_url = ?, is_breaking_news = ?, position = ? WHERE id = ?";
            stmt = con.prepareStatement(query);
            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getDescription());
            stmt.setString(3, news.getImageUrl());
            stmt.setBoolean(4, news.isBreakingNews());
            stmt.setInt(5, news.getPosition());
            stmt.setInt(6, news.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating news", e);
            return false;
        } finally {
            closeResources(null, stmt, con);
        }
    }

    public boolean deleteNews(int id) {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            String query = "DELETE FROM news WHERE id = ?";
            stmt = con.prepareStatement(query);
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting news", e);
            return false;
        } finally {
            closeResources(null, stmt, con);
        }
    }

    public List<News> searchNews(String keyword) {
        List<News> searchResults = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            String query = "SELECT * FROM news WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ? ORDER BY created_at DESC";
            stmt = con.prepareStatement(query);
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            rs = stmt.executeQuery();
            while (rs.next()) {
                searchResults.add(createNewsFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching news", e);
        } finally {
            closeResources(rs, stmt, con);
        }
        return searchResults;
    }

    public void updateBreakingNewsStatus() {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();

            // 1. Dapatkan breaking news terbaru
            String getLatestBreakingNews = "SELECT id FROM news WHERE is_breaking_news = true ORDER BY created_at DESC LIMIT 1";
            stmt = con.prepareStatement(getLatestBreakingNews);
            rs = stmt.executeQuery();

            Integer latestBreakingNewsId = null;
            if (rs.next()) {
                latestBreakingNewsId = rs.getInt("id");
            }

            // 2. Update semua breaking news lama menjadi regular news
            if (latestBreakingNewsId != null) {
                String updateOldBreakingNews = "UPDATE news SET is_breaking_news = false WHERE id != ? AND is_breaking_news = true";
                stmt = con.prepareStatement(updateOldBreakingNews);
                stmt.setInt(1, latestBreakingNewsId);
                stmt.executeUpdate();
            }

            LOGGER.info("Breaking news status updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating breaking news status", e);
        } finally {
            closeResources(rs, stmt, con);
        }
    }

    public News getNewsByTitle(String title) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        News news = null;
        try {
            con = getConnection();
            String query = "SELECT * FROM news WHERE title = ?";
            stmt = con.prepareStatement(query);
            stmt.setString(1, title);
            rs = stmt.executeQuery();
            if (rs.next()) {
                news = createNewsFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting news by title", e);
        } finally {
            closeResources(rs, stmt, con);
        }
        return news;
    }

    private News createNewsFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int adminId = rs.getInt("admin_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String imageUrl = rs.getString("image_url");
        Timestamp createdAt = rs.getTimestamp("created_at");
        boolean isBreakingNews = rs.getBoolean("is_breaking_news");
        int position = rs.getInt("position");
        
        System.out.println("Creating news: ID=" + id + ", Title=" + title + ", ImageUrl=" + imageUrl + ", IsBreaking=" + isBreakingNews);
        
        News news = new News(id, adminId, title, description, imageUrl, createdAt, isBreakingNews);
        news.setPosition(position);
        return news;
    }
}
