package model;

public class Color8Bit {
	
	private int[] rgb;
	private String hex;
	
	public Color8Bit(int r, int g, int b) {
		this.rgb = new int[] {r, g, b};
	}
	
	public Color8Bit(String color) {
		if (color.startsWith("#")) {
			hex = color;
		} else {
			switch (color) {
			case "white":
				rgb = new int[] {255, 255, 255};
				break;
			case "black":
				rgb = new int[] {0, 0, 0};
				break;
			case "yellow":
				rgb = new int[] {255, 242, 125};
				break;
			case "blue":
				rgb = new int[] {54, 205, 255};
				break;
			}
		}
	}
	
	public int r() { return rgb[0]; }
	public int g() { return rgb[1]; }
	public int b() { return rgb[2]; }

}
