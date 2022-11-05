package browser.app.ui;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import browser.app.SearchTabPipeline;
import browser.interaction.InteractionCallback;
import browser.interaction.InteractionHandler;
import browser.model.Vector2;

public class SearchTab extends BrowserTab {

    private Tab tab;
    private GridPane grid;
    private Label statusLabel;
    private TextField urlInput;
    private BookmarksBar bookmarksBar;
    private ScrollPane scroll;
    private Canvas canvas;
    
    private final SearchTabPipeline pipeline;
    private final InteractionHandler interactionHandler;

    public SearchTab(Stage stage, InteractionCallback interactionCallback) {
        super(TabType.SEARCH, stage);
        setupUI();
        interactionHandler = new InteractionHandler(interactionCallback);
        pipeline = new SearchTabPipeline(this.id, canvas, tab, interactionHandler);
    }

    public void loadURL(String url) {
        pipeline.loadWebpage(url);
        urlInput.setText(url);
    }

    private void setupUI() {
        grid = new GridPane();
        urlInput = new TextField();
        urlInput.setText("");
        statusLabel = new Label("Loading  ");
        bookmarksBar = new BookmarksBar(urlInput);
      
        canvas = new Canvas();
        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(canvas);
        scroll.setFitToWidth(true);

        grid.add(urlInput, 0, 0);
//        grid.add(bookmarksBar, 0, 1, 1, 1);
        grid.add(scroll, 0, 2, 1, 1);
      
        GridPane.setMargin(urlInput, new Insets(5, 10, 5, 10));
      
        tab = new Tab("New Tab", grid);
        tab.setId(TabType.SEARCH.toString());
        tab.getStyleClass().add("search_tab");

        ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
//        onResize(stage);
        };

        canvas.setOnMouseClicked(event -> {
            Vector2 position = new Vector2((float) event.getX(), (float) event.getY());
            interactionHandler.handleClickEvent(position);
        });
      
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
        bookmarksBar.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
        if (scene != null) {
            scroll.setPrefSize(scene.getWidth(), scene.getHeight() - urlInput.getHeight() - bookmarksBar.getHeight());

            for (Node child : scroll.getChildrenUnmodifiable()) {
                if (child instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) child;
                }
            }

            canvas.setWidth(scene.getWidth() - 20);
            pipeline.updateScreenWidth((float) canvas.getWidth());
            canvas.setHeight(scene.getHeight() - urlInput.getHeight() - bookmarksBar.getHeight());

            if (pipeline.loadedWebpage()) pipeline.redrawWebpage();
//            gc.setFill(Color.BLUE);
//            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//            gc.setFill(Color.WHITE);
//            gc.fillRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
//            gc.setFill(Color.BLACK);
//            gc.fillText(String.format("Tab %d", this.id), 50, 50);
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
