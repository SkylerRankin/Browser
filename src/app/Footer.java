package app;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Footer extends GridPane {
	
	private Rectangle progressBar;
	private Rectangle progressBarBackground;
	private Label label;
	
	public Footer() {
		
		this.getStyleClass().add("footer");
		
		progressBar = new Rectangle();
		progressBar.setWidth(150);
		progressBar.setHeight(10);
		progressBar.setFill(Color.GREEN);
		
		GridPane.setMargin(progressBar, new Insets(5, 0, 5, 5));
		
		progressBarBackground = new Rectangle();
		progressBarBackground.setWidth(200);
		progressBarBackground.setHeight(10);
		progressBarBackground.setFill(Color.DARKGRAY);
		GridPane.setMargin(progressBarBackground, new Insets(5, 0, 5, 5));
		
		label = new Label("Rendering");
		GridPane.setMargin(label, new Insets(0, 5, 0, 0));
		
		this.add(progressBarBackground, 0, 0);
		this.add(progressBar, 0, 0);
		this.add(label, 1, 0);
		
		ColumnConstraints col1 = new ColumnConstraints();
	    col1.setPercentWidth(50);
	    ColumnConstraints col2 = new ColumnConstraints();
	    col2.setPercentWidth(50);
	    getColumnConstraints().addAll(col1, col2);
		
		GridPane.setHalignment(label, HPos.RIGHT);
	}
	
	public void setProgress(double d) {}

}
