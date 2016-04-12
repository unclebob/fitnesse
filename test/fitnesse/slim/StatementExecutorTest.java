package fitnesse.slim;

import org.junit.Before;

public class StatementExecutorTest extends StatementExecutorTestBase {

  public static class MySystemUnderTestJava extends MySystemUnderTestBase {
    private boolean echoCalled = false;
    private boolean speakCalled;
    private boolean shoutCalled = false;

    @Override
    public void speak() {
      speakCalled = true;
    }

    @Override
    public boolean speakCalled() {
      return speakCalled;
    }

    @Override
    public void echo() {
      echoCalled = true;
    }

    @Override
    public boolean echoCalled() {
      return echoCalled;
    }

    public void shout() {shoutCalled = true;}

    public boolean shoutCalled() {return shoutCalled;}

  }

  public static class MyAnnotatedSystemUnderTestFixtureJava extends
      MyAnnotatedSystemUnderTestFixture {
    @SystemUnderTest
    public MySystemUnderTestBase sut = new MySystemUnderTestJava();
    private boolean echoCalled = false;

    @Override
    public void echo() {
      echoCalled = true;
    }

    @Override
    public boolean echoCalled() {
      return echoCalled;
    }

    @Override
    public MySystemUnderTestBase getSystemUnderTest() {
      return sut;
    }
  }

  public static class FixtureWithNamedSystemUnderTestJava extends FixtureWithNamedSystemUnderTestBase {
    public MySystemUnderTestBase systemUnderTest = new MySystemUnderTestJava();
    private boolean echoCalled;

    @Override
    public void echo() {
      echoCalled = true;
    }

    @Override
    public boolean echoCalled() {
      return echoCalled;
    }

    @Override
    public MySystemUnderTestBase getSystemUnderTest() {
      return systemUnderTest;
    }
  }

  public static class SimpleFixtureJava extends SimpleFixture {
    private boolean echoCalled;

    @Override
    public void echo() {
      echoCalled = true;
    }

    @Override
    public boolean echoCalled() {
      return echoCalled;
    }
  }

  public static class EchoSupportJava extends EchoSupport {
    private boolean echoCalled;
    private boolean speakCalled;

    @Override
    public void echo() {
      echoCalled = true;
    }

    @Override
    public void speak() {
      speakCalled = true;
    }

    @Override
    public boolean speakCalled() {
      return speakCalled;
    }

    @Override
    public boolean echoCalled() {
      return echoCalled;
    }
  }

  public static class FileSupportJava extends FileSupport {
    private boolean deleteCalled;

    @Override
    public void delete(String fileName) {
      deleteCalled = true;
    }

    @Override
    public boolean deleteCalled() {
      return deleteCalled;
    }
  }

  @Override
  @Before
  public final void init() {
    statementExecutor = new StatementExecutor();
  }

  @Override
  protected String annotatedFixtureName() {
    return MyAnnotatedSystemUnderTestFixtureJava.class.getName();
  }

  @Override
  protected String namedFixtureName() {
    return FixtureWithNamedSystemUnderTestJava.class.getName();
  }

  @Override
  protected String simpleFixtureName() {
    return SimpleFixtureJava.class.getName();
  }

  @Override
  protected String echoLibraryName() {
    return EchoSupportJava.class.getName();
  }

  @Override
  protected String fileSupportName() {
    return FileSupportJava.class.getName();
  }
}
