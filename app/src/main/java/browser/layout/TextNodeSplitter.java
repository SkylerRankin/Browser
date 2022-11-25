package browser.layout;

import java.util.ArrayList;
import java.util.List;

import browser.css.CSSStyle;
import browser.model.BoxNode;

public class TextNodeSplitter {

    private final TextDimensionCalculator textDimensionCalculator;

    public TextNodeSplitter(TextDimensionCalculator textDimensionCalculator) {
        this.textDimensionCalculator = textDimensionCalculator;
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
        CSSStyle style = boxNode.correspondingRenderNode.style;
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

    private static class Range {
        public int start;
        public int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
