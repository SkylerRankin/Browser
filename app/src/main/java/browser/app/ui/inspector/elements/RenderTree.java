package browser.app.ui.inspector.elements;

import java.util.*;

import javafx.geometry.Insets;
import javafx.scene.Node;
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
    private final RenderNodeSelectedCallback nodeSelectedCallback;
    private RenderNode lastRenderNode;
    private SearchTabPipeline pipeline;
    private RenderNode selectedRenderNode;

    public RenderTree(RenderNodeSelectedCallback nodeSelectedCallback) {
        this.nodeSelectedCallback = nodeSelectedCallback;
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
        if (expandedNodes.size() == 0) {
            addStartingExpandedIDs(root);
        }
        addNodeAndChildren(root);
        requestLayout();
    }

    public void setSelectedRenderNode(RenderNode node) {
        selectedRenderNode = node;
        int selectedId = node == null ? -1 : node.id;
        for (Node childNode : vbox.getChildren()) {
            if (childNode instanceof RenderTreeRow row) {
                if (row.getRenderNode().id == selectedId) {
                    row.getStyleClass().remove("render_tree_row_hovered");
                    row.getStyleClass().add("render_tree_row_selected");
                } else {
                    row.getStyleClass().remove("render_tree_row_selected");
                }
            }
        }
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
        boolean isSelected = selectedRenderNode != null && selectedRenderNode.id == node.id;
        if (node.type.equals(HTMLElements.TEXT)) {
            RenderTreeRow row = new RenderTreeRow(node, RenderTreeRow.RowMode.Text, isSelected);
            addRowEventHandlers(row);
            vbox.getChildren().add(row);
        } else if (expandedNodes.contains(node.id)) {
            RenderTreeRow rowStart = new RenderTreeRow(node, RenderTreeRow.RowMode.Start, isSelected);
            addRowEventHandlers(rowStart);
            vbox.getChildren().add(rowStart);

            for (RenderNode child : node.children) {
                addNodeAndChildren(child);
            }

            RenderTreeRow rowEnd = new RenderTreeRow(node, RenderTreeRow.RowMode.End, isSelected);
            addRowEventHandlers(rowEnd);
            vbox.getChildren().add(rowEnd);

            rowStartIdToRowEnd.put(rowStart.getRenderNode().id, rowEnd);
            rowEndIdToRowStart.put(rowEnd.getRenderNode().id, rowStart);
        } else {
            RenderTreeRow row = new RenderTreeRow(node, RenderTreeRow.RowMode.Collapsed, isSelected);
            addRowEventHandlers(row);
            vbox.getChildren().add(row);
        }
    }

    private void addRowEventHandlers(RenderTreeRow row) {
        RenderTreeRow.RowMode mode = row.getMode();
        if (mode == RenderTreeRow.RowMode.Start && !row.getRenderNode().type.equals(HTMLElements.TEXT)) {
            row.getCollapseButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
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
                refresh();
            });
        } else if (mode == RenderTreeRow.RowMode.Collapsed && !row.getRenderNode().type.equals(HTMLElements.TEXT)) {
            row.getExpandButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                int id = row.getRenderNode().id;
                expandedNodes.add(id);
                refresh();
            });
        }

        if (mode == RenderTreeRow.RowMode.Start || mode == RenderTreeRow.RowMode.Collapsed) {
            row.getRowSelector().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                nodeSelectedCallback.onNodeSelected(row.getRenderNode());
            });
        }

        row.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (selectedRenderNode == null || selectedRenderNode.id != row.getRenderNode().id) {
                row.getStyleClass().add(rowHoveredClass);
                if (mode == RenderTreeRow.RowMode.Start) {
                    RenderTreeRow endRow = rowStartIdToRowEnd.get(row.getRenderNode().id);
                    endRow.getStyleClass().add(rowHoveredClass);
                } else if (mode == RenderTreeRow.RowMode.End) {
                    RenderTreeRow startRow = rowEndIdToRowStart.get(row.getRenderNode().id);
                    startRow.getStyleClass().add(rowHoveredClass);
                }
            }

            if (RenderSettings.hoveredElementID != row.getRenderNode().id) {
                RenderSettings.hoveredElementID = row.getRenderNode().id;
                pipeline.redrawWebpage(RenderCompleteCallback.RenderType.InspectorUpdate);
            }
        });

        row.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (selectedRenderNode == null || selectedRenderNode.id != row.getRenderNode().id) {
                row.getStyleClass().remove(rowHoveredClass);
                if (mode == RenderTreeRow.RowMode.Start) {
                    RenderTreeRow endRow = rowStartIdToRowEnd.get(row.getRenderNode().id);
                    endRow.getStyleClass().remove(rowHoveredClass);
                } else if (mode == RenderTreeRow.RowMode.End) {
                    RenderTreeRow startRow = rowEndIdToRowStart.get(row.getRenderNode().id);
                    startRow.getStyleClass().remove(rowHoveredClass);
                }
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
