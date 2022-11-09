package browser.app.ui.inspector.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import browser.model.RenderNode;

public class RenderTreeRow extends HBox {

    public enum RowMode { Text, Start, End, Collapsed }

    private final int baseIndentationSize = 10;
    private final int indentationSize = 20;
    private final int arrowImageSize = 8;

    private final RenderNode node;
    private final RowMode mode;
    private final Image downArrowImage;
    private final Image rightArrowImage;

    private HBox expandButton;
    private HBox collapseButton;
    private FlowPane rowSelectorPane;

    public RenderTreeRow(RenderNode node, RowMode mode, boolean isSelected) {
        this.node = node;
        this.mode = mode;
        getStyleClass().add("render_tree_row");
        if (isSelected) {
            getStyleClass().add("render_tree_row_selected");
        }
        setPadding(new Insets(0, 0, 0, baseIndentationSize + indentationSize * node.depth));

        File downArrowImageFile = new File("./src/main/resources/images/downArrow.png");
        downArrowImage = new Image(downArrowImageFile.toURI().toString(), false);
        File rightArrowImageFile = new File("./src/main/resources/images/rightArrow.png");
        rightArrowImage = new Image(rightArrowImageFile.toURI().toString(), false);
        // Add extra padding for text and end rows since they do not have the arrow image.
        int leftPadding = 20 + baseIndentationSize + indentationSize * node.depth;
        Insets padding = new Insets(0, 0, 0, leftPadding);

        switch (mode) {
            case Text -> {
                addRowPlainText();
                setPadding(padding);
            }
            case Start -> addRowStartText();
            case End -> {
                addRowEndText();
                setPadding(padding);
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
    public HBox getExpandButton() {
        return expandButton;
    }
    public HBox getCollapseButton() {
        return collapseButton;
    }
    public FlowPane getRowSelector() { return rowSelectorPane; }

    private void addRowStartText() {
        collapseButton = new HBox();
        collapseButton.setPrefWidth(20);
        collapseButton.setMinWidth(20);
        collapseButton.getStyleClass().add("render_tree_row_arrow_container");
        collapseButton.setAlignment(Pos.CENTER);
        ImageView arrow = new ImageView(downArrowImage);
        arrow.setFitWidth(arrowImageSize);
        arrow.setFitHeight(arrowImageSize);
        collapseButton.getChildren().add(arrow);

        rowSelectorPane = new FlowPane();
        rowSelectorPane.getStyleClass().add("render_tree_row_content_pane");

        Text tagStart = new Text(String.format("<%s", node.type));
        tagStart.getStyleClass().add("render_tree_row_html");
        Text tagEnd = new Text(">");
        tagEnd.getStyleClass().add("render_tree_row_html");

        List<Text> attributesText = getAttributesTextList();

        rowSelectorPane.getChildren().addAll(tagStart);
        if (attributesText.size() > 0) {
            rowSelectorPane.getChildren().addAll(attributesText);
        }
        rowSelectorPane.getChildren().add(tagEnd);

        getChildren().addAll(collapseButton, rowSelectorPane);
    }

    private void addRowEndText() {
        Text tagEnd = new Text(String.format("</%s>", node.type));
        tagEnd.getStyleClass().add("render_tree_row_html");
        getChildren().addAll(tagEnd);
    }

    private void addRowCollapsedText() {
        expandButton = new HBox();
        expandButton.setPrefWidth(20);
        expandButton.setMinWidth(20);
        expandButton.getStyleClass().add("render_tree_row_arrow_container");
        expandButton.setAlignment(Pos.CENTER);
        ImageView arrow = new ImageView(rightArrowImage);
        arrow.setFitWidth(arrowImageSize);
        arrow.setFitHeight(arrowImageSize);
        expandButton.getChildren().add(arrow);

        rowSelectorPane = new FlowPane();
        rowSelectorPane.getStyleClass().add("render_tree_row_content_pane");

        Text tagStart = new Text(String.format("<%s", node.type));
        tagStart.getStyleClass().add("render_tree_row_html");
        Text tagStartEnd = new Text(">");
        tagStartEnd.getStyleClass().add("render_tree_row_html");
        Text ellipses = new Text(node.text == null ? "..." : node.text);
        ellipses.getStyleClass().add("render_tree_row_ellipses");
        Text tagEnd = new Text(String.format("</%s>", node.type));
        tagEnd.getStyleClass().add("render_tree_row_html");
        List<Text> attributesText = getAttributesTextList();

        rowSelectorPane.getChildren().addAll(tagStart);
        if (attributesText.size() > 0) {
            rowSelectorPane.getChildren().addAll(attributesText);
        }
        rowSelectorPane.getChildren().addAll(tagStartEnd, ellipses, tagEnd);

        getChildren().addAll(expandButton, rowSelectorPane);
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

    private void addRowPlainText() {
        Text tagStart = new Text(String.format("<%s>", node.type));
        tagStart.getStyleClass().add("render_tree_row_html");
        Text text = new Text(node.text);
        text.getStyleClass().add("render_tree_row_ellipses");
        Text tagEnd = new Text(String.format("</%s>", node.type));
        tagEnd.getStyleClass().add("render_tree_row_html");

        getChildren().addAll(tagStart);
        if (node.text != null) {
            getChildren().addAll(text);
        }
        getChildren().addAll(tagEnd);
    }

}
