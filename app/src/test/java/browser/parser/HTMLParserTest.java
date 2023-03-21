package browser.parser;

import browser.util.ParserTestDriver;

import org.junit.Before;
import org.junit.Test;

public class HTMLParserTest {

    private ParserTestDriver testDriver;

    @Before
    public void before() {
        HTMLElements.init();
        testDriver = new ParserTestDriver();
    }

    @Test
    public void basicValidHTML() {
        testDriver.runParseTest("basicValidHTML");
    }

    @Test
    public void mixedComments() {
        testDriver.runParseTest("mixedComments");
    }

    @Test
    public void commentsWithWhitespace() {
        testDriver.runParseTest("commentsWithWhitespace");
    }

    @Test
    public void wikipediaFile() {
        testDriver.runParseTest("wikipediaFilePage");
    }

}
