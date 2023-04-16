package browser.app.ui;

import javafx.geometry.Insets;
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
import browser.tasks.RenderCompleteCallback;

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
    private double splitPaneDividerPosition = -1;

    public SearchTab(Stage stage, InteractionCallback interactionCallback) {
        super(TabType.SEARCH, stage);
        setupUI();
        interactionHandler = new InteractionHandler(interactionCallback);
        pipeline = new SearchTabPipeline(this.id, canvas, tab, interactionHandler, getRenderCompleteCallback());
        inspectorPanel.setPipeline(pipeline);
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

        canvas = new Canvas();

        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(canvas);
        scroll.setFitToWidth(true);

        inspectorPanel = new InspectorPanel();

        splitPane = new SplitPane();
        splitPane.getStyleClass().add("split_pane");
        splitPane.getItems().addAll(scroll);

        grid.add(urlInput, 0, 0);
        grid.add(splitPane, 0, 1, 1, 1);
      
        GridPane.setMargin(urlInput, new Insets(5, 10, 5, 10));
      
        tab = new Tab("New Tab", grid);
        tab.setId(TabType.SEARCH.toString());
        tab.getStyleClass().add("search_tab");

        canvas.setOnMouseClicked(event -> {
            Vector2 position = new Vector2((float) event.getX(), (float) event.getY());
            interactionHandler.handleClickEvent(position);
        });

        urlInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                onSearch();
            }
        });
    }

    public void initialLoad(Stage stage, String url) {
        grid.setPrefWidth(stage.getWidth());
        urlInput.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
        if (scene != null) {
            double usedHeight = urlInput.getHeight() + 50;
            scroll.setPrefSize(scene.getWidth(), scene.getHeight() - usedHeight);
            canvas.setWidth(scene.getWidth());
            pipeline.updateScreenDimensions((float) canvas.getWidth(), (float) canvas.getHeight());
            canvas.setHeight(scene.getHeight() - usedHeight);
            loadURL(url);
        }
    }

    @Override
    public void onResize(Stage stage) {
        grid.setPrefWidth(stage.getWidth());
        urlInput.setPrefWidth(stage.getWidth() - statusLabel.getWidth() - 20);
        if (scene != null) {
            scroll.setPrefSize(scene.getWidth(), scene.getHeight() - urlInput.getHeight());
            canvas.setWidth(scene.getWidth());
            pipeline.updateScreenDimensions((float) canvas.getWidth(), (float) canvas.getHeight());
            canvas.setHeight(scene.getHeight() - urlInput.getHeight());

            if (pipeline.loadedWebpage()) pipeline.redrawWebpage();
        }
    }

    public void onRefresh() {
        if (pipeline.loadedWebpage()) pipeline.redrawWebpage();
    }

    public void toggleInspector() {
        double totalWidth = scene.getWidth();
        double newCanvasWidth;
        if (splitPane.getItems().size() == 1) {
            splitPane.getItems().add(inspectorPanel);
            inspectorPanel.updateRenderTree(pipeline.getRootRenderNode());

            if (splitPaneDividerPosition == -1) {
                splitPaneDividerPosition = getInitialSplitPanePosition();
            }
            splitPane.setDividerPosition(0, splitPaneDividerPosition);

            splitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
                splitPaneDividerPosition = clampSplitPanePosition(newVal.doubleValue());
                double canvasWidth = getCanvasWidth();
                canvas.setWidth(canvasWidth);
                pipeline.updateScreenDimensions((float) canvas.getWidth(), (float) canvas.getHeight());
                if (pipeline.loadedWebpage()) {
                    pipeline.redrawWebpage();
                }
            });

            newCanvasWidth = getCanvasWidth();
        } else {
            inspectorPanel.preClose();
            splitPaneDividerPosition = splitPane.getDividerPositions()[0];
            splitPane.getItems().remove(1);
            newCanvasWidth = totalWidth;
        }

        canvas.setWidth(newCanvasWidth);
        pipeline.updateScreenDimensions((float) canvas.getWidth(), (float) canvas.getHeight());
        if (pipeline.loadedWebpage()) {
            pipeline.redrawWebpage();
        }
    }

    public Tab getActor() {
        return tab;
    }

    private void onSearch() {
        String input = urlInput.getText();
        pipeline.loadWebpage(input);
    }

    private double getCanvasWidth() {
        boolean inspectorOpen = splitPane.getItems().size() == 2;
        if (inspectorOpen) {
            double totalWidth = scene.getWidth();
            double inspectorWidth = (1 - splitPaneDividerPosition) * totalWidth;
            return totalWidth - inspectorWidth;
        } else {
            return scene.getWidth();
        }
    }

    private double getInitialSplitPanePosition() {
        double totalWidth = scene.getWidth();
        double inspectorMinWidth = inspectorPanel.getMinWidth();
        return 1 - (inspectorMinWidth / totalWidth);
    }

    private double clampSplitPanePosition(double position) {
        double min = 1 - (inspectorPanel.getMaxWidth() / scene.getWidth());
        double max = 1 - (inspectorPanel.getMinWidth() / scene.getWidth());
        return Math.min(Math.max(position, min), max);
    }

    private RenderCompleteCallback getRenderCompleteCallback() {
        return (root, renderType) -> {
            if (renderType == RenderCompleteCallback.RenderType.NewLayout && splitPane.getItems().size() == 2) {
                inspectorPanel.updateRenderTree(root);
            }
        };
    }

}
