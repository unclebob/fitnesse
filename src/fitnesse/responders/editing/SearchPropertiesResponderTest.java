package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import static fitnesse.responders.editing.SearchPropertiesResponder.ATTRIBUTE;
import static fitnesse.responders.editing.SearchPropertiesResponder.SELECTED;
import fitnesse.testutil.RegexTestCase;
import fitnesse.wiki.*;

public class SearchPropertiesResponderTest extends RegexTestCase {
  private WikiPage root;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  public void testResponse() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties = data.getProperties();
    properties.set("Test", "true");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new SearchPropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals("max-age=0", response.getHeader("Cache-Control"));

    String content = response.getContent();
    assertSubString("PageOne", content);
    assertHasRegexp("<input.*value=\"Search Properties\".*>", content);
    assertHasRegexp("<input.*name=\"responder\".*value=\"executeSearchProperties\"", content);

    for (String attributeName : WikiPage.ACTION_ATTRIBUTES) {
      assertAttributeCheckboxCreated(content, attributeName);
    }
  }

  private void assertAttributeCheckboxCreated(String content, String attributeName) {
    assertSubString("<input type=\"checkbox\" name=\"" + attributeName + "" +
      ATTRIBUTE + SELECTED + "\"/>", content);
    assertSubString("<input type=\"checkbox\" name=\"" + attributeName + "Value\"/>", content);
  }
}
