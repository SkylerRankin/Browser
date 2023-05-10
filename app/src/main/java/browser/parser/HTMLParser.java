package browser.parser;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import browser.constants.HTMLConstants;
import browser.model.DOMNode;
import browser.model.HTMLToken;
import browser.parser.HTMLLexer.HTMLTokenType;

public class HTMLParser {

    public DOMNode generateDOMTree(String html) {
        HTMLLexer lexer = new HTMLLexer(html.trim());
        List<HTMLToken> tokens = lexer.getTokens();
        removeCommentTokens(tokens);
        ListIterator<HTMLToken> tokenIterator = tokens.listIterator();
        consumeStartingWhitespace(tokenIterator);

        DOMNode root = null;
        DOMNode current = null;
        int preTagDepth = 0;

        while (tokenIterator.hasNext()) {
            HTMLToken nextToken = tokenIterator.next();
            switch (nextToken.type) {
                case TAG_OPEN -> {
                    DOMNode node = createDOMNode(tokenIterator, preTagDepth > 0);
                    if (node.type.equals(HTMLElements.PRE)) {
                        preTagDepth++;
                    }

                    // The doctype tag is not included in DOM tree, so it is skipped. Any whitespace text tokens
                    // occurring after the tag are also skipped.
                    if (isDocTypeNode(node)) {
                        handleWhitespaceAfterTag(tokenIterator, node, true, false);
                        break;
                    }

                    // The new tag is either the root, or a child of the previous node.
                    if (current == null) {
                        root = node;
                    } else {
                        current.addChild(node);
                    }

                    if (!isSingularTag(node, tokenIterator)) {
                        current = node;
                    } else if (node.type.equals(HTMLElements.PRE)) {
                        preTagDepth++;
                    }
                }
                case TAG_END_OPEN -> {
                    // A previously opened tag is closed. The token iterator is skipped forward to the tag closing
                    // token. Any whitespace text tokens occurring afterwards are skipped. The active node becomes
                    // the parent of the current active node.
                    HTMLToken currentToken = nextToken;
                    while (!isTagCloseToken(currentToken)) {
                        currentToken = tokenIterator.next();
                    }

                    if (current.type.equals(HTMLElements.PRE)) {
                        preTagDepth--;
                    }

                    handleWhitespaceAfterTag(tokenIterator, current, true, preTagDepth > 0);

                    current = current.parent;
                }
                case TEXT -> {
                    // A text node is created and added as a child of the current active node.
                    DOMNode textNode = new DOMNode(HTMLElements.TEXT);
                    textNode.content = nextToken.value;
                    if (current == null) {
                        // Skip starting text not in a tag.
                    } else {
                        current.addChild(textNode);
                    }
                }
                default -> System.err.printf("Unexpected next token: %s %s.\n", nextToken.type, nextToken.value);
            }
        }

        return root;
    }

    private DOMNode createDOMNode(ListIterator<HTMLToken> tokenIterator, boolean preserveWhitespace) {
        HTMLToken firstToken = tokenIterator.next();
        DOMNode node = null;
        if (isTagCloseToken(firstToken)) {
            // This element had no name, such as "<>" or "</>".
            node = new DOMNode("");
        } else if (firstToken.type.equals(HTMLTokenType.TAG_NAME)) {
            // A normal tag with a name
            node = new DOMNode(firstToken.value.toLowerCase());
            setNodeAttributes(node, tokenIterator);
        } else {
            System.err.printf("Unexpected token (%s:%s) after tag open.\n", firstToken.type, firstToken.value);
        }

        boolean setWhitespaceFlag = isSingularTag(node, tokenIterator);
        handleWhitespaceAfterTag(tokenIterator, node, setWhitespaceFlag, preserveWhitespace);

        return node;
    }

    private void setNodeAttributes(DOMNode node, Iterator<HTMLToken> tokenIterator) {
        HTMLToken nextToken = tokenIterator.next();
        String currentAttributeName = null;
        while (!isTagCloseToken(nextToken)) {
            if (nextToken.type.equals(HTMLTokenType.ATTRIBUTE_NAME)) {
                currentAttributeName = nextToken.value.toLowerCase();
                node.attributes.put(currentAttributeName, null);
            } else if (nextToken.type.equals(HTMLTokenType.ATTRIBUTE_VALUE)) {
                node.attributes.put(currentAttributeName, nextToken.value);
            } else if (nextToken.type.equals(HTMLTokenType.ATTRIBUTE_END_QUOTES)) {
                node.attributes.putIfAbsent(currentAttributeName, "");
            }

            if (!tokenIterator.hasNext()) {
                break;
            } else {
                nextToken = tokenIterator.next();
            }
        }
    }

    private boolean isTagCloseToken(HTMLToken token) {
        return token.type.equals(HTMLTokenType.TAG_CLOSE) || token.type.equals(HTMLTokenType.TAG_END_CLOSE);
    }

    private boolean isDocTypeNode(DOMNode node) {
        return HTMLConstants.docTypeStrings.contains(node.type.toLowerCase());
    }

    private boolean isSingularTag(DOMNode node, ListIterator<HTMLToken> tokenIterator) {
        if (HTMLConstants.voidElements.contains(node.type)) {
            return true;
        }

        if (tokenIterator.previous().type.equals(HTMLTokenType.TAG_END_CLOSE)) {
            // Restore the iterator index.
            tokenIterator.next();
            return true;
        }
        // Restore the iterator index.
        tokenIterator.next();

        return false;
    }

    private void handleWhitespaceAfterTag(ListIterator<HTMLToken> tokenIterator, DOMNode currentNode, boolean setFlag, boolean preserveWhitespace) {
        if (!tokenIterator.hasNext()) {
            return;
        }

        boolean whitespaceAfter = false;
        boolean restorePreviousToken = true;

        HTMLToken tokenAfterClose = tokenIterator.next();
        if (tokenAfterClose.type.equals(HTMLTokenType.TEXT)) {
            if (tokenAfterClose.value.isBlank()) {
                restorePreviousToken = false;
                whitespaceAfter = true;
            } else if (Character.isWhitespace(tokenAfterClose.value.charAt(0))) {
                whitespaceAfter = true;
            }
        }

        if (setFlag) {
            currentNode.whiteSpaceAfter = whitespaceAfter;
        }

        if (restorePreviousToken || preserveWhitespace) {
            tokenIterator.previous();
        }
    }

    private void removeCommentTokens(List<HTMLToken> tokens) {
        Set<HTMLTokenType> commentTypes = Set.of(
                HTMLTokenType.COMMENT_START,
                HTMLTokenType.COMMENT,
                HTMLTokenType.COMMENT_END);

        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i).type.equals(HTMLTokenType.COMMENT_END)) {
                // If token after comment is whitespace, remove it.
                if (i < tokens.size() - 1 && tokens.get(i + 1).type.equals(HTMLTokenType.TEXT) && tokens.get(i + 1).value.isBlank()) {
                    tokens.remove(i + 1);
                }
            }

            if (commentTypes.contains(tokens.get(i).type)) {
                tokens.remove(i);
            }
        }
    }

    private void consumeStartingWhitespace(ListIterator<HTMLToken> tokenIterator) {
        HTMLToken token = tokenIterator.next();
        while (token.type.equals(HTMLTokenType.TEXT) && token.value.isBlank()) {
            token = tokenIterator.next();
        }
        tokenIterator.previous();
    }

}
