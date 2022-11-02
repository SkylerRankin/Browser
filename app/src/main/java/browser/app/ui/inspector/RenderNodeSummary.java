package browser.app.ui.inspector;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import browser.model.RenderNode;

public class RenderNodeSummary extends VBox {

    private Label typeLabel = new Label("");
    private Label positionLabel = new Label("");
    private Label sizeLabel = new Label("");


    public RenderNodeSummary() {

        Label subtitle = new Label("Node Summary");
        subtitle.setPadding(new Insets(0, 0, 0, 5));
        subtitle.getStyleClass().add("title");
        this.getChildren().add(subtitle);

        this.getChildren().addAll(
                typeLabel,
                positionLabel,
                sizeLabel
        );
    }

    public void onClick(RenderNode node) {
        typeLabel.setText(node.type);
        positionLabel.setText(String.format("Position: (%.0f, %.0f)", node.box.x, node.box.y));
        sizeLabel.setText(String.format("Size: %.0f x %.0f", node.box.width, node.box.height));
    }

}
