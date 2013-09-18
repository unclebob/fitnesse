package fitnesse.html.template;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExceptionUtils;

/**
 * Resource loader for Velocity. It loads resources rooted in a base directory.
 * <p>Please not that no effort is taken to "chroot" the path.</p>
 */
public class ClasspathResourceLoader extends ResourceLoader {

  private String base;
  
  @Override
  public long getLastModified(Resource resource) {
    return 0;
  }

  @Override
  public InputStream getResourceStream(String name) throws ResourceNotFoundException {
    InputStream result = null;

    if (StringUtils.isEmpty(name)) {
      throw new ResourceNotFoundException("No template name provided");
    }

    String path = base + name;
    try {
      result = ClassUtils.getResourceAsStream(getClass(), path);
    } catch (Exception fnfe) {
      throw (ResourceNotFoundException) ExceptionUtils.createWithCause(
          ResourceNotFoundException.class, "problem with template: " + path, fnfe);
    }

    if (result == null) {
      throw new ResourceNotFoundException("ClasspathResourceLoader Error: cannot find resource " + path);
    }

    return result;
  }

  @Override
  public void init(ExtendedProperties configuration) {
    base = configuration.getString("base");
    if (!base.endsWith("/")) {
      base = base + "/";
    }
  }

  @Override
  public boolean isSourceModified(Resource resource) {
    return false;
  }

}
