package browser.app.ui.inspector.rendertree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import browser.model.RenderNode;

public class RenderTreeRow extends FlowPane {

    public enum RowMode { Start, End, Collapsed }

    private final int baseIndentationSize = 10;
    private final int indentationSize = 20;
    private final int arrowImageSize = 8;

    private final RenderNode node;
    private final RowMode mode;
    private final Image downArrowImage;
    private final Image rightArrowImage;

    public RenderTreeRow(RenderNode node, RowMode mode) {
        this.node = node;
        this.mode = mode;
        getStyleClass().add("render_tree_row");
        setPadding(new Insets(0, 0, 0, baseIndentationSize + indentationSize * node.depth));

        File downArrowImageFile = new File("./src/main/resources/images/downArrow.png");
        downArrowImage = new Image(downArrowImageFile.toURI().toString(), true);
        File rightArrowImageFile = new File("./src/main/resources/images/rightArrow.png");
        rightArrowImage = new Image(rightArrowImageFile.toURI().toString(), true);

        switch (mode) {
            case Start -> addRowStartText();
            case End -> {
                addRowEndText();
                // Add extra padding since end tags do not have the arrow image.
                setPadding(new Insets(0, 0, 0, arrowImageSize + baseIndentationSize + indentationSize * node.depth));
            }
            case Collapsed -> addRowCollapsedText();
        }
    }

    public RenderNode getRenderNode() {
        return node;
    }

    public RowMode getMode() {
        return mode;
    }

    private void addRowStartText() {
        ImageView arrow = new ImageView(downArrowImage);
        arrow.setFitWidth(arrowImageSize);
        arrow.setFitHeight(arrowImageSize);
        Text tagStart = new Text(String.format(" <%s", node.type));
        tagStart.getStyleClass().add("render_tree_row_html");
        Text tagEnd = new Text(">");
        tagEnd.getStyleClass().add("render_tree_row_html");

        List<Text> attributesText = getAttributesTextList();

        getChildren().addAll(arrow, tagStart);
        if (attributesText.size() > 0) {
            getChildren().addAll(attributesText);
        }
        getChildren().add(tagEnd);
    }

    private void addRowEndText() {
        Text tagEnd = new Text(String.format(" </%s>", node.type));
        tagEnd.getStyleClass().add("render_tree_row_html");
        getChildren().addAll(tagEnd);
    }

    private void addRowCollapsedText() {
        ImageView arrow = new ImageView(rightArrowImage);
        arrow.setFitWidth(arrowImageSize);
        arrow.setFitHeight(arrowImageSize);
        Text tagStart = new Text(String.format(" <%s", node.type));
        tagStart.getStyleClass().add("render_tree_row_html");
        Text tagStartEnd = new Text(">");
        tagStartEnd.getStyleClass().add("render_tree_row_html");
        Text ellipses = new Text("...");
        ellipses.getStyleClass().add("render_tree_row_ellipses");
        Text tagEnd = new Text(String.format("</%s>", node.type));
        tagEnd.getStyleClass().add("render_tree_row_html");
        List<Text> attributesText = getAttributesTextList();

        getChildren().addAll(arrow, tagStart);
        if (attributesText.size() > 0) {
            getChildren().addAll(attributesText);
        }
        getChildren().addAll(tagStartEnd, ellipses, tagEnd);
    }

    private List<Text> getAttributesTextList() {
        List<Text> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : node.attributes.entrySet()) {
            String prefix = " ";
            Text attributeName = new Text(prefix + entry.getKey());
            attributeName.getStyleClass().add("render_tree_row_attribute_name");
            Text equals = new Text("=");
            equals.getStyleClass().add("render_tree_row_html");
            Text quotation1 = new Text("\"");
            quotation1.getStyleClass().add("render_tree_row_html");
            Text value = new Text(entry.getValue());
            value.getStyleClass().add("render_tree_row_attribute_value");
            Text quotation2 = new Text("\"");
            quotation2.getStyleClass().add("render_tree_row_html");

            list.add(attributeName);
            list.add(equals);
            list.add(quotation1);
            list.add(value);
            list.add(quotation2);
        }

        return list;
    }

}
