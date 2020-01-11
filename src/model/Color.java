package model;

public class Color {
	
	private int[] rgb;
	private String hex;
	
	public Color(int r, int g, int b) {
		this.rgb = new int[] {r, g, b};
	}
	
	public Color(String color) {
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
			}
		}
	}

}
