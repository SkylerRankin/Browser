package browser.parser.app.ui.inspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.VBox;
import browser.model.RenderNode;

public class TreeViewer extends VBox {
    
    private RenderNode root;
    private RenderNodeSummary renderNodeSummary;
    private int nextID;
    private Map<Integer, RenderNodeRow> rows;
    private Map<Integer, List<Integer>> openChildren;
    
    public TreeViewer(RenderNodeSummary renderNodeSummary) {
        openChildren = new HashMap<Integer, List<Integer>>();
        rows = new HashMap<Integer, RenderNodeRow>();
        this.renderNodeSummary = renderNodeSummary;
        this.setFillWidth(true);
    }
    
    public void update(RenderNode root) {
        this.root = root;
        collapseRow(-1);
        openChildren.clear();
        rows.clear();
        addRow(-1, root);
    }
    
    public void expandRow(int id) {
        RenderNodeRow renderNodeRow = rows.get(id);
        if (renderNodeRow == null) {
            System.out.printf("Invalid renderNodeRow id %d\n", id);
            return;
        }
        
        for (RenderNode child : renderNodeRow.getRenderNode().children) {
            addRow(id, child);
        }
        
    }
    
    public void collapseRow(int id) {
        if (openChildren.get(id) == null) return;
        for (Integer childID : openChildren.get(id)) {
            collapseRow(childID);
            if (rows.get(childID) != null) {
                getChildren().remove(rows.get(childID));
            }
        }
        openChildren.remove(id);
    }
    
    private void addRow(Integer parent, RenderNode renderNode) {
        int id = getNextID();
        int depth = rows.containsKey(parent) ? rows.get(parent).getDepth() : -1;
        RenderNodeRow node = new RenderNodeRow(id, renderNode, this, renderNodeSummary, depth + 1);
        rows.put(id, node);
        
        List<Integer> children = openChildren.get(parent);
        int index = children == null ? 
                getChildren().indexOf(rows.get(parent)) + 1 :
                getChildren().indexOf(rows.get(children.get(children.size() - 1))) + 1;
        this.getChildren().add(index, node);
        
        if (children == null) openChildren.put(parent, new ArrayList<Integer>());
        openChildren.get(parent).add(id);
    }
    
    public int getNextID() {
        return nextID++;
    }
    
}
