package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class ClientBuilder<T> {

  protected final Descriptor descriptor;
  protected boolean fastTest;
  protected boolean manualStart;
  protected boolean remoteDebug;

  public ClientBuilder(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  protected String buildCommand(String commandPattern, String testRunner, String classPath) {
    String command = Descriptor.replace(commandPattern, "%p", classPath);
    command = Descriptor.replace(command, "%m", testRunner);
    return command;
  }

  public ClientBuilder<T> withFastTest(boolean fastTest) {
    this.fastTest = fastTest;
    return this;
  }

  public ClientBuilder<T> withManualStart(boolean manualStart) {
    this.manualStart = manualStart;
    return this;
  }

  public ClientBuilder<T> withRemoteDebug(boolean remoteDebug) {
    this.remoteDebug = remoteDebug;
    return this;
  }

  public abstract T build() throws IOException;

}
