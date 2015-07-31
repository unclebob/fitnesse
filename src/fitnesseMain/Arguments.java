// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.ContextConfigurator;
import util.CommandLine;

import static fitnesse.ConfigurationParameter.*;
import static fitnesse.ContextConfigurator.*;

public class Arguments {

  private static final CommandLine COMMAND_LINE = new CommandLine(
          "[-v][-p port][-d dir][-r root][-l logDir][-f config][-e days][-o][-i][-a credentials][-c command][-b output]");

  private final String rootPath;
  private final Integer port;
  private final String rootDirectory;
  private final String logDirectory;
  private final boolean omitUpdate;
  private final Integer daysTillVersionsExpire;
  private final String credentials;
  private final boolean installOnly;
  private final String command;
  private final String output;
  private final String configFile;
  private final boolean verboseLogging;

  public Arguments(String... args) {
    if (!COMMAND_LINE.parse(args)) {
      throw new IllegalArgumentException("Can not parse command line");
    }
    String port = COMMAND_LINE.getOptionArgument("p", "port");
    this.port = port != null ? Integer.valueOf(port) : null;
    this.rootPath = COMMAND_LINE.getOptionArgument("d", "dir");
    this.rootDirectory = COMMAND_LINE.getOptionArgument("r", "root");
    this.logDirectory = COMMAND_LINE.getOptionArgument("l", "logDir");
    final String days = COMMAND_LINE.getOptionArgument("e", "days");
    this.daysTillVersionsExpire = days != null ? Integer.valueOf(days) : null;
    this.credentials = COMMAND_LINE.getOptionArgument("a", "credentials");
    this.command = COMMAND_LINE.getOptionArgument("c", "command");
    this.output = COMMAND_LINE.getOptionArgument("b", "output");
    this.configFile = COMMAND_LINE.getOptionArgument("f", "config");
    this.verboseLogging = COMMAND_LINE.hasOption("v");
    this.omitUpdate = COMMAND_LINE.hasOption("o");
    this.installOnly = COMMAND_LINE.hasOption("i");
  }

  static void printUsage() {
    ContextConfigurator defaults = ContextConfigurator.systemDefaults();

    System.err.println("Usage: java -jar fitnesse.jar [-vpdrlfeoaicb]");
    System.err.println("\t-p <port number> {" + DEFAULT_PORT + "}");
    System.err.println("\t-d <working directory> {" +
      defaults.get(ROOT_PATH) + "}");
    System.err.println("\t-r <page root directory> {" +
      defaults.get(ROOT_DIRECTORY) + "}");
    System.err.println("\t-l <log directory> {no logging}");
    System.err.println("\t-f <config properties file> {" +
      defaults.get(CONFIG_FILE) + "}");
    System.err.println("\t-e <days> {" + defaults.get(VERSIONS_CONTROLLER_DAYS) +
      "} Number of days before page versions expire");
    System.err.println("\t-o omit updates");
    System.err
      .println("\t-a {user:pwd | user-file-name} enable authentication.");
    System.err.println("\t-i Install only, then quit.");
    System.err.println("\t-c <command> execute single command.");
    System.err.println("\t-b <filename> redirect command output.");
    System.err.println("\t-v {off} Verbose logging");
  }

  public String getRootPath(ContextConfigurator configurator) {
    return rootPath == null ? configurator.get(ROOT_PATH) : rootPath;
  }

  public String getConfigFile(ContextConfigurator configurator) {
    return configFile == null ? (getRootPath(configurator) + "/" + configurator.get(CONFIG_FILE)) : configFile;
  }

  public ContextConfigurator update(ContextConfigurator defaults) {
    ContextConfigurator result = defaults;

    result = result.withParameter(LOG_LEVEL, verboseLogging ? "verbose" : "normal");
    if (configFile != null)
      result = result.withParameter(CONFIG_FILE, configFile);
    if (port != null)
      result = result.withPort(port);
    if (rootPath != null)
      result = result.withRootPath(rootPath);
    if (rootDirectory != null)
      result = result.withRootDirectoryName(rootDirectory);
    if (output != null)
      result = result.withParameter(OUTPUT, output);
    if (logDirectory != null)
      result = result.withParameter(LOG_DIRECTORY, logDirectory);
    if (daysTillVersionsExpire != null)
      result = result.withParameter(VERSIONS_CONTROLLER_DAYS, daysTillVersionsExpire.toString());
    if (omitUpdate)
      result = result.withParameter(OMITTING_UPDATES, "true");
    if (installOnly)
      result = result.withParameter(INSTALL_ONLY, "true");
    if (command != null)
      result = result.withParameter(COMMAND, command);
    if (credentials != null)
      result = result.withParameter(CREDENTIALS, credentials);

    return result;
  }

}
