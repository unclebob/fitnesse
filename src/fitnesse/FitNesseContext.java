// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.components.Logger;
import fitnesse.html.template.PageFactory;
import fitnesse.reporting.FormatterFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.testrunner.run.FileBasedTestRunFactory;
import fitnesse.testrunner.run.TestRunFactoryRegistry;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.util.StringUtils;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.UrlPathVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.VersionsController;
import fitnesse.wikitext.VariableSource;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class FitNesseContext {
  public static final String WIKI_PROTOCOL_PROPERTY = "wiki.protocol";
  public static final String SSL_PARAMETER_CLASS_PROPERTY = "wiki.protocol.ssl.parameter.class";
  public static final String SSL_CLIENT_AUTH_PROPERTY = "wiki.protocol.ssl.client.auth";

  public static final String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
  public static final String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
  public static final String testResultsDirectoryName = "testResults";

  public final FitNesseVersion version;
  public final FitNesse fitNesse;

  public final TestSystemFactory testSystemFactory;
  public final TestSystemListener testSystemListener;
  public final TestRunFactoryRegistry testRunFactoryRegistry;

  public final FormatterFactory formatterFactory;

  public final int port;
  private final WikiPageFactory wikiPageFactory;
  public final String rootPath;
  private final String rootDirectoryName;
  public final Integer maximumWorkers;
  public final String contextRoot;
  public final ResponderFactory responderFactory;
  public final String theme;
  public final PageFactory pageFactory;

  public final SystemVariableSource variableSource;
  public final VersionsController versionsController;
  public final RecentChanges recentChanges;
  public final Logger logger;
  public final Authenticator authenticator;
  private final Properties properties;

  protected FitNesseContext(FitNesseVersion version, WikiPageFactory wikiPageFactory, String rootPath,
                            String rootDirectoryName, int maximumWorkers, String contextRoot,
                            VersionsController versionsController, RecentChanges recentChanges, int port,
                            Authenticator authenticator, Logger logger,
                            TestSystemFactory testSystemFactory, TestSystemListener testSystemListener,
                            FormatterFactory formatterFactory,
                            Properties properties,
                            SystemVariableSource variableSource,
                            String theme) {
    super();
    this.version = version;
    this.wikiPageFactory = wikiPageFactory;
    this.rootPath = rootPath;
    this.rootDirectoryName = rootDirectoryName;
    this.maximumWorkers = maximumWorkers;
    this.contextRoot = contextRoot;
    this.versionsController = versionsController;
    this.recentChanges = recentChanges;
    this.port = port;
    this.authenticator = authenticator;
    this.logger = logger;
    this.testSystemFactory = testSystemFactory;
    this.testSystemListener = testSystemListener;
    this.formatterFactory = formatterFactory;
    this.properties = properties;
    this.theme = theme;
    responderFactory = new ResponderFactory(getRootPagePath());
    this.variableSource = variableSource;
    fitNesse = new FitNesse(this);
    pageFactory = new PageFactory(this);
    testRunFactoryRegistry = new TestRunFactoryRegistry(this);
    testRunFactoryRegistry.addFactory(new FileBasedTestRunFactory(this));
  }

  public WikiPage getRootPage() {
    return getRootPage(variableSource);
  }

  public WikiPage getRootPage(Map<String, String> customProperties) {
    return getRootPage(new UrlPathVariableSource(variableSource, customProperties));
  }

  private WikiPage getRootPage(VariableSource variableSource) {
    return wikiPageFactory.makePage(new File(rootPath, rootDirectoryName), rootDirectoryName, null, variableSource);

  }
  public File getTestHistoryDirectory() {
    String testHistoryPath = getProperty("test.history.path");
    if (testHistoryPath == null) {
      testHistoryPath = String.format(unifiedPathPattern("%s/files/%s"), getRootPagePath(), testResultsDirectoryName);
    }
    return new File(testHistoryPath);
  }

  public String getTestProgressPath() {
    return String.format(unifiedPathPattern("%s/files/testProgress"), getRootPagePath());
  }

  public String getRootPagePath() {
    return String.format(unifiedPathPattern("%s/%s"), rootPath, rootDirectoryName);
  }

  public Properties getProperties() {
    return properties;
  }

  public String getProperty(String name) {
    return variableSource.getProperty(name);
  }

  private String unifiedPathPattern(String s) {
    return StringUtils.replace(s, "/", File.separator);
  }
}
