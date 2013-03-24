package fitnesseMain;

import fitnesse.*;
import fitnesse.FitNesseContext.Builder;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.components.PluginsClassLoader;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.run.formatters.TestTextFormatter;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.PageVersionPruner;
import util.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FitNesseMain {
  private static String extraOutput;
  public static boolean dontExitAfterSingleCommand;

  public static void main(String[] args) throws Exception {
    Arguments arguments = parseCommandLine(args);
    if (arguments != null) {
      launchFitNesse(arguments);
    } else {
      printUsage();
      System.exit(1);
    }
  }

  public static void launchFitNesse(Arguments arguments) throws Exception {
    loadPlugins();
    FitNesseContext context = loadContext(arguments);
    Updater updater = null;
    if (!arguments.isOmittingUpdates())
      updater = new UpdaterImplementation(context);
    PageVersionPruner.daysTillVersionsExpire = arguments
      .getDaysTillVersionsExpire();
    FitNesse fitnesse = new FitNesse(context, updater);
    update(arguments, fitnesse);
    launch(arguments, context, fitnesse);
  }

  private static void loadPlugins() throws Exception {
    new PluginsClassLoader().addPluginsToClassLoader();
  }

  static void update(Arguments arguments,FitNesse fitnesse) throws Exception {
    if (!arguments.isOmittingUpdates())
      fitnesse.applyUpdates();
  }

  static void launch(Arguments arguments, FitNesseContext context,
      FitNesse fitnesse) throws Exception {
    if (!arguments.isInstallOnly()) {
      boolean started = fitnesse.start();
      if (started) {
        printStartMessage(arguments, context);
        if (arguments.getCommand() != null) {
          executeSingleCommand(arguments, fitnesse, context);
        }
      }
    }
  }

  private static void executeSingleCommand(Arguments arguments, FitNesse fitnesse, FitNesseContext context) throws Exception {
    TestTextFormatter.finalErrorCount = 0;
    System.out.println("Executing command: " + arguments.getCommand());

    OutputStream os;

    boolean outputRedirectedToFile = arguments.getOutput() != null;

    if (outputRedirectedToFile) {
      System.out.println("-----Command Output redirected to " + arguments.getOutput() + "-----");
      os = new FileOutputStream(arguments.getOutput());
    } else {
      System.out.println("-----Command Output-----");
      os = System.out;
    }

    fitnesse.executeSingleCommand(arguments.getCommand(), os);
    fitnesse.stop();

    if (outputRedirectedToFile) {
      os.close();
    } else {
      System.out.println("-----Command Complete-----");
    }

    if (shouldExitAfterSingleCommand()) {
      System.exit(TestTextFormatter.finalErrorCount);
    }
  }

  private static boolean shouldExitAfterSingleCommand() {
    return !dontExitAfterSingleCommand;
  }

  private static FitNesseContext loadContext(Arguments arguments)
    throws Exception {
    Builder builder = new Builder();
    WikiPageFactory wikiPageFactory = new WikiPageFactory();
    ComponentFactory componentFactory = new ComponentFactory(arguments.getRootPath());

    builder.port = arguments.getPort();
    builder.rootPath = arguments.getRootPath();
    builder.rootDirectoryName = arguments.getRootDirectory();

    builder.pageTheme = componentFactory.getProperty(ComponentFactory.THEME);
    builder.defaultNewPageContent = componentFactory
        .getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);

    builder.root = wikiPageFactory.makeRootPage(builder.rootPath,
      builder.rootDirectoryName, componentFactory);

    builder.logger = makeLogger(arguments);
    builder.authenticator = makeAuthenticator(arguments.getUserpass(),
      componentFactory);

    FitNesseContext context = builder.createFitNesseContext();

    extraOutput = componentFactory.loadPlugins(context.responderFactory,
        wikiPageFactory);
    extraOutput += componentFactory.loadWikiPage(wikiPageFactory);
    extraOutput += componentFactory.loadResponders(context.responderFactory);
    extraOutput += componentFactory.loadSymbolTypes();
    extraOutput += componentFactory.loadContentFilter();
    extraOutput += componentFactory.loadSlimTables();


    WikiImportTestEventListener.register();

    return context;
  }

  public static Arguments parseCommandLine(String[] args) {
    CommandLine commandLine = new CommandLine(
      "[-p port][-d dir][-r root][-l logDir][-e days][-o][-i][-a userpass][-c command][-b output]");
    Arguments arguments = null;
    if (commandLine.parse(args)) {
      arguments = new Arguments();
      if (commandLine.hasOption("p"))
        arguments.setPort(commandLine.getOptionArgument("p", "port"));
      if (commandLine.hasOption("d"))
        arguments.setRootPath(commandLine.getOptionArgument("d", "dir"));
      if (commandLine.hasOption("r"))
        arguments.setRootDirectory(commandLine.getOptionArgument("r", "root"));
      if (commandLine.hasOption("l"))
        arguments.setLogDirectory(commandLine.getOptionArgument("l", "logDir"));
      if (commandLine.hasOption("e"))
        arguments.setDaysTillVersionsExpire(commandLine.getOptionArgument("e", "days"));
      if (commandLine.hasOption("a"))
        arguments.setUserpass(commandLine.getOptionArgument("a", "userpass"));
      if (commandLine.hasOption("c"))
        arguments.setCommand(commandLine.getOptionArgument("c", "command"));
      if (commandLine.hasOption("b"))
        arguments.setOutput(commandLine.getOptionArgument("b", "output"));
      arguments.setOmitUpdates(commandLine.hasOption("o"));
      arguments.setInstallOnly(commandLine.hasOption("i"));
    }
    return arguments;
  }

  private static Logger makeLogger(Arguments arguments) {
    String logDirectory = arguments.getLogDirectory();
    return logDirectory != null ? new Logger(logDirectory) : null;
  }

  public static Authenticator makeAuthenticator(String authenticationParameter,
                                                ComponentFactory componentFactory) throws Exception {
    Authenticator authenticator = new PromiscuousAuthenticator();
    if (authenticationParameter != null) {
      if (new File(authenticationParameter).exists())
        authenticator = new MultiUserAuthenticator(authenticationParameter);
      else {
        String[] values = authenticationParameter.split(":");
        authenticator = new OneUserAuthenticator(values[0], values[1]);
      }
    }

    return componentFactory.getAuthenticator(authenticator);
  }

  private static void printUsage() {
    System.err.println("Usage: java -jar fitnesse.jar [-pdrleoab]");
    System.err.println("\t-p <port number> {" + Arguments.DEFAULT_PORT + "}");
    System.err.println("\t-d <working directory> {" + Arguments.DEFAULT_PATH
      + "}");
    System.err.println("\t-r <page root directory> {" + Arguments.DEFAULT_ROOT
      + "}");
    System.err.println("\t-l <log directory> {no logging}");
    System.err.println("\t-e <days> {" + Arguments.DEFAULT_VERSION_DAYS
      + "} Number of days before page versions expire");
    System.err.println("\t-o omit updates");
    System.err
      .println("\t-a {user:pwd | user-file-name} enable authentication.");
    System.err.println("\t-i Install only, then quit.");
    System.err.println("\t-c <command> execute single command.");
    System.err.println("\t-b <filename> redirect command output.");
  }

  private static void printStartMessage(Arguments args, FitNesseContext context) {
    System.out.println("FitNesse (" + FitNesse.VERSION + ") Started...");
    System.out.print(context.toString());
    System.out.println("\tpage version expiration set to "
      + args.getDaysTillVersionsExpire() + " days.");
    if (extraOutput != null)
      System.out.print(extraOutput);
  }
}
