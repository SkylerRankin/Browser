package browser.app.ui.inspector.settings;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import browser.app.SearchTabPipeline;
import browser.renderer.RenderSettings;

public class SettingsPanel extends VBox {

    private SearchTabPipeline pipeline;

    public SettingsPanel() {
        getStyleClass().add("settings_panel");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        getChildren().add(scrollPane);

        VBox vbox = new VBox();

        SettingsPanelToggle outlinesToggle = new SettingsPanelToggle("Show box outlines");
        outlinesToggle.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            RenderSettings.renderOutlines = !RenderSettings.renderOutlines;
            outlinesToggle.setValue(RenderSettings.renderOutlines);
            pipeline.redrawWebpage();
        });

        SettingsPanelToggle marginsToggle = new SettingsPanelToggle("Show margins");
        marginsToggle.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            RenderSettings.renderMargins = !RenderSettings.renderMargins;
            marginsToggle.setValue(RenderSettings.renderMargins);
            pipeline.redrawWebpage();
        });

        SettingsPanelToggle paddingToggle = new SettingsPanelToggle("Show padding");
        paddingToggle.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            RenderSettings.renderPadding = !RenderSettings.renderPadding;
            paddingToggle.setValue(RenderSettings.renderPadding);
            pipeline.redrawWebpage();
        });

        vbox.getChildren().addAll(outlinesToggle, marginsToggle, paddingToggle);
        scrollPane.setContent(vbox);
    }

    public void setPipeline(SearchTabPipeline pipeline) {
        this.pipeline = pipeline;
    }

}
