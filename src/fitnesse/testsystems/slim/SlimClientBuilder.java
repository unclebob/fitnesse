package fitnesse.testsystems.slim;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.*;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class SlimClientBuilder extends ClientBuilder {

  private static final AtomicInteger slimPortOffset = new AtomicInteger(0);
  private final int slimPort;
  private final Descriptor descriptor;
  private final PageData data;
  private SlimClient slimClient;
  private boolean started;

  public SlimClientBuilder(WikiPage page, Descriptor descriptor) {
    super(page);
    this.data = page.getData();
    this.descriptor = descriptor;
    slimPort = getNextSlimPort();
  }

  public void start() throws IOException {
    final String classPath = descriptor.getClassPath();
    final String slimArguments = buildArguments();
    CommandRunner slimRunner;
    if (fastTest) {
      slimRunner = new MockCommandRunner();
      createSlimService(slimArguments);
    }
    else if (manualStart) {
      slimRunner = new MockCommandRunner();
    } else {
      slimRunner = new CommandRunner(buildCommand(), "", createClasspathEnvironment(classPath));
    }
    setExecutionLog(new ExecutionLog(page, slimRunner));

    slimRunner.asynchronousStart();

    slimClient = new SlimClient(slimRunner, determineSlimHost(), getSlimPort(), fastTest, manualStart);

    waitForConnection();
    started = true;
  }

  public String buildCommand() {
    String slimArguments = buildArguments();
    String slimCommandPrefix = super.buildCommand(descriptor);
    return String.format("%s %s", slimCommandPrefix, slimArguments);
  }

  private String buildArguments() {
    int slimSocket = getSlimPort();
    String slimFlags = getSlimFlags();
    return String.format("%s %d", slimFlags, slimSocket);
  }

  //For testing only.  Makes responder faster.
  void createSlimService(String args) throws SocketException {
    while (!tryCreateSlimService(args))
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
  }

  // For testing only
  private boolean tryCreateSlimService(String args) throws SocketException {
    try {
      SlimService.parseCommandLine(args.trim().split(" "));
      SlimService.startWithFactoryAsync(new JavaSlimFactory());
      return true;
    } catch (SocketException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  void waitForConnection() {
    while (!isConnected())
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
  }

  private boolean isConnected() {
    try {
      slimClient.connect();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public int getSlimPort() {
    return slimPort;
  }

  private int findFreePort() {
    int port;
    try {
      ServerSocket socket = new ServerSocket(0);
      port = socket.getLocalPort();
      socket.close();
    } catch (Exception e) {
      port = -1;
    }
    return port;
  }

  private int getNextSlimPort() {
    int base;

    if (System.getProperty("slim.port") != null) {
      base = Integer.parseInt(System.getProperty("slim.port"));
    } else {
      base = getSlimPortBase();
    }

    if (base == 0) {
      return findFreePort();
    }

    synchronized (slimPortOffset) {
      int offset = slimPortOffset.get();
      offset = (offset + 1) % 10;
      slimPortOffset.set(offset);
      return offset + base;
    }
  }

  public static void clearSlimPortOffset() {
    slimPortOffset.set(0);
  }

  private int getSlimPortBase() {
    int base = 8085;
    try {
      String slimPort = data.getVariable("SLIM_PORT");
      if (slimPort != null) {
        int slimPortInt = Integer.parseInt(slimPort);
        base = slimPortInt;
      }
    } catch (Exception e) {
    }
    return base;
  }

  String determineSlimHost() {
    String slimHost = data.getVariable("SLIM_HOST");
    return slimHost == null ? "localhost" : slimHost;
  }

  String getSlimFlags() {
    String slimFlags = data.getVariable("SLIM_FLAGS");
    if (slimFlags == null)
      slimFlags = "";
    return slimFlags;
  }


  public SlimClient getSlimClient() {
    return slimClient;
  }
}
