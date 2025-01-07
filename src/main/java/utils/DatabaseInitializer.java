package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try {
            // Read SQL file
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            DatabaseInitializer.class.getResourceAsStream("/sql/create_tables.sql")
                    )
            );

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            // Execute SQL statements
            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Split SQL statements by semicolon and execute each
                for (String statement : sql.toString().split(";")) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }

                System.out.println("Database tables initialized successfully");
            }

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}