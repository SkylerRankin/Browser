package browser.layout;

import java.util.ArrayList;
import java.util.List;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.Vector2;

public class TextNodeSplitter {

    private final TextDimensionCalculator textDimensionCalculator;

    public TextNodeSplitter(TextDimensionCalculator textDimensionCalculator) {
        this.textDimensionCalculator = textDimensionCalculator;
    }

    /**
     * Checks if a box can be split. Text splits only happen on spaces.
     * @param boxNode       The box to check.
     * @return      True if the box is a text node that can be split.
     */
    public boolean canBeSplit(BoxNode boxNode) {
        if (!boxNode.isTextNode) {
            return false;
        }

        String text = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
        return text.contains(" ");
    }

    /**
     * Given a box that represents a text render node, this method splits the text into a list of box nodes such that
     * each fits into the available width. Text splitting is only done on spaces within the text. The `textStartIndex`
     * and `textEndIndex` values are populated in each box node such that they all map to a different subsequence of
     * the original text.
     * Two width arguments are used because the usual use case for this method is filling the remaining space of a line
     * with text, and then moving the remaining text to a new line.
     * @param boxNode       The text box node to split.
     * @param firstLineAvailableWidth        The width of the first line to fit the text into.
     * @param fullAvailableWidth        The full available width to fit the text into, after the first line.
     * @return      A list of box nodes that fit into the available width.
     */
    public List<BoxNode> split(BoxNode boxNode, float firstLineAvailableWidth, float fullAvailableWidth) {
        if (!boxNode.isTextNode) {
            return null;
        }
        CSSStyle style = boxNode.style;
        float spaceWidth = textDimensionCalculator.getDimension(" ", style).x;

        List<Range> lineRanges = new ArrayList<>();
        float cumulativeWidth = 0;
        int currentLineStartIndex = 0;
        int currentLineEndIndex = 0;
        float currentAvailableWidth = firstLineAvailableWidth;
        List<String> wordsInCurrentLine = new ArrayList<>();
        String[] words = boxNode.correspondingRenderNode.text.split("\s");
        for (String word : words) {
            float width = textDimensionCalculator.getDimension(word, style).x;
            float widthForWord = (wordsInCurrentLine.size() == 0 ? 0 : spaceWidth) + width;
            if (cumulativeWidth + widthForWord <= currentAvailableWidth) {
                // Word and the space before it fits in current line.
                cumulativeWidth += widthForWord;
                currentLineEndIndex += word.length() + (wordsInCurrentLine.size() == 0 ? 0 : 1);
                wordsInCurrentLine.add(word);
            } else {
                // Word does not fit in current row.
                if (wordsInCurrentLine.size() > 0) {
                    lineRanges.add(new Range(currentLineStartIndex, currentLineEndIndex));
                    wordsInCurrentLine.clear();
                    currentAvailableWidth = fullAvailableWidth;
                }

                wordsInCurrentLine.add(word);
                cumulativeWidth = width;
                currentLineStartIndex = currentLineEndIndex + 1;
                currentLineEndIndex = currentLineStartIndex + word.length();
            }

            wordsInCurrentLine.add(word);
        }

        if (wordsInCurrentLine.size() > 0) {
            lineRanges.add(new Range(currentLineStartIndex, currentLineEndIndex));
        }

        return createBoxesForLines(boxNode, lineRanges);
    }

