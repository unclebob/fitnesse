// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html.template;

import fitnesse.FitNesseContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import util.FileUtil;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

public class PageFactory {
  public static final String THEME_PROPERTY = "Theme";
  public static final String DEFAULT_THEME = "bootstrap";

  private final String theme;
  private final String contextRoot;
  private VelocityEngine velocityEngine = null;

  public PageFactory(FitNesseContext context) {
    super();
    String theme = context.getProperty(THEME_PROPERTY);
    this.theme = theme != null ? theme : DEFAULT_THEME;
    this.velocityEngine = newVelocityEngine(context, this.theme);
    this.contextRoot = context.contextRoot;
  }

  public HtmlPage newPage() {
    return new HtmlPage(getVelocityEngine(), "skeleton.vm", theme, contextRoot);
  }

  public String render(VelocityContext context, String templateName) {
    Writer writer = new StringWriter();
    Template template = getVelocityEngine().getTemplate(templateName, FileUtil.CHARENCODING);
    template.merge(context, writer);
    return writer.toString();
  }

  public String getTheme() {
    return theme;
  }

  public VelocityEngine getVelocityEngine() {
    return velocityEngine;
  }

  public String toString() {
    return getClass().getName();
  }

  private VelocityEngine newVelocityEngine(FitNesseContext context, String theme) {
    Properties properties = new Properties();

    properties.setProperty(VelocityEngine.INPUT_ENCODING, FileUtil.CHARENCODING);
    properties.setProperty(VelocityEngine.OUTPUT_ENCODING, FileUtil.CHARENCODING);

    properties.setProperty(VelocityEngine.RESOURCE_LOADER, "file,themepath,classpath");

    properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
        String.format("%s/files/fitnesse/templates", context.getRootPagePath()));

    properties.setProperty("themepath." + VelocityEngine.RESOURCE_LOADER + ".class",
        ClasspathResourceLoader.class.getName());
    properties.setProperty("themepath." + VelocityEngine.RESOURCE_LOADER + ".base",
        String.format("/fitnesse/resources/%s/templates", theme));

    properties.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
        ClasspathResourceLoader.class.getName());
    properties.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".base",
        "/fitnesse/resources/templates");

    properties.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
            VelocityLogger.class.getName());

    VelocityEngine engine = new VelocityEngine();
    engine.init(properties);

    engine.loadDirective(TraverseDirective.class.getName());
    engine.loadDirective(EscapeDirective.class.getName());
    return engine;
  }
}
