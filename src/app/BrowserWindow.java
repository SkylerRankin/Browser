package app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import app.ui.BrowserTab;
import app.ui.Footer;
import app.ui.NewTab;
import app.ui.ResizeButton;
import app.ui.ResizeOverlay;
import app.ui.SearchTab;
import app.ui.SettingsButton;
import app.ui.SettingsTab;
import app.ui.BrowserTab.TabType;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tasks.CalculateLayoutTask;
import tasks.LoadWebpageTask;
import tasks.LoadingAnimationTask;
import tasks.RenderWebpageTask;

public class BrowserWindow extends Application {
        
	private Scene scene;
	
    private AnchorPane anchor;
    private TabPane tabPane;
    
    private Footer footer;
    
    private List<BrowserTab> tabs;
    private int currentTab;
    
    private boolean settingsTabOpen;
    private AtomicBoolean loading;
    
    private long currentTime;
    private List<Long> taskDurations;
    
    private double offsetX = 0;
    private double offsetY = 0;
    
    private void setupUI(Stage stage) {
        stage.setTitle("Browser");
        stage.initStyle(StageStyle.UNDECORATED);
        tabs = new ArrayList<BrowserTab>();
        currentTab = 0;
        settingsTabOpen = false;
        
        SearchTab startingTab = new SearchTab(stage);
        NewTab newTab = new NewTab();
        
        setTabCloseListener(startingTab);
        setTabCloseListener(newTab);
        
        tabs.add(startingTab);
        tabs.add(newTab);
        
        tabPane = new TabPane();
        tabPane.getTabs().add(tabs.get(currentTab).getActor());
        tabPane.getTabs().add(tabs.get(1).getActor());

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
				if (newValue.getId() != null && newValue.getId().equals(TabType.NEW.toString())) {
					addNewTab(stage, TabType.SEARCH);
				}
			}
        });
        
        SettingsButton settingsButton = new SettingsButton();
        setSettingsButtonListener(settingsButton, stage);
        
        HBox hbox = new HBox();
        hbox.getChildren().addAll(settingsButton);
        addWindowButtons(hbox, stage);
        
        anchor = new AnchorPane();
        anchor.getChildren().addAll(tabPane, hbox);
        
        AnchorPane.setTopAnchor(hbox, 3.0);
        AnchorPane.setRightAnchor(hbox, 3.0);
        
        BorderPane root = new BorderPane();
        root.setCenter(anchor);
        
        footer = new Footer();
        
        root.setBottom(footer);
        
        StackPane stack = new StackPane();
        stack.getChildren().addAll(root, new ResizeOverlay(stage));
        
        scene = new Scene(stack, 1000, 600);
        
        tabPane.setPrefWidth(scene.getWidth());
        tabPane.setPrefHeight(scene.getHeight() - 20);
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        
        // Prevent swiping from making new tabs
        tabPane.addEventFilter(SwipeEvent.ANY, new EventHandler<SwipeEvent>() {
			@Override
			public void handle(SwipeEvent event) {
				System.out.println("swipe");
				event.consume();
			}
        });
        
        tabPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                offsetX = stage.getX() - event.getScreenX();
                offsetY = stage.getY() - event.getScreenY();
            }
        });
        
        tabPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() + offsetX);
                stage.setY(event.getScreenY() + offsetY);
            }
        });
        
        File cssFile = new File("./res/css/javafx_window.css");
        String path = cssFile.toURI().toString();
        scene.getStylesheets().add(path);
        
        setKeyListener(scene, stage);

        stage.setScene(scene);
        stage.show();
        
        
        for (BrowserTab tab : tabs) {
        	tab.scene = scene;
        	tab.onResize(stage);
        }
        
        ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
        	tabPane.setPrefWidth(scene.getWidth());
        	tabPane.setPrefHeight(scene.getHeight() - 20);
        	footer.setPrefWidth(scene.getWidth());
        	AnchorPane.setTopAnchor(hbox, 3.0);
            AnchorPane.setRightAnchor(hbox, 3.0);
        	for (BrowserTab tab : tabs) {
        		tab.onResize(stage);
        	}
