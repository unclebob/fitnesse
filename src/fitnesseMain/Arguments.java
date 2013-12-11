// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import java.util.Properties;

import fitnesse.ContextConfigurator;
import util.CommandLine;

import static org.junit.Assert.assertEquals;

public class Arguments {
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";

  private final CommandLine commandLine = new CommandLine(
          "[-v][-p port][-d dir][-r root][-l logDir][-f config][-e days][-o][-i][-a userpass][-c command][-b output]");

  private final String rootPath;
  private final Integer port;
  private final String rootDirectory;
  private final String logDirectory;
  private final boolean omitUpdate;
  private final Integer daysTillVersionsExpire;
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
    String port = commandLine.getOptionArgument("p", "port");
    this.port = port != null ? Integer.valueOf(port) : null;
    this.rootPath = commandLine.getOptionArgument("d", "dir");
    this.rootDirectory = commandLine.getOptionArgument("r", "root");
    this.logDirectory = commandLine.getOptionArgument("l", "logDir");
    final String days = commandLine.getOptionArgument("e", "days");
    this.daysTillVersionsExpire = days != null ? Integer.valueOf(days) : null;
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
    System.err.println("\t-p <port number> {" + ContextConfigurator.DEFAULT_PORT + "}");
    System.err.println("\t-d <working directory> {" + ContextConfigurator.DEFAULT_PATH
      + "}");
    System.err.println("\t-r <page root directory> {" + ContextConfigurator.DEFAULT_ROOT
      + "}");
    System.err.println("\t-l <log directory> {no logging}");
    System.err.println("\t-f <config properties file> {" + DEFAULT_CONFIG_FILE + "}");
    System.err.println("\t-e <days> {" + ContextConfigurator.DEFAULT_VERSION_DAYS
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
    return rootPath == null ? ContextConfigurator.DEFAULT_PATH : rootPath;
  }

  public int getPort() {
    return port == null ? getDefaultPort() : port;
  }

  private int getDefaultPort() {
    return command == null ? ContextConfigurator.DEFAULT_PORT : ContextConfigurator.DEFAULT_COMMAND_PORT;
  }

  public String getRootDirectory() {
    return rootDirectory == null ? ContextConfigurator.DEFAULT_ROOT : rootDirectory;
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
    return daysTillVersionsExpire == null ? ContextConfigurator.DEFAULT_VERSION_DAYS : daysTillVersionsExpire;
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
    return configFile == null ? (getRootPath() + "/" + DEFAULT_CONFIG_FILE) : configFile;
  }

  public boolean hasVerboseLogging() {
    return verboseLogging;
  }

  public Properties asProperties() {
    Properties properties = new Properties();
    properties.setProperty("LogLevel", verboseLogging ? "verbose" : "normal");
    if (configFile != null) properties.setProperty("config.properties", configFile);
    if (port != null) properties.setProperty("Port", port.toString());
    if (rootPath != null) properties.setProperty("RootPath", rootPath);
    if (rootDirectory != null) properties.setProperty("RootDirectory", rootDirectory);
    if (output != null) properties.setProperty("RedirectOutput", output);
    if (logDirectory != null) properties.setProperty("LogDirectory", logDirectory);
    if (daysTillVersionsExpire != null) properties.setProperty("VersionsController.days", daysTillVersionsExpire.toString());
    if (omitUpdate) properties.setProperty("OmittingUpdates", "true");
    if (installOnly) properties.setProperty("InstallOnly", "true");
    if (command != null) properties.setProperty("Command", command);
    if (userpass != null) properties.setProperty("Credentials", userpass);
    return properties;
  }
}
