package fitnesse;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import java.io.StringWriter;
import java.io.Writer;

public class VelocityFactory {
  private VelocityEngine velocityEngine = null;
  private static VelocityFactory instance = null;
  private String rootPath;
  private String rootDirectoryName;

  public static void makeVelocityFactory(FitNesseContext context) {
    if (instance==null)
      instance = new VelocityFactory(context.rootPath, context.rootDirectoryName);
  }

  public static void setVelocityEngine(VelocityEngine velocityEngine) {
    instance.velocityEngine = velocityEngine;
  }

  public VelocityFactory(String rootPath, String rootDirectoryName) {
    this.rootPath = rootPath;
    this.rootDirectoryName = rootDirectoryName;
  }


  public static String translateTemplate(VelocityContext velocityContext, String templateFileName) {
    StringWriter writer = new StringWriter();
    translateTemplate(velocityContext, templateFileName, writer);
    return writer.toString();
  }

  public static void translateTemplate(VelocityContext velocityContext, String templateFileName, Writer writer) {
    Template template = instance.getVelocityEngine().getTemplate(templateFileName);
    template.merge(velocityContext, writer);
  }

  public static VelocityEngine getVelocityEngine() {
    if (instance.velocityEngine == null) {
      instance.velocityEngine = new VelocityEngine();
      instance.velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "file,classpath");
      String templatePath = String.format("%s/%s/files/templates", instance.rootPath, instance.rootDirectoryName);
      instance.velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templatePath);

//      instance.velocityEngine.setProperty(
//            "file." + VelocityEngine.RESOURCE_LOADER + ".class",
//            FileResourceLoader.class.getName());

      instance.velocityEngine.setProperty(
            "classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
            org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());

      instance.velocityEngine.init(templatePath + "/velocity.properties");
    }
    return instance.velocityEngine;
  }
}
