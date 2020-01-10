package test;

import java.util.List;

import org.junit.Test;

import css.CSSStyle;
import javafx.embed.swing.JFXPanel;
import layout.TextDimensionCalculator;
import model.Vector2;

public class TextDimensionCalculatorTest {
	
	@Test
	public void test() {
		JFXPanel jfxPanel = new JFXPanel();
		CSSStyle style = new CSSStyle();
		Vector2 dimensions = TextDimensionCalculator.getTextDimension("testing", style);
		System.out.printf("%s\n", dimensions.toString());
	}
	
	@Test
	public void splitToWidthTest() {
		CSSStyle style = new CSSStyle();
		List<String> lines = TextDimensionCalculator.splitToWidth("testing", style, 25);
	}

}
