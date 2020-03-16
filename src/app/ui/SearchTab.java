package app.ui;

import app.SearchTabPipeline;
import app.ui.BrowserTab.TabType;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SearchTab extends BrowserTab {
	
	private Tab tab;
    private GraphicsContext gc;
    private GridPane grid;
	private Label statusLabel;
    private TextField urlInput;
    private ScrollPane scroll;
    private Canvas canvas;
    
    private SearchTabPipeline pipeline;
	
	public SearchTab(Stage stage) {
		super(TabType.SEARCH, stage);
		setupUI();
		pipeline = new SearchTabPipeline(this.id, canvas, tab);
	}
	
	public void loadStartupPage() {
	    pipeline.loadWebpage("file://res/html/test.html");
	}
	
	private void setupUI() {
	    grid = new GridPane();
	    urlInput = new TextField();
	    urlInput.setText("http://gallium.inria.fr/~fpottier/menhir/");
	    statusLabel = new Label("Loading  ");
      
	    canvas = new Canvas();
	    gc = canvas.getGraphicsContext2D();
      
	    scroll = new ScrollPane();
	    scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
	    scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
	    scroll.setContent(canvas);
	    scroll.setFitToWidth(true);
      
	    HBox hbox = new HBox();
      
	    grid.add(urlInput, 0, 0);
	    grid.add(hbox, 1, 0);
	    grid.add(scroll, 0, 1, 1, 1);
      
	    GridPane.setMargin(urlInput, new Insets(5, 10, 5, 10));
      
	    tab = new Tab("New Tab", grid);
	    tab.setId(TabType.SEARCH.toString());
	    tab.getStyleClass().add("search_tab");
      
	    ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
//        onResize(stage);
	    };
      
	    stage.widthProperty().addListener(stageSizeListener);
	    
	    urlInput.setOnKeyPressed(event -> {
	        if (event.getCode().equals(KeyCode.ENTER)) {
	            onSearch();
	        }
	    });
	}
	
	@Override
	public void onResize(Stage stage) {
		grid.setPrefWidth(stage.getWidth());
        urlInput.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
        if (scene != null) {
    		scroll.setPrefSize(scene.getWidth(), scene.getHeight() - urlInput.getHeight());
    		
    		for (Node child : scroll.getChildrenUnmodifiable()) {
    			if (child instanceof ScrollBar) {
    				ScrollBar bar = (ScrollBar) child;
    			}
            }
    		
        	canvas.setWidth(scene.getWidth() - 20);
        	pipeline.updateScreenWidth((float) canvas.getWidth());
        	canvas.setHeight(scene.getHeight() - urlInput.getHeight() - 50);
            gc.setFill(Color.BLUE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
            gc.setFill(Color.BLACK);
            gc.fillText(String.format("Tab %d", this.id), 50, 50);
        }
	}
	
	public Tab getActor() {
		return tab;
	}
	
	private void onSearch() {
	    String input = urlInput.getText();
	    pipeline.loadWebpage(input);
	}
	
}
