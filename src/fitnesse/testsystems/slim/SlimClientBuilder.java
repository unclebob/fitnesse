package fitnesse.testsystems.slim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.socketservice.SocketFactory;

import fitnesse.slim.JavaSlimFactory;
import fitnesse.slim.SlimService;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;
import util.StringUtil;

import static util.StringUtil.combineArrays;

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
      commandRunner = new CommandRunner(buildCommand(), "", createClasspathEnvironment(getClassPath()), determineTimeout());
    }

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort(), determineTimeout());
  }

  @Override
  protected String defaultTestRunner() {
    return "fitnesse.slim.SlimService";
  }

  protected String[] buildCommand() {
    String[] slimArguments = buildArguments();
    String[] slimCommandPrefix = super.buildCommand(getCommandPattern(), getTestRunner(), getClassPath());
    return combineArrays(slimCommandPrefix, slimArguments);
  }

  protected String[] buildArguments() {
    int slimSocket = getSlimPort();
    String slimFlags = getSlimFlags();
    if ("".equals(slimFlags))
      return new String[] { Integer.toString(slimSocket) };
    return new String[] { slimFlags, Integer.toString(slimSocket) };
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
      String port = getVariable("slim.port");
      if (port == null) {
        port = getVariable(SLIM_PORT);
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
      String poolSize = getVariable("slim.pool.size");
      if (poolSize != null) {
        return Integer.parseInt(poolSize);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return 10;
  }

  protected String determineSlimHost() {
    String slimHost = getVariable("slim.host");
    if (slimHost == null) {
      slimHost = getVariable(SLIM_HOST);
    }
    return slimHost == null ? "localhost" : slimHost;
  }

  protected String getSlimFlags() {
    String slimFlags = getVariable("slim.flags");
    if (slimFlags == null) {
      slimFlags = getVariable(SLIM_FLAGS);
    }
    return slimFlags == null ? "" : slimFlags;
  }

  protected int determineTimeout() {
    if (isDebug()) {
      try {
        String debugTimeout = getVariable("slim.debug.timeout");
        if (debugTimeout != null) {
          return Integer.parseInt(debugTimeout);
        }
      } catch (NumberFormatException e) {
        // stick with default
      }
    }
    try {
      String timeout = getVariable("slim.timeout");
      if (timeout != null) {
        return Integer.parseInt(timeout);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return 10;
  }

  private boolean useManualStartForTestSystem() {
    if (isDebug()) {
      String useManualStart = getVariable("manually.start.test.runner.on.debug");
      if (useManualStart == null) {
        useManualStart = getVariable(MANUALLY_START_TEST_RUNNER_ON_DEBUG);
      }
      return (useManualStart != null && useManualStart.toLowerCase().equals("true"));
    }
    return false;
  }
}
