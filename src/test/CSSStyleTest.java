package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import css.CSSStyle;

public class CSSStyleTest {
	
	@Test
	public void setPropertyTest() {
		CSSStyle style = new CSSStyle();
		style.setProperty("textAlign", "LEFT");
		assertEquals("FONT", style.fontFamily);
	}

}
