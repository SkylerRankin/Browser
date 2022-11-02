package browser.app.ui.inspector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import browser.model.RenderNode;
import browser.parser.HTMLElements;

public class RenderNodeRow extends Pane {
    
    private int id;
    private RenderNode renderNode;
    private TreeViewer treeViewer;
    private int depth;
    private boolean expanded;
    private Label label;
    private ImageView iconImageView;
    private Image triangleRightImage;
    private Image triangleDownImage;
    private RenderNodeSummary renderNodeSummary;
    
    private static Map<String, Image> icons;
    
    private int margin = 30;
    
    public RenderNodeRow(int id, RenderNode renderNode, TreeViewer treeViewer, RenderNodeSummary renderNodeSummary, int depth) {
        this.id = id;
        this.renderNode = renderNode;
        this.treeViewer = treeViewer;
        this.depth = depth;
        this.renderNodeSummary = renderNodeSummary;

        this.setPrefWidth(treeViewer.getWidth());
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5 + depth * margin));
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        if (icons == null) loadIcons();
        iconImageView = new ImageView();
        
        if (renderNode.type.equals(HTMLElements.TEXT)) {
            label = new Label(renderNode.text);
            setIcon("text");
        } else {
            label = new Label(renderNode.type);
            iconImageView = new ImageView(triangleRightImage);
            if (renderNode.children.size() > 0) setIcon("triangle_right");
            else setIcon("");
        }
        
        label.getStyleClass().add("row_text");
        
        hbox.getChildren().addAll(iconImageView, label);
        
        this.getChildren().add(hbox);
        this.getStyleClass().add("render_node_row");
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onClick();
            }
        });
    }
    
    public static void loadIcons() {
        icons = new HashMap<String, Image>();
        String[] fileNames = {"triangle_right", "triangle_down", "text"};
        for (String fileName : fileNames) {
            File imageFile = new File(String.format("./res/images/%s.png", fileName));
            String imageFilePath = imageFile.toURI().toString();
            Image image = new Image(imageFilePath, 10, 10, true, true);
            icons.put(fileName, image);
        }
    }
    
    private void setIcon(String name) {
        if (icons.containsKey(name)) {
            iconImageView.setImage(icons.get(name));
        }
    }
    
    public RenderNode getRenderNode() {
        return renderNode;
    }
    
    public int getDepth() {
        return depth;
    }
    
    private void onClick() {
        expanded = !expanded;
        if (expanded && (renderNode.children.size() == 0 || renderNode.type.equals(HTMLElements.TEXT))) expanded = false;
        if (expanded) {
            this.getStyleClass().add("expanded");
            label.getStyleClass().add("row_text_expanded");
            setIcon("triangle_down");
            treeViewer.expandRow(id);
        } else if (!renderNode.type.equals(HTMLElements.TEXT) && renderNode.children.size() > 0) {
            this.getStyleClass().remove("expanded");
            label.getStyleClass().remove("row_text_expanded");
            setIcon("triangle_right");
            treeViewer.collapseRow(id);
        }
        renderNodeSummary.onClick(renderNode);
    }

}
