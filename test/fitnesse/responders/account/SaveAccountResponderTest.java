package fitnesse.responders.account;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.Password;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class SaveAccountResponderTest {
  private static final String PAGE_NAME = "PageOne";
  private FitNesseContext context;
  private WikiPage root;
  private MockRequest request;
  private WikiPage page;
  private Responder responder;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    responder = new SaveAccountResponder();
  }

  @Test
  public void testChangePasswordWithInvalidInput() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("invalidInput", "Change My Password");
    request.addInput("PasswordText", "admin");
    request.addInput("ConfirmPasswordText", "admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Invalid input to modify account."));
  }

  @Test
  public void testChangePasswordWithoutAuthentication() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.addInput("changePassword", "Change My Password");
    request.addInput("PasswordText", "admin");
    request.addInput("ConfirmPasswordText", "admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("You have to be logged in to use this feature."));
  }

  @Test
  public void testChangePasswordWithMismatchedPassword() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("changePassword", "Change My Password");
    request.addInput("CurrentPasswordText", "admin");
    request.addInput("NewPasswordText", "admin");
    request.addInput("ConfirmPasswordText", "admin1");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Password should not be empty and they should match."));
  }

  @Test
  public void testChangePasswordWithWrongCurrentPassword() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("changePassword", "Change My Password");
    request.addInput("CurrentPasswordText", "nonadmin");
    request.addInput("NewPasswordText", "admin");
    request.addInput("ConfirmPasswordText", "admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Current password is incorrect."));
  }

  @Test
  public void testChangePasswordResponse() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "oldadmin");
    request.addInput("changePassword", "Change My Password");
    request.addInput("CurrentPasswordText","oldadmin");
    request.addInput("NewPasswordText", "admin");
    request.addInput("ConfirmPasswordText", "admin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "admin:admin");
    List<String> fileLines = FileUtil.getFileLines(file);
    String initialText = fileLines.get(0);

    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/" + PAGE_NAME, response.getHeader("Location"));

    fileLines = FileUtil.getFileLines(file);
    String newText = fileLines.get(1);
    assertTrue(initialText.contains("admin"));
    assertNotEquals(initialText, newText);
  }

  @Test
  public void testCreateUserWithoutAuthentication() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.addInput("createUser", "Create User");
    request.addInput("UserNameText", "admin");
    request.addInput("UserPasswordText", "admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("You have to be logged in to use this feature."));
  }

  @Test
  public void testCreateUserWithNonAdminUser() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("nonadmin", "nonadmin");
    request.addInput("createUser", "Create User");
    request.addInput("UserNameText", "admin");
    request.addInput("UserPasswordText", "admin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "nonadmin:nonadmin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Only admin can create or delete users."));
  }

  @Test
  public void testCreateUserWithoutPasswordResponse() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("createUser", "Create User");
    request.addInput("UserNameText", "anotheruser");
    request.addInput("UserPasswordText", "");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "admin:admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Username or password field is empty."));
  }

  @Test
  public void testCreateExistingUser() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("createUser", "Create User");
    request.addInput("UserNameText", "nonadmin");
    request.addInput("UserPasswordText", "nonadmin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "!fitnesse.authentication.HashingCipher\nadmin:admin\nnonadmin:nonadmin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("User already exists."));
  }

  @Test
  public void testCreateUserResponse() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("createUser", "Create User");
    request.addInput("UserNameText", "anotherUser");
    request.addInput("UserPasswordText", "anotherUser");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "!fitnesse.authentication.HashingCipher\nadmin:admin");
    int initialSize = FileUtil.getFileLines(file).size();

    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/" + PAGE_NAME, response.getHeader("Location"));

    String finalText = FileUtil.getFileContent(file);
    assertTrue(finalText.contains("anotherUser"));

    int finalSize = FileUtil.getFileLines(file).size();
    assertEquals(1, finalSize - initialSize);
  }

  @Test
  public void testDeleteUserWithoutAuthentication() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.addInput("deleteUser", "Delete User");
    request.addInput("UserNameText", "admin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "nonadmin:nonadmin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("You have to be logged in to use this feature."));
  }

  @Test
  public void testDeleteUserWithNonAdminUser() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("nonadmin", "nonadmin");
    request.addInput("deleteUser", "Delete User");
    request.addInput("UserNameText", "admin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "nonadmin:nonadmin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("Only admin can create or delete users."));
  }

  @Test
  public void testDeleteNonExistentUser() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("deleteUser", "Delete User");
    request.addInput("UserNameText", "nonExistent");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "admin:admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("User does not exist."));
  }

  @Test
  public void testDeleteAdminUser() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("deleteUser", "Delete User");
    request.addInput("UserNameText", "admin");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "admin:admin");

    Response response = responder.makeResponse(context, request);
    assertEquals(412, response.getStatus());
    assertTrue(((SimpleResponse) response).getContent().contains("You cannot delete admin user."));
  }

  @Test
  public void testDeleteUserResponse() throws Exception {
    createRequest();
    addDefaultRequestInputs();
    request.setCredentials("admin", "admin");
    request.addInput("deleteUser", "Delete User");
    request.addInput("UserNameText", "anotherUser");
    request.addInput("UserPasswordText", "anotherUser");

    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "!fitnesse.authentication.HashingCipher\nadmin:admin\nanotherUser:anotherUser");
    List<String> fileLines = FileUtil.getFileLines(file);
    int initialSize = fileLines.size();

    Response response = responder.makeResponse(context, request);
    assertEquals(303, response.getStatus());
    assertEquals("/" + PAGE_NAME, response.getHeader("Location"));

    fileLines = FileUtil.getFileLines(file);
    int finalSize = fileLines.size();
    assertEquals(1, initialSize - finalSize);
    for (String eachUser : fileLines) {
      assertTrue(!eachUser.contains("anotherUser"));
    }
  }

  private void createRequest() {
    page = WikiPageUtil.addPage(root, PathParser.parse(PAGE_NAME), "");
    request = new MockRequest();
    addDefaultRequestInputs();
  }

  private void addDefaultRequestInputs() {
    request.addInput("PageType", "Test");
    request.addInput("Properties", "on");
    request.addInput("Search", "on");
    request.addInput("RecentChanges", "on");
    request.addInput(WikiPageProperty.PRUNE, "on");
    request.addInput(WikiPageProperty.SECURE_READ, "on");
    request.setResource(PAGE_NAME);
  }

}
