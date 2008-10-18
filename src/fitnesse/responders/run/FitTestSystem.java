package fitnesse.responders.run;

import fitnesse.components.CommandRunningFitClient;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

public class FitTestSystem extends TestSystem {
  private CommandRunningFitClient client;

  public FitTestSystem(FitNesseContext context, WikiPage page, TestSystemListener listener) {
    super(context, page, listener);
  }

  public ExecutionLog createRunner(String classPath, String className) throws Exception {
    String command = buildCommand(className, classPath);
    client = new CommandRunningFitClient(listener, command, context.port, context.socketDealer);
    return new ExecutionLog(page, client.commandRunner);
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

  public void start() throws Exception {
    client.start();
  }
}
