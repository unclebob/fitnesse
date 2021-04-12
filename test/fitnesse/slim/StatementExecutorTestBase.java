package fitnesse.slim;

import fitnesse.slim.test.ConstructorThrows;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class StatementExecutorTestBase {

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

  interface Echo {
    void echo();

    boolean echoCalled();
  }

  interface Speak {
    void speak();

    boolean speakCalled();
  }

  interface Delete {
    void delete(String fileName);

    boolean deleteCalled();
  }

  interface SystemUnderTestFixture {
    MySystemUnderTestBase getSystemUnderTest();
  }

  abstract static class MySystemUnderTestBase implements Speak, Echo {

  }

  abstract static class MyAnnotatedSystemUnderTestFixture implements Echo,
      SystemUnderTestFixture {
  }

  abstract static class FixtureWithNamedSystemUnderTestBase implements Echo,
      SystemUnderTestFixture {
  }

  abstract static class SimpleFixture implements Echo {
  }

  abstract static class EchoSupport implements Echo, Speak {
  }

  abstract static class FileSupport implements Delete {
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
  public void shouldCallMethodOnFieldAnnotatedWithSystemUnderTestWhenFixtureDoesNotHaveMethodAndMethodIsInSubclass() throws Exception {
    MyAnnotatedSystemUnderTestFixture myFixture = createAnnotatedFixture();
    executeShoutStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.echoCalled());
    assertTrue((((StatementExecutorTest.MySystemUnderTestJava)myFixture.getSystemUnderTest())).shoutCalled());
    assertFalse(myFixture.getSystemUnderTest().speakCalled());
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
    createAnnotatedFixture();
    try {
      statementExecutor.call(INSTANCE_NAME, "noSuchMethod");
      fail("Executed non-existing method.");
    } catch (SlimException e) {
      String expectedErrorMessage = SlimVersion.PRETTY_PRINT_TAG_START + SlimServer.NO_METHOD_IN_CLASS + " "+ String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S, "noSuchMethod", 0,
          annotatedFixtureName(),"");
      assertTrue(e.getMessage(), e.getMessage().contains(expectedErrorMessage));
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

  @Test
  public void shouldThrowStopTestExceptionFromConstructor() {
    try {
      statementExecutor.create(INSTANCE_NAME, ConstructorThrows.class.getCanonicalName(), new Object[] { "stop test" });
    } catch (SlimException e) {
      assertTrue(e.toString(), e.toString().startsWith(SlimServer.EXCEPTION_STOP_TEST_TAG));
      assertTrue(statementExecutor.stopHasBeenRequested());
      return;
    }
    fail("should not get here");
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

  protected void executeShoutStatementAndVerifyResultIsVoid() throws Exception {
    Object result = statementExecutor.call(INSTANCE_NAME, "shout");
    assertEquals(voidMessage(), result);
  }
}
