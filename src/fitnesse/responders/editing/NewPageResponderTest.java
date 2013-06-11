package fitnesse.responders.editing;

import fitnesse.wiki.PageBuilder;
import util.RegexTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class NewPageResponderTest extends RegexTestCase {

  private WikiPage root;
  private MockRequest request;
  private NewPageResponder responder;
  private PageBuilder pageBuilder;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    FitNesseUtil.makeTestContext(root);
    pageBuilder = root.getPageCrawler();
    request = new MockRequest();
    responder = new NewPageResponder();
  }

  public void testResponse() throws Exception {
    request.setResource("root");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root),
        request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertSubString("textarea class=\"wikitext no_wrap\"", body);
  }

  public void testTemplateListPopulates() throws Exception {
    pageBuilder.addPage(root, PathParser.parse("TemplateLibrary"), "template library");

    pageBuilder.addPage(root, PathParser.parse("TemplateLibrary.TemplateOne"), "template 1");
    pageBuilder.addPage(root, PathParser.parse("TemplateLibrary.TemplateTwo"), "template 2");
    pageBuilder.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");

    request.setResource("ChildPage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root),
        request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("select id=\"" + EditResponder.TEMPLATE_MAP + "\"", body);
    assertSubString("option value=\"" + ".TemplateLibrary.TemplateOne" + "\"", body);
    assertSubString("option value=\"" + ".TemplateLibrary.TemplateTwo" + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertSubString("textarea class=\"wikitext no_wrap\"", body);
  }

}
