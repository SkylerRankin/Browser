package app.ui.inspector;

import java.io.File;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import model.RenderNode;
import parser.HTMLElements;

public class RenderNodeRow extends Pane {
    
    private int id;
    private RenderNode renderNode;
    private TreeViewer treeViewer;
    private int depth;
    private boolean expanded;
    private Label label;
    private ImageView triangleImageView;
    private Image triangleRightImage;
    private Image triangleDownImage;
    
    private int margin = 30;
    
    public RenderNodeRow(int id, RenderNode renderNode, TreeViewer treeViewer, int depth) {
        this.id = id;
        this.renderNode = renderNode;
        this.treeViewer = treeViewer;
        this.depth = depth;
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5 + depth * margin));
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        File rightImageFile = new File("./res/images/triangle_right.png");
        String rightImagePath = rightImageFile.toURI().toString();
        
        File downImageFile = new File("./res/images/triangle_down.png");
        String downImagePath = downImageFile.toURI().toString();
        
        triangleRightImage = new Image(rightImagePath, 10, 10, true, true);
        triangleDownImage = new Image(downImagePath, 10, 10, true, true);
        
        triangleImageView = new ImageView(triangleRightImage);
        
        label = new Label(renderNode.type.equals(HTMLElements.TEXT) ?
                renderNode.text :
                renderNode.type
        );
        label.getStyleClass().add("row_text");
        
        hbox.getChildren().addAll(triangleImageView, label);
        
        this.getChildren().add(hbox);
        this.getStyleClass().add("render_node_row");
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onClick();
            }
        });
    }
    
    public RenderNode getRenderNode() {
        return renderNode;
    }
    
    public int getDepth() {
        return depth;
    }
    
    private void onClick() {
        expanded = !expanded;
        if (expanded && renderNode.children.size() == 0) expanded = false;
        if (expanded) {
            this.getStyleClass().add("expanded");
            label.getStyleClass().add("row_text_expanded");
            triangleImageView.setImage(triangleDownImage);
            treeViewer.expandRow(id);
        } else {
            this.getStyleClass().remove("expanded");
            label.getStyleClass().remove("row_text_expanded");
            triangleImageView.setImage(triangleRightImage);
            treeViewer.collapseRow(id);
        }
    }

}
