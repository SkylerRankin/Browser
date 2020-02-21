package app.ui;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class ResizeButton extends Button {
    
    public ResizeButton(int width, int height) {
        this.getStyleClass().add("resize-button");
        this.setPrefWidth(width);
        this.setPrefHeight(height);
    }
    
    public void setOnDragListener(EventHandler<MouseEvent> handler) {
        this.setOnMouseDragged(handler);
    }

}
