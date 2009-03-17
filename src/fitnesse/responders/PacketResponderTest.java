package fitnesse.responders;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class PacketResponderTest {
  protected WikiPage root;
  protected MockRequest request;
  protected Responder responder;
  protected PageCrawler crawler;
  protected FitNesseContext context;
  private SimpleResponse response;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new PacketResponder();
    context = new FitNesseContext(root);
  }

  private SimpleResponse makeResponse() throws Exception {
    return (SimpleResponse) responder.makeResponse(context, request);
  }

  private void assertPageWithTableResponseWith(String table, String expected) throws Exception {
    crawler.addPage(root, PathParser.parse("TablePage"), table);
    request.setResource("TablePage");
    SimpleResponse response = makeResponse();
    assertEquals(expected, response.getContent());
  }

  @Test
  public void noSuchPage() throws Exception {
    request.setResource("NoSuchPage");
    response = makeResponse();
    assertEquals(404, response.getStatus());
  }

  @Test
  public void pageWithNoTables() throws Exception {
    crawler.addPage(root, PathParser.parse("SimplePage"), "simple content");
    request.setResource("SimplePage");
    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());
    assertEquals("{\"tables\": []}", response.getContent());
  }

  @Test
  public void pageWithOneSimpleTable() throws Exception {
    assertPageWithTableResponseWith("|cell|\n", "{\"tables\": [{\"cell\": {}}]}");
  }

  @Test
  public void pageWithOneSimplePair() throws Exception {
    assertPageWithTableResponseWith("|name|value|\n", "{\"tables\": [{\"name\": \"value\"}]}");
  }

  @Test
  public void pageWithTwoPairs() throws Exception {
    assertPageWithTableResponseWith("|name1|value1|\n|name2|value2|\n",
      "{\"tables\": [{\n" +
        " \"name1\": \"value1\",\n" +
        " \"name2\": \"value2\"\n" +
        "}]}");
  }

  @Test
  public void twoTablesWithSimplePairs() throws Exception {
    assertPageWithTableResponseWith("|n|v|\n\n|n2|v2|\n",
      "{\"tables\": [\n" +
        " {\"n\": \"v\"},\n" +
        " {\"n2\": \"v2\"}\n" +
        "]}");
  }

  @Test
  public void oneTableWithNestedPair() throws Exception {
    JSONObject expected = new JSONObject("{\"tables\": [{\"n\": {\"m\": \"v\"}}]}");
    assertPageWithTableResponseWith("|n|\n||m|v|\n", expected.toString(1));
  }

  @Test
  public void tableWithBlankLinesIgnored() throws Exception {
    JSONObject expected = new JSONObject("{\"tables\": [{\"n\": {\"m\": \"v\"}}]}");
    assertPageWithTableResponseWith("|n|\n||||\n||m|v|\n", expected.toString(1));
  }

  @Test
  public void deeplyNestedTable() throws Exception {
    String table =
      "|bob|\n" +
        "||Angela|\n" +
        "|||Lexy|6|\n" +
        "|||Sami|4|\n" +
        "|||Mandy|2|\n" +
        "||Micah|\n" +
        "|||Luka|5|\n" +
        "||Gina|\n" +
        "||Justin|\n";
    String expectedString = "{\"tables\": [{\"bob\": {\n" +
      " \"Angela\": {\n" +
      "  \"Lexy\": \"6\",\n" +
      "  \"Mandy\": \"2\",\n" +
      "  \"Sami\": \"4\"\n" +
      " },\n" +
      " \"Gina\": {},\n" +
      " \"Justin\": {},\n" +
      " \"Micah\": {\"Luka\": \"5\"}\n" +
      "}}]}";
    JSONObject expected = new JSONObject(expectedString);
    assertPageWithTableResponseWith(table, expected.toString(1));
  }

  @Test
  public void jsonpQueryArgument() throws Exception {
    request.addInput("jsonp", "load");
    assertPageWithTableResponseWith("|cell|\n", "load({\"tables\": [{\"cell\": {}}]})");
  }
}
