package browser.app.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

import browser.constants.ResourceConstants;

import lombok.Getter;

public class ControlBar extends HBox {

    @Getter
    private final TextField input;
    @Getter
    private final Button backButton;
    @Getter
    private final Button forwardButton;
    @Getter
    private final Button homeButton;
    @Getter
    private final Button refreshButton;
    @Getter
    private final Button closeButton;
    private final int buttonSize = 20;

    private ImageView defaultRefreshImage;
    private ImageView loadingRefreshImage;

    public ControlBar() {
        getStyleClass().add("control-bar");

        HBox leftButtonsBox = new HBox();
        leftButtonsBox.getStyleClass().add("left-buttons-debug");
        leftButtonsBox.setPadding(new Insets(15));
        leftButtonsBox.setSpacing(15);
        leftButtonsBox.setAlignment(Pos.CENTER_LEFT);
        leftButtonsBox.setPrefWidth(400);

        backButton = createImageButton("/images/left_arrow.png", "control-button");
        backButton.getStyleClass().add("control-button");

        forwardButton = createImageButton("/images/right_arrow.png", "control-button");
        forwardButton.getStyleClass().add("control-button");

        homeButton = createImageButton("/images/square.png", "control-button");
        homeButton.getStyleClass().add("control-button");

        refreshButton = createImageButton("/images/refresh.png", "control-button");
        refreshButton.getStyleClass().add("control-button");

        leftButtonsBox.getChildren().addAll(backButton, forwardButton, homeButton, refreshButton);

        HBox inputBox = new HBox();
        inputBox.setPadding(new Insets(15));
        inputBox.setAlignment(Pos.CENTER);
        input = new TextField();
        input.setPrefHeight(30);
        setInputFont(input);
        inputBox.getChildren().add(input);
        HBox.setHgrow(input, Priority.ALWAYS);
        HBox.setHgrow(inputBox, Priority.ALWAYS);

        HBox rightButtonsBox = new HBox();
        rightButtonsBox.setPadding(new Insets(15));
        rightButtonsBox.setSpacing(15);
        rightButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        rightButtonsBox.setPrefWidth(250);
        closeButton = createImageButton("/images/close.png", "close-button");
        closeButton.getStyleClass().add("close-button");
        rightButtonsBox.getChildren().add(closeButton);

        getChildren().addAll(leftButtonsBox, inputBox, rightButtonsBox);
        setLoadingImages();
    }

    public void setLoading(boolean loading) {
        if (loading) {
            refreshButton.setGraphic(loadingRefreshImage);
            refreshButton.setDisable(true);
        } else {
            refreshButton.setGraphic(defaultRefreshImage);
            refreshButton.setDisable(false);
        }
    }

    public void setURL(String url) {
        if (url.equals(ResourceConstants.START_PAGE_URL)) {
            url = "";
        }
        input.setText(url);
    }

    private void setLoadingImages() {
        try {
            File defaultIconFile = new File(Objects.requireNonNull(getClass().getResource("/images/refresh.png")).toURI());
            defaultRefreshImage = new ImageView(new Image(defaultIconFile.toURI().toString(), buttonSize, buttonSize, false, true));
            File loadingIconFile = new File(Objects.requireNonNull(getClass().getResource("/images/loading.gif")).toURI());
            loadingRefreshImage = new ImageView(new Image(loadingIconFile.toURI().toString(), buttonSize, buttonSize, false, true));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void setInputFont(TextField input) {
        try {
            InputStream stream = ControlBar.class.getResourceAsStream("/fonts/rubik/Rubik-Regular.ttf");
            if (stream != null) {
                Font font = Font.loadFont(stream, 14);
                input.setFont(font);
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Button createImageButton(String imagePath, String styleClass) {
        Button button = new Button();
        try {
            File iconFile = new File(Objects.requireNonNull(getClass().getResource(imagePath)).toURI());
            ImageView image = new ImageView(new Image(iconFile.toURI().toString(), buttonSize, buttonSize, false, true));
            button.getStyleClass().add(styleClass);
            button.setGraphic(image);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return button;
    }

}
