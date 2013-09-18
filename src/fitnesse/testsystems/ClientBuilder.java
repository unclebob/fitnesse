package fitnesse.testsystems;

import java.io.IOException;
import java.util.regex.Matcher;

public abstract class ClientBuilder<T> {

  protected final Descriptor descriptor;

  public ClientBuilder(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  protected String buildCommand(String commandPattern, String testRunner, String classPath) {
    String command = replace(commandPattern, "%p", classPath);
    command = replace(command, "%m", testRunner);
    return command;
  }


  protected static String replace(String value, String mark, String replacement) {
    return value.replaceAll(mark, Matcher.quoteReplacement(replacement));
  }

  public abstract T build() throws IOException;

}
