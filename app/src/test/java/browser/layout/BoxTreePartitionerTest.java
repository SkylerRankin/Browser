package browser.layout;

import static org.junit.Assert.assertEquals;

import java.util.List;

import browser.model.BoxNode;

import org.junit.Before;
import org.junit.Test;

public class BoxTreePartitionerTest {

    private BoxTreePartitioner boxTreePartitioner;

    @Before
    public void setup() {
        boxTreePartitioner = new BoxTreePartitioner();
    }

    @Test
    public void simpleDepth1Test() {
        BoxNode root = new BoxNode();
        root.id = 0;
        BoxNode node1 = new BoxNode();
        node1.id = 1;
        BoxNode node2 = new BoxNode();
        node2.id = 2;
        root.children.addAll(List.of(node1, node2));
        node1.parent = root;
        node2.parent = root;

        InlineFormattingContext context = new InlineFormattingContext(0, 0);
        boxTreePartitioner.partition(node2, context);

        assertEquals(2, root.children.size());
        assertEquals(1, root.children.get(0).id);
        assertEquals(2, root.children.get(1).id);
    }

    @Test
    public void singleSplitTest() {
        BoxNode root = new BoxNode();
        root.id = 0;
        BoxNode node11 = new BoxNode();
        node11.id = 1;
        BoxNode node12 = new BoxNode();
        node12.id = 2;
        BoxNode node21 = new BoxNode();
        node21.id = 3;
        BoxNode node22 = new BoxNode();
        node22.id = 4;
        BoxNode node23 = new BoxNode();
        node23.id = 5;

        BoxNode.nextId = 6;

        root.children.addAll(List.of(node11, node12));
        node11.children.addAll(List.of(node21, node22));
        node12.children.add(node23);

        node11.parent = root;
        node12.parent = root;
        node21.parent = node11;
        node22.parent = node11;
        node23.parent = node12;

        InlineFormattingContext context = new InlineFormattingContext(0, 0);
        boxTreePartitioner.partition(node22, context);

        assertEquals(3, root.children.size());

        assertEquals(1, root.children.get(0).id);
        assertEquals(6, root.children.get(1).id);
        assertEquals(2, root.children.get(2).id);

        assertEquals(1, root.children.get(0).children.size());
        assertEquals(3, root.children.get(0).children.get(0).id);

        assertEquals(1, root.children.get(1).children.size());
        assertEquals(4, root.children.get(1).children.get(0).id);

        assertEquals(1, root.children.get(2).children.size());
        assertEquals(5, root.children.get(2).children.get(0).id);
    }

