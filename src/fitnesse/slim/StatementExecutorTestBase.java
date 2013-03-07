package fitnesse.slim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class StatementExecutorTestBase {

  protected static final String MESSAGE_NO_METHOD_IN_CLASS = "message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>";
  protected static final String INSTANCE_NAME = "myInstance";
  protected StatementExecutorInterface statementExecutor;

  // some untyped languages do not explicitly define void return values
  protected String voidMessage() {
    return "/__VOID__/";
  }

  // echo is a keyword in some languages, allow for other methodNames
  protected String echoMethodName() {
    return "echo";
  }

  // delete is a keyword in some languages, allow for other methodNames
  protected String deleteMethodName() {
    return "delete";
  }

  protected int library = 0;

  public interface Echo {
    public void echo();

    public boolean echoCalled();
  }

  public interface Speak {
    public void speak();

    public boolean speakCalled();
  }

  public interface Delete {
    public void delete(String fileName);

    public boolean deleteCalled();
  }

  public interface SystemUnderTestFixture {
    public MySystemUnderTestBase getSystemUnderTest();
  }

  public abstract static class MySystemUnderTestBase implements Speak, Echo {
  }

  public static abstract class MyAnnotatedSystemUnderTestFixture implements Echo,
      SystemUnderTestFixture {
  }

  public static abstract class FixtureWithNamedSystemUnderTestBase implements Echo,
      SystemUnderTestFixture {
  }

  public static abstract class SimpleFixture implements Echo {
  }

  public static abstract class EchoSupport implements Echo, Speak {
  }

  public static abstract class FileSupport implements Delete {
  }

  public abstract void init() throws Exception;

  @Test
  public void shouldCallMethodOnGivenInstanceBeforeTryingToInvokeOnSystemUnderTest() throws Exception {
    MyAnnotatedSystemUnderTestFixture myInstance = createAnnotatedFixture();
    Object result = statementExecutor.call(INSTANCE_NAME, echoMethodName());
    assertEquals(voidMessage(), result);
    assertTrue(myInstance.echoCalled());
    assertFalse(myInstance.getSystemUnderTest().speakCalled());
  }

  @Test
  public void shouldCallMethodOnFieldAnnotatedWithSystemUnderTestWhenFixtureDoesNotHaveMethod() throws Exception {
    MyAnnotatedSystemUnderTestFixture myFixture = createAnnotatedFixture();
    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.echoCalled());
    assertTrue(myFixture.getSystemUnderTest().speakCalled());
  }

  @Test
  public void shouldCallMethodOnFieldNamed_systemUnderTest_WhenFixtureDoesNotHaveMethod() throws Exception {
    FixtureWithNamedSystemUnderTestBase myFixture = createNamedFixture();
    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.echoCalled());
    assertTrue(myFixture.getSystemUnderTest().speakCalled());
  }

  @Test
  public void shouldReportMissingMethodOnFixtureClassWhenMethodCanNotBeFoundOnBothFixtureAndSystemUnderTest()
      throws Exception {
    try {
      createAnnotatedFixture();
      String result = (String) statementExecutor.call(INSTANCE_NAME, "noSuchMethod");
      fail("Executed non-existing method.");
    } catch (SlimException e) {
      String expectedErrorMessage = String.format(MESSAGE_NO_METHOD_IN_CLASS, "noSuchMethod", 0,
          annotatedFixtureName());
      assertTrue(e.getMessage().contains(expectedErrorMessage));
    }
  }

  @Test
  public void shouldPreferMethodOnFixtureOverMethodOnSystemUnderTest() throws Exception {
    FixtureWithNamedSystemUnderTestBase instance = createNamedFixture();
    statementExecutor.call(INSTANCE_NAME, echoMethodName());
    assertFalse(instance.getSystemUnderTest().echoCalled());
    assertTrue(instance.echoCalled());
  }

  @Test
  public void shouldPreferMethodOnFixtureOverMethodOnLibrary() throws Exception {
    SimpleFixture instance = createSimpleFixture();
    EchoSupport echoLibrary = createEchoLibrary();
    statementExecutor.call(INSTANCE_NAME, echoMethodName());
    assertFalse(echoLibrary.echoCalled());
    assertTrue(instance.echoCalled());
  }

  @Test
  public void shouldPreferMethodOnSystemUnderTestOverMethodOnLibrary() throws Exception {
    FixtureWithNamedSystemUnderTestBase instance = createNamedFixture();
    EchoSupport echoLibrary = createEchoLibrary();
    statementExecutor.call(INSTANCE_NAME, "speak");
    assertFalse(echoLibrary.speakCalled());
    assertTrue(instance.getSystemUnderTest().speakCalled());
  }

  @Test
  public void shouldPreferMethodsOnLibrariesCreatedLaterOverMethodsOnLibrariesCreatedEarlier() throws Exception {
    createSimpleFixture();
    EchoSupport echoLibrary1 = createEchoLibrary();
    EchoSupport echoLibrary2 = createEchoLibrary();
    EchoSupport echoLibrary3 = createEchoLibrary();
    statementExecutor.call(INSTANCE_NAME, "speak");
    assertFalse(echoLibrary1.speakCalled());
    assertFalse(echoLibrary2.speakCalled());
    assertTrue(echoLibrary3.speakCalled());
  }

  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture_WithSystemUnderTestInFixture()
      throws Exception {
    createNamedFixture();
    FileSupport library = createFileSupportLibrary();
    assertNotNull(library);
    Object result = statementExecutor.call(INSTANCE_NAME, deleteMethodName(), "filename.txt");
    assertEquals(voidMessage(), result);
    assertTrue(library.deleteCalled());
  }

  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture() throws Exception {
    createFixtureInstance(echoLibraryName());
    FileSupport library = createFileSupportLibrary();
    assertNotNull(library);
    Object result = statementExecutor.call(INSTANCE_NAME, deleteMethodName(), "filename.txt");
    assertEquals(voidMessage(), result);
    assertTrue(library.deleteCalled());
  }

  protected MyAnnotatedSystemUnderTestFixture createAnnotatedFixture() throws Exception {
    createFixtureInstance(annotatedFixtureName());
    return (MyAnnotatedSystemUnderTestFixture) getVerifiedInstance();
  }

  protected abstract String annotatedFixtureName();

  protected FixtureWithNamedSystemUnderTestBase createNamedFixture() throws Exception {
    createFixtureInstance(namedFixtureName());
    return (FixtureWithNamedSystemUnderTestBase) getVerifiedInstance();
  }

  protected abstract String namedFixtureName();

  protected SimpleFixture createSimpleFixture() throws Exception {
    createFixtureInstance(simpleFixtureName());
    return (SimpleFixture) getVerifiedInstance();
  }

  protected abstract String simpleFixtureName();

  protected EchoSupport createEchoLibrary() throws Exception {
    String instanceName = "library" + library++;
    statementExecutor.create(instanceName, echoLibraryName(), new Object[] {});
    return (EchoSupport) statementExecutor.getInstance(instanceName);
  }

  protected abstract String echoLibraryName();

  protected FileSupport createFileSupportLibrary() throws Exception {
    String instanceName = "library" + library++;
    statementExecutor.create(instanceName, fileSupportName(), new Object[] {});
    return (FileSupport) statementExecutor.getInstance(instanceName);
  }

  protected abstract String fileSupportName();

  protected void createFixtureInstance(String fixtureClass) throws Exception {
    statementExecutor.create(INSTANCE_NAME, fixtureClass, new Object[] {});
  }

  protected Echo getVerifiedInstance() {
    Echo myInstance = (Echo) statementExecutor.getInstance(INSTANCE_NAME);
    assertFalse(myInstance.echoCalled());
    return myInstance;
  }

  protected void executeStatementAndVerifyResultIsVoid() throws Exception {
    Object result = statementExecutor.call(INSTANCE_NAME, "speak");
    assertEquals(voidMessage(), result);
  }
}
