package browser.interaction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import browser.model.InteractableElement;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

public class InteractionHandler {
    private final InteractionCallback interactionCallback;
    private RenderNode rootRenderNode;
    private List<InteractableElement> interactableElements;

    public InteractionHandler(InteractionCallback interactionCallback) {
        this.interactionCallback = interactionCallback;
    }

    public void handleClickEvent(Vector2 position) {
        if (rootRenderNode == null) {
            return;
        }

        for (final InteractableElement element : interactableElements) {
            if (element.interactsWithPoint(position)) {
                switch (element.getInteractionType()) {
                    case REDIRECT -> {
                        String url = element.getParameter(InteractableElement.URL_KEY);
                        interactionCallback.onRedirect(url, false);
                    }
                    case REDIRECT_NEW_TAB -> {
                        String url = element.getParameter(InteractableElement.URL_KEY);
                        interactionCallback.onRedirect(url, true);
                    }
                }
                break;
            }
        }
    }

    public void setRootRenderNode(RenderNode root) {
        rootRenderNode = root;
        buildInteractableElementList();
    }

    private void buildInteractableElementList() {
        interactableElements = new ArrayList<>();
        Deque<RenderNode> stack = new ArrayDeque<>();
        stack.add(rootRenderNode);
        while (!stack.isEmpty()) {
            RenderNode currentNode = stack.removeLast();
            InteractableElement element = getInteractableElement(currentNode);
            if (element != null) {
                interactableElements.add(element);
            }

            stack.addAll(currentNode.children);
        }
    }

    private InteractableElement getInteractableElement(RenderNode node) {
        switch (node.type) {
            case HTMLElements.A -> {
                if (!node.attributes.containsKey("href")) {
                    return null;
                }
                boolean containsNewTabAttribute = node.attributes.containsKey("target") &&
                        node.attributes.get("target").equals("_blank");
                InteractableElement.InteractionType interactionType = containsNewTabAttribute ?
                        InteractableElement.InteractionType.REDIRECT_NEW_TAB :
                        InteractableElement.InteractionType.REDIRECT;
                InteractableElement element = new InteractableElement(HTMLElements.A, node.box, interactionType);
                element.addParameter(InteractableElement.URL_KEY, node.attributes.get("href"));
                return element;
            }
            default -> {
                return null;
            }
        }
    }

}
