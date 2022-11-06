package browser.app.ui.inspector;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import browser.app.SearchTabPipeline;
import browser.app.ui.inspector.elements.RenderTree;
import browser.app.ui.inspector.settings.SettingsPanel;
import browser.model.RenderNode;

public class InspectorPanel extends TabPane {

    private final RenderTree renderTree;
    private final SettingsPanel settingsPanel;

    public InspectorPanel() {
        setMinWidth(200);
        setMaxWidth(800);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getStyleClass().clear();
        getStyleClass().add("inspector_container");

        Tab elementsTab = new Tab();
        elementsTab.getStyleClass().addAll("inspector_tab", "inspector_tab_selected");
        renderTree = new RenderTree();
        elementsTab.setText("Elements");
        elementsTab.setContent(renderTree);

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
    }

    public void setPipeline(SearchTabPipeline pipeline) {
        settingsPanel.setPipeline(pipeline);
        renderTree.setPipeline(pipeline);
    }
}
