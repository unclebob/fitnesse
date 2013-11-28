// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.FitNesseContext;
import util.CommandLine;

public class Arguments {
  public static final String DEFAULT_PATH = ".";
  public static final String DEFAULT_ROOT = "FitNesseRoot";
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";
  public static final int DEFAULT_COMMAND_PORT = 9123;
  public static final int DEFAULT_VERSION_DAYS = 14;

  private final CommandLine commandLine = new CommandLine(
          "[-v][-p port][-d dir][-r root][-l logDir][-f config][-e days][-o][-i][-a userpass][-c command][-b output]");

  private final String rootPath;
  private final int port;
  private final String rootDirectory;
  private final String logDirectory;
  private final boolean omitUpdate;
  private final int daysTillVersionsExpire;
  private final String userpass;
  private final boolean installOnly;
  private final String command;
  private final String output;
  private final String configFile;
  private final boolean verboseLogging;

  public Arguments(String... args) {
    if (!commandLine.parse(args)) {
      throw new IllegalArgumentException("Can not parse command line");
    }
    this.port = Integer.parseInt(commandLine.getOptionArgument("p", "port", "-1"));
    this.rootPath = commandLine.getOptionArgument("d", "dir", DEFAULT_PATH);
    this.rootDirectory = commandLine.getOptionArgument("r", "root", DEFAULT_ROOT);
    this.logDirectory = commandLine.getOptionArgument("l", "logDir");
    this.daysTillVersionsExpire = Integer.parseInt(commandLine.getOptionArgument("e", "days", "" + DEFAULT_VERSION_DAYS));
    this.userpass = commandLine.getOptionArgument("a", "userpass");
    this.command = commandLine.getOptionArgument("c", "command");
    this.output = commandLine.getOptionArgument("b", "output");
    this.configFile = commandLine.getOptionArgument("f", "config");
    this.verboseLogging = commandLine.hasOption("v");
    this.omitUpdate = commandLine.hasOption("o");
    this.installOnly = commandLine.hasOption("i");
  }

  static void printUsage() {
    System.err.println("Usage: java -jar fitnesse.jar [-vpdrleoab]");
    System.err.println("\t-p <port number> {" + FitNesseContext.DEFAULT_PORT + "}");
    System.err.println("\t-d <working directory> {" + DEFAULT_PATH
      + "}");
    System.err.println("\t-r <page root directory> {" + DEFAULT_ROOT
      + "}");
    System.err.println("\t-l <log directory> {no logging}");
    System.err.println("\t-f <config properties file> {" + DEFAULT_CONFIG_FILE + "}");
    System.err.println("\t-e <days> {" + DEFAULT_VERSION_DAYS
      + "} Number of days before page versions expire");
    System.err.println("\t-o omit updates");
    System.err
      .println("\t-a {user:pwd | user-file-name} enable authentication.");
    System.err.println("\t-i Install only, then quit.");
    System.err.println("\t-c <command> execute single command.");
    System.err.println("\t-b <filename> redirect command output.");
    System.err.println("\t-v {off} Verbose logging");
  }

  public String getRootPath() {
    return rootPath;
  }

  public int getPort() {
    return port == -1 ? getDefaultPort() : port;
  }

  private int getDefaultPort() {
    return command == null ? FitNesseContext.DEFAULT_PORT : DEFAULT_COMMAND_PORT;
  }

  public String getRootDirectory() {
    return rootDirectory;
  }

  public String getLogDirectory() {
    return logDirectory;
  }

  public boolean isOmittingUpdates() {
    return omitUpdate;
  }

  public String getUserpass() {
    if (userpass == null || userpass.length() == 0)
      return null;
    else
      return userpass;
  }

  public int getDaysTillVersionsExpire() {
    return daysTillVersionsExpire;
  }

  public boolean isInstallOnly() {
    return installOnly;
  }

  public String getCommand() {
    return command;
  }

  public String getOutput() {
    return output;
  }

  public String getConfigFile() {
    return configFile == null ? (rootPath + "/" + DEFAULT_CONFIG_FILE) : configFile;
  }

  public boolean hasVerboseLogging() {
    return verboseLogging;
  }
}
