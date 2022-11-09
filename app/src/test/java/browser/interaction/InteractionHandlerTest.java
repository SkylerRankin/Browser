package browser.interaction;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import browser.app.Pipeline;
import browser.model.Box;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

import org.junit.Before;
import org.junit.Test;

public class InteractionHandlerTest {

    @Before
    public void setup() {
        Pipeline.init();
    }

    @Test
    public void handleClickEvent_Links() {
        InteractionCallback interactionCallback = mock(InteractionCallback.class);
        InteractionHandler handler = new InteractionHandler(interactionCallback);

        RenderNode renderRoot = new RenderNode(HTMLElements.BODY);
        renderRoot.box = new Box(0, 0, 100, 100);
        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.box = new Box(10, 10, 80, 80);
        RenderNode h1 = new RenderNode(HTMLElements.H1);
        h1.box = new Box(10, 10, 80, 30);
        RenderNode a1 = new RenderNode(HTMLElements.A);
        a1.box = new Box(10, 40, 80, 10);
        a1.attributes.put("href", "link1");
        RenderNode span = new RenderNode(HTMLElements.SPAN);
        span.box = new Box(10, 50, 50, 50);
        RenderNode a2 = new RenderNode(HTMLElements.A);
        a2.box = new Box(20, 50, 10, 10);
        a2.attributes.put("href", "link2");
        a2.attributes.put("target", "_blank");

        renderRoot.addChild(div);
        div.addChildren(h1, a1, span);
        span.addChild(a2);

        handler.setRootRenderNode(renderRoot);

        handler.handleClickEvent(new Vector2(0, 0));
        verify(interactionCallback, never()).onRedirect(anyString(), anyBoolean());
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(94, 45));
        verify(interactionCallback, never()).onRedirect(anyString(), anyBoolean());
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(25, 75));
        verify(interactionCallback, never()).onRedirect(anyString(), anyBoolean());
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(63, 41));
        verify(interactionCallback).onRedirect(eq("link1"), eq(false));
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(60, 40));
        verify(interactionCallback).onRedirect(eq("link1"), eq(false));
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(29, 59));
        verify(interactionCallback).onRedirect(eq("link2"), eq(true));
        reset(interactionCallback);

        handler.handleClickEvent(new Vector2(22, 58));
        verify(interactionCallback).onRedirect(eq("link2"), eq(true));
        reset(interactionCallback);
    }

}
