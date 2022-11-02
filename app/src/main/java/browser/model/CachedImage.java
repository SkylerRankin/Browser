package browser.model;

import javafx.scene.image.Image;

public class CachedImage {

    private Image image;
    private long lastAccessed;
    private long size;

    public CachedImage(Image image) {
        this.image = image;
    }

}
