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

  public static class MyFixture {
    public boolean called = false;

    @SystemUnderTest
    public MySystemUnderTest systemUnderTest;

    public MyFixture() {
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
    MyFixture myInstance = createFixture();

    Object result = statementExecutor.call("myInstance", "echo");
    assertEquals("/__VOID__/", result);
    assertTrue(myInstance.called);
    assertFalse(myInstance.systemUnderTest.called);
  }

  private MyFixture createFixture() {
    Object created = statementExecutor.create("myInstance", MyFixture.class.getName(),
        new Object[] {});
    MyFixture myInstance = (MyFixture) statementExecutor.getInstance("myInstance");
    assertEquals("OK", created);
    assertFalse(myInstance.called);
    return myInstance;
  }

  @Test
  public void shouldCallMethodOnSystemUnderTestWhenFixtureDoesNotHaveMethod() {
    MyFixture myFixture = createFixture();

    Object result = statementExecutor.call("myInstance", "specialCall");
    assertEquals("/__VOID__/", result);
    assertFalse(myFixture.called);
    assertTrue(myFixture.systemUnderTest.called);
  }

  @Test
  public void shouldReportMissingMethodOnFixtureClassWhenMethodCanNotBeFoundOnBothFixtureAndSystemUnderTest() {
    createFixture();
    String result = (String) statementExecutor.call("myInstance", "noSuchMethod");
    String expectedErrorMessage = String.format(MESSAGE_NO_METHOD_IN_CLASS, "noSuchMethod", 0,
        MyFixture.class.getName());
    assertTrue(result.contains(expectedErrorMessage));
  }
}
