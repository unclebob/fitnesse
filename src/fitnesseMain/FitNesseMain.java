package fitnesseMain;

import fitnesse.*;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.PageVersionPruner;
import util.CommandLine;

import java.io.File;

public class FitNesseMain {
  private static String extraOutput;
  public static FitNesse fitnesse;

  public static void main(String[] args) throws Exception {
    Arguments arguments = parseCommandLine(args);
    if (arguments != null) {
      FitNesseContext context = loadContext(arguments);
      Updater updater = new UpdaterImplementation(context);
      PageVersionPruner.daysTillVersionsExpire = arguments.getDaysTillVersionsExpire();
      fitnesse = new FitNesse(context, updater);
      if (!arguments.isOmittingUpdates())
        fitnesse.applyUpdates();
      boolean started = fitnesse.start();
      if (started)
        printStartMessage(arguments, context);
    } else {
      printUsage();
      System.exit(1);
    }
  }

  private static FitNesseContext loadContext(Arguments arguments) throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.port = arguments.getPort();
    context.rootPath = arguments.getRootPath();
    ComponentFactory componentFactory = new ComponentFactory(context.rootPath);
    context.rootDirectoryName = arguments.getRootDirectory();
    context.setRootPagePath();
    String defaultNewPageContent = componentFactory.getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);
    if (defaultNewPageContent != null)
      context.defaultNewPageContent = defaultNewPageContent;
    WikiPageFactory wikiPageFactory = new WikiPageFactory();
    context.responderFactory = new ResponderFactory(context.rootPagePath);
    context.logger = makeLogger(arguments);
    context.authenticator = makeAuthenticator(arguments.getUserpass(), componentFactory);
    context.htmlPageFactory = componentFactory.getHtmlPageFactory(new HtmlPageFactory());
    context.shouldCollectHistory = !arguments.isOmittingHistory();

    extraOutput = componentFactory.loadPlugins(context.responderFactory, wikiPageFactory);
    extraOutput += componentFactory.loadWikiPage(wikiPageFactory);
    extraOutput += componentFactory.loadResponders(context.responderFactory);
    extraOutput += componentFactory.loadWikiWidgets();
    extraOutput += componentFactory.loadWikiWidgetInterceptors();
    extraOutput += componentFactory.loadContentFilter();

    context.root = wikiPageFactory.makeRootPage(context.rootPath, context.rootDirectoryName, componentFactory);

    WikiImportTestEventListener.register();

    return context;
  }

  public static Arguments parseCommandLine(String[] args) {
    CommandLine commandLine = new CommandLine("[-p port][-d dir][-r root][-l logDir][-e days][-o][-h][-a userpass]");
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
      arguments.setOmitUpdates(commandLine.hasOption("o"));
      arguments.setOmitHistory(commandLine.hasOption("h"));
    }
    return arguments;
  }

  private static Logger makeLogger(Arguments arguments) {
    String logDirectory = arguments.getLogDirectory();
    return logDirectory != null ? new Logger(logDirectory) : null;
  }

  public static Authenticator makeAuthenticator(String authenticationParameter, ComponentFactory componentFactory) throws Exception {
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
    System.err.println("Usage: java -jar fitnesse.jar [-pdrleoa]");
    System.err.println("\t-p <port number> {" + Arguments.DEFAULT_PORT + "}");
    System.err.println("\t-d <working directory> {" + Arguments.DEFAULT_PATH + "}");
    System.err.println("\t-r <page root directory> {" + Arguments.DEFAULT_ROOT + "}");
    System.err.println("\t-l <log directory> {no logging}");
    System.err.println("\t-e <days> {" + Arguments.DEFAULT_VERSION_DAYS + "} Number of days before page versions expire");
    System.err.println("\t-o omit updates");
    System.err.println("\t-h do not record test history.");    
    System.err.println("\t-a {user:pwd | user-file-name} enable authentication.");
  }

  private static void printStartMessage(Arguments args, FitNesseContext context) {
    System.out.println("FitNesse (" + FitNesse.VERSION + ") Started...");
    System.out.print(context.toString());
    System.out.println("\tpage version expiration set to " + args.getDaysTillVersionsExpire() + " days.");
    System.out.print(extraOutput);
  }
}
