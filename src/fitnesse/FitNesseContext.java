// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.wiki.RecentChanges;
import fitnesse.html.template.PageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.testrunner.RunningTestingTracker;
import fitnesse.wiki.WikiPage;

import java.io.File;
import java.util.Properties;

public class FitNesseContext {
  public final static String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
  public final static String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
  public static final String testResultsDirectoryName = "testResults";

  /**
   * Use the builder to create your FitNesse contexts.
   */
  public static final class Builder {
    public WikiPage root;

    public int port = -1;
    public String rootPath;
    public String rootDirectoryName;

    public Logger logger;
    public Authenticator authenticator = new PromiscuousAuthenticator();
    public RecentChanges recentChanges;
    public Properties properties;

    public Builder() {
      super();
    }

    public Builder(FitNesseContext context) {
      super();
      if (context != null) {
        root = context.root;
        port = context.port;
        rootPath = context.rootPath;
        rootDirectoryName = context.rootDirectoryName;
        logger = context.logger;
        authenticator = context.authenticator;
        recentChanges = context.recentChanges;
        properties = context.properties;
      }
    }

    public final FitNesseContext createFitNesseContext() {
      return new FitNesseContext(root,
          rootPath,
          rootDirectoryName,
          recentChanges,
          port,
          authenticator,
          logger,
          properties);
    }
  }


  public final WikiPage root;
  public final RunningTestingTracker runningTestingTracker = new RunningTestingTracker();

  public final int port;
  public final String rootPath;
  public final String rootDirectoryName;
  public final ResponderFactory responderFactory;
  public final PageFactory pageFactory = new PageFactory(this);

  // Remove this, let it use getProperty instead
  public final RecentChanges recentChanges;
  public final Logger logger;
  public final Authenticator authenticator;
  private final Properties properties;



  private FitNesseContext(WikiPage root, String rootPath,
      String rootDirectoryName,
      RecentChanges recentChanges, int port,
      Authenticator authenticator, Logger logger, Properties properties) {
    super();
    this.root = root;
    this.rootPath = rootPath != null ? rootPath : ".";
    this.rootDirectoryName = rootDirectoryName != null ? rootDirectoryName : "FitNesseRoot";
    this.recentChanges = recentChanges;
    this.port = port >= 0 ? port : 80;
    this.authenticator = authenticator != null ? authenticator : new PromiscuousAuthenticator();
    this.logger = logger;
    this.properties = properties;
    responderFactory = new ResponderFactory(getRootPagePath());
  }

  public String toString() {
    String endl = System.getProperty("line.separator");
    StringBuffer buffer = new StringBuffer();
    buffer.append("\t").append("port:              ").append(port).append(endl);
    buffer.append("\t").append("root page:         ").append(root).append(endl);
    buffer.append("\t").append("logger:            ").append(logger == null ? "none" : logger.toString()).append(endl);
    buffer.append("\t").append("authenticator:     ").append(authenticator).append(endl);
    buffer.append("\t").append("page factory:      ").append(pageFactory).append(endl);
    buffer.append("\t").append("page theme:        ").append(pageFactory.getTheme()).append(endl);

    return buffer.toString();
  }

  public File getTestHistoryDirectory() {
    return new File(String.format("%s/files/%s", getRootPagePath(), testResultsDirectoryName));
  }

  public String getTestProgressPath() {
    return String.format("%s/files/testProgress/", getRootPagePath());
  }

  public String getRootPagePath() {
    return String.format("%s/%s", rootPath, rootDirectoryName);
  }

  public String getProperty(String name) {
    String p = System.getenv(name);
    if (p != null) return p;
    p = System.getProperty(name);
    if (p != null) return p;

    return properties != null ? properties.getProperty(name) : null;
  }
}
