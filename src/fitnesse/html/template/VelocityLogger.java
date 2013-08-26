package fitnesse.html.template;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Log messages from Velocity. Currently nothing is logged.
 */
public class VelocityLogger implements LogChute {

  @Override
  public void init(RuntimeServices runtimeServices) throws Exception {
    // Nothing to do here.
  }

  @Override
  public void log(int i, String s) {
    // Nothing to see.
  }

  @Override
  public void log(int i, String s, Throwable throwable) {
    // Nothing to see.
  }

  @Override
  public boolean isLevelEnabled(int i) {
    // Do not log.
    return false;
  }
}
