package browser.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import browser.app.ui.*;
import browser.app.ui.BrowserTab.TabType;
import browser.app.ui.inspector.InspectorHandler;
import browser.interaction.InteractionCallback;

public class BrowserWindow extends Application {
        
    private Scene scene;
    private Stage stage;
    private AnchorPane anchor;
    private TabPane tabPane;

    private List<BrowserTab> tabs = new ArrayList<>();
    private int currentTabIndex = 0;
    
    private boolean settingsTabOpen = false;
    private AtomicBoolean loading;
    
    private long currentTime;
    private List<Long> taskDurations;
    
    private double offsetX = 0;
    private double offsetY = 0;
    
    private InspectorHandler inspectorHandler;
    
    private void setupUI(Stage stage) {
        this.stage = stage;
        stage.setTitle("Browser");
        stage.initStyle(StageStyle.UNDECORATED);
        tabPane = new TabPane();

        addNewTab(stage, TabType.NEW);
        addNewTab(stage, TabType.SEARCH);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getId() != null && newValue.getId().equals(TabType.NEW.toString())) {
                addNewTab(stage, TabType.SEARCH);
            } else {
                for (BrowserTab tab : tabs) {
                    if (tab.getActor().equals(newValue)) {
                        if (tab instanceof SearchTab searchTab) {
                            searchTab.onRefresh();
                        }
                        break;
                    }
                }
            }
        });
        
        HBox hbox = new HBox();
        addWindowButtons(hbox, stage);
        
        anchor = new AnchorPane();
        anchor.getChildren().addAll(tabPane, hbox);
        
        AnchorPane.setTopAnchor(hbox, 3.0);
        AnchorPane.setRightAnchor(hbox, 3.0);
        
        BorderPane root = new BorderPane();
        root.setCenter(anchor);
        
        StackPane stack = new StackPane();
        stack.getChildren().addAll(root, new ResizeOverlay(stage));
        
        scene = new Scene(stack, 1500, 800);
        
        tabPane.setPrefWidth(scene.getWidth());
        tabPane.setPrefHeight(scene.getHeight());
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
        
        File windowCSSFile = new File("./src/main/resources/css/javafx_window.css");
        File inspectorCSSFile = new File("./src/main/resources/css/inspector.css");
        scene.getStylesheets().addAll(windowCSSFile.toURI().toString(), inspectorCSSFile.toURI().toString());
        
        setKeyListener(scene, stage);

        stage.setScene(scene);
        stage.show();

        for (BrowserTab tab : tabs) {
            tab.scene = scene;
            tab.onResize(stage);
        }
        
        ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
            tabPane.setPrefWidth(scene.getWidth());
            tabPane.setPrefHeight(scene.getHeight());
            AnchorPane.setTopAnchor(hbox, 3.0);
            AnchorPane.setRightAnchor(hbox, 3.0);
            Tab currentTab = tabPane.getSelectionModel().selectedItemProperty().getValue();
            for (BrowserTab tab : tabs) {
                if (tab.getActor().equals(currentTab)) {
                    tab.onResize(stage);
                    break;
                }
            }
        };
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);

        inspectorHandler = new InspectorHandler(stage);
    }

    private void addNewTab(Stage stage, TabType type) {
        String startupPageURL = "file://src/main/resources/html/startup_page.html";
        addNewTab(stage, type, startupPageURL);
    }
    
    private void addNewTab(Stage stage, TabType type, String urlToLoad) {
        BrowserTab newTab;
        if (type.equals(TabType.SETTINGS)) {
            newTab = new SettingsTab(stage);
        } else if (type.equals(TabType.SEARCH)) {
            newTab = new SearchTab(stage, getInteractionCallback());
        } else {
            newTab = new NewTab();
        }

        newTab.scene = scene;
        newTab.onResize(stage);
        setTabCloseListener(newTab);

        if (type == TabType.NEW) {
            tabs.add(newTab);
            tabPane.getTabs().add(newTab.getActor());
        } else if (type == TabType.SEARCH) {
            int newTabIndex = tabs.size() == 0 ? 0 : tabs.size() - 1;
            tabs.add(newTabIndex, newTab);
            currentTabIndex = newTabIndex;
            tabPane.getTabs().add(newTabIndex, newTab.getActor());
            tabPane.getSelectionModel().select(currentTabIndex);
            ((SearchTab) newTab).loadURL(urlToLoad);
        }
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
        KeyCodeCombination ctrlI = new KeyCodeCombination(KeyCode.I, KeyCodeCombination.CONTROL_DOWN);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (ctrlW.match(event)) {
                    System.out.println("ctrl w");
                } else if (ctrlT.match(event)) {
//                    addNewTab(stage, TabType.SEARCH);
                } else if (ctrlR.match(event)) {
                    Tab currentTab = tabPane.getSelectionModel().selectedItemProperty().getValue();
                    for (BrowserTab tab : tabs) {
                        if (tab instanceof SearchTab searchTab && searchTab.getActor().equals(currentTab)) {
                            searchTab.onRefresh();
                            break;
                        }
                    }
                } else if (ctrlTab.match(event)) {
                    System.out.println("ctrl tab");
                    event.consume();
                } else if (ctrlI.match(event)) {
                    Tab currentTab = tabPane.getSelectionModel().selectedItemProperty().getValue();
                    for (BrowserTab tab : tabs) {
                        if (tab instanceof SearchTab searchTab && searchTab.getActor().equals(currentTab)) {
                            searchTab.toggleInspector();
                            break;
                        }
                    }
                }
            }

        });
    }

    private InteractionCallback getInteractionCallback() {
        return (url, newTab) -> {
            if (newTab) {
                addNewTab(stage, TabType.SEARCH, url);
            } else {
                if (tabs.get(currentTabIndex).getType() == TabType.SEARCH) {
                    ((SearchTab) tabs.get(currentTabIndex)).loadURL(url);
                }
            }
        };
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Pipeline.init();
        setupUI(stage);
        loading = new AtomicBoolean();
        taskDurations = new ArrayList<Long>();
    }
    
}
