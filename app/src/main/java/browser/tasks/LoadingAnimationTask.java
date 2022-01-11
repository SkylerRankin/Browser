package browser.tasks;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class LoadingAnimationTask extends Task<Object> {
    
    private AtomicBoolean loading;
    private Label statusLabel;
    
    public LoadingAnimationTask(AtomicBoolean loading, final Label statusLabel) {
        this.loading = loading;
        this.statusLabel = statusLabel;
    }

    @Override
    protected Object call() throws Exception {
        while(true) {
            if (loading.get()) {
                String s = statusLabel.getText();
                Platform.runLater(() -> {
                    statusLabel.setText(s.substring(1) + s.substring(0, 1));
                });
                Thread.sleep(100);
            } else {
                break;
            }
        }
        return null;
    }

}
