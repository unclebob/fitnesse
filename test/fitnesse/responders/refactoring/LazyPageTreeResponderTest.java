package fitnesse.responders.refactoring;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import fitnesse.Responder;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Test;

public class LazyPageTreeResponderTest extends ResponderTestCase {

    @Override
    protected Responder responderInstance() {
        return new LazyPageTreeResponder();
    }

    @Test
    public void testMakeResponseWithRootPath() throws Exception {
        WikiPageUtil.addPage(root, PathParser.parse("ChildA"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("ChildB"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("ChildB.SubChild"), "content");

        request.addInput("path", "ROOT");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        // Assert pages with children first, then without children
        assertThat(response.getContent(), jsonEquals("[{\"name\":\"ChildB\",\"path\":\"ChildB\",\"hasChildren\":true},{\"name\":\"ChildA\",\"path\":\"ChildA\",\"hasChildren\":false}]"));
    }

    @Test
    public void testMakeResponseWithSpecificPath() throws Exception {
        WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("SomePage.SubChild"), "content");

        request.addInput("path", "SomePage");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        assertThat(response.getContent(), jsonEquals("[{\"name\":\"SubChild\",\"path\":\"SomePage.SubChild\",\"hasChildren\":false}]"));
    }

    @Test
    public void testMakeResponseWithoutPath() throws Exception {
        WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "content");

        request.addInput("path", "");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        assertThat(response.getContent(), jsonEquals("[{\"name\":\"SomePage\",\"path\":\"SomePage\",\"hasChildren\":false}]"));
    }

    @Test
    public void testMakeResponseSorting() throws Exception {
        WikiPageUtil.addPage(root, PathParser.parse("ZChild"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("ZChild.Sub"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("AChild"), "content");
        WikiPageUtil.addPage(root, PathParser.parse("BChild"), "content");

        request.addInput("path", "ROOT");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        // Assert ZChild with children first, then AChild, BChild alphabetically
        assertThat(response.getContent(), jsonEquals("[{\"name\":\"ZChild\",\"path\":\"ZChild\",\"hasChildren\":true},{\"name\":\"AChild\",\"path\":\"AChild\",\"hasChildren\":false},{\"name\":\"BChild\",\"path\":\"BChild\",\"hasChildren\":false}]"));
    }

    @Test
    public void testMakeResponseNonExistentPath() throws Exception {
        request.addInput("path", "NonExistent");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        assertThat(response.getContent(), jsonEquals("[]"));
    }

    @Test
    public void testGetSecureOperation() {
        LazyPageTreeResponder responder = new LazyPageTreeResponder();
        assertThat(responder.getSecureOperation(), instanceOf(AlwaysSecureOperation.class));
    }
}
