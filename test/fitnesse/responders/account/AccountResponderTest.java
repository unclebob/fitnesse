package fitnesse.responders.account;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertSubString;

public class AccountResponderTest {
  private WikiPage root;
  private MockRequest request;
  private AccountResponder responder;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
    responder = new AccountResponder();
  }

  @Test
  public void testResponseWithoutAuth() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "<html>");
    PageData data = page.getData();
    page.commit(data);

    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<title>Account: ChildPage</title>", body);
    assertSubString("Change Password", body);
    assertSubString("Log in to view details.", body);
  }


  @Test
  public void testResponseWithAuthAdmin() throws Exception {
    request.setCredentials("admin", "admin");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "<html>");
    PageData data = page.getData();
    page.commit(data);

    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertEquals(2, getCountOfString("method=\"post\"", body));
    assertEquals(2, getCountOfString("name=\"responder\"", body));
    assertEquals(5, getCountOfString("<input type=\"text\"", body));
    assertEquals(3, getCountOfString("<input type=\"submit\"", body));
    assertSubString("<title>Account: ChildPage</title>", body);
    assertSubString("Change Password", body);
    assertSubString("Create / Delete Users", body);
    assertSubString("placeholder=\"Not used for deleting users.\"", body);
    assertSubString("<input type=\"submit\" name=\"changePassword\" value=\"Change My Password\"/>", body);
    assertSubString("<input type=\"submit\" name=\"createUser\" value=\"Create User\"/>", body);
    assertSubString("<input type=\"submit\" name=\"deleteUser\" value=\"Delete User\"/>", body);
  }


  @Test
  public void testResponseWithAuthNonAdmin() throws Exception {
    request.setCredentials("nonadmin", "nonadmin");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "<html>");
    PageData data = page.getData();
    page.commit(data);

    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("name=\"responder\"", body);
    assertEquals(1, getCountOfString("method=\"post\"", body));
    assertEquals(1, getCountOfString("name=\"responder\"", body));
    assertEquals(3, getCountOfString("<input type=\"text\"", body));
    assertEquals(1, getCountOfString("<input type=\"submit\"", body));
    assertSubString("<title>Account: ChildPage</title>", body);
    assertSubString("Change Password", body);
    assertSubString("<input type=\"submit\" name=\"changePassword\" value=\"Change My Password\"/>", body);
  }

  private static int getCountOfString(String textToFind, String body) {
    Pattern pattern = Pattern.compile(textToFind);
    Matcher matcher = pattern.matcher(body);
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  private SimpleResponse makeResponse() throws Exception {
    request.setResource("ChildPage");
    return (SimpleResponse) responder.makeResponse(context, request);
  }

}
