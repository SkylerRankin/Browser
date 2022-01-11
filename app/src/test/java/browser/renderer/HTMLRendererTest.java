package browser.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import browser.test.RenderTestCanvas;
import org.junit.Test;

import browser.parser.app.Pipeline;
import browser.css.CSSStyle;
import browser.css.CSSLoader;
import browser.css.DefaultColors;
import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import browser.layout.BoxLayoutCalculator;
import browser.model.CSSColor;
import browser.model.RenderNode;
import browser.parser.RenderTreeGenerator;

public class HTMLRendererTest {

    private static Map<Integer, RenderNode> parentNodeMap;

    private static RenderNode createSimpleRenderTree() {

        RenderNode root = new RenderNode("body");
        RenderNode h1 = new RenderNode("h1");
        RenderNode div = new RenderNode("div");
        RenderNode h2 = new RenderNode("h2");
        RenderNode hr = new RenderNode("hr");
        RenderNode p2 = new RenderNode("p");
        RenderNode img = new RenderNode("img");
        RenderNode ul = new RenderNode("ol");
        RenderNode li1 = new RenderNode("li");
        RenderNode li2 = new RenderNode("li");
        RenderNode li3 = new RenderNode("li");

        li1.text = "first item";
        li2.text = "second item";
        li3.text = "third item";

        img.box.fixedWidth = true;
        img.box.width = 250;
        img.box.fixedHeight = true;
        img.box.height = 200;

        img.attributes.put("src", "https://upload.wikimedia.org/wikipedia/en/9/90/ElderScrollsOblivionScreenshot11.jpg");

        parentNodeMap = new HashMap<Integer, RenderNode>();
        parentNodeMap.put(1, root);
        parentNodeMap.put(2, root);
        parentNodeMap.put(3, div);
        parentNodeMap.put(4, div);
        parentNodeMap.put(5, div);
        parentNodeMap.put(6, div);
        parentNodeMap.put(7, ul);
        parentNodeMap.put(8, ul);
        parentNodeMap.put(9, ul);
        parentNodeMap.put(10, div);

        h1.style.color = new CSSColor("SeaGreen");
        h1.style.fontFamily = "Courier New";

        h2.style.fontStyle = CSSStyle.fontStyleType.ITALICS;

        div.style.paddingTop = 5;
        div.style.paddingBottom = 5;
        div.style.paddingRight = 5;
        div.style.paddingLeft = 5;

        div.box.fixedWidth = true;
        div.box.width = 300;

        h2.style.marginTop = 5;
        hr.style.marginTop = 15;
        hr.style.marginBottom = 15;
        p2.style.marginTop = 15;
        p2.style.marginBottom = 5;

        p2.style.backgroundColor = new CSSColor("LightGray");

//		root.box.x = 0;		root.box.y = 0;		root.box.width = 200f;			root.box.height = 100f;
//		h1.box.x = 0;		h1.box.y = 0;		h1.box.width = 32.75f;			h1.box.height = 15.96f;
//		div.box.x = 0;		div.box.y = 15.96f;	h1.box.width = 196.44f;			h1.box.height = 15.96f;
//		p1.box.x = 0;		p1.box.y = 15.96f;	p1.box.width = 103.15f;			p1.box.height = 15.96f;
//		p2.box.x = 0;		p2.box.y = 31.92f;	p2.box.width = 196.44f;			p2.box.height = 15.96f;

        root.id = 0;		root.depth = 0;
        h1.id = 1;			h1.depth = 1;
        div.id = 2;			div.depth = 1;
        h2.id = 3;			h2.depth = 2;
        hr.id = 4;			hr.depth = 2;
        p2.id = 5;			p2.depth = 2;
        ul.id = 6;			ul.depth = 2;
        li1.id = 7;			li1.depth = 3;
        li2.id = 8;			li1.depth = 3;
        li3.id = 9;			li1.depth = 3;
//		img.id = 10;			p2.depth = 2;

        h1.text = "A Title";
        h2.text = "A subtitle.";
//		p2.text = "Menhir is a LR(1) parser generator fo.";

        p2.text = "Menhir is a LR(1) parser generator for the OCaml programming language. That is, Menhir compiles LR(1) grammar specifications down to OCaml code.";

        root.children.add(h1);
        root.children.add(div);
        div.children.add(h2);
        div.children.add(hr);
        div.children.add(p2);
        div.children.add(ul);
        ul.children.add(li1);
        ul.children.add(li2);
        ul.children.add(li3);
//		div.children.add(img);

        return root;
    }

