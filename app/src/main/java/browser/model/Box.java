package browser.model;

import java.util.ArrayList;
import java.util.List;

public class Box {
    
    public float x;
    public float y;
    public float width;
    public float height;
	public boolean fixedWidth = false;
	public boolean fixedHeight = false;
    
    /**
     * Convert box coordinates to global coordinates. Take a point 
     * relative to the top left corner of the rectangle and express it
     * in terms of the actual position on the screen.
     * @param p     The point relative to the box.
     * @return      A point relative to the screen.
     */
    public Vector2 localToGlobal(Vector2 p) {
        return new Vector2(p.x + this.x, p.y + this.y);
    }
    
    public List<Vector2> getCorners() {
        List<Vector2> corners = new ArrayList<>();
        corners.add(new Vector2(x, y));
        corners.add(new Vector2(x + width, y));
        corners.add(new Vector2(x, y + height));
        corners.add(new Vector2(x + width, y + height));
        return corners;
    }

}