//            statusLabel.setText(String.valueOf(stage.getWidth()));
//            urlInput.setPrefWidth(stage.getWidth() - searchButton.getWidth() - statusLabel.getWidth() - 20);
//            canvas.setWidth(stage.getWidth());
//            canvas.setHeight(stage.getHeight() - searchButton.getHeight());
        };
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener); 
    }
    
    private void addNewTab(Stage stage, TabType type) {
    	BrowserTab newTab;
    	if (type.equals(TabType.SETTINGS)) {
    		newTab = new SettingsTab(stage);
    	} else {
    		newTab = new SearchTab(stage);
    	}
    	newTab.scene = scene;
    	newTab.onResize(stage);
    	setTabCloseListener(newTab);
    	tabs.add(tabs.size() - 1, newTab);
    	currentTab = tabs.size() - 2;
    	tabPane.getTabs().add(tabs.size() - 2, newTab.getActor());
    	tabPane.getSelectionModel().select(currentTab);
    }
    
    private void setTabCloseListener(BrowserTab tab) {
    	tab.getActor().setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				tabs.remove(tabs.indexOf(tab));
				if (tab.getType().equals(TabType.SETTINGS)) {
					settingsTabOpen = false;
				}
			}
    	});
    }
    
    private void addWindowButtons(HBox hbox, Stage stage) {
        Button closeButton = createImageButton("./res/images/browser_close_16.png", "close-button");
        Button windowedButton = createImageButton("./res/images/browser_maximize_16.png", "window-bar-button");
        Button minimizeButton = createImageButton("./res/images/browser_minimize_16.png", "window-bar-button");
        
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                System.exit(0);
            }
        });
        
        minimizeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.setIconified(true);
            }
        });
        
        hbox.getChildren().addAll(minimizeButton, windowedButton, closeButton);

    }
    
    private Button createImageButton(String imagePath, String styleClass) {
        Button button = new Button();
        File iconFile = new File(imagePath);
        ImageView image = new ImageView(new Image(iconFile.toURI().toString(), 256, 256, false, false));
        image.setFitHeight(16);
        image.setFitWidth(16);
        button.getStyleClass().add(styleClass);
        button.setGraphic(image);
        button.setPrefWidth(50);
        button.setPadding(new Insets(5, 5, 5, 5));
        return button;
    }

    private void setSettingsButtonListener(Button button, Stage stage) {
    	button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!settingsTabOpen) {
					addNewTab(stage, TabType.SETTINGS);
					settingsTabOpen = true;
				} else {
					for (int i = 0; i < tabs.size(); i++) {
						if (tabs.get(i).getType().equals(TabType.SETTINGS)) {
							tabPane.getSelectionModel().select(i);
							break;
						}
					}
				}
			}
    		
    	});
    }
    
    private void setKeyListener(Scene scene, Stage stage) {
    	KeyCodeCombination ctrlW = new KeyCodeCombination(KeyCode.W, KeyCodeCombination.CONTROL_DOWN);
    	KeyCodeCombination ctrlT = new KeyCodeCombination(KeyCode.T, KeyCodeCombination.CONTROL_DOWN);
    	KeyCodeCombination ctrlR = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);
    	KeyCodeCombination ctrlTab = new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.CONTROL_DOWN);
    	
    	scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (ctrlW.match(event)) {
					System.out.println("ctrl w");
				} else if (ctrlT.match(event)) {
//					addNewTab(stage, TabType.SEARCH);
				} else if (ctrlR.match(event)) {
					System.out.println("ctrl r");
				} else if (ctrlTab.match(event)) {
					System.out.println("ctrl tab");
					event.consume();
				}
			}
    		
    	});
    }
    
    private void startLoadWebpageTask() {
//        currentTime = System.nanoTime();
//        LoadWebpageTask lwt = new LoadWebpageTask(urlInput.getText());
//        lwt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent event) {
//                System.out.println(lwt.getValue());
//                recordTimeDuration();
//                startCalculateLayoutsTask();
//            }
//        });
//        new Thread(lwt).start();
    }
    
    private void startCalculateLayoutsTask() {
        CalculateLayoutTask clt = new CalculateLayoutTask();
        clt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                recordTimeDuration();
                startRenderWebpageTask();
            }
        });
        new Thread(clt).start();
    }
    
    private void startRenderWebpageTask() {
        RenderWebpageTask rwt = new RenderWebpageTask();
        rwt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loading.set(false);
//                statusLabel.setText("Loaded");
                recordTimeDuration();
                String[] labels = {"Fetch", "Layout", "Render"};
                for (int i = 0; i < 3; ++i) {
                    System.out.printf("%6s: %.3fs\n", labels[i], taskDurations.get(i) / Math.pow(10, 9));
                }
            }
        });
        new Thread(rwt).start();
    }
    
//    private void registerSearchListener(int tab) {
//        tabs.get(tab).getSearchButton().setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                String url = tabs.get(tab).getURLInput().getText();
//                if (url.length() > 0) {
//                    loading.set(true);
////                    statusLabel.setText("Loading   ");
//                    taskDurations.clear();
//                    startLoadWebpageTask();
////                    LoadingAnimationTask lat = new LoadingAnimationTask(loading, statusLabel);
////                    new Thread(lat).start();
//                }
//            }
//            
//        });
//    }
//    
//    private void registerURLBarListener(int tab) {
//        tabs.get(tab).getURLInput().setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event) {
//                if (event.getCode().equals(KeyCode.ENTER)) {
//                    tabs.get(tab).getSearchButton().fire();
//                }
//            }
//        });
//    }
    
    private void recordTimeDuration() {
        long now = System.nanoTime();
        taskDurations.add(now - currentTime);
        currentTime = now;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        setupUI(stage);
//        registerSearchListener(0);
//        registerURLBarListener(0);
        loading = new AtomicBoolean();
        taskDurations = new ArrayList<Long>();
    }
    
}
