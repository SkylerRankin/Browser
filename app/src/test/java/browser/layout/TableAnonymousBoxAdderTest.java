package browser.layout;

import static org.junit.Assert.assertEquals;

import browser.app.Pipeline;
import browser.css.CSSStyle;
import browser.model.BoxNode;

import org.junit.Before;
import org.junit.Test;

public class TableAnonymousBoxAdderTest {

    private TableAnonymousBoxAdder tableAnonymousBoxAdder;

    @Before
    public void setup() {
        Pipeline.init();
        tableAnonymousBoxAdder = new TableAnonymousBoxAdder();
    }

    @Test
    public void parentOfCellIsNotRow() {
        BoxNode table = new BoxNode();
        table.innerDisplayType = CSSStyle.DisplayType.TABLE;
        BoxNode cell = new BoxNode();
        cell.innerDisplayType = CSSStyle.DisplayType.TABLE_CELL;
        table.children.add(cell);
        cell.parent = table;

        tableAnonymousBoxAdder.addAnonymousBoxes(table);
        System.out.println(table.toRecursiveString());
        assertEquals(1, table.children.size());
        assertEquals(CSSStyle.DisplayType.TABLE_ROW, table.children.get(0).innerDisplayType);
        assertEquals(1, table.children.get(0).children.size());
        assertEquals(CSSStyle.DisplayType.TABLE_CELL, table.children.get(0).children.get(0).innerDisplayType);
    }

}
