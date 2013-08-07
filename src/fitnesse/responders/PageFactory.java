// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.responders.templateUtilities.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

public class PageFactory {

  private static VelocityEngine velocityEngine = null;
  private FitNesseContext context;

  public PageFactory(FitNesseContext context) {
    super();
    this.context = context;
  }

  public HtmlPage newPage() {
    return new HtmlPage(getVelocityEngine(), "skeleton.vm", context.pageTheme);
  }

  public String render(VelocityContext context, String templateName) {
    Writer writer = new StringWriter();
    Template template = getVelocityEngine().getTemplate(templateName);
    template.merge(context, writer);
    return writer.toString();
  }

  public String toString() {
    return getClass().getName();
  }

  public VelocityEngine getVelocityEngine() {
    if (velocityEngine == null) {
      Properties properties = new Properties();

      properties.setProperty(VelocityEngine.RESOURCE_LOADER, "file,themepath,classpath");

      properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
          String.format("%s/%s/files/fitnesse/templates", context.rootPath, context.rootDirectoryName));

      properties.setProperty("themepath." + VelocityEngine.RESOURCE_LOADER + ".class",
          ClasspathResourceLoader.class.getName());
      properties.setProperty("themepath." + VelocityEngine.RESOURCE_LOADER + ".base",
          String.format("/fitnesse/resources/%s/templates", context.pageTheme));

      properties.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
          fitnesse.responders.templateUtilities.ClasspathResourceLoader.class.getName());
      properties.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".base",
          "/fitnesse/resources/templates");

      properties.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
              VelocityLogger.class.getName());

      velocityEngine = new VelocityEngine();
      velocityEngine.init(properties);

      velocityEngine.loadDirective(TraverseDirective.class.getName());
      velocityEngine.loadDirective(EscapeDirective.class.getName());
    }
    return velocityEngine;
  }

}
