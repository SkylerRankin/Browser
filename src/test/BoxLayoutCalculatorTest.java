package test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import javafx.embed.swing.JFXPanel;
import layout.BoxLayoutCalculator;
import model.RenderNode;

public class BoxLayoutCalculatorTest {
	
	private RenderNode root;
	private Map<Integer, RenderNode> parentMap;
	
	@Before
	public void before() {
		JFXPanel jfxPanel = new JFXPanel();
	}
	
	private void createSimpleRenderTree() {
		
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div = new RenderNode("div");
		RenderNode p1 = new RenderNode("p");
		RenderNode p2 = new RenderNode("p");
		
		root.id = 0;	root.depth = 0;
		h1.id = 1;		h1.depth = 1;
		div.id = 2;		div.depth = 1;
		p1.id = 3;		p1.depth = 2;
		p2.id = 4;		p2.depth = 2;
		
		h1.text = "A Test";
		p1.text = "The first paragraph.";
		p2.text = "The second, much longer, paragraph.";
		
		root.children.add(h1);
		root.children.add(div);
		div.children.add(p1);
		div.children.add(p2);
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, div);
		parentMap.put(4, div);
	}
	
	@Test
	public void setBoxBoundsTest_simple() {
		createSimpleRenderTree();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.setBoxBounds(root);
//		blc.printBoxes(root);
	}
	
	@Test
	public void calculateBoxesTest_simple() {
		createSimpleRenderTree();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.setBoxBounds(root);
		blc.printBoxes(root);
		blc.calculateBoxes(root);
		blc.printBoxes(root);
	}

}
