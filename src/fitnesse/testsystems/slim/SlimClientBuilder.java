package fitnesse.testsystems.slim;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ArrayUtils;

import fitnesse.FitNesseContext;
import fitnesse.slim.SlimPipeSocket;
import fitnesse.socketservice.ClientSocketFactory;
import fitnesse.socketservice.PlainClientSocketFactory;
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.socketservice.SslClientSocketFactory;
import fitnesse.testsystems.*;

import static fitnesse.slim.SlimPipeSocket.STDERR_PREFIX;
import static fitnesse.slim.SlimPipeSocket.STDOUT_PREFIX;

public class SlimClientBuilder extends ClientBuilder<SlimCommandRunningClient> {
  public static final String SLIM_PORT = "SLIM_PORT";
  public static final String SLIM_HOST = "SLIM_HOST";
  public static final String SLIM_FLAGS = "SLIM_FLAGS";
  private static final String SLIM_VERSION = "SLIM_VERSION";
  public static final String MANUALLY_START_TEST_RUNNER_ON_DEBUG = "MANUALLY_START_TEST_RUNNER_ON_DEBUG";
  public static final String MANUALLY_START_TEST_RUNNER = "MANUALLY_START_TEST_RUNNER";
  public static final String SLIM_SSL = "SLIM_SSL";
  public static final int SLIM_USE_PIPE_PORT = 1;

  private static final AtomicInteger slimPortOffset = new AtomicInteger(0);


  private int slimPort;

