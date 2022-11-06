package browser.app.ui.inspector.elements;

import javafx.scene.control.SplitPane;

public class ElementsPanel extends SplitPane {

    private final RenderTree renderTree;

    public ElementsPanel() {
        renderTree = new RenderTree();
    }

}
