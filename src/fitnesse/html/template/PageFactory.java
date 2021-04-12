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
  private final String theme;
  private final String contextRoot;
  private VelocityEngine velocityEngine = null;

  public PageFactory(FitNesseContext context) {
    this.theme = context.theme;
    this.velocityEngine = newVelocityEngine(context.getRootPagePath(), this.theme);
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

  @Override
  public String toString() {
    return getClass().getName();
  }

  private VelocityEngine newVelocityEngine(String rootPagePath, String theme) {
    Properties properties = new Properties();
    properties.setProperty(VelocityEngine.CHECK_EMPTY_OBJECTS, "false");
    properties.setProperty(VelocityEngine.INPUT_ENCODING, FileUtil.CHARENCODING);

    properties.setProperty(VelocityEngine.RESOURCE_LOADERS, "file,themepath,classpath");

    properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
        String.format("%s/files/fitnesse/templates", rootPagePath));

    properties.setProperty(VelocityEngine.RESOURCE_LOADER + ".themepath.class",
        ClasspathResourceLoader.class.getName());
    properties.setProperty(VelocityEngine.RESOURCE_LOADER + ".themepath.base",
        String.format("/fitnesse/resources/%s/templates", theme));

    properties.setProperty(VelocityEngine.RESOURCE_LOADER + ".classpath.class",
        ClasspathResourceLoader.class.getName());
    properties.setProperty(VelocityEngine.RESOURCE_LOADER + ".classpath.base",
        "/fitnesse/resources/templates");

    VelocityEngine engine = new VelocityEngine();
    engine.init(properties);

    engine.loadDirective(TraverseDirective.class.getName());
    engine.loadDirective(EscapeDirective.class.getName());
    return engine;
  }
}