    private static RenderNode createTree4() {
        // Two nested divs with padding, and some text in the middle
        // Outer div has fixed size, inner doesn't

        parentNodeMap = new HashMap<Integer, RenderNode>();

        RenderNode root = new RenderNode("body");
        RenderNode A = new RenderNode("div");
        RenderNode B = new RenderNode("div");
        RenderNode C = new RenderNode("text");

        C.text = "some text";

        A.box.fixedWidth = true;
        A.box.fixedHeight = true;

        A.box.width = 100;
        A.box.height = 50;

        A.style.paddingTop = 5;
        A.style.paddingBottom = 5;
        A.style.paddingLeft = 10;
        A.style.paddingRight = 10;

        B.style.paddingTop = 5;
        B.style.paddingBottom = 5;
        B.style.paddingLeft = 5;
        B.style.paddingRight = 5;

        root.id = 0;	root.depth = 0;
        A.id = 1;		A.depth = 1;
        B.id = 2;		B.depth = 2;
        C.id = 3;		C.depth = 3;

        root.children.add(A);
        A.children.add(B);
        B.children.add(C);

        parentNodeMap.put(1, root);
        parentNodeMap.put(2, A);
        parentNodeMap.put(3, B);
        return root;
    }

    private static RenderNode createTree_textwrap() {
        // Single long line of text in a width-constrained p
        parentNodeMap = new HashMap<Integer, RenderNode>();

        RenderNode root = new RenderNode("body");
        RenderNode div = new RenderNode("div");
        RenderNode p = new RenderNode("p");
        RenderNode text = new RenderNode("text");

        text.text = "An elegant weapon for a more civilized time. For over a thousand generations the Jedi Knights were the guardians of peace and justice in the Old Republic. Before the dark times, before the Empire.";

        div.box.fixedWidth = true;
        div.box.width = 300;

        root.id = 0;	root.depth = 0;
        div.id = 1;		div.depth = 1;
        p.id = 2;		p.depth = 2;
        text.id = 3;	text.depth = 3;

        root.children.add(div);
        div.children.add(p);
        p.children.add(text);

        parentNodeMap.put(1, root);
        parentNodeMap.put(2, div);
        parentNodeMap.put(3, p);
        return root;
    }

    private static RenderNode createTree_textwrapbold() {
        // One line of text with two bolded words.
        parentNodeMap = new HashMap<Integer, RenderNode>();

        RenderNode root = new RenderNode("body");
        RenderNode div = new RenderNode("div");
        RenderNode p = new RenderNode("p");
        RenderNode text1 = new RenderNode("text");
        RenderNode b = new RenderNode("b");
        RenderNode text2 = new RenderNode("text");
        RenderNode text3 = new RenderNode("text");

        text1.text = "An elegant weapon for a more civilized time. For over a thousand generations the ";
        text2.text = "Jedi Knights ";
        text3.text = "were the guardians of peace and justice in the Old Republic. Before the dark times, before the Empire.";

        div.box.fixedWidth = true;
        div.box.width = 400;

        root.id = 0;	root.depth = 0;
        div.id = 1;		div.depth = 1;
        p.id = 2;		p.depth = 2;
        text1.id = 3;	text1.depth = 3;
        b.id = 4;		b.depth = 3;
        text2.id = 5;	text2.depth = 4;
        text3.id = 6;	text3.depth = 3;


        root.children.add(div);
        div.children.add(p);
        p.children.add(text1);
        p.children.add(b);
        b.children.add(text2);
        p.children.add(text3);

        parentNodeMap.put(1, root);
        parentNodeMap.put(2, div);
        parentNodeMap.put(3, p);
        parentNodeMap.put(4, p);
        parentNodeMap.put(5, b);
        parentNodeMap.put(6, p);

        return root;
    }

