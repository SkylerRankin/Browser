package browser.css;

import java.util.*;

import browser.constants.CSSConstants.SelectorCombinator;
import browser.model.*;

public class SelectorMatcher {

    private static final Map<Integer, Boolean> matchCache = new HashMap<>();

    public static boolean selectorGroupMatchesNode(CSSSelectorGroup selectorGroup, RenderNode node) {
        return selectorGroupMatchesNode(selectorGroup, node, selectorGroup.selectors.size() - 1);
    }

    public static void clearCache() {
        matchCache.clear();
    }

    private static boolean selectorGroupMatchesNode(CSSSelectorGroup selectorGroup, RenderNode node, int selectorIndex) {
        int hash = String.format("%s %s %s", selectorGroup, node.id, selectorIndex).hashCode();
        if (matchCache.containsKey(hash)) {
            return matchCache.get(hash);
        }

        // check if current selector matches node
        CSSSelector currentSelector = selectorGroup.selectors.get(selectorIndex);
        if (!selectorMatchesNode(currentSelector, node)) {
            matchCache.put(hash, false);
            return false;
        }

        // if this is the first selector, return true
        if (selectorIndex == 0) {
            matchCache.put(hash, true);
            return true;
        }

        // else, there is a previous combinator. get the list of all candidate nodes based on combinator
        List<RenderNode> candidates = getCandidateNodes(selectorGroup.combinators.get(selectorIndex - 1), node);

        // recurse on each candidate
        for (RenderNode candidate : candidates) {
            if (selectorGroupMatchesNode(selectorGroup, candidate, selectorIndex - 1)) {
                matchCache.put(hash, true);
                return true;
            }
        }

        matchCache.put(hash, false);
        return false;
    }

    private static boolean selectorMatchesNode(CSSSelector selector, RenderNode node) {
        for (CSSUnitSelector unitSelector : selector.unitSelectors) {
            String value = unitSelector.value;
            switch (unitSelector.type) {
                case ATTRIBUTE -> {
                    if (!attributesMatch((CSSUnitAttributeSelector) unitSelector, node)) {
                        return false;
                    }
                }
                case CLASS -> {
                    if (node.attributes.containsKey("class")) {
                        String classAttribute = node.attributes.get("class");
                        boolean containsClass = false;
                        for (String classString : classAttribute.split("\\s")) {
                            if (classString.equalsIgnoreCase(value)) {
                                containsClass = true;
                                break;
                            }
                        }

                        if (!containsClass) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                case ID -> {
                    if (node.attributes.containsKey("id")) {
                        if (!node.attributes.get("id").equalsIgnoreCase(value)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                case PSEUDO -> {
                    // TODO check for pseudo properties
                    return false;
                }
                case TYPE -> {
                    if (!node.type.equals(value)) {
                        return false;
                    }
                }
                case UNIVERSAL -> {
                    // Universal matches all render nodes.
                }
            }
        }
        return true;
    }

    private static List<RenderNode> getCandidateNodes(SelectorCombinator combinator, RenderNode node) {
        List<RenderNode> candidates = new ArrayList<>();

        switch (combinator) {
            case ADJACENT_SIBLING -> {
                if (node.parent != null) {
                    int indexInParent = node.parent.children.indexOf(node);
                    if (indexInParent > 0) {
                        candidates.add(node.parent.children.get(indexInParent - 1));
                    }
                }
            }
            case CHILD -> {
                if (node.parent != null) {
                    candidates.add(node.parent);
                }
            }
            case DESCENDANT -> {
                RenderNode current = node.parent;
                while (current != null) {
                    candidates.add(current);
                    current = current.parent;
                }
            }
            case SIBLING -> {
                if (node.parent != null) {
                    int indexInParent = node.parent.children.indexOf(node);
                    for (int i = 0; i < node.parent.children.size(); i++) {
                        if (i != indexInParent) {
                            candidates.add(node.parent.children.get(i));
                        }
                    }
                }
            }
        }

        return candidates;
    }

    private static boolean attributesMatch(CSSUnitAttributeSelector selector, RenderNode node) {
        boolean keyMatch = false;
        String value = null;
        for (String key : node.attributes.keySet()) {
            if ((selector.caseInsensitive && key.equalsIgnoreCase(selector.attributeName)) ||
                    key.equals(selector.attributeName)) {
                keyMatch = true;
                value = node.attributes.get(key);
                break;
            }
        }

        if (!keyMatch) {
            return false;
        }

        String expected = selector.value;
        if (selector.caseInsensitive) {
            value = value.toLowerCase();
            expected = expected.toLowerCase();
        }

        switch (selector.comparisonType) {
            case NONE -> {
                return true;
            }
            case EXACT -> {
                return value.equals(expected);
            }
            case HYPHEN -> {
                return value.equals(expected) || value.startsWith(String.format("%s-", expected));
            }
            case PREFIX -> {
                return value.startsWith(expected);
            }
            case SUFFIX -> {
                return value.endsWith(expected);
            }
            case OCCURRENCE -> {
                return value.contains(expected);
            }
            case MEMBER_IN_LIST -> {
                for (String item : value.split("\\s")) {
                    if (item.equals(expected)) {
                        return true;
                    }
                }
                return false;
            }
        }

        return false;
    }

}
