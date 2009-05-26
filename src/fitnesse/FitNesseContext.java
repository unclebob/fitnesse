// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.run.SocketDealer;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import java.io.File;
import java.io.StringWriter;

public class FitNesseContext {
  public FitNesse fitnesse;
  public int port = 80;
  public String rootPath = ".";
  public String rootDirectoryName = "FitNesseRoot";
  public String rootPagePath = "";
  public String defaultNewPageContent = "!contents -R2 -g -p -f -h";
  public WikiPage root;
  public ResponderFactory responderFactory = new ResponderFactory(rootPagePath);
  public Logger logger;
  public SocketDealer socketDealer = new SocketDealer();
  public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
  public Authenticator authenticator = new PromiscuousAuthenticator();
  public HtmlPageFactory htmlPageFactory = new HtmlPageFactory();
  public static String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
  public static String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
  public static FitNesseContext globalContext;
  private VelocityEngine velocityEngine;
  public boolean shouldCollectHistory = false;
//  public Ruby rubyRuntime;

  public FitNesseContext() {
    this(null);
  }

  public FitNesseContext(WikiPage root) {
    this.root = root;
  }
//
//  public Ruby getRubyRuntime() {
//    if (rubyRuntime == null) {
//      rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST);
//    }
//    return rubyRuntime;
//  }

  public VelocityEngine getVelocityEngine() {
    if (velocityEngine == null) {
      velocityEngine = new VelocityEngine();
      String templatePath = String.format("%s/%s/files/templates", rootPath, rootDirectoryName);
      velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templatePath);
      try {
        velocityEngine.init();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return velocityEngine;
  }

  public String toString() {
    String endl = System.getProperty("line.separator");
    StringBuffer buffer = new StringBuffer();
    buffer.append("\t").append("port:              ").append(port).append(endl);
    buffer.append("\t").append("root page:         ").append(root).append(endl);
    buffer.append("\t").append("logger:            ").append(logger == null ? "none" : logger.toString()).append(endl);
    buffer.append("\t").append("authenticator:     ").append(authenticator).append(endl);
    buffer.append("\t").append("html page factory: ").append(htmlPageFactory).append(endl);

    return buffer.toString();
  }

  public static int getPort() {
    return globalContext != null ? globalContext.port : -1;
  }

  public void setVelocityEngine(VelocityEngine velocityEngine) {
    this.velocityEngine = velocityEngine;
  }

  public File getTestHistoryDirectory() {
    return new File(String.format("%s/files/testResults", rootPagePath));
  }

  public void setRootPagePath() {
    rootPagePath = rootPath + "/" + rootDirectoryName;
  }

  public String translateTemplate(VelocityContext velocityContext, String templateFileName) throws Exception {
    Template template = getVelocityEngine().getTemplate(templateFileName);
    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);
    return writer.toString();
  }
}
