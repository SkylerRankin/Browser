package browser.interaction;

import browser.model.InteractableElement;

public interface InteractionCallback {

    void onEvent(InteractableElement.InteractionType type, String data);

}
