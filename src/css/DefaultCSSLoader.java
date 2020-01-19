package css;

import model.RenderNode;

public class DefaultCSSLoader {
	
	public static void loadCSS(RenderNode node) {
		
		switch(node.type) {
		case "h1":
			node.style.fontSize = 75;
			break;
		case "h2":
			node.style.fontSize = 60;
			break;
		case "span":
			node.style.diplay = CSSStyle.displayType.INLINE;
			break;
		}
		
	}

}
