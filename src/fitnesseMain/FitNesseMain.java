package fitnesseMain;

import fitnesse.ContextConfigurator;
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
    return launchFitNesse(arguments, properties);
  }

  public Integer launchFitNesse(Arguments arguments, Properties properties) throws Exception {
    configureLogging(arguments.hasVerboseLogging());
    loadPlugins();

    FitNesseContext context = loadContext(arguments, properties);

    logStartupInfo(context);

    update(arguments, context);
    return launch(arguments, context);
  }

  boolean update(Arguments arguments, FitNesseContext context) throws IOException {
    if (!arguments.isOmittingUpdates()) {
      Updater updater = new UpdaterImplementation(context);
      return updater.update();
    }
    return false;
  }

  private void loadPlugins() throws Exception {
    new PluginsClassLoader().addPluginsToClassLoader();
  }

  Integer launch(Arguments arguments, FitNesseContext context) throws Exception {
    if (!arguments.isInstallOnly()) {
      boolean started = context.fitNesse.start();
      if (started) {
        if (arguments.getCommand() != null) {
          return executeSingleCommand(arguments, context);
        }
      }
    }
    return null;
  }

  private int executeSingleCommand(Arguments arguments, FitNesseContext context) throws Exception {
    TestTextFormatter.finalErrorCount = 0;
    LOG.info("Executing command: " + arguments.getCommand());

    OutputStream os;

    boolean outputRedirectedToFile = arguments.getOutput() != null;

    if (outputRedirectedToFile) {
      LOG.info("-----Command Output redirected to " + arguments.getOutput() + "-----");
      os = new FileOutputStream(arguments.getOutput());
    } else {
      LOG.info("-----Command Output-----");
      os = System.out;
    }

    context.fitNesse.executeSingleCommand(arguments.getCommand(), os);
    context.fitNesse.stop();

    if (outputRedirectedToFile) {
      os.close();
    } else {
      LOG.info("-----Command Complete-----");
    }

    return TestTextFormatter.finalErrorCount;
  }

  private FitNesseContext loadContext(Arguments arguments, Properties properties) throws IOException, PluginException {
    Properties cascadedProperties = new Properties(properties);
    cascadedProperties.putAll(arguments.asProperties());
    return new ContextConfigurator(cascadedProperties).makeFitNesseContext();
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
