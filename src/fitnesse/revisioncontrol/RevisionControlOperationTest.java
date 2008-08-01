package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import junit.framework.TestCase;
import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;

public class RevisionControlOperationTest extends TestCase {

    public void testShouldCreateActionHTMLLinkWithOperationDetails() throws Exception {
        String pageName = "TestPage";
        HtmlTag actionLink = ADD.makeActionLink(pageName);
        assertEquals(link(ADD, pageName), actionLink.html());

        actionLink = DELETE.makeActionLink(pageName);
        assertEquals(link(DELETE, pageName), actionLink.html());
    }

    private String link(RevisionControlOperation operation, String pageName) {
        return "<!--" + operation.getName() + " button-->" + HtmlElement.endl + "<a href=\"" + pageName + "?" + operation.getQuery()
                + "\" accesskey=\"" + operation.getAccessKey() + "\">" + operation.getName() + "</a>" + HtmlElement.endl;
    }

}
