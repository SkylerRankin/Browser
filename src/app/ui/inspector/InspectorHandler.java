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
    private RenderNode renderNode;
    
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
    
    private RenderNode testRenderNode() {
        RenderNode root = new RenderNode("html");
        RenderNode head = new RenderNode("head");
        RenderNode body = new RenderNode("body");
        RenderNode h1 = new RenderNode("h1");
        RenderNode t1 = new RenderNode("text");
        t1.text = "Testing the Inspector";
        RenderNode div = new RenderNode("div");
        RenderNode p = new RenderNode("p");
        RenderNode t2 = new RenderNode("text");
        t2.text = "Deep inside of the";
        RenderNode b = new RenderNode("b");
        RenderNode t3 = new RenderNode("text");
        t3.text = " render tree";

        root.children.add(head);
        root.children.add(body);
        body.children.add(h1);
        h1.children.add(t1);
        body.children.add(div);
        div.children.add(p);
        p.children.add(t2);
        p.children.add(b);
        b.children.add(t3);
        
        return root;
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
