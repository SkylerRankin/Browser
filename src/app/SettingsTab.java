package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SettingsTab extends BrowserTab {

	public SettingsTab(Stage stage) {
		super(TabType.SETTINGS, stage);
		tab = new Tab("Settings", getUI());
		tab.setId(TabType.SETTINGS.toString());
	}
	
	public BorderPane getUI() {
		
		BorderPane pane = new BorderPane();
		GridPane grid = new GridPane();
		Insets top = new Insets(50);
		Insets side = new Insets(150);
		pane.setCenter(grid);
		BorderPane.setMargin(grid, top);
		
		
		
		ColumnConstraints column1 = new ColumnConstraints();
	    column1.setPercentWidth(25);
	    ColumnConstraints column2 = new ColumnConstraints();
	    column2.setPercentWidth(25);
	    grid.getColumnConstraints().addAll(column1, column2); // each get 50% of width
		
		Label l = new Label("Show layout grid");
		ToggleSwitch sw = new ToggleSwitch();
		
		GridPane.setColumnIndex(l, 0);
		GridPane.setRowIndex(l, 0);
		GridPane.setColumnIndex(sw, 1);
		GridPane.setRowIndex(sw, 0);
		
		grid.getChildren().addAll(l, sw);
		
		return pane;
		
	}

}
