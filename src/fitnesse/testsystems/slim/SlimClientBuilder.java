package fitnesse.testsystems.slim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

import fitnesse.FitNesseContext;
import fitnesse.socketservice.SocketFactory;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.MockCommandRunner;

import org.apache.commons.lang.ArrayUtils;

public class SlimClientBuilder extends ClientBuilder<SlimCommandRunningClient> {
  public static final String SLIM_PORT = "SLIM_PORT";
  public static final String SLIM_HOST = "SLIM_HOST";
  public static final String SLIM_FLAGS = "SLIM_FLAGS";
  private static final String SLIM_VERSION = "SLIM_VERSION";
  public static final String MANUALLY_START_TEST_RUNNER_ON_DEBUG = "MANUALLY_START_TEST_RUNNER_ON_DEBUG";
  public static final String SLIM_SSL = "SLIM_SSL";
  
  private static final AtomicInteger slimPortOffset = new AtomicInteger(0);


  private int slimPort;

  public SlimClientBuilder(Descriptor descriptor) {
    super(descriptor);
    slimPort = getNextSlimPort();
  }

  @Override
  public SlimCommandRunningClient build() throws IOException {
    CommandRunner commandRunner;

    if (useManualStartForTestSystem()) {
      commandRunner = new MockCommandRunner(getExecutionLogListener());
    } else {
      commandRunner = new CommandRunner(buildCommand(), "", createClasspathEnvironment(getClassPath()), getExecutionLogListener(), determineTimeout());
    }

    return new SlimCommandRunningClient(commandRunner, determineSlimHost(), getSlimPort(), determineTimeout(), getSlimVersion(), determineSSL(), determineHostSSLParameterClass());
  }

  protected String determineClientSSLParameterClass() {
      String sslParameterClassName = getVariable("slim.ssl");
      if (sslParameterClassName == null) {
    	  sslParameterClassName = getVariable(SLIM_SSL);
      }
      if (sslParameterClassName != null && sslParameterClassName.equalsIgnoreCase("false")) sslParameterClassName=null;
      return sslParameterClassName;
  }

  protected boolean determineSSL() {
      return (determineClientSSLParameterClass() != null );
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
    String slimFlags[] = getSlimFlags();
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
      ServerSocket socket = SocketFactory.tryCreateServerSocket(0);
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
      return "true".equalsIgnoreCase(useManualStart);
    }
    return false;
  }
}
