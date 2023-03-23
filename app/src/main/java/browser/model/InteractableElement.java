package browser.model;

import java.util.HashMap;
import java.util.Map;

public class InteractableElement {

    public enum InteractionType {
        REDIRECT,
        HOVER_START,
        HOVER_END
    }

    public static final String URL_KEY = "url";

    private final InteractionType interactionType;
    private final String element;
    private final Box box;
    private final Map<String, String> parameters;

    public InteractableElement(String element, Box box, InteractionType interactionType) {
        this.interactionType = interactionType;
        this.box = box;
        this.element = element;
        parameters = new HashMap<>();
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    public boolean interactsWithPoint(Vector2 point) {
        return box.overlapsPoint(point);
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

}
