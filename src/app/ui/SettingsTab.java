package app.ui;

import app.ui.BrowserTab.TabType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
		Insets insets = new Insets(50, 50, 50, 50);
		pane.setCenter(grid);
		BorderPane.setMargin(grid, insets);
		
		ColumnConstraints leftMargin = new ColumnConstraints();
		leftMargin.setPercentWidth(30);
		ColumnConstraints middleLeft = new ColumnConstraints();
		middleLeft.setPercentWidth(30);
		ColumnConstraints middleRight = new ColumnConstraints();
		middleRight.setPrefWidth(30);
	    ColumnConstraints rightMargin = new ColumnConstraints();
	    rightMargin.setPercentWidth(30);
	    grid.getColumnConstraints().addAll(leftMargin, middleLeft, middleRight);
	    
	    grid.setVgap(10);
		
	    
		Label l = new Label("Show layout grid");
		ToggleSwitch sw = new ToggleSwitch();
		Label l2 = new Label("Some other setting");
		ToggleSwitch sw2 = new ToggleSwitch();
		
		GridPane.setColumnIndex(l, 1);
		GridPane.setRowIndex(l, 0);
		GridPane.setColumnIndex(sw, 2);
		GridPane.setRowIndex(sw, 0);
		
		GridPane.setColumnIndex(l2, 1);
		GridPane.setRowIndex(l2, 1);
		GridPane.setColumnIndex(sw2, 2);
		GridPane.setRowIndex(sw2, 1);
		
		grid.getChildren().addAll(l, sw, l2, sw2);
		
		return pane;
		
	}

}