  public SlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
    slimPort = getNextSlimPort();
  }

  @Override
  public SlimCommandRunningClient build() {
    CommandRunner commandRunner = determineCommandRunner();

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(),
        getSlimPort(), determineTimeout(), getSlimVersion(),
        determineSocketFactory(commandRunner));

  }

  protected CommandRunner determineCommandRunner() {
    if (getSlimPort() == SLIM_USE_PIPE_PORT) {
      // Wrap executionLogListener
      return new CommandRunner(buildCommand(),
        createClasspathEnvironment(getClassPath()),
          getExecutionLogListener(), determineTimeout()) {

        @Override
        protected void redirectOutputs(Process process, final ExecutionLogListener executionLogListener) throws IOException {
          InputStream stderr = process.getErrorStream();
          new Thread(new OutputReadingRunnable(stderr, new OutputWriter() {
            @Override
            public void write(String output) {
              // Separate StdOut and StdErr and remove prefix"
              String originalMsg;
              originalMsg = extractOriginalMessage(output, STDOUT_PREFIX);
              if (originalMsg != null) {
                executionLogListener.stdOut(originalMsg);
              } else {
                originalMsg = extractOriginalMessage(output, STDERR_PREFIX);
                if (originalMsg != null) {
                  executionLogListener.stdErr(originalMsg);
                  setCommandErrorMessage(originalMsg);
                } else {
                  executionLogListener.stdOut(output);
                }
              }
            }

            /**
             * This reverts the wrap that the LoggingOutputStream.flush method
             * is doing.
             *
             * @param prefixedMessage
             * @param level
             * @return == null : the message is not prefixed with the given
             *         level != null : the original message content
             */
            private String extractOriginalMessage(String prefixedMessage,
                String level) {
              if (prefixedMessage.startsWith(level))
                return prefixedMessage.substring(level.length()
                    + SlimPipeSocket.FOLLOWING_LINE_PREFIX.length());
              return null;
            }

          }), "CommandRunner stdOutErr").start();

        }
      };

    } else if (useManualStartForTestSystem()) {
      return new MockCommandRunner(
          "Connection to running SlimService: " + determineSlimHost() + ":"
              + getSlimPort(), getExecutionLogListener(), determineTimeout());
    } else {
      return new CommandRunner(buildCommand(), createClasspathEnvironment(getClassPath()), getExecutionLogListener(), determineTimeout());
    }
  }

  protected ClientSocketFactory determineSocketFactory(CommandRunner commandRunner) {
    if (getSlimPort() == SLIM_USE_PIPE_PORT) {
      return new PipeBasedSocketFactory(commandRunner);
    } else if ((determineClientSSLParameterClass() != null)) {
      return new SslClientSocketFactory(determineHostSSLParameterClass());
    } else {
      return new PlainClientSocketFactory();
    }
  }

  protected String determineClientSSLParameterClass() {
      String sslParameterClassName = getVariable("slim.ssl");
      if (sslParameterClassName == null) {
    	  sslParameterClassName = getVariable(SLIM_SSL);
      }
      if (sslParameterClassName != null && sslParameterClassName.equalsIgnoreCase("false")) sslParameterClassName=null;
      return sslParameterClassName;
  }

  protected String determineHostSSLParameterClass() {
      return getVariable(FitNesseContext.SSL_PARAMETER_CLASS_PROPERTY);
  }

  public double getSlimVersion() {
    double version = SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION;
    try {
      String slimVersion = getVariable(SLIM_VERSION);
      if (slimVersion != null) {
        version = Double.valueOf(slimVersion);
      }
    } catch (NumberFormatException e) {
      // stick with default
    }
    return version;
  }

  @Override
  protected String defaultTestRunner() {
    return "fitnesse.slim.SlimService";
  }

  protected String[] buildCommand() {
    String[] slimArguments = buildArguments();
    String[] slimCommandPrefix = super.buildCommand(getCommandPattern(), getTestRunner(), getClassPath());
    return (String[]) ArrayUtils.addAll(slimCommandPrefix, slimArguments);
  }

  protected String[] buildArguments() {
    Object[] arguments = new String[] {};
    String useSSL =  determineClientSSLParameterClass();
    if (useSSL != null){
    	arguments = ArrayUtils.add(arguments, "-ssl");
    	arguments = ArrayUtils.add(arguments, useSSL);
    }
    String[] slimFlags = getSlimFlags();
    if (slimFlags != null)
    	for (String flag : slimFlags)
    		arguments = ArrayUtils.add(arguments, flag);

	arguments = ArrayUtils.add(arguments, Integer.toString(getSlimPort()));

    return (String[]) arguments;
  }

  public int getSlimPort() {
    return slimPort;
  }

  protected void setSlimPort(int slimPort) {
    this.slimPort = slimPort;
  }

  private int findFreePort() {
    int port;
    try {
      ServerSocket socket = new PlainServerSocketFactory().createServerSocket(0);
      port = socket.getLocalPort();
      socket.close();
    } catch (Exception e) {
      port = -1;
    }
    return port;
  }

  protected int getNextSlimPort() {
    final int base = getSlimPortBase();
    final int poolSize = getSlimPortPoolSize();

    if (base == 0) {
      return findFreePort();
    }
    if (base == SLIM_USE_PIPE_PORT)
      return SLIM_USE_PIPE_PORT;

    synchronized (SlimClientBuilder.class) {
      int offset = slimPortOffset.get();
      int port = offset + base;
      offset = (offset + 1) % poolSize;
      slimPortOffset.set(offset);
      // is port available??
      return port;
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
    return SLIM_USE_PIPE_PORT;
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

  protected String[] getSlimFlags() {
    String slimFlags = getVariable("slim.flags");
    if (slimFlags == null) {
      slimFlags = getVariable(SLIM_FLAGS);
    }
    return slimFlags == null ? new String[] {} : parseCommandLine(slimFlags);
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
      if (useManualStart != null) {
        return "true".equalsIgnoreCase(useManualStart);
      }
    }
    String useManualStart = getVariable("manually.start.test.runner");
    if (useManualStart == null) {
      useManualStart = getVariable(MANUALLY_START_TEST_RUNNER);
    }
    return "true".equalsIgnoreCase(useManualStart);

  }
}
