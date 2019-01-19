package fitnesse.html.template;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExtProperties;

import java.io.InputStreamReader;
import java.io.Reader;

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
  public Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {
    InputStreamReader result = null;

    if (StringUtils.isEmpty(name)) {
      throw new ResourceNotFoundException("No template name provided");
    }

    String path = base + name;
    try {
      result = new InputStreamReader(ClassUtils.getResourceAsStream(getClass(), path), encoding);
    } catch (Exception fnfe) {
      throw new ResourceNotFoundException("problem with template: " + path, fnfe);
    }

    if (result == null) {
      throw new ResourceNotFoundException("ClasspathResourceLoader Error: cannot find resource " + path);
    }

    return result;
  }

  @Override
  public void init(ExtProperties configuration) {
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
