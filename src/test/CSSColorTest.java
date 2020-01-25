package test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import css.DefaultColors;
import model.CSSColor;

public class CSSColorTest {
	
	@Before
	public void init() {
		DefaultColors.init();
	}
	
	@Test
	public void constructorNamesTest() {
		CSSColor color = new CSSColor("aqua");
		assertEquals("00FFFF", color.getHex());
		assertEquals(0, color.getRGB()[0]);
		assertEquals(255, color.getRGB()[1]);
		assertEquals(255, color.getRGB()[2]);
		
		color = new CSSColor("PowderBlue");
		assertEquals("B0E0E6", color.getHex());
		assertEquals(176, color.getRGB()[0]);
		assertEquals(224, color.getRGB()[1]);
		assertEquals(230, color.getRGB()[2]);
	}
	
	@Test
	public void constructorRGBTest() {
		CSSColor color = new CSSColor("rgb(220, 20, 60)");
		assertEquals("DC143C", color.getHex());
		color = new CSSColor("rgb(34, 139, 34)");
		assertEquals("228B22", color.getHex());
	}
	
	@Test
	public void constructorHexTest() {
		CSSColor color = new CSSColor("#DC143C");
		assertEquals(220, color.getRGB()[0]);
		assertEquals(20, color.getRGB()[1]);
		assertEquals(60, color.getRGB()[2]);
		color = new CSSColor("#228B22");
		assertEquals(34, color.getRGB()[0]);
		assertEquals(139, color.getRGB()[1]);
		assertEquals(34, color.getRGB()[2]);
	}

}
