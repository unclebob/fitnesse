package fitnesseMain;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.PluginException;
import fitnesse.FitNesseContext;
import fitnesse.Updater;
import fitnesse.components.PluginsClassLoader;
import fitnesse.reporting.TestTextFormatter;
import fitnesse.updates.UpdaterImplementation;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class FitNesseMain {
  private static final Logger LOG = Logger.getLogger(FitNesseMain.class.getName());

  public static void main(String[] args) throws Exception {
    Arguments arguments = null;
    try {
      arguments = new Arguments(args);
    } catch (IllegalArgumentException e) {
      Arguments.printUsage();
      exit(1);
    }
    Integer exitCode = new FitNesseMain().launchFitNesse(arguments);
    if (exitCode != null) {
      exit(exitCode);
    }
  }

  protected static void exit(int exitCode) {
    System.exit(exitCode);
  }

  public Integer launchFitNesse(Arguments arguments) throws Exception {
    Properties properties = loadConfigFile(arguments.getConfigFile());
    Properties cascadedProperties = cascadeProperties(arguments, properties);

    return launchFitNesse(cascadedProperties);
  }

  public Integer launchFitNesse(Properties properties) throws Exception {
    configureLogging("verbose".equalsIgnoreCase(properties.getProperty(ConfigurationParameter.LOG_LEVEL)));
    loadPlugins();

    FitNesseContext context = loadContext(properties);

    logStartupInfo(context);

    update(context);

    return launch(context);
  }

  private boolean update(FitNesseContext context) throws IOException {
    if (!"true".equalsIgnoreCase(context.getProperty(ConfigurationParameter.OMITTING_UPDATES))) {
      Updater updater = new UpdaterImplementation(context);
      return updater.update();
    }
    return false;
  }

  private void loadPlugins() throws Exception {
    new PluginsClassLoader().addPluginsToClassLoader();
  }

  Integer launch(FitNesseContext context) throws Exception {
    if (!"true".equalsIgnoreCase(context.getProperty(ConfigurationParameter.INSTALL_ONLY))) {
      boolean started = context.fitNesse.start();
      if (started) {
        String command = context.getProperty(ConfigurationParameter.COMMAND);
        if (command != null) {
          String output = context.getProperty(ConfigurationParameter.OUTPUT);
          return executeSingleCommand(context.fitNesse, command, output);
        }
      }
    }
    return null;
  }

  private int executeSingleCommand(FitNesse fitNesse, String command, String outputFile) throws Exception {
    TestTextFormatter.finalErrorCount = 0;

    LOG.info("Executing command: " + command);

    OutputStream os;

    boolean outputRedirectedToFile = outputFile != null;

    if (outputRedirectedToFile) {
      LOG.info("-----Command Output redirected to " + outputFile + "-----");
      os = new FileOutputStream(outputFile);
    } else {
      LOG.info("-----Command Output-----");
      os = System.out;
    }

    fitNesse.executeSingleCommand(command, os);
    fitNesse.stop();

    if (outputRedirectedToFile) {
      os.close();
    } else {
      LOG.info("-----Command Complete-----");
    }

    return TestTextFormatter.finalErrorCount;
  }

  private FitNesseContext loadContext(Properties properties) throws IOException, PluginException {
    return new ContextConfigurator(properties).makeFitNesseContext();
  }

  private Properties cascadeProperties(Arguments arguments, Properties properties) {
    Properties configProperties = new Properties(System.getProperties());
    configProperties.putAll(properties);
    Properties argumentProperties = new Properties(configProperties);
    argumentProperties.putAll(arguments.asProperties());
    return argumentProperties;
  }

  private void logStartupInfo(FitNesseContext context) {
    LOG.info("root page: " + context.root);
    LOG.info("logger: " + (context.logger == null ? "none" : context.logger.toString()));
    LOG.info("authenticator: " + context.authenticator);
    LOG.info("page factory: " + context.pageFactory);
    LOG.info("page theme: " + context.pageFactory.getTheme());
    LOG.info("Starting FitNesse on port: " + context.port);
  }

  public Properties loadConfigFile(final String propertiesFile) {
    FileInputStream propertiesStream = null;
    Properties properties = new Properties();
    File configurationFile = new File(propertiesFile);
    try {
      propertiesStream = new FileInputStream(configurationFile);
    } catch (FileNotFoundException e) {
      try {
        LOG.info(String.format("No configuration file found (%s)", configurationFile.getCanonicalPath()));
      } catch (IOException e1) {
        LOG.info(String.format("No configuration file found (%s)", propertiesFile));
      }
    }

    if (propertiesStream != null) {
      try {
        properties.load(propertiesStream);
        propertiesStream.close();
      } catch (IOException e) {
        LOG.log(Level.WARNING, String.format("Error reading configuration: %s", e.getMessage()));
      }
    }

    return properties;
  }

  public void configureLogging(boolean verbose) {
    if (loggingSystemPropertiesDefined()) {
      return;
    }

    InputStream in = FitNesseMain.class.getResourceAsStream((verbose ? "verbose-" : "") + "logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(in);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Log configuration failed", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          LOG.log(Level.SEVERE, "Unable to close Log configuration file", e);
        }
      }
    }
    LOG.finest("Configured verbose logging");
  }

  private boolean loggingSystemPropertiesDefined() {
    return System.getProperty("java.util.logging.config.class") != null ||
            System.getProperty("java.util.logging.config.file") != null;
  }

}
