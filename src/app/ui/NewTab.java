package app.ui;

import app.ui.BrowserTab.TabType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class NewTab extends BrowserTab {
	
	private Tab tab;
	
	public NewTab() {
		super(TabType.NEW, null);
		tab = new Tab("+", new Label(""));
		tab.setId(TabType.NEW.toString());
		tab.setClosable(false);
	}
	
	public Tab getActor() {
		return tab;
	}

}
