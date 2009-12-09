package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class StatementExecutorTest {
  private static final String INSTANCE_NAME = "myInstance";
  private static final String MESSAGE_NO_METHOD_IN_CLASS = "message:<<NO_METHOD_IN_CLASS %s[%d] %s.>>";
  private StatementExecutor statementExecutor;

  public static class MySystemUnderTest {
    public boolean called = false;

    public void specialCall() {
      called = true;
    }
  }

  public static class MyAnnotatedSystemUnderTestFixture {
    public boolean called = false;

    @SystemUnderTest
    public MySystemUnderTest sut;

    public MyAnnotatedSystemUnderTestFixture() {
      sut = new MySystemUnderTest();
    }

    public void echo() {
      called = true;
    }
  }

  public static class MyNamedSystemUnderTestFixture {
    public boolean called = false;

    public MySystemUnderTest systemUnderTest;

    public MyNamedSystemUnderTestFixture() {
      systemUnderTest = new MySystemUnderTest();
    }

    public void echo() {
      called = true;
    }
  }
  
  public static class SimpleFixture {
    public String echo(String message) {
      return message;
    }
  }
  
  public static class FileSupport {
    public boolean called;
    public void delete(String fileName) {
      called = true;
    }
  }

  @Before
  public final void init() {
    statementExecutor = new StatementExecutor();
  }

  @Test
  public void shouldCallMethodOnGivenInstanceBeforeTryingToInvokeOnSystemUnderTest() {
    MyAnnotatedSystemUnderTestFixture myInstance = createAnnotatedFixture();

    Object result = statementExecutor.call(INSTANCE_NAME, "echo");
    assertEquals("/__VOID__/", result);
    assertTrue(myInstance.called);
    assertFalse(myInstance.sut.called);
  }

  private MyAnnotatedSystemUnderTestFixture createAnnotatedFixture() {
    createFixtureInstance(MyAnnotatedSystemUnderTestFixture.class);

    MyAnnotatedSystemUnderTestFixture myInstance = (MyAnnotatedSystemUnderTestFixture) statementExecutor.getInstance(INSTANCE_NAME);
    assertFalse(myInstance.called);
    return myInstance;
  }

  private void createFixtureInstance(Class<?> fixtureClass) {
    Class<?> clazz = fixtureClass;
    Object created = statementExecutor.create(INSTANCE_NAME, clazz.getName(),
        new Object[] {});
    assertEquals("OK", created);
  }

  @Test
  public void shouldCallMethodOnFieldAnnotatedWithSystemUnderTestWhenFixtureDoesNotHaveMethod() {
    MyAnnotatedSystemUnderTestFixture myFixture = createAnnotatedFixture();

    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.sut.called);
  }
  
  @Test
  public void shouldCallMethodOnFieldNamed_systemUnderTest_WhenFixtureDoesNotHaveMethod() {
    MyNamedSystemUnderTestFixture myFixture = createNamedFixture();
    
    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.systemUnderTest.called);
  }

  private void executeStatementAndVerifyResultIsVoid() {
    Object result = statementExecutor.call(INSTANCE_NAME, "specialCall");
    assertEquals("/__VOID__/", result);
  }
  
  private MyNamedSystemUnderTestFixture createNamedFixture() {
    createFixtureInstance(MyNamedSystemUnderTestFixture.class);
    MyNamedSystemUnderTestFixture myInstance = (MyNamedSystemUnderTestFixture) statementExecutor.getInstance(INSTANCE_NAME);
    assertFalse(myInstance.called);
    return myInstance;
  }

  @Test
  public void shouldReportMissingMethodOnFixtureClassWhenMethodCanNotBeFoundOnBothFixtureAndSystemUnderTest() {
    createAnnotatedFixture();
    String result = (String) statementExecutor.call(INSTANCE_NAME, "noSuchMethod");
    String expectedErrorMessage = String.format(MESSAGE_NO_METHOD_IN_CLASS, "noSuchMethod", 0,
        MyAnnotatedSystemUnderTestFixture.class.getName());
    assertTrue(result.contains(expectedErrorMessage));
  }
  
  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture_WithSystemUnderTestInFixture() {
    createNamedFixture();
    statementExecutor.create("library1", FileSupport.class.getName(), new Object[]{});
    FileSupport library1 = (FileSupport) statementExecutor.getInstance("library1");
    assertNotNull(library1);
    Object result = statementExecutor.call(INSTANCE_NAME, "delete", "filename.txt");
    assertEquals("/__VOID__/", result);
    assertTrue(library1.called);
    
  }
  
  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture() {
    createFixtureInstance(SimpleFixture.class);
    statementExecutor.create("library1", FileSupport.class.getName(), new Object[]{});
    FileSupport library1 = (FileSupport) statementExecutor.getInstance("library1");
    assertNotNull(library1);
    Object result = statementExecutor.call(INSTANCE_NAME, "delete", "filename.txt");
    assertEquals("/__VOID__/", result);
    assertTrue(library1.called);
    
  }
}
