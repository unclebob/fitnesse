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

import static fitnesse.ConfigurationParameter.*;

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
    ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults();
    contextConfigurator = contextConfigurator.updatedWith(System.getProperties());
    contextConfigurator = contextConfigurator.updatedWith(ConfigurationParameter.loadProperties(new File(arguments.getConfigFile(contextConfigurator))));
    contextConfigurator = arguments.update(contextConfigurator);

    return launchFitNesse(contextConfigurator);
  }

  public Integer launchFitNesse(ContextConfigurator contextConfigurator) throws Exception {
    configureLogging("verbose".equalsIgnoreCase(contextConfigurator.get(LOG_LEVEL)));
    loadPlugins();

    FitNesseContext context = contextConfigurator.makeFitNesseContext();

    logStartupInfo(context);

    update(context);

    return launch(context);
  }

  private boolean update(FitNesseContext context) throws IOException {
    if (!"true".equalsIgnoreCase(context.getProperty(OMITTING_UPDATES.getKey()))) {
      Updater updater = new UpdaterImplementation(context);
      return updater.update();
    }
    return false;
  }

  private void loadPlugins() throws Exception {
    new PluginsClassLoader().addPluginsToClassLoader();
  }

  Integer launch(FitNesseContext context) throws Exception {
    if (!"true".equalsIgnoreCase(context.getProperty(INSTALL_ONLY.getKey()))) {
      String command = context.getProperty(COMMAND.getKey());
      if (command != null) {
        String output = context.getProperty(OUTPUT.getKey());
        return executeSingleCommand(context.fitNesse, command, output);
      } else {
        context.fitNesse.start();
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

  private void logStartupInfo(FitNesseContext context) {
    LOG.info("root page: " + context.root);
    LOG.info("logger: " + (context.logger == null ? "none" : context.logger.toString()));
    LOG.info("authenticator: " + context.authenticator);
    LOG.info("page factory: " + context.pageFactory);
    LOG.info("page theme: " + context.pageFactory.getTheme());
    LOG.info("Starting FitNesse on port: " + context.port);
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
