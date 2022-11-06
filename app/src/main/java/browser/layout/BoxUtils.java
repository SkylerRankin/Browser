package browser.layout;

import browser.model.Box;
import browser.model.RenderNode;

public class BoxUtils {

    public static Box getBoxWithoutPadding(RenderNode node) {
        Box box = new Box();
        box.x = node.box.x + node.style.paddingLeft;
        box.y = node.box.y + node.style.paddingTop;
        box.width = node.box.width - node.style.paddingRight - node.style.paddingLeft;
        box.height = node.box.height - node.style.paddingBottom - node.style.paddingTop;
        return box;
    }

}