    @Test
    public void test() {
        String[] args = new String[] {};
        Application.launch(RenderTestCanvas.class, args);
    }

    public static void render1(GraphicsContext gc, double width) {
        DefaultColors.init();
        ImageCache.loadDefaultImages();
        ImageCache.loadImage("https://upload.wikimedia.org/wikipedia/en/9/90/ElderScrollsOblivionScreenshot11.jpg");
        RenderNode root = createSimpleRenderTree();
//		RenderNode root = createTree4();
        CSSLoader cssLoader = new CSSLoader(null, parentNodeMap, new ArrayList<String>());
        cssLoader.loadDefaults(root);
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentNodeMap, 500f);
        RenderTreeGenerator rtg = new RenderTreeGenerator();
        rtg.nodeID = 11;
        rtg.transformNode(root);
        blc.setBoxBounds(root);
        blc.propagateMaxSizes(root);
        blc.printBoxes(root);
//		rtg.splitLongText(root, parentNodeMap);
//		blc.clearBoxBounds(root);
//		blc.setBoxBounds(root);
        blc.printBoxes(root);

        blc.calculateBoxes(root);
        blc.printBoxes(root);
        HTMLRenderer.render(gc, root);

    }

    public static void render3(GraphicsContext gc, double width) {
        DefaultColors.init();

//		RenderNode renderRoot = createTree_textwrap();
        RenderNode renderRoot = createTree_textwrapbold();

        RenderTreeGenerator rtg = new RenderTreeGenerator();
        rtg.cleanUpText(renderRoot, false);
        // For the test, consume the first 4 node IDs
        for (int i = 0; i < 4; i++) RenderTreeGenerator.getNextID();

        CSSLoader cssLoader = new CSSLoader(null, parentNodeMap, new ArrayList<String>());
        cssLoader.applyAllCSS(renderRoot);
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentNodeMap, (float) width);
        rtg.transformNode(renderRoot);
        blc.setBoxBounds(renderRoot);
        blc.propagateMaxSizes(renderRoot);
        blc.finalizeDimensions(renderRoot);
        blc.calculateBoxes(renderRoot);
        blc.applyJustification(renderRoot);
        blc.printBoxes(renderRoot);
        HTMLRenderer.render(gc, renderRoot);
    }

    public static void render(GraphicsContext gc, double width) {

        Pipeline pipeline = new Pipeline();
        Pipeline.init();
        pipeline.loadWebpage("file://res/html/startup_page.html");
//		pipeline.loadWebpage("file://res/html/error_page.html");
//		pipeline.loadWebpage("http://gallium.inria.fr/~fpottier/menhir/");
//		pipeline.loadWebpage("https://www.kernel.org/doc/man-pages/");
//		pipeline.loadWebpage("https://www.kernel.org/doc/man-pages/download.html");
//		pipeline.loadWebpage("http://man7.org/linux/man-pages/man0/aio.h.0p.html");
//		pipeline.loadWebpage("https://www.cis.upenn.edu/~cis341/current/");
//		pipeline.loadWebpage("https://mirrors.edge.kernel.org/pub/linux/docs/man-pages/");
//		pipeline.loadWebpage("https://caml.inria.fr/pub/docs/manual-ocaml/libref/Option.html");
//		pipeline.loadWebpage("https://caml.inria.fr/pub/docs/manual-ocaml/libref/Oo.html");
        pipeline.calculateLayout((float) width);
        pipeline.render(gc);

    }

}
