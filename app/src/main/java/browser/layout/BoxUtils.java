package browser.layout;

import browser.model.Box;
import browser.model.BoxNode;

public class BoxUtils {

    public static Box getBoxWithoutPadding(BoxNode node) {
        Box box = new Box();
        box.x = node.x + node.style.paddingLeft;
        box.y = node.y + node.style.paddingTop;
        box.width = node.width - node.style.paddingRight - node.style.paddingLeft;
        box.height = node.height - node.style.paddingBottom - node.style.paddingTop;
        return box;
    }

}
