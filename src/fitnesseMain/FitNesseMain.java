package fitnesseMain;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.Updater;
import fitnesse.components.PluginsClassLoader;
import fitnesse.reporting.ExitCodeListener;
import fitnesse.updates.WikiContentUpdater;

import java.io.*;
import java.net.BindException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static fitnesse.ConfigurationParameter.*;

public class FitNesseMain {
  private static final Logger LOG = Logger.getLogger(FitNesseMain.class.getName());

  private final ExitCodeListener exitCodeListener = new ExitCodeListener();

  public static void main(String[] args) throws Exception {
    Arguments arguments = null;
    try {
      arguments = new Arguments(args);
    } catch (IllegalArgumentException e) {
      Arguments.printUsage();
      exit(1);
    }
    Integer exitCode = 0;
    try {
        exitCode = new FitNesseMain().launchFitNesse(arguments);
    } catch (Exception e){
      LOG.log(Level.SEVERE, "Error while starting the FitNesse", e);
      exitCode = 1;
    }
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
    loadPlugins(contextConfigurator.get(ConfigurationParameter.ROOT_PATH));

    if (contextConfigurator.get(COMMAND) != null) {
      contextConfigurator.withTestSystemListener(exitCodeListener);
    }

    FitNesseContext context = contextConfigurator.makeFitNesseContext();

    if (!establishRequiredDirectories(context.getRootPagePath())) {
      LOG.severe("FitNesse cannot be started...");
      LOG.severe("Unable to create FitNesse root directory in " + context.getRootPagePath());
      LOG.severe("Ensure you have sufficient permissions to create this folder.");
      return 1;
    }

    logStartupInfo(context);

    if (update(context)) {
      LOG.info("**********************************************************");
      LOG.info("Files have been updated to a new version.");
      LOG.info("Please read the release notes on ");
      LOG.info("http://localhost:" + context.port + "/FitNesse.ReleaseNotes");
      LOG.info("to find out about the new features and fixes.");
      LOG.info("**********************************************************");
    }

    if ("true".equalsIgnoreCase(contextConfigurator.get(INSTALL_ONLY))) {
      return null;
    }

    try {
      return launch(context);
    } catch (BindException e) {
      LOG.severe("FitNesse cannot be started...");
      LOG.severe("Port " + context.port + " is already in use.");
      LOG.severe("Use the -p <port#> command line argument to use a different port.");
      return 1;
    }

  }

  private boolean establishRequiredDirectories(String rootPagePath) {
    return establishDirectory(new File(rootPagePath)) &&
            establishDirectory(new File(rootPagePath, "files"));
  }

  private static boolean establishDirectory(File path) {
    return path.exists() || path.mkdir();
  }

  private boolean update(FitNesseContext context) throws IOException {
    if (!"true".equalsIgnoreCase(context.getProperty(OMITTING_UPDATES.getKey()))) {
      Updater updater = new WikiContentUpdater(context);
      return updater.update();
    }
    return false;
  }

  private void loadPlugins(String rootPath) throws Exception {
    new PluginsClassLoader(rootPath).addPluginsToClassLoader();
  }

  private Integer launch(FitNesseContext context) throws Exception {
    if (!"true".equalsIgnoreCase(context.getProperty(INSTALL_ONLY.getKey()))) {
      String command = context.getProperty(COMMAND.getKey());
      if (command != null) {
        String output = context.getProperty(OUTPUT.getKey());
        executeSingleCommand(context.fitNesse, command, output);

        return exitCodeListener.getFailCount();
      } else {
        LOG.info("Starting FitNesse on port: " + context.port);
        context.fitNesse.start();
      }
    }
    return null;
  }

  private void executeSingleCommand(FitNesse fitNesse, String command, String outputFile) throws Exception {

    LOG.info("Executing command: " + command);

    OutputStream os;

    boolean outputRedirectedToFile = outputFile != null;

    if (outputRedirectedToFile) {
      LOG.info("Command Output redirected to: " + outputFile);
      os = new FileOutputStream(outputFile);
    } else {
      os = System.out;
    }

    fitNesse.executeSingleCommand(command, os);
    fitNesse.stop();

    if (outputRedirectedToFile) {
      os.close();
    }
  }

  private void logStartupInfo(FitNesseContext context) {
    // This message is on standard output for backward compatibility with Jenkins Fitnesse plugin.
    // (ConsoleHandler of JUL uses standard error output for all messages).
    System.out.println("Bootstrapping FitNesse, the fully integrated standalone wiki and acceptance testing framework.");
    
    LOG.info("root page: " + context.getRootPage());
    LOG.info("logger: " + (context.logger == null ? "none" : context.logger.toString()));
    LOG.info("authenticator: " + context.authenticator);
    LOG.info("page factory: " + context.pageFactory);
    LOG.info("page theme: " + context.pageFactory.getTheme());
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
