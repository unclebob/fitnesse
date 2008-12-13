package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.components.CommandRunningFitClient;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class FitTestSystem extends TestSystem {
  private CommandRunningFitClient client;
  private FitNesseContext context;

  public FitTestSystem(FitNesseContext context, WikiPage page, TestSystemListener listener) {
    super(page, listener);
    this.context = context;
  }

  protected ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws Exception {
    String command = buildCommand(descriptor, classPath);
    client = new CommandRunningFitClient(this, command, context.port, context.socketDealer, fastTest);
    return new ExecutionLog(page, client.commandRunner);
  }


  public void bye() throws Exception {
    client.done();
    client.join();
  }

  public String runTestsAndGenerateHtml(PageData pageData) throws Exception {
    String html = pageData.getHtml();
    if (html.length() == 0)
      client.send(emptyPageContent);
    else
      client.send(html);
    return html;
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
