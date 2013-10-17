package fitnesse.testsystems.slim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

import fitnesse.socketservice.SocketFactory;

import fitnesse.slim.JavaSlimFactory;
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

  @Override
  public SlimCommandRunningClient build() throws IOException {
    CommandRunner commandRunner;

    if (useManualStartForTestSystem()) {
      commandRunner = new MockCommandRunner();
    } else {
      commandRunner = new CommandRunner(buildCommand(), "", descriptor.createClasspathEnvironment(descriptor.getClassPath()));
    }

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort());
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

  public int getSlimPort() {
    return slimPort;
  }

  private int findFreePort() {
    int port;
    try {
      ServerSocket socket = SocketFactory.tryCreateServerSocket(0);
      port = socket.getLocalPort();
      socket.close();
    } catch (Exception e) {
      port = -1;
    }
    return port;
  }

  private int getNextSlimPort() {
    final int base = getSlimPortBase();
    final int poolSize = getSlimPortPoolSize();

    if (base == 0) {
      return findFreePort();
    }

    synchronized (SlimClientBuilder.class) {
      int offset = slimPortOffset.get();
      offset = (offset + 1) % poolSize;
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
      if (port == null) {
        port = descriptor.getVariable(SLIM_PORT);
      }

      if (port != null) {
        return Integer.parseInt(port);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return 8085;
  }

  private int getSlimPortPoolSize() {
    try {
      String poolSize = descriptor.getVariable("slim.pool.size");
      if (poolSize != null) {
        return Integer.parseInt(poolSize);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return 10;
  }

  String determineSlimHost() {
    String slimHost = descriptor.getVariable("slim.host");
    if (slimHost == null) {
      slimHost = descriptor.getVariable(SLIM_HOST);
    }
    return slimHost == null ? "localhost" : slimHost;
  }

  String getSlimFlags() {
    String slimFlags = descriptor.getVariable("slim.flags");
    if (slimFlags == null) {
      slimFlags = descriptor.getVariable(SLIM_FLAGS);
    }
    return slimFlags == null ? "" : slimFlags;
  }

  private boolean useManualStartForTestSystem() {
    if (descriptor.isDebug()) {
      String useManualStart = descriptor.getVariable("manually.start.test.runner.on.debug");
      if (useManualStart == null) {
        useManualStart = descriptor.getVariable(MANUALLY_START_TEST_RUNNER_ON_DEBUG);
      }
      return (useManualStart != null && useManualStart.toLowerCase().equals("true"));
    }
    return false;
  }
}
