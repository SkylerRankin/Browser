package browser.app.ui.inspector;

import browser.app.ui.inspector.elementdetails.RenderNodeDetailsPanel;
import browser.app.ui.inspector.elements.RenderNodeSelectedCallback;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import browser.app.SearchTabPipeline;
import browser.app.ui.inspector.elements.RenderTree;
import browser.app.ui.inspector.settings.SettingsPanel;
import browser.model.RenderNode;

public class InspectorPanel extends TabPane {

    private final RenderTree renderTree;
    private final RenderNodeDetailsPanel renderNodeDetailsPanel;
    private final SettingsPanel settingsPanel;
    private RenderNode selectedRenderNode = null;

    public InspectorPanel() {
        setMinWidth(200);
        setMaxWidth(800);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getStyleClass().clear();
        getStyleClass().add("inspector_container");

        Tab elementsTab = new Tab();
        elementsTab.setText("Elements");
        elementsTab.getStyleClass().addAll("inspector_tab", "inspector_tab_selected");
        SplitPane elementsSplitPane = new SplitPane();
        elementsSplitPane.getStyleClass().add("inspector_elements_split_pane");
        elementsSplitPane.setOrientation(Orientation.VERTICAL);

        renderNodeDetailsPanel = new RenderNodeDetailsPanel();
        renderTree = new RenderTree(getRenderNodeSelectedHandler());
        elementsSplitPane.getItems().addAll(renderTree, renderNodeDetailsPanel);
        elementsTab.setContent(elementsSplitPane);

        Tab performanceTab = new Tab();
        performanceTab.getStyleClass().add("inspector_tab");
        performanceTab.setText("Performance");
        performanceTab.setContent(new Text("Not implemented"));

        Tab settingsTab = new Tab();
        settingsTab.getStyleClass().add("inspector_tab");
        settingsTab.setText("Settings");
        settingsPanel = new SettingsPanel();
        settingsTab.setContent(settingsPanel);

        getTabs().addAll(elementsTab, performanceTab, settingsTab);

        getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            if (oldTab != null) {
                oldTab.getStyleClass().remove("inspector_tab_selected");
            }
            newTab.getStyleClass().add("inspector_tab_selected");
        });
    }

    public void updateRenderTree(RenderNode root) {
        renderTree.update(root);
        requestLayout();
    }

    /**
     * Called before the inspector panel is removed from view.
     */
    public void preClose() {
        renderNodeDetailsPanel.update(null);
        renderTree.setSelectedRenderNode(null);
    }

    /**
     * Called when a render node is clicked in the render tree panel. This updates the render node details panel
     * with the selected node.
     */
    public RenderNodeSelectedCallback getRenderNodeSelectedHandler() {
        return (node -> {
            renderNodeDetailsPanel.update(node);
            renderTree.setSelectedRenderNode(node);
        });
    }

    public void setPipeline(SearchTabPipeline pipeline) {
        settingsPanel.setPipeline(pipeline);
        renderTree.setPipeline(pipeline);
    }
}
