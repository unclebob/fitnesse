package fitnesse.slim;

import static fitnesse.slim.StatementExecutor.MESSAGE_NO_METHOD_IN_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class StatementExecutorTest {
  private StatementExecutor statementExecutor;

  public static class MySystemUnderTest {
    public boolean called = false;

    public void specialCall() {
      called = true;
    }
  }

  public static class MyAnnotatedFixture {
    public boolean called = false;

    @SystemUnderTest
    public MySystemUnderTest sut;

    public MyAnnotatedFixture() {
      sut = new MySystemUnderTest();
    }

    public void echo() {
      called = true;
    }
  }

  public static class MyNamedFixture {
    public boolean called = false;

    public MySystemUnderTest systemUnderTest;

    public MyNamedFixture() {
      systemUnderTest = new MySystemUnderTest();
    }

    public void echo() {
      called = true;
    }
  }

  @Before
  public final void init() {
    statementExecutor = new StatementExecutor();
  }

  @Test
  public void shouldCallMethodOnGivenInstanceBeforeTryingToInvokeOnSystemUnderTest() {
    MyAnnotatedFixture myInstance = createAnnotatedFixture();

    Object result = statementExecutor.call("myInstance", "echo");
    assertEquals("/__VOID__/", result);
    assertTrue(myInstance.called);
    assertFalse(myInstance.sut.called);
  }

  private MyAnnotatedFixture createAnnotatedFixture() {
    createFixtureInstance(MyAnnotatedFixture.class);

    MyAnnotatedFixture myInstance = (MyAnnotatedFixture) statementExecutor.getInstance("myInstance");
    assertFalse(myInstance.called);
    return myInstance;
  }

  private void createFixtureInstance(Class<?> fixtureClass) {
    Class<?> clazz = fixtureClass;
    Object created = statementExecutor.create("myInstance", clazz.getName(),
        new Object[] {});
    assertEquals("OK", created);
  }

  @Test
  public void shouldCallMethodOnFieldAnnotatedWithSystemUnderTestWhenFixtureDoesNotHaveMethod() {
    MyAnnotatedFixture myFixture = createAnnotatedFixture();

    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.sut.called);
  }
  
  @Test
  public void shouldCallMethodOnFieldNamed_systemUnderTest_WhenFixtureDoesNotHaveMethod() {
    MyNamedFixture myFixture = createNamedFixture();
    
    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.systemUnderTest.called);
  }

  private void executeStatementAndVerifyResultIsVoid() {
    Object result = statementExecutor.call("myInstance", "specialCall");
    assertEquals("/__VOID__/", result);
  }
  
  private MyNamedFixture createNamedFixture() {
    createFixtureInstance(MyNamedFixture.class);
    
    MyNamedFixture myInstance = (MyNamedFixture) statementExecutor.getInstance("myInstance");
    assertFalse(myInstance.called);
    return myInstance;
  }

  @Test
  public void shouldReportMissingMethodOnFixtureClassWhenMethodCanNotBeFoundOnBothFixtureAndSystemUnderTest() {
    createAnnotatedFixture();
    String result = (String) statementExecutor.call("myInstance", "noSuchMethod");
    String expectedErrorMessage = String.format(MESSAGE_NO_METHOD_IN_CLASS, "noSuchMethod", 0,
        MyAnnotatedFixture.class.getName());
    assertTrue(result.contains(expectedErrorMessage));
  }
  
}