    @Test
    public void multipleSplitTest() {
        BoxNode root = new BoxNode();
        root.id = 0;

        BoxNode node11 = new BoxNode();
        node11.id = 1;
        BoxNode node12 = new BoxNode();
        node12.id = 2;
        BoxNode node13 = new BoxNode();
        node13.id = 3;

        BoxNode node21 = new BoxNode();
        node21.id = 4;

        BoxNode node31 = new BoxNode();
        node31.id = 5;
        BoxNode node32 = new BoxNode();
        node32.id = 6;

        BoxNode node41 = new BoxNode();
        node41.id = 7;
        BoxNode node42 = new BoxNode();
        node42.id = 8;
        BoxNode node43 = new BoxNode();
        node43.id = 9;

        BoxNode node51 = new BoxNode();
        node51.id = 10;

        BoxNode.nextId = 11;

        root.children.addAll(List.of(node11, node12, node13));
        node12.children.add(node21);
        node21.children.addAll(List.of(node31, node32));
        node31.children.addAll(List.of(node41, node42, node43));
        node43.children.add(node51);

        node11.parent = root;
        node12.parent = root;
        node13.parent = root;
        node21.parent = node12;
        node31.parent = node21;
        node32.parent = node21;
        node41.parent = node31;
        node42.parent = node31;
        node43.parent = node31;
        node51.parent = node43;

        InlineFormattingContext context = new InlineFormattingContext(0, 0);
        boxTreePartitioner.partition(node51, context);

        // Depth 0
        assertEquals(4, root.children.size());
        assertEquals(1, root.children.get(0).id);
        assertEquals(2, root.children.get(1).id);
        assertEquals(13, root.children.get(2).id);
        assertEquals(3, root.children.get(3).id);

        // Depth 1
        assertEquals(0, root.children.get(0).children.size());

        assertEquals(1, root.children.get(1).children.size());
        assertEquals(4, root.children.get(1).children.get(0).id);

        assertEquals(1, root.children.get(2).children.size());
        assertEquals(12, root.children.get(2).children.get(0).id);

        assertEquals(0, root.children.get(3).children.size());

        // Depth 2
        BoxNode child10 = root.children.get(1).children.get(0);
        assertEquals(1, child10.children.size());
        assertEquals(5, child10.children.get(0).id);

        BoxNode child20 = root.children.get(2).children.get(0);
        assertEquals(2, child20.children.size());
        assertEquals(11, child20.children.get(0).id);
        assertEquals(6, child20.children.get(1).id);

        // Depth 3
        BoxNode child100 = root.children.get(1).children.get(0).children.get(0);
        assertEquals(2, child100.children.size());
        assertEquals(7, child100.children.get(0).id);
        assertEquals(8, child100.children.get(1).id);

        BoxNode child200 = root.children.get(2).children.get(0).children.get(0);
        assertEquals(1, child200.children.size());
        assertEquals(9, child200.children.get(0).id);

        BoxNode child201 = root.children.get(2).children.get(0).children.get(1);
        assertEquals(0, child201.children.size());

        // Depth 4
        BoxNode child1000 = root.children.get(1).children.get(0).children.get(0).children.get(0);
        assertEquals(0, child1000.children.size());

        BoxNode child1001 = root.children.get(1).children.get(0).children.get(0).children.get(1);
        assertEquals(0, child1001.children.size());

        BoxNode child2000 = root.children.get(2).children.get(0).children.get(0).children.get(0);
        assertEquals(1, child2000.children.size());
        assertEquals(10, child2000.children.get(0).id);
    }

    @Test
    public void rightBinaryTreeTest() {
        BoxNode root = new BoxNode();
        root.id = 0;

        BoxNode node11 = new BoxNode();
        node11.id = 1;
        BoxNode node12 = new BoxNode();
        node12.id = 2;

        BoxNode node21 = new BoxNode();
        node21.id = 3;
        BoxNode node22 = new BoxNode();
        node22.id = 4;

        BoxNode node31 = new BoxNode();
        node31.id = 5;
        BoxNode node32 = new BoxNode();
        node32.id = 6;

        BoxNode.nextId = 7;

        root.children.addAll(List.of(node11, node12));
        node12.children.addAll(List.of(node21, node22));
        node22.children.addAll(List.of(node31, node32));

        node11.parent = root;
        node12.parent = root;
        node21.parent = node12;
        node22.parent = node12;
        node31.parent = node22;
        node32.parent = node22;

        InlineFormattingContext context = new InlineFormattingContext(0, 0);
        boxTreePartitioner.partition(node32, context);

        // Depth 0
        assertEquals(3, root.children.size());
        assertEquals(1, root.children.get(0).id);
        assertEquals(2, root.children.get(1).id);
        assertEquals(8, root.children.get(2).id);

        // Depth 1
        assertEquals(0, root.children.get(0).children.size());

        assertEquals(2, root.children.get(1).children.size());
        assertEquals(3, root.children.get(1).children.get(0).id);
        assertEquals(4, root.children.get(1).children.get(1).id);

        assertEquals(1, root.children.get(2).children.size());
        assertEquals(7, root.children.get(2).children.get(0).id);

        // Depth 2
        assertEquals(0, root.children.get(1).children.get(0).children.size());

        assertEquals(1, root.children.get(1).children.get(1).children.size());
        assertEquals(5, root.children.get(1).children.get(1).children.get(0).id);

        assertEquals(1, root.children.get(2).children.get(0).children.size());
        assertEquals(6, root.children.get(2).children.get(0).children.get(0).id);
    }

}