    /**
     * Updates a text node's textStartIndex and textEndIndex properties such that the text in that range fits within
     * the provided width. If there is remaining text that does not fit, a new box node is returned that has its
     * textStartIndex and textEndIndex set to contain the remaining text. If the text does fit, or the box node has
     * no text, null is returned and the original box node is not modified.
     * @param boxNode       The text node to fit to the provided width.
     * @param availableWidth        The width that the text should fit.
     * @return      A box node with text index ranges containing the remaining text, or null if no split is required.
     */
    public BoxNode splitNodeAcrossLines(BoxNode boxNode, float availableWidth) {
        if (!boxNode.isTextNode) {
            return null;
        }

        CSSStyle style = boxNode.style;
        String text = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
        Vector2 textDimension = textDimensionCalculator.getDimension(text, style);
        if (textDimension.x <= availableWidth) {
            // The text fits in the available space. No extra node is needed.
            return null;
        }

        int leadingSpaces = countLeadingSpaces(text);
        int trailingSpaces = countTrailingSpaces(text);

        if (!text.substring(leadingSpaces).contains("\s")) {
            // The text cannot be split to fit the space.
            return null;
        }

        float spaceWidth = textDimensionCalculator.getDimension(" ", style).x;
        final int startIndex = boxNode.textStartIndex;
        int currentEndIndex = boxNode.textStartIndex;
        float currentWidth = 0;

        String leadingSpaceText = leadingSpaces > 0 ? text.substring(0, leadingSpaces) : "";
        String trailingSpaceText = trailingSpaces > 0 ? text.substring(text.length() - trailingSpaces) : "";
        String textWithoutSpace = text.substring(leadingSpaces, text.length() - trailingSpaces);

        String[] words = textWithoutSpace.split("\s+");
        boolean containsSpace = words.length > 1;
        words[0] += leadingSpaceText;
        if (containsSpace) {
            words[words.length - 1] += trailingSpaceText;
        }

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            float wordWidth = textDimensionCalculator.getDimension(word, style).x;
            float newCurrentWidth = currentWidth + (currentEndIndex == startIndex ? 0 : spaceWidth) + wordWidth;
            if (newCurrentWidth <= availableWidth) {
                currentWidth = newCurrentWidth;
                currentEndIndex += word.length() + (currentEndIndex == startIndex ? 0 : 1);
            } else {
                if (i == 0) {
                    currentEndIndex = boxNode.textStartIndex + word.length();
                }
                break;
            }
        }


        BoxNode newBoxNode = new BoxNode(boxNode);
        newBoxNode.textStartIndex = currentEndIndex + (containsSpace ? 1 : 0);
        newBoxNode.whiteSpaceAfter = containsSpace;
        String newBoxText = newBoxNode.correspondingRenderNode.text.substring(newBoxNode.textStartIndex, newBoxNode.textEndIndex);
        boxNode.textEndIndex = currentEndIndex;

        if (newBoxText.isBlank() || newBoxText.isEmpty()) {
            return null;
        }

        return newBoxNode;
    }

    /**
     * Checks if a text node can be split to fit a given width. This only checks if the first split will work, meaning
     * the text will be broken at the space character closest to but not exceeding the given width. The text remaining
     * after the split may or may not be able to fit as well.
     * @param boxNode       The text box to check.
     * @param availableWidth        The available width for the text.
     * @return      True if the box's text can be split to fit into the width.
     */
    public boolean canSplitNodeToFitWidth(BoxNode boxNode, float availableWidth) {
        if (!boxNode.isTextNode) {
            return false;
        } else {
            String text = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            int leadingSpaces = countLeadingSpaces(text);
            int spaceIndex = text.indexOf(" ", leadingSpaces);
            if (spaceIndex == -1) {
                return false;
            }

            String firstWord = text.substring(0, spaceIndex);
            float firstWordWidth = textDimensionCalculator.getDimension(firstWord, boxNode.style).x;
            return firstWordWidth <= availableWidth;
        }
    }

    private List<BoxNode> createBoxesForLines(BoxNode root, List<Range> lineRanges) {
        List<BoxNode> boxes = new ArrayList<>();
        for (Range range : lineRanges) {
            BoxNode box = new BoxNode(root);
            box.textStartIndex = range.start;
            box.textEndIndex = range.end;
            boxes.add(box);
        }
        return boxes;
    }

    private int countLeadingSpaces(String string) {
        int i = 0;
        while (i < string.length() && string.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    private int countTrailingSpaces(String string) {
        int i = string.length() - 1;
        int count = 0;
        while (i >= 0 && string.charAt(i) == ' ') {
            i--;
            count++;
        }

        return count == string.length() ? 0 : count;
    }

    private static class Range {
        public int start;
        public int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
