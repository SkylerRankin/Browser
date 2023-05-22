package browser.app;

import java.util.ArrayList;
import java.util.List;

import browser.model.BoxNode;

public class DebugLogger {

    public static void printAllBoxNodeStyles(BoxNode start) {
        List<BoxNode> queue = new ArrayList<>();
        queue.add(start);

        while (queue.size() > 0) {
            BoxNode boxNode = queue.remove(0);
            System.out.println(boxNode.toString());
            System.out.println(boxNode.style.computedPropertiesToString());
            queue.addAll(boxNode.children);
        }
    }

}
