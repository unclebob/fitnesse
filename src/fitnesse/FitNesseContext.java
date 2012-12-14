// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.responders.PageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.run.SocketDealer;
import fitnesse.wiki.WikiPage;

import java.io.File;

public class FitNesseContext {
  public final static String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
  public final static String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
  public static final String testResultsDirectoryName = "testResults";

  public final WikiPage root;
  public final SocketDealer socketDealer;
  public final RunningTestingTracker runningTestingTracker = new RunningTestingTracker();

  public final int port;
  public final String rootPath;
  public final String rootDirectoryName;

  public String defaultNewPageContent = "!contents -R2 -g -p -f -h";
  public Logger logger;
  public Authenticator authenticator = new PromiscuousAuthenticator();
  public PageFactory pageFactory = new PageFactory(this);
  public boolean doNotChunk;
  public String pageTheme = "fitnesse_straight";

  private ResponderFactory responderFactory;

  public FitNesseContext() {
    this(null, null, null, 80);
  }

  public FitNesseContext(WikiPage root) {
    this(root, null, null, 80);
  }

  public FitNesseContext(WikiPage root, String rootPath,
      String rootDirectoryName, int port) {
    this(root, rootPath, rootDirectoryName, port, null);
  }

  public FitNesseContext(WikiPage root, String rootPath,
        String rootDirectoryName, int port, SocketDealer socketDealer) {
    this.root = root;
    this.port = port;
    this.rootPath = rootPath != null ? rootPath : "." ;
    this.rootDirectoryName = rootDirectoryName != null ? rootDirectoryName : "FitNesseRoot";
    this.socketDealer = socketDealer != null ? socketDealer : new SocketDealer();
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

  public ResponderFactory getResponderFactory() {
    if (responderFactory == null) {
      responderFactory = new ResponderFactory(getRootPagePath());
    }
    return responderFactory;
  }
}
