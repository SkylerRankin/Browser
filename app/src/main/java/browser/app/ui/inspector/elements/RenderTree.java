package browser.app.ui.inspector.elements;

import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import browser.app.SearchTabPipeline;
import browser.model.RenderNode;
import browser.parser.HTMLElements;
import browser.renderer.RenderSettings;
import browser.tasks.RenderCompleteCallback;

public class RenderTree extends ScrollPane {

    // Render nodes within this depth are expanded to start with.
    private final int maxStartingDepth = 2;
    private final String rowHoveredClass = "render_tree_row_hovered";

    private final VBox vbox;
    private final Set<Integer> expandedNodes = new HashSet<>();
    private final Map<Integer, RenderTreeRow> rowStartIdToRowEnd = new HashMap<>();
    private final Map<Integer, RenderTreeRow> rowEndIdToRowStart = new HashMap<>();
    private RenderNode lastRenderNode;
    private SearchTabPipeline pipeline;

    public RenderTree() {
        setFitToWidth(true);
        setFitToHeight(true);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPadding(new Insets(10, 0, 0, 0));
        vbox = new VBox();
        setContent(vbox);
        getStyleClass().add("render_tree");
    }

    public void update(RenderNode root) {
        lastRenderNode = root;
        vbox.getChildren().clear();
        rowStartIdToRowEnd.clear();
        rowEndIdToRowStart.clear();
        expandedNodes.clear();
        addStartingExpandedIDs(root);
        addNodeAndChildren(root);
        requestLayout();
    }

    private void refresh() {
        vbox.getChildren().clear();
        rowStartIdToRowEnd.clear();
        rowEndIdToRowStart.clear();
        addNodeAndChildren(lastRenderNode);
    }

    private void addStartingExpandedIDs(RenderNode root) {
        List<RenderNode> nodes = new ArrayList<>();
        nodes.add(root);
        while (!nodes.isEmpty()) {
            RenderNode node = nodes.remove(nodes.size() - 1);
            if (node.depth < maxStartingDepth) {
                expandedNodes.add(node.id);
                nodes.addAll(node.children);
            } else if (node.depth == maxStartingDepth) {
                expandedNodes.add(node.id);
            }
        }
    }

    private void addNodeAndChildren(RenderNode node) {
        if (node.type.equals(HTMLElements.TEXT)) {
            RenderTreeRow row = new RenderTreeRow(node, RenderTreeRow.RowMode.Text);
            addRowEventHandlers(row);
            vbox.getChildren().add(row);
        } else if (expandedNodes.contains(node.id)) {
            RenderTreeRow rowStart = new RenderTreeRow(node, RenderTreeRow.RowMode.Start);
            addRowEventHandlers(rowStart);
            vbox.getChildren().add(rowStart);

            for (RenderNode child : node.children) {
                addNodeAndChildren(child);
            }

            RenderTreeRow rowEnd = new RenderTreeRow(node, RenderTreeRow.RowMode.End);
            addRowEventHandlers(rowEnd);
            vbox.getChildren().add(rowEnd);

            rowStartIdToRowEnd.put(rowStart.getRenderNode().id, rowEnd);
            rowEndIdToRowStart.put(rowEnd.getRenderNode().id, rowStart);
        } else {
            RenderTreeRow row = new RenderTreeRow(node, RenderTreeRow.RowMode.Collapsed);
            addRowEventHandlers(row);
            vbox.getChildren().add(row);
        }
    }

    private void addRowEventHandlers(RenderTreeRow row) {
        RenderTreeRow.RowMode mode = row.getMode();
        if (mode != RenderTreeRow.RowMode.End && !row.getRenderNode().type.equals(HTMLElements.TEXT)) {
            row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int id = row.getRenderNode().id;
                if (expandedNodes.contains(id)) {
                    // Remove this id and all child node ids from the expanded nodes set.
                    List<RenderNode> nodes = new ArrayList<>();
                    nodes.add(row.getRenderNode());
                    while (!nodes.isEmpty()) {
                        RenderNode node = nodes.remove(nodes.size() - 1);
                        int childID = node.id;
                        if (expandedNodes.contains(childID)) {
                            expandedNodes.remove(childID);
                            nodes.addAll(node.children);
                        }
                    }
                } else {
                    expandedNodes.add(id);
                }
                refresh();
            });
        }

        row.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            row.getStyleClass().add(rowHoveredClass);
            if (mode == RenderTreeRow.RowMode.Start) {
                RenderTreeRow endRow = rowStartIdToRowEnd.get(row.getRenderNode().id);
                endRow.getStyleClass().add(rowHoveredClass);
            } else if (mode == RenderTreeRow.RowMode.End) {
                RenderTreeRow startRow = rowEndIdToRowStart.get(row.getRenderNode().id);
                startRow.getStyleClass().add(rowHoveredClass);
            }

            if (RenderSettings.hoveredElementID != row.getRenderNode().id) {
                RenderSettings.hoveredElementID = row.getRenderNode().id;
                pipeline.redrawWebpage(RenderCompleteCallback.RenderType.InspectorUpdate);
            }
        });

        row.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            row.getStyleClass().remove(rowHoveredClass);
            if (mode == RenderTreeRow.RowMode.Start) {
                RenderTreeRow endRow = rowStartIdToRowEnd.get(row.getRenderNode().id);
                endRow.getStyleClass().remove(rowHoveredClass);
            } else if (mode == RenderTreeRow.RowMode.End) {
                RenderTreeRow startRow = rowEndIdToRowStart.get(row.getRenderNode().id);
                startRow.getStyleClass().remove(rowHoveredClass);
            }

            if (RenderSettings.hoveredElementID == row.getRenderNode().id) {
                RenderSettings.hoveredElementID = -1;
                pipeline.redrawWebpage(RenderCompleteCallback.RenderType.InspectorUpdate);
            }
        });
    }

    public void setPipeline(SearchTabPipeline pipeline) {
        this.pipeline = pipeline;
    }

}
