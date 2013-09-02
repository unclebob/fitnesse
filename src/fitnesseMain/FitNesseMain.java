package fitnesseMain;

import fitnesse.Arguments;
import fitnesse.ComponentFactory;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContext.Builder;
import fitnesse.Updater;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.components.PluginsClassLoader;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.reporting.TestTextFormatter;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolProvider;
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
    ComponentFactory componentFactory = new ComponentFactory(arguments.getRootPath());

    // Enrich properties with command line values:
    componentFactory.getProperties().setProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS, Integer.toString(arguments.getDaysTillVersionsExpire()));

    WikiPageFactory wikiPageFactory = (WikiPageFactory) componentFactory.createComponent(ComponentFactory.WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);

    builder.port = arguments.getPort();
    builder.rootPath = arguments.getRootPath();
    builder.rootDirectoryName = arguments.getRootDirectory();

    builder.pageTheme = componentFactory.getProperty(ComponentFactory.THEME);
    builder.defaultNewPageContent = componentFactory
        .getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);
    builder.recentChanges = (RecentChanges) componentFactory.createComponent(ComponentFactory.RECENT_CHANGES_CLASS, RecentChangesWikiPage.class);

    // This should be done before the root wiki page is created:
    //extraOutput = componentFactory.loadVersionsController(arguments.getDaysTillVersionsExpire());

    builder.root = wikiPageFactory.makeRootPage(builder.rootPath,
      builder.rootDirectoryName);

    builder.logger = makeLogger(arguments);
    builder.authenticator = makeAuthenticator(arguments.getUserpass(),
      componentFactory);

    FitNesseContext context = builder.createFitNesseContext();

    SymbolProvider symbolProvider = SymbolProvider.wikiParsingProvider;

    extraOutput += componentFactory.loadPlugins(context.responderFactory, symbolProvider);
    extraOutput += componentFactory.loadResponders(context.responderFactory);
    extraOutput += componentFactory.loadSymbolTypes(symbolProvider);
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
    if (extraOutput != null)
      System.out.print(extraOutput);
  }
}
