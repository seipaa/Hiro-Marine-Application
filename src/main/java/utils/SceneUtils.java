package utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneUtils {
    public static void changeScene(Node node, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(SceneUtils.class.getResource(fxmlPath));
            Stage stage = (Stage) node.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            AlertUtils.showError("Error", "Gagal memuat halaman: " + e.getMessage());
        }
    }
} 