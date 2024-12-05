package controllers;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
public class RecommendationTabController {
    @FXML
    private HBox scrollPaneContent;
    @FXML
    private VBox scrollPane2Content;

    public void addCardToScrollPane(AnchorPane card) {
        scrollPaneContent.getChildren().add(card);
    }

    public void addCardToScrollPane2(AnchorPane card) {
        scrollPane2Content.getChildren().add(card);
    }
}