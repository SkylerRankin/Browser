package browser.parser.app.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;

public class NewTab extends BrowserTab {
	
	private Tab tab;
	
	public NewTab() {
		super(TabType.NEW, null);
		tab = new Tab("+", new Label(""));
		tab.setId(TabType.NEW.toString());
		tab.setClosable(false);
	    tab.getStyleClass().add("new_tab");
	}
	
	public Tab getActor() {
		return tab;
	}

}
