package browser.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;

import browser.css.DefaultColors;

public class CSSColor {

    private double opacity = 1.0;
    private int[] rgb = new int[0];
    private String hex;

    private static final Pattern rgbRegex = Pattern.compile("(rgb\\()(\\d+)(,\\s*)(\\d+)(,\\s*)(\\d+)(\\))");
    private static final Pattern rgbaRegex = Pattern.compile("(rgba?\\()(\\d+)(,\\s*)(\\d+)(,\\s*)(\\d+)(,\\s*)(\\d+)(\\))");

    public CSSColor(String color) {
        color = color.toLowerCase();
        if (color.matches("[A-Za-z]+")) {
            hex = DefaultColors.getHex(color);
            if (hex != null) {
                setRGB(hex);
            }
        } else if (color.matches("rgb\\(\\d+,\\s*\\d+,\\s*\\d+\\)")) {
            Matcher match = rgbRegex.matcher(color);
            if (match.find()) {
                rgb = new int[] {
                    Integer.parseInt(match.group(2)),
                    Integer.parseInt(match.group(4)),
                    Integer.parseInt(match.group(6)),
                };
                setHex(rgb);
            }
        } else if (color.matches("rgba\\(\\d+,\\s?\\d+,\\s?\\d+,\\s?\\d+\\)")) {
            Matcher match = rgbaRegex.matcher(color);
            if (match.find()) {
                rgb = new int[] {
                    Integer.parseInt(match.group(2)),
                    Integer.parseInt(match.group(4)),
                    Integer.parseInt(match.group(6)),
                    Integer.parseInt(match.group(8))
                };
                opacity = Integer.parseInt(match.group(8)) / 255.0;
                setHex(rgb);
            }
        } else if (color.matches("#([a-z0-9]{3}|[a-z0-9]{6})")) {
            hex = color.substring(1);
            if (hex.length() == 3) {
                char c1 = hex.charAt(0);
                char c2 = hex.charAt(1);
                char c3 = hex.charAt(2);
                hex = String.format("%c%c%c%c%c%c", c1, c1, c2, c2, c3, c3);
            }
            setRGB(hex);
        }

        if (hex == null || rgb.length == 0) {
            System.err.printf("CSSColor : Invalid color %s\n", color);
        }
    }

    private void setRGB(String hex) {
        if (hex.length() == 3) {
            char c1 = hex.charAt(0);
            char c2 = hex.charAt(1);
            char c3 = hex.charAt(2);
            hex = String.format("%c%c%c%c%c%c", c1, c1, c2, c2, c3, c3);
        }
        rgb = new int[3];
        rgb[0] = Integer.parseInt(hex.substring(0, 2), 16);
        rgb[1] = Integer.parseInt(hex.substring(2, 4), 16);
        rgb[2] = Integer.parseInt(hex.substring(4, 6), 16);
    }

    private void setHex(int[] rgb) {
        hex = (Integer.toHexString(rgb[0]) + Integer.toHexString(rgb[1]) + Integer.toHexString(rgb[2])).toUpperCase();
    }

    public String getHex() { return hex; }

    public int[] getRGB() { return rgb; }

    public Color toPaint() {
        return Color.color(rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, opacity);
    }

    public String toString() {
        return String.format("#%s", hex);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CSSColor color)) {
            return false;
        }

        return hex.equals(color.hex);
    }

}
