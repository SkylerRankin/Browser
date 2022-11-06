package browser.app.ui.inspector.settings;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class SettingsPanelToggle extends HBox {

    private final String toggleOffClass = "settings_toggle_value_off";

    private Label valueText;
    private boolean toggleValue = false;

    public SettingsPanelToggle(String toggleText) {
        getStyleClass().add("settings_toggle");
        Label text = new Label(toggleText);
        text.setPrefWidth(150);
        text.setAlignment(Pos.CENTER_LEFT);
        text.getStyleClass().add("settings_toggle_label");
        valueText = new Label("false");
        valueText.getStyleClass().addAll("settings_toggle_value", toggleOffClass);
        valueText.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(text, valueText);
    }

    public void setValue(boolean on) {
        valueText.setText(on ? "true" : "false");
        if (on) {
            valueText.getStyleClass().remove(toggleOffClass);
        } else {
            valueText.getStyleClass().add(toggleOffClass);
        }
    }

    public Label getButton() {
        return valueText;
    }

}
