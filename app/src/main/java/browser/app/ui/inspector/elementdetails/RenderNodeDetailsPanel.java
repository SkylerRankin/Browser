package browser.app.ui.inspector.elementdetails;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import browser.model.RenderNode;

public class RenderNodeDetailsPanel extends ScrollPane {

    public RenderNodeDetailsPanel() {
        getStyleClass().add("inspector_render_node_details");
    }

    public void update(RenderNode renderNode) {
        getChildren().clear();

        if (renderNode == null) {
            getChildren().add(new Text("Select an element from the tree."));
            return;
        }

        VBox vbox = new VBox();

        vbox.getChildren().add(new Text(String.format("<%s> id=%d, depth=%d", renderNode.type, renderNode.id, renderNode.depth)));
        vbox.getChildren().add(new Text(String.format("Size = %dpx x %dpx, Position = (%d, %d)",
                (int) Math.floor(renderNode.box.width), (int) Math.floor(renderNode.box.height),
                (int) Math.floor(renderNode.box.x), (int) Math.floor(renderNode.box.y))));
        vbox.getChildren().add(new Text("CSS Properties"));
        vbox.getChildren().add(new Text("font-size: " + renderNode.style.fontSize));

        getChildren().add(vbox);
    }

}
