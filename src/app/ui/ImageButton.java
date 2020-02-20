package app.ui;

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageButton extends Button {
    
    public ImageButton(String imagePath) {
        File iconFile = new File(imagePath);
        ImageView image = new ImageView(new Image(iconFile.toURI().toString(), 256, 256, false, false));
        image.setFitHeight(16);
        image.setFitWidth(16);
        this.setGraphic(image);
    }

}
