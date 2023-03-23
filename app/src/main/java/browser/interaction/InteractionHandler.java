package browser.interaction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import browser.model.*;
import browser.parser.HTMLElements;

public class InteractionHandler {
    private final InteractionCallback interactionCallback;
    private BoxNode rootBoxNode;
    private List<InteractableElement> interactableElements;
    private boolean hover = false;

    public InteractionHandler(InteractionCallback interactionCallback) {
        this.interactionCallback = interactionCallback;
    }

    public void handleClickEvent(Vector2 position) {
        if (rootBoxNode == null) {
            return;
        }

        for (final InteractableElement element : interactableElements) {
            if (element.interactsWithPoint(position)) {
                switch (element.getInteractionType()) {
                    case REDIRECT -> {
                        String url = element.getParameter(InteractableElement.URL_KEY);
                        interactionCallback.onEvent(InteractableElement.InteractionType.REDIRECT, url);
                    }
                }
                break;
            }
        }
    }

    public void handleMouseMoveEvent(Vector2 position) {
        if (interactableElements == null || interactableElements.size() == 0) {
            return;
        }

        boolean intersected = false;
        for (final InteractableElement element : interactableElements) {
            if (element.interactsWithPoint(position)) {
                if (!hover) {
                    interactionCallback.onEvent(InteractableElement.InteractionType.HOVER_START, null);
                    hover = true;
                }
                intersected = true;
                break;
            }
        }

        if (!intersected) {
            if (hover) {
                interactionCallback.onEvent(InteractableElement.InteractionType.HOVER_END, null);
                hover = false;
            }
        }

    }

    public void setRootBoxNode(BoxNode root) {
        rootBoxNode = root;
        buildInteractableElementList();
    }

    private void buildInteractableElementList() {
        interactableElements = new ArrayList<>();
        Deque<BoxNode> stack = new ArrayDeque<>();
        stack.add(rootBoxNode);
        while (!stack.isEmpty()) {
            BoxNode currentNode = stack.removeLast();
            InteractableElement element = getInteractableElement(currentNode);
            if (element != null) {
                interactableElements.add(element);
            }

            stack.addAll(currentNode.children);
        }
    }

    private InteractableElement getInteractableElement(BoxNode node) {
        if (node.correspondingRenderNode == null) {
            return null;
        }
        switch (node.correspondingRenderNode.type) {
            case HTMLElements.A -> {
                if (!node.correspondingRenderNode.attributes.containsKey("href")) {
                    return null;
                }

                Box box = new Box(node.x, node.y, node.width, node.height);
                InteractableElement element = new InteractableElement(HTMLElements.A, box, InteractableElement.InteractionType.REDIRECT);
                element.addParameter(InteractableElement.URL_KEY, node.correspondingRenderNode.attributes.get("href"));
                return element;
            }
            default -> {
                return null;
            }
        }
    }

}
