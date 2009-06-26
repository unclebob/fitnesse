package fitnesse;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import java.io.StringWriter;

public class VelocityFactory {
  private VelocityEngine velocityEngine = null;
  private static VelocityFactory instance = null;
  private String rootPath;
  private String rootDirectoryName;

  public static void makeVelocityFactory(FitNesseContext context) {
    instance = new VelocityFactory(context.rootPath, context.rootDirectoryName);
  }

  public static void setVelocityEngine(VelocityEngine velocityEngine) {
    instance.velocityEngine = velocityEngine;
  }

  public VelocityFactory(String rootPath, String rootDirectoryName) {
    this.rootPath = rootPath;
    this.rootDirectoryName = rootDirectoryName;
  }


  public static String translateTemplate(VelocityContext velocityContext, String templateFileName) throws Exception {
    Template template = instance.getVelocityEngine().getTemplate(templateFileName);
    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);
    return writer.toString();
  }

  public static VelocityEngine getVelocityEngine() {
    if (instance.velocityEngine == null) {
      instance.velocityEngine = new VelocityEngine();
      String templatePath = String.format("%s/%s/files/templates", instance.rootPath, instance.rootDirectoryName);
      instance.velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templatePath);
      try {
        instance.velocityEngine.init();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return instance.velocityEngine;
  }
}
