package browser.app.ui;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import browser.app.SearchTabPipeline;
import browser.app.ui.inspector.InspectorPanel;
import browser.interaction.InteractionCallback;
import browser.interaction.InteractionHandler;
import browser.model.Vector2;

public class SearchTab extends BrowserTab {

    private final SearchTabPipeline pipeline;
    private final InteractionHandler interactionHandler;

    private Tab tab;
    private GridPane grid;
    private Label statusLabel;
    private TextField urlInput;
    private SplitPane splitPane;
    private ScrollPane scroll;
    private Canvas canvas;
    private InspectorPanel inspectorPanel;
    private double splitPaneDividerPosition = 0;

    public SearchTab(Stage stage, InteractionCallback interactionCallback) {
        super(TabType.SEARCH, stage);
        setupUI();
        interactionHandler = new InteractionHandler(interactionCallback);
        pipeline = new SearchTabPipeline(this.id, canvas, tab, interactionHandler);
        inspectorPanel.setPipeline(pipeline);
    }

    public void loadURL(String url) {
        pipeline.loadWebpage(url, root -> {
            inspectorPanel.updateRenderTree(root);
        });
        urlInput.setText(url);
    }

    private void setupUI() {
        grid = new GridPane();
        urlInput = new TextField();
        urlInput.setText("");
        statusLabel = new Label("Loading  ");

        canvas = new Canvas();

        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(canvas);
        scroll.setFitToWidth(true);

        inspectorPanel = new InspectorPanel();

        splitPane = new SplitPane();
        splitPane.getStyleClass().add("split_pane");
        splitPane.getItems().addAll(scroll, inspectorPanel);
        splitPane.setDividerPosition(0, splitPaneDividerPosition);

        grid.add(urlInput, 0, 0);
        grid.add(splitPane, 0, 1, 1, 1);
      
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
        if (scene != null) {
            scroll.setPrefSize(scene.getWidth(), scene.getHeight() - urlInput.getHeight());

            for (Node child : scroll.getChildrenUnmodifiable()) {
                if (child instanceof ScrollBar) {
                    ScrollBar bar = (ScrollBar) child;
                }
            }

            canvas.setWidth(scene.getWidth() - 20);
            pipeline.updateScreenWidth((float) canvas.getWidth());
            canvas.setHeight(scene.getHeight() - urlInput.getHeight());

            if (pipeline.loadedWebpage()) pipeline.redrawWebpage();
//            gc.setFill(Color.BLUE);
//            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//            gc.setFill(Color.WHITE);
//            gc.fillRect(10, 10, canvas.getWidth() - 20, canvas.getHeight() - 20);
//            gc.setFill(Color.BLACK);
//            gc.fillText(String.format("Tab %d", this.id), 50, 50);
        }
    }

    public void toggleInspector() {
        if (splitPane.getItems().size() == 1) {
            splitPane.getItems().add(inspectorPanel);
            splitPane.setDividerPosition(0, splitPaneDividerPosition);
        } else {
            splitPaneDividerPosition = splitPane.getDividerPositions()[0];
            splitPane.getItems().remove(1);
        }
    }

    public Tab getActor() {
        return tab;
    }

    private void onSearch() {
        String input = urlInput.getText();
        pipeline.loadWebpage(input, root -> {
            inspectorPanel.updateRenderTree(root);
        });
    }

}
