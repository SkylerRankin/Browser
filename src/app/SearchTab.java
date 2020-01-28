package app;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
        urlInput.getStyleClass().add("search-bar");
        statusLabel = new Label("Loading  ");
        
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        
        scroll = new ScrollPane();
        scroll.setContent(canvas);
        
//        grid.add(searchButton, 0, 0);
        grid.add(urlInput, 0, 0);
        grid.add(scroll, 0, 1, 1, 1);
        
        GridPane.setMargin(urlInput, new Insets(5));
        
		tab = new Tab("New Tab", grid);
		tab.setId(TabType.SEARCH.toString());
		
		ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
			setSizing();
        };
		stage.widthProperty().addListener(stageSizeListener);
//		setSizing();

	}
	
	@Override
	public void onResize(Stage stage) {
		setSizing();
	}
	
	private void setSizing() {
		grid.setPrefWidth(stage.getWidth());
		scroll.setPrefSize(stage.getWidth(), stage.getHeight() - searchButton.getHeight());
        urlInput.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
//        canvas.setWidth(stage.getWidth());
//        canvas.setHeight(stage.getHeight() - searchButton.getHeight());
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
