package app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tasks.CalculateLayoutTask;
import tasks.LoadWebpageTask;
import tasks.LoadingAnimationTask;
import tasks.RenderWebpageTask;

public class BrowserWindow extends Application {
    
    private GraphicsContext gc;
    private Label statusLabel;
    private Button searchButton;
    private TextField urlInput;
    
    private AtomicBoolean loading;
    
    private long currentTime;
    private List<Long> taskDurations;
    
    private void setupUI(Stage stage) {
        stage.setTitle("Browser");
        
        GridPane grid = new GridPane();
        
        searchButton = new Button();
        searchButton.setText("Search");
        
        urlInput = new TextField();
        
        statusLabel = new Label("Loading  ");
        
        Canvas canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(canvas);
        
        grid.add(searchButton, 0, 0);
        grid.add(urlInput, 1, 0);
        grid.add(statusLabel, 2, 0);
        grid.add(scroll, 0, 1, 3, 1);
        
        Scene scene = new Scene(grid, 800, 600);
        scene.getStylesheets().add("app//style.css");

        stage.setScene(scene);
        stage.show();
        
        scroll.setPrefSize(800, stage.getHeight() - searchButton.getHeight());
        urlInput.setPrefWidth(stage.getWidth() - searchButton.getWidth() - statusLabel.getWidth() - 20);
        canvas.setWidth(stage.getWidth());
        canvas.setHeight(stage.getHeight() - searchButton.getHeight());
        
        gc.strokeLine(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFont(new Font("Arial", 40));
        gc.fillText("Google", 10, 100);
        
        ChangeListener<Number> stageSizeListener = (obs, oldValue, newValue) -> {
            statusLabel.setText(String.valueOf(stage.getWidth()));
            urlInput.setPrefWidth(stage.getWidth() - searchButton.getWidth() - statusLabel.getWidth() - 20);
            canvas.setWidth(stage.getWidth());
            canvas.setHeight(stage.getHeight() - searchButton.getHeight());
        };
            

        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener); 
    }

    private void startLoadWebpageTask() {
        currentTime = System.nanoTime();
        LoadWebpageTask lwt = new LoadWebpageTask(urlInput.getText());
        lwt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println(lwt.getValue());
                recordTimeDuration();
                startCalculateLayoutsTask();
            }
        });
        new Thread(lwt).start();
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
                statusLabel.setText("Loaded");
                recordTimeDuration();
                String[] labels = {"Fetch", "Layout", "Render"};
                for (int i = 0; i < 3; ++i) {
                    System.out.printf("%6s: %.3fs\n", labels[i], taskDurations.get(i) / Math.pow(10, 9));
                }
            }
        });
        new Thread(rwt).start();
    }
    
    private void registerSearchListener() {
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String url = urlInput.getText();
                if (url.length() > 0) {
                    loading.set(true);
                    statusLabel.setText("Loading   ");
                    taskDurations.clear();
                    startLoadWebpageTask();
                    LoadingAnimationTask lat = new LoadingAnimationTask(loading, statusLabel);
                    new Thread(lat).start();
                }
            }
            
        });
    }
    
    private void registerURLBarListener() {
        urlInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    searchButton.fire();
                }
            }
        });
    }
    
    private void recordTimeDuration() {
        long now = System.nanoTime();
        taskDurations.add(now - currentTime);
        currentTime = now;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        setupUI(stage);
        registerSearchListener();
        registerURLBarListener();
        loading = new AtomicBoolean();
        taskDurations = new ArrayList<Long>();
    }
    
}
