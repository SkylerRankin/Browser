package app;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SearchTab extends BrowserTab {
	
	private Tab tab;
    private GraphicsContext gc;
    private GridPane grid;
	private Label statusLabel;
    private Button searchButton;
    private TextField urlInput;
    private ScrollPane scroll;
    private Canvas canvas;
	
	public SearchTab(Stage stage) {
		super(TabType.SEARCH, stage);
		
		grid = new GridPane();
        
        searchButton = new Button();
        searchButton.setText("Search");
        urlInput = new TextField();
//        urlInput.getStyleClass().add("search-bar");
        statusLabel = new Label("Loading  ");
        
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        
        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(canvas);
        scroll.setFitToWidth(true);
        
        grid.add(urlInput, 0, 0);
        grid.add(scroll, 0, 1, 1, 1);
        
        GridPane.setMargin(urlInput, new Insets(5));
        
		tab = new Tab("New Tab", grid);
		tab.setId(TabType.SEARCH.toString());
		
		ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
			onResize(stage);
        };
        
		stage.widthProperty().addListener(stageSizeListener);
	}
	
	
	@Override
	public void onResize(Stage stage) {
		grid.setPrefWidth(stage.getWidth());
        urlInput.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
        if (scene != null) {
    		scroll.setPrefSize(scene.getWidth(), scene.getHeight() - urlInput.getHeight() - 20);
    		
    		for (Node child : scroll.getChildrenUnmodifiable()) {
    			if (child instanceof ScrollBar) {
    				ScrollBar bar = (ScrollBar) child;
    				System.out.printf("%f, %f %s\n", bar.getHeight(), bar.getWidth(), bar.getOrientation().toString());
    			}
            	System.out.println(child);
            }
    		
//    		System.out.println(scroll.getWidth());
        	canvas.setWidth(scene.getWidth() - 20);
        	canvas.setHeight(scene.getHeight() - urlInput.getHeight() - 20 - 40);
            gc.setFill(Color.BLUE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
        	System.out.println(scroll.getViewportBounds());

            System.out.printf("canvas size = (%f, %f)\n", scene.getWidth(), scene.getHeight());
        }
	}
	
	public Tab getActor() {
		return tab;
	}
	
	public TextField getURLInput() {
		return urlInput;
	}
	
	public Button getSearchButton() {
		return searchButton;
	}

}
