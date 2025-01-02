package dao;

import utils.DatabaseConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.util.ArrayList;

public class ImageDAO {

    public void saveImage(String imagePath) {
        String sql = "INSERT INTO images (image_data) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             FileInputStream fis = new FileInputStream(imagePath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBinaryStream(1, fis, fis.available());
            pstmt.executeUpdate();
            System.out.println("Image inserted successfully!");

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }

    public static void insertEntry(byte[] imageData, File file) throws SQLException {
        Connection con = null;
        PreparedStatement st = null;

        try {
            con = DatabaseConnection.getConnection();
            String query = "INSERT INTO images (name, data) VALUES (?, ?)";
            st = con.prepareStatement(query);
            st.setString(1, file.getName());
            st.setBytes(2, imageData);

            int rowsInserted = st.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Image saved successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<byte[]> getImages() {
        ArrayList<byte[]> result = new ArrayList();
        Connection con = null;
        PreparedStatement st = null;

        try {
            con = DatabaseConnection.getConnection();
            String sql = "SELECT name, data FROM images";
            st = con.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            while(rs.next())
            {
                result.add(rs.getBytes("data"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
