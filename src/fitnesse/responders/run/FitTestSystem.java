package fitnesse.responders.run;

import fitnesse.components.CommandRunningFitClient;
import fitnesse.components.CommandRunner;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.FitNesseContext;
import fitnesse.wiki.PageData;

public class FitTestSystem extends TestSystem {
  private CommandRunningFitClient client;

  public FitTestSystem(FitNesseContext context, PageData data, TestSystemListener listener) {
    super(context, data, listener);
  }

  public CommandRunner start(String classPath, String className) throws Exception {
    String command = buildCommand(className, classPath);
    client = new CommandRunningFitClient(listener, command, context.port, context.socketDealer);
    client.start();
    return client.commandRunner;
  }


  public void bye() throws Exception {
    client.done();
    client.join();
  }

  public void send(String s) throws Exception {
    client.send(s);
  }

  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }

  public void kill() throws Exception {
    client.kill();
  }
}
