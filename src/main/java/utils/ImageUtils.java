package utils;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

public class ImageUtils {

    public static Image convertToImage(byte[] imageData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        return new Image(bis);
    }
}