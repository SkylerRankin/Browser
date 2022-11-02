package browser.model;

public class Vector2 {
    
    public float x;
    public float y;
    
    public Vector2() {
        x = 0f;
        y = 0f;
    }
    
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return String.format("[%.2f, %.2f]", x, y);
    }

}
