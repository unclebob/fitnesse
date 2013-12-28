// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import java.util.Properties;

import fitnesse.ConfigurationParameter;
import util.CommandLine;

import static fitnesse.ContextConfigurator.*;

public class Arguments {
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";

  private final CommandLine commandLine = new CommandLine(
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
    this.credentials = commandLine.getOptionArgument("a", "credentials");
    this.command = commandLine.getOptionArgument("c", "command");
    this.output = commandLine.getOptionArgument("b", "output");
    this.configFile = commandLine.getOptionArgument("f", "config");
    this.verboseLogging = commandLine.hasOption("v");
    this.omitUpdate = commandLine.hasOption("o");
    this.installOnly = commandLine.hasOption("i");
  }

  static void printUsage() {
    System.err.println("Usage: java -jar fitnesse.jar [-vpdrleoab]");
    System.err.println("\t-p <port number> {" + DEFAULT_PORT + "}");
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
    return rootPath == null ? DEFAULT_PATH : rootPath;
  }

  public String getConfigFile() {
    return configFile == null ? (getRootPath() + "/" + DEFAULT_CONFIG_FILE) : configFile;
  }

  public Properties asProperties() {
    Properties properties = new Properties();
    properties.setProperty(ConfigurationParameter.LOG_LEVEL, verboseLogging ? "verbose" : "normal");
    if (configFile != null) properties.setProperty(ConfigurationParameter.CONFIG_FILE, configFile);
    if (port != null) properties.setProperty(ConfigurationParameter.PORT, port.toString());
    if (rootPath != null) properties.setProperty(ConfigurationParameter.ROOT_PATH, rootPath);
    if (rootDirectory != null) properties.setProperty(ConfigurationParameter.ROOT_DIRECTORY, rootDirectory);
    if (output != null) properties.setProperty(ConfigurationParameter.OUTPUT, output);
    if (logDirectory != null) properties.setProperty(ConfigurationParameter.LOG_DIRECTORY, logDirectory);
    if (daysTillVersionsExpire != null) properties.setProperty(ConfigurationParameter.VERSIONS_CONTROLLER_DAYS, daysTillVersionsExpire.toString());
    if (omitUpdate) properties.setProperty(ConfigurationParameter.OMITTING_UPDATES, "true");
    if (installOnly) properties.setProperty(ConfigurationParameter.INSTALL_ONLY, "true");
    if (command != null) properties.setProperty(ConfigurationParameter.COMMAND, command);
    if (credentials != null) properties.setProperty(ConfigurationParameter.CREDENTIALS, credentials);
    return properties;
  }
}
