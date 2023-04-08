package browser.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import browser.app.ui.*;
import browser.interaction.InteractionHandler;
import browser.model.Vector2;
import browser.tasks.RenderCompleteCallback;

public class BrowserWindow extends Application {

    private final String startupPageURL = "file://src/main/resources/html/startup_page.html";
    private final float scrollMultiple = 0.001f;

    private Scene scene;
    private Canvas canvas;
    private CanvasRenderer canvasRenderer;
    private ControlBar controlBar;
    private List<String> history = new ArrayList<>();
    private int indexInHistory = -1;
    private double offsetX = 0;
    private double offsetY = 0;

    private void setupUI(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Kelp");
        stage.getIcons().add(new Image("/images/icon.png"));

        VBox vbox = new VBox();
        controlBar = new ControlBar();
        canvas = new Canvas();
        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setContent(canvas);
        scroll.setFitToWidth(true);
        scroll.getContent().setOnScroll(event -> {
            scroll.setVvalue(scroll.getVvalue() - event.getDeltaY() * scrollMultiple);
        });
        VBox.setVgrow(scroll, Priority.ALWAYS);
        vbox.getChildren().addAll(controlBar, scroll);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(vbox, new ResizeOverlay(stage));

        // Allow undecorated window to be dragged.
        stack.setOnMousePressed(event -> {
            offsetX = stage.getX() - event.getScreenX();
            offsetY = stage.getY() - event.getScreenY();
        });
        stack.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + offsetX);
            stage.setY(event.getScreenY() + offsetY);
        });

        // Register search callback
        controlBar.getInput().setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                String search = controlBar.getInput().getText();
                renderPage(search, true);
            }
        });

        // Register home button callback
        controlBar.getHomeButton().setOnMouseClicked(event -> {
            controlBar.getInput().setText(startupPageURL);
            renderPage(startupPageURL, true);
        });

        // Register back button callback
        controlBar.getBackButton().setOnMouseClicked(event -> {
            if (history.size() > 1 && indexInHistory > 0) {
                indexInHistory--;
                String url = history.get(indexInHistory);
                renderPage(url, false);
            }
        });

        // Register forward button callback
        controlBar.getForwardButton().setOnMouseClicked(event -> {
            if (history.size() > 1 && indexInHistory < history.size() - 1) {
                indexInHistory++;
                String url = history.get(indexInHistory);
                renderPage(url, false);
            }
        });

        // Register refresh button callback
        controlBar.getRefreshButton().setOnMouseClicked(event -> {
            renderPage(history.get(indexInHistory), false);
        });

        // Register close button callback
        controlBar.getCloseButton().setOnMouseClicked(event -> {
            System.exit(0);
        });

        InteractionHandler interactionHandler = new InteractionHandler((type, data) -> {
            switch (type) {
                case REDIRECT -> renderPage(data, true);
                case HOVER_START -> scene.setCursor(Cursor.HAND);
                case HOVER_END -> scene.setCursor(Cursor.DEFAULT);
            }
        });

        // Register page click callback
        canvas.setOnMouseClicked(event -> {
            Vector2 position = new Vector2((float) event.getX(), (float) event.getY());
            interactionHandler.handleClickEvent(position);
        });

        canvas.setOnMouseMoved(event -> {
            Vector2 position = new Vector2((float) event.getX(), (float) event.getY());
            interactionHandler.handleMouseMoveEvent(position);
        });

        canvas.setOnMouseExited(event -> {
            scene.setCursor(Cursor.DEFAULT);
        });

        scene = new Scene(stack, 1500, 800);
        File windowCSSFile = new File("./src/main/resources/css/javafx_window_new.css");
        scene.getStylesheets().add(windowCSSFile.toURI().toString());
        stage.setScene(scene);
        stage.show();

        RenderCompleteCallback renderCompleteCallback = (root, type) -> {
            controlBar.setLoading(false);
            ScrollPane.ScrollBarPolicy policy = canvasRenderer.getRenderedHeight() > scroll.getHeight() ?
                    ScrollPane.ScrollBarPolicy.ALWAYS :
                    ScrollPane.ScrollBarPolicy.NEVER;
            scroll.setVbarPolicy(policy);
            scene.setCursor(Cursor.DEFAULT);
        };
        canvasRenderer = new CanvasRenderer(canvas, interactionHandler, renderCompleteCallback, controlBar.getHeight());
        canvas.setWidth(stage.getWidth());

        // Register width resize callback
        ChangeListener<Number> stageWidthChangeListener = (obs, oldValue, newValue) -> {
            canvas.setWidth(stage.getWidth());
            canvasRenderer.updateScreenWidth((float) scene.getWidth());
            canvasRenderer.refresh();
        };
        stage.widthProperty().addListener(stageWidthChangeListener);
    }

    private void renderPage(String url, boolean addToHistory) {
        if (addToHistory) {
            if (indexInHistory != history.size() - 1) {
                history = history.subList(0, indexInHistory + 1);
            }
            indexInHistory++;
            history.add(url);
        }
        canvasRenderer.updateScreenWidth((float) canvas.getWidth());
        controlBar.setLoading(true);
        canvasRenderer.renderPage(url);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pipeline.init();
        setupUI(stage);
        renderPage(startupPageURL, true);
    }
}
