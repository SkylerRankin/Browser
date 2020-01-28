package app;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class BrowserTab {
	
	public static enum TabType {SEARCH, NEW, SETTINGS};
	
	protected Tab tab;
	protected TabType type;
	protected Stage stage;
	
	public BrowserTab(TabType type, Stage stage) {
		this.type = type;
		this.stage = stage;
	}
	
	public void onResize(Stage stage) {
		
	}
	
	public TabType getType() {
		return type;
	}
	
	public Tab getActor() {
		return tab;
	}
}
