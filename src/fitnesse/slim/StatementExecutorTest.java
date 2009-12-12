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
  private int library = 0;
  private StatementExecutor statementExecutor;

  public static class MySystemUnderTest {
    public boolean speakCalled = false;
    public boolean echoCalled = false;
    
    public void speak() {
      speakCalled = true;
    }
    
    public void echo() {
      echoCalled = true;
    }
  }

  public static class MyAnnotatedSystemUnderTestFixture {
    public boolean called = false;

    @SystemUnderTest
    public MySystemUnderTest sut = new MySystemUnderTest();

    public void echo() {
      called = true;
    }
  }

  public static class FixtureWithNamedSystemUnderTest {
    public boolean called = false;

    public MySystemUnderTest systemUnderTest = new MySystemUnderTest();

    public void echo() {
      called = true;
    }
  }
  
  public static class SimpleFixture {
    public boolean echoCalled = false;
    public void echo() {
      echoCalled = true;
    }
  }
  public static class EchoSupport {
    public boolean echoCalled = false;
    public boolean speakCalled = false;
    public void echo() {
      echoCalled = true;
    }
    public void speak() {
      speakCalled = true;
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
    assertFalse(myInstance.sut.speakCalled);
  }

  private MyAnnotatedSystemUnderTestFixture createAnnotatedFixture() {
    createFixtureInstance(MyAnnotatedSystemUnderTestFixture.class);

    MyAnnotatedSystemUnderTestFixture myInstance = (MyAnnotatedSystemUnderTestFixture) statementExecutor.getInstance(INSTANCE_NAME);
    assertFalse(myInstance.called);
    return myInstance;
  }

  private void createFixtureInstance(Class<?> fixtureClass) {
    Object created = statementExecutor.create(INSTANCE_NAME, ((Class<?>) fixtureClass).getName(),
        new Object[] {});
    assertEquals("OK", created);
  }

  @Test
  public void shouldCallMethodOnFieldAnnotatedWithSystemUnderTestWhenFixtureDoesNotHaveMethod() {
    MyAnnotatedSystemUnderTestFixture myFixture = createAnnotatedFixture();

    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.sut.speakCalled);
  }
  
  @Test
  public void shouldCallMethodOnFieldNamed_systemUnderTest_WhenFixtureDoesNotHaveMethod() {
    FixtureWithNamedSystemUnderTest myFixture = createNamedFixture();
    
    executeStatementAndVerifyResultIsVoid();
    assertFalse(myFixture.called);
    assertTrue(myFixture.systemUnderTest.speakCalled);
  }

  private void executeStatementAndVerifyResultIsVoid() {
    Object result = statementExecutor.call(INSTANCE_NAME, "speak");
    assertEquals("/__VOID__/", result);
  }
  
  private FixtureWithNamedSystemUnderTest createNamedFixture() {
    createFixtureInstance(FixtureWithNamedSystemUnderTest.class);
    FixtureWithNamedSystemUnderTest myInstance = (FixtureWithNamedSystemUnderTest) statementExecutor.getInstance(INSTANCE_NAME);
    assertFalse(myInstance.called);
    return myInstance;
  }
  private SimpleFixture createSimpleFixture() {
    createFixtureInstance(SimpleFixture.class);
    SimpleFixture myInstance = (SimpleFixture) statementExecutor.getInstance(INSTANCE_NAME);
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
  public void shouldPreferMethodOnFixtureOverMethodOnSystemUnderTest() {
    FixtureWithNamedSystemUnderTest instance = createNamedFixture();
    statementExecutor.call(INSTANCE_NAME, "echo");
    assertFalse(instance.systemUnderTest.echoCalled);
    assertTrue(instance.called);
  }

  @Test
  public void shouldPreferMethodOnFixtureOverMethodOnLibrary() {
    SimpleFixture instance = createSimpleFixture();
    EchoSupport echoLibrary = createEchoLibrary();
    statementExecutor.call(INSTANCE_NAME, "echo");
    assertFalse(echoLibrary.echoCalled);
    assertTrue(instance.echoCalled);
  }
  
  @Test
  public void shouldPreferMethodOnSystemUnderTestOverMethodOnLibrary() {
    FixtureWithNamedSystemUnderTest instance = createNamedFixture();
    EchoSupport echoLibrary = createEchoLibrary();
    
    statementExecutor.call(INSTANCE_NAME, "speak");
    assertFalse(echoLibrary.speakCalled);
    assertTrue(instance.systemUnderTest.speakCalled);
  }
  
  @Test
  public void shouldPreferMethodsOnLibrariesCreatedLaterOverMethodsOnLibrariesCreatedEarlier() {
    createSimpleFixture();
    EchoSupport echoLibrary1 = createEchoLibrary();
    EchoSupport echoLibrary2 = createEchoLibrary();
    EchoSupport echoLibrary3 = createEchoLibrary();
    statementExecutor.call(INSTANCE_NAME, "speak");
    assertFalse(echoLibrary1.speakCalled);
    assertFalse(echoLibrary2.speakCalled);
    assertTrue(echoLibrary3.speakCalled);
  }
  
  private EchoSupport createEchoLibrary() {
    String instanceName = "library" + library++;
    statementExecutor.create(instanceName, EchoSupport.class.getName(), new Object[]{});
    return (EchoSupport) statementExecutor.getInstance(instanceName);
    
  }
  
  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture_WithSystemUnderTestInFixture() {
    createNamedFixture();
    FileSupport library1 = createFileSupportLibrary();
    assertNotNull(library1);
    Object result = statementExecutor.call(INSTANCE_NAME, "delete", "filename.txt");
    assertEquals("/__VOID__/", result);
    assertTrue(library1.called);
    
  }

  private FileSupport createFileSupportLibrary() {
    String instanceName = "library" + library++;
    statementExecutor.create(instanceName, FileSupport.class.getName(), new Object[]{});
    FileSupport library1 = (FileSupport) statementExecutor.getInstance(instanceName);
    return library1;
  }
  
  @Test
  public void shouldCallMethodOnInstallLibraryWhenMethodIsNotFoundInAFixture() {
    createFixtureInstance(EchoSupport.class);
    FileSupport library1 = createFileSupportLibrary();
    assertNotNull(library1);
    Object result = statementExecutor.call(INSTANCE_NAME, "delete", "filename.txt");
    assertEquals("/__VOID__/", result);
    assertTrue(library1.called);
    
  }
}
