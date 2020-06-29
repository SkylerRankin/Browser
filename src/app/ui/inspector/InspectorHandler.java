package app.ui.inspector;

import java.io.File;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.RenderNode;

public class InspectorHandler {
    
    private static InspectorHandler inspectorHandler;
    
    private Stage stage;
    private Scene scene;
    private ScrollPane scroll;
    private Label subtitle;
    private TreeViewer treeViewer;
    private boolean open;
    private float height = 300f;
    private float width = 500f;
    
    public InspectorHandler(Stage browserStage) {
        inspectorHandler = this;
        build(browserStage);
        setKeyListener();
        setResizeListener();
        open = false;
    }
    
    public static InspectorHandler get() {
        return inspectorHandler;
    }
    
    private void build(Stage browserStage) {
        subtitle = new Label("Render Node Tree");
        subtitle.setPadding(new Insets(0, 0, 0, 5));
        subtitle.getStyleClass().add("title");
        
        treeViewer = new TreeViewer();
        
        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(treeViewer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(height - subtitle.getHeight());
        
        VBox vbox = new VBox();
        vbox.getChildren().addAll(subtitle, scroll);

        scene = new Scene(vbox, width, height);

        stage = new Stage();
        stage.setTitle("Inspector");
        stage.setScene(scene);

        stage.setX(browserStage.getX() + browserStage.getWidth() / 2.0);
        stage.setY(browserStage.getY() + browserStage.getHeight() / 2.0);
        
        File cssFile = new File("./res/css/inspector.css");
        String path = cssFile.toURI().toString();
        scene.getStylesheets().add(path);
    }
    
    private void setResizeListener() {
        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            scroll.setPrefHeight(newValue.doubleValue() - subtitle.getHeight());
        }); 
    }
    
    private void setKeyListener() {
        KeyCodeCombination ctrlI = new KeyCodeCombination(KeyCode.I, KeyCodeCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (ctrlI.match(event)) {
                    toggle();
                }
            }
        });
    }
            
    public void update(RenderNode root) {
        treeViewer.update(root);
    }
    
    public void toggle() {
        open = !open;
        if (open) stage.show();
        else stage.hide();
    }

}
