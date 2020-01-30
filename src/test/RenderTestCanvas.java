package test;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RenderTestCanvas extends Application {

	public GraphicsContext gc;
	
	@Override
	public void start(Stage stage) throws Exception {
		setupUI(stage);
	}
	
    private void setupUI(Stage stage) {
        stage.setTitle("Render Testing Canvas");
        
        GridPane grid = new GridPane();
        
        Canvas canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(canvas);
        
        grid.add(scroll, 0, 0);
        
        Scene scene = new Scene(grid, 1200, 800);

        stage.setScene(scene);
        stage.show();
        
        scroll.setPrefSize(1200, stage.getHeight());
        canvas.setWidth(stage.getWidth());
        canvas.setHeight(stage.getHeight());
        
        HTMLRendererTest.render(gc, 1100);
        
    }

}
