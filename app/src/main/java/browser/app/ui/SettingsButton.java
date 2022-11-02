package browser.app.ui;

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SettingsButton extends Button {

    public SettingsButton() {
        File iconFile = new File("./res/images/lines.png");
        ImageView image = new ImageView(new Image(iconFile.toURI().toString(), 16, 16, false, false));
        this.getStyleClass().add("settings-button");
        this.setGraphic(image);
    }

}
