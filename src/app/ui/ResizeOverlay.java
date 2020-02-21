package app.ui;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ResizeOverlay extends Pane {
    
    private ResizeButton bottomRightButton;
    
    private int width = 1000;
    private int height = 600;
    private int buttonSize = 20;
    
    public ResizeOverlay(Stage stage) {
        this.setPickOnBounds(false);
        
        bottomRightButton = new ResizeButton(buttonSize, buttonSize);
        bottomRightButton.setLayoutX(width - buttonSize);
        bottomRightButton.setLayoutY(height - buttonSize);
        
        this.getChildren().add(bottomRightButton);
        
        bottomRightButton.setOnDragListener(event -> {
            stage.setWidth(event.getSceneX());
            stage.setHeight(event.getSceneY());
        });
        
        setStageResizeListener(stage);
        
    }
    
    private void setStageResizeListener(Stage stage) {
        ChangeListener<Number> widthSizeListner = (obs, oldValue, newValue) -> {
            bottomRightButton.setLayoutX(newValue.intValue() - buttonSize);
        };
        ChangeListener<Number> heightSizeListener = (obs, oldValue, newValue) -> {
            bottomRightButton.setLayoutY(newValue.intValue() - buttonSize);
        };
        stage.widthProperty().addListener(widthSizeListner);
        stage.heightProperty().addListener(heightSizeListener);
    }

}
