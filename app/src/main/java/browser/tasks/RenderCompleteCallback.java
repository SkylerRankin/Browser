package browser.tasks;

import browser.model.RenderNode;

public interface RenderCompleteCallback {

    public enum RenderType {
        NewLayout,
        InspectorUpdate
    }

    /**
     * A callback used after rendering has completed. The rendered render node is passed, along with an enum indicating
     * the type of render. This is needed since renders for new layouts require the inspector tree to be recreated,
     * but renders for inspector highlights do not.
     * @param root  The root RenderNode that was rendered.
     * @param renderType    The type of render that was completed.
     */
    void onRenderCompleted(RenderNode root, RenderType renderType);

}
