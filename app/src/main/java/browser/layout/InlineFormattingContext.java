package browser.layout;

import java.util.ArrayList;
import java.util.List;

import browser.model.BoxNode;

public class InlineFormattingContext {

    public final float width;
    public final int contextRootId;
    public float startX;
    public float endX;

    private final List<LineBox> lineBoxes;
    private final List<Float> maxHeightPerLine;
    private final List<Float> yStartPerLine;

    public InlineFormattingContext(float width, int rootId) {
        this.width = width;
        this.contextRootId = rootId;
        lineBoxes = new ArrayList<>();
        maxHeightPerLine = new ArrayList<>();
        yStartPerLine = new ArrayList<>();
    }

    public void initialize(BoxNode rootBox) {
        startX = rootBox.x;
        endX = startX + rootBox.width;
        yStartPerLine.add(rootBox.y);
        maxHeightPerLine.add(0f);
        lineBoxes.add(new LineBox(width));
    }

    public float getCurrentLineYStart() {
        return yStartPerLine.get(yStartPerLine.size() - 1);
    }

    public void addBoxToCurrentLine(BoxNode boxNode) {
        lastLineBox().boxes.add(boxNode);
        if (maxHeightPerLine.get(maxHeightPerLine.size() - 1) < boxNode.height) {
            maxHeightPerLine.set(maxHeightPerLine.size() - 1, boxNode.height);
        }
    }

    public void moveToNextLine() {
        float lastYStart = yStartPerLine.get(yStartPerLine.size() - 1);
        float lastMaxHeight = maxHeightPerLine.get(maxHeightPerLine.size() - 1);
        yStartPerLine.add(lastYStart + lastMaxHeight);
        maxHeightPerLine.add(0f);
        lineBoxes.add(new LineBox(width));
    }

    private LineBox lastLineBox() {
        return lineBoxes.get(lineBoxes.size() - 1);
    }

    public static class LineBox {
        public final List<BoxNode> boxes = new ArrayList<>();
        public final float width;

        public LineBox(float width) {
            this.width = width;
        }
    }

}
