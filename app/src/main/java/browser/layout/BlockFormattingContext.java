package browser.layout;

import java.util.HashMap;
import java.util.Map;

import browser.model.BoxNode;

public class BlockFormattingContext {

    Map<Integer, BoxNode> lastAddedBox = new HashMap<>();

    public void setLastPlacedBoxForId(int id, BoxNode boxNode) {
        lastAddedBox.put(id, boxNode);
    }

    public BoxNode getLastPlacedBoxForId(int id) {
        return lastAddedBox.get(id);
    }

}
