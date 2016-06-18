package fitnesse.responders.editing;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class NewPageResponderTest {

  private FitNesseContext context;
  private WikiPage root;
  private MockRequest request;
  private NewPageResponder responder;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
    responder = new NewPageResponder();
  }

  @Test
  public void testResponse() throws Exception {
    request.setResource("root");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertSubString("textarea class=\"wikitext no_wrap mousetrap\"", body);
  }

  @Test
  public void testTemplateListPopulates() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary"), "template library");

    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary.TemplateOne"), "template 1");
    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary.TemplateTwo"), "template 2");
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");

    request.setResource("ChildPage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
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
    assertSubString("textarea class=\"wikitext no_wrap mousetrap\"", body);
  }

  @Test
  public void shouldSetPageTemplateIfProvidedAsArgument() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("FancyTemplate"), "template page");
    request.setResource("");
    request.addInput(NewPageResponder.PAGE_TEMPLATE, ".FancyTemplate");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<form", body);
    assertSubString("name=\"" + NewPageResponder.PAGE_TEMPLATE + "\"", body);
    assertSubString("value=\".FancyTemplate\"", body);
  }
}
