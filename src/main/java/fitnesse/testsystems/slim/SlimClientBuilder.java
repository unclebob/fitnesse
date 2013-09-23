package fitnesse.testsystems.slim;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimCommandRunningClient;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;

public class SlimClientBuilder extends ClientBuilder<SlimCommandRunningClient> {

  public static final String SLIM_PORT = "SLIM_PORT";
  public static final String SLIM_HOST = "SLIM_HOST";
  public static final String SLIM_FLAGS = "SLIM_FLAGS";
  public static final String MANUALLY_START_TEST_RUNNER_ON_DEBUG = "MANUALLY_START_TEST_RUNNER_ON_DEBUG";

  private static final AtomicInteger slimPortOffset = new AtomicInteger(0);

  private final int slimPort;

  public SlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
    slimPort = getNextSlimPort();
  }

  public static String defaultTestRunner() {
    return "fitnesse.slim.SlimService";
  }

  @Override
  public SlimCommandRunningClient build() throws IOException {
    CommandRunner commandRunner;

    if (useManualStartForTestSystem()) {
      commandRunner = new MockCommandRunner();
    } else {
      commandRunner = new CommandRunner(buildCommand(), "", descriptor.createClasspathEnvironment(descriptor.getClassPath()));
    }

    return new SlimCommandRunningClient(descriptor.getTestRunner(), commandRunner, determineSlimHost(), getSlimPort());
  }

  protected String buildCommand() {
    String slimArguments = buildArguments();
    String slimCommandPrefix = super.buildCommand(descriptor.getCommandPattern(), descriptor.getTestRunner(), descriptor.getClassPath());
    return String.format("%s %s", slimCommandPrefix, slimArguments);
  }

  protected String buildArguments() {
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
    int base = getSlimPortBase();

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
    try {
      String port = descriptor.getVariable("slim.port");
      if (port != null) {
        return Integer.parseInt(port);
      }

      port = descriptor.getVariable(SLIM_PORT);
      if (port != null) {
        return Integer.parseInt(port);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return 8085;
  }

  String determineSlimHost() {
    String slimHost = descriptor.getVariable(SLIM_HOST);
    return slimHost == null ? "localhost" : slimHost;
  }

  String getSlimFlags() {
    String slimFlags = descriptor.getVariable(SLIM_FLAGS);
    return slimFlags == null ? "" : slimFlags;
  }

  private boolean useManualStartForTestSystem() {
    if (descriptor.isDebug()) {
      String useManualStart = descriptor.getVariable(MANUALLY_START_TEST_RUNNER_ON_DEBUG);
      return (useManualStart != null && useManualStart.toLowerCase().equals("true"));
    }
    return false;
  }

}
