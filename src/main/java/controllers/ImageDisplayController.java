package controllers;

import dao.ImageDAO;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.ImageUtils;

import java.util.ArrayList;

public class ImageDisplayController {

    @FXML
    private ImageView imageView;

    public void initialize() {
        ArrayList<byte[]> imagesData = ImageDAO.getImages();

        if (!imagesData.isEmpty()) {
            byte[] imageData = imagesData.get(0);
            Image image = ImageUtils.convertToImage(imageData);

            // Setel gambar ke ImageView
            imageView.setImage(image);
        }
    }
}