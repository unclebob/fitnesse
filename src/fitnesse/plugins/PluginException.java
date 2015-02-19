package fitnesse.plugins;

/**
 * Problems with plugins...
 */
public class PluginException extends Exception {

  public PluginException(String s, Throwable throwable) {
    super(s, throwable);
  }
}
