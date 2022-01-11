package browser.parser.app.ui;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class BrowserTab {
	
	public static enum TabType {SEARCH, NEW, SETTINGS};
	public static int tabID = 0;
	
	public Scene scene;
	
	protected Tab tab;
	protected TabType type;
	protected Stage stage;
	protected int id;
	
	public BrowserTab(TabType type, Stage stage) {
		this.type = type;
		this.stage = stage;
		this.id = tabID++;
	}
	
	public void onResize(Stage stage) {
		
	}
	
	public TabType getType() {
		return type;
	}
	
	public Tab getActor() {
		return tab;
	}
	
	public int id() { return id; }
}
