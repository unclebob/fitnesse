package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.AfterClass;
import org.junit.Before;

import fitnesse.slim.StatementExecutorTestBase.Delete;
import fitnesse.slim.StatementExecutorTestBase.Echo;
import fitnesse.slim.StatementExecutorTestBase.EchoSupport;
import fitnesse.slim.StatementExecutorTestBase.FileSupport;
import fitnesse.slim.StatementExecutorTestBase.FixtureWithNamedSystemUnderTestBase;
import fitnesse.slim.StatementExecutorTestBase.MyAnnotatedSystemUnderTestFixture;
import fitnesse.slim.StatementExecutorTestBase.MySystemUnderTestBase;
import fitnesse.slim.StatementExecutorTestBase.SimpleFixture;
import fitnesse.slim.StatementExecutorTestBase.Speak;
import fitnesse.slim.StatementExecutorTestBase.SystemUnderTestFixture;

public abstract class Jsr223StatementExecutorTestBase extends StatementExecutorTestBase {
  protected static Jsr223SlimFactory slimFactory;
  protected static Jsr223Bridge bridge;

  public static class FileSupportJsr223 extends FileSupport {

    private FixtureProxyJsr223 fixtureProxy;

    public FileSupportJsr223(FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void delete(String fileName) {
      fixtureProxy.delete(fileName);
    }

    public boolean deleteCalled() {
      return fixtureProxy.deleteCalled();
    }
  }

  public static class EchoSupportJsr223 extends EchoSupport {

    private FixtureProxyJsr223 fixtureProxy;

    public EchoSupportJsr223(FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void echo() {
      fixtureProxy.echo();
    }

    public boolean echoCalled() {
      return fixtureProxy.echoCalled();
    }

    public void speak() {
      fixtureProxy.speak();
    }

    public boolean speakCalled() {
      return fixtureProxy.speakCalled();
    }
  }

  public static class SimpleFixtureJsr223 extends SimpleFixture {
    private FixtureProxyJsr223 fixtureProxy;

    public SimpleFixtureJsr223(FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void echo() {
      fixtureProxy.echo();
    }

    public boolean echoCalled() {
      return fixtureProxy.echoCalled();
    }
  }

  public static class FixtureWithNamedSystemUnderTestJsr223 extends
      FixtureWithNamedSystemUnderTestBase {

    private FixtureProxyJsr223 fixtureProxy;

    public FixtureWithNamedSystemUnderTestJsr223(FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void echo() {
      fixtureProxy.echo();
    }

    public boolean echoCalled() {
      return fixtureProxy.echoCalled();
    }

    public MySystemUnderTestBase getSystemUnderTest() {
      return fixtureProxy.getSystemUnderTest();
    }
  }

  public static class MySystemUnderTestJsr223 extends MySystemUnderTestBase {
    private FixtureProxyJsr223 fixtureProxy;

    public MySystemUnderTestJsr223(FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void echo() {
      fixtureProxy.echo();
    }

    public boolean echoCalled() {
      return fixtureProxy.echoCalled();
    }

    public void speak() {
      fixtureProxy.speak();
    }

    public boolean speakCalled() {
      return fixtureProxy.speakCalled();
    }
  }

  public static class MyAnnotatedSystemUnderTestFixtureJsr223 extends
      MyAnnotatedSystemUnderTestFixture {
    private FixtureProxyJsr223 fixtureProxy;

    public MyAnnotatedSystemUnderTestFixtureJsr223(
        FixtureProxyJsr223 fixtureProxy) {
      this.fixtureProxy = fixtureProxy;
    }

    public void echo() {
      fixtureProxy.echo();
    }

    public boolean echoCalled() {
      return fixtureProxy.echoCalled();
    }

    public MySystemUnderTestBase getSystemUnderTest() {
      return fixtureProxy.getSystemUnderTest();
    }
  }

  public static class FixtureProxyJsr223 implements Echo, Speak, Delete,
      SystemUnderTestFixture {

    private Object proxy;

    public FixtureProxyJsr223(Object instance) {
      proxy = instance;
    }

    public void echo() {
    }

    public boolean echoCalled() {
      return (Boolean) callMethod("echoCalled");
    }

    public void speak() {
    }

    public boolean speakCalled() {
      return (Boolean) callMethod("speakCalled");
    }

    public void delete(String fileName) {
    }

    public boolean deleteCalled() {
      return (Boolean) callMethod("deleteCalled");
    }

    public MySystemUnderTestBase getSystemUnderTest() {
      return new MySystemUnderTestJsr223(new FixtureProxyJsr223(
          callMethod("getSystemUnderTest")));
    }

    private Object callMethod(String method, Object... args) {
      try {
        return bridge.invokeMethod(proxy, method, args);
      } catch (Throwable e) {
        return e.toString();
      }
    }
  }

  @AfterClass
  public static void tearDownClass() {
    slimFactory.stop();
  }

  @Override
  @Before
  public void init() throws Exception {
    statementExecutor = slimFactory.getStatementExecutor();
    statementExecutor.addPath(getTestModulePath());
  }

  protected abstract String getTestModulePath();

  @Override
  protected Echo getVerifiedInstance() {
    FixtureProxyJsr223 myInstance = new FixtureProxyJsr223(statementExecutor
        .getInstance(INSTANCE_NAME));
    assertFalse(myInstance.echoCalled());
    return myInstance;
  }

  protected void createFixtureInstance(String fixtureClass) {
    Object created = statementExecutor.create(INSTANCE_NAME, fixtureClass,
        new Object[] {});
    assertEquals("OK", created);
  }

  @Override
  protected MyAnnotatedSystemUnderTestFixture createAnnotatedFixture() {
    createFixtureInstance(annotatedFixtureName());
    return new MyAnnotatedSystemUnderTestFixtureJsr223(
        (FixtureProxyJsr223) getVerifiedInstance());
  }

  @Override
  protected FixtureWithNamedSystemUnderTestBase createNamedFixture() {
    createFixtureInstance(namedFixtureName());
    return new FixtureWithNamedSystemUnderTestJsr223(
        (FixtureProxyJsr223) getVerifiedInstance());
  }

  @Override
  protected SimpleFixture createSimpleFixture() {
    createFixtureInstance(simpleFixtureName());
    return new SimpleFixtureJsr223((FixtureProxyJsr223) getVerifiedInstance());
  }

  @Override
  protected EchoSupport createEchoLibrary() {
    String instanceName = "library" + library++;
    Object created = statementExecutor.create(instanceName, echoLibraryName(),
        new Object[] {});
    assertEquals("OK", created);
    return new EchoSupportJsr223(new FixtureProxyJsr223(statementExecutor
        .getInstance(instanceName)));
  }

  @Override
  protected FileSupport createFileSupportLibrary() {
    String instanceName = "library" + library++;
    Object created = statementExecutor.create(instanceName, fileSupportName(),
        new Object[] {});
    assertEquals("OK", created);
    return new FileSupportJsr223(new FixtureProxyJsr223(statementExecutor
        .getInstance(instanceName)));
  }

  @Override
  protected String annotatedFixtureName() {
    return "MyAnnotatedSystemUnderTestFixture";
  }

  @Override
  protected String namedFixtureName() {
    return "FixtureWithNamedSystemUnderTest";
  }

  @Override
  protected String echoLibraryName() {
    return "EchoSupport";
  }

  @Override
  protected String fileSupportName() {
    return "FileSupport";
  }

  @Override
  protected String simpleFixtureName() {
    return "SimpleFixture";
  }

}
