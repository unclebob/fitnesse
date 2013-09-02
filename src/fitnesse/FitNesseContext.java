// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.wiki.RecentChanges;
import fitnesse.components.SocketDealer;
import fitnesse.html.template.PageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.testrunner.RunningTestingTracker;
import fitnesse.wiki.WikiPage;

import java.io.File;

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
    public SocketDealer socketDealer;

    public Logger logger;
    public Authenticator authenticator = new PromiscuousAuthenticator();
    public String defaultNewPageContent;
    public RecentChanges recentChanges;
    public String pageTheme;

    public Builder() {
      super();
    }

    public Builder(WikiPage root) {
      super();
      this.root = root;
    }

    public Builder(FitNesseContext context) {
      super();
      if (context != null) {
        root = context.root;
        port = context.port;
        rootPath = context.rootPath;
        rootDirectoryName = context.rootDirectoryName;
        socketDealer = context.socketDealer;
        logger = context.logger;
        authenticator = context.authenticator;
        defaultNewPageContent = context.defaultNewPageContent;
        pageTheme = context.pageTheme;
        recentChanges = context.recentChanges;
      }
    }

    public final FitNesseContext createFitNesseContext() {
      return new FitNesseContext(root,
          rootPath,
          rootDirectoryName,
          pageTheme,
          defaultNewPageContent,
          recentChanges,
          port,
          socketDealer,
          authenticator,
          logger);
    }
  }


  public final WikiPage root;
  public final SocketDealer socketDealer;
  public final RunningTestingTracker runningTestingTracker = new RunningTestingTracker();

  public final int port;
  public final String rootPath;
  public final String rootDirectoryName;
  public final ResponderFactory responderFactory;
  public final PageFactory pageFactory = new PageFactory(this);

  public final String defaultNewPageContent;
  public final RecentChanges recentChanges;
  public final Logger logger;
  public final Authenticator authenticator;
  public final String pageTheme;


  private FitNesseContext(WikiPage root, String rootPath,
      String rootDirectoryName, String pageTheme, String defaultNewPageContent,
      RecentChanges recentChanges, int port, SocketDealer socketDealer,
      Authenticator authenticator, Logger logger) {
    super();
    this.root = root;
    this.rootPath = rootPath != null ? rootPath : ".";
    this.rootDirectoryName = rootDirectoryName != null ? rootDirectoryName : "FitNesseRoot";
    this.pageTheme = pageTheme != null ? pageTheme : "fitnesse_straight";
    this.defaultNewPageContent = defaultNewPageContent != null ? defaultNewPageContent : "!contents -R2 -g -p -f -h";
    this.recentChanges = recentChanges;
    this.port = port >= 0 ? port : 80;
    this.socketDealer = socketDealer != null ? socketDealer : new SocketDealer();
    this.authenticator = authenticator != null ? authenticator : new PromiscuousAuthenticator();
    this.logger = logger;
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
    buffer.append("\t").append("page theme:        ").append(pageTheme).append(endl);

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
}
