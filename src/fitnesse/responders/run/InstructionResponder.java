package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testrunner.run.PartitioningTestRunFactory;
import fitnesse.testrunner.run.TestRun;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.InstructionTestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.List;

public class InstructionResponder implements Responder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    return new InstructionResponse(context, request);
  }

  static class InstructionResponse extends Response {

    public InstructionResponse(FitNesseContext context, Request request) {
      super("text");
      this.request = request;
      this.context = context;
    }

    @Override
    public void sendTo(ResponseSender sender) throws IOException {
      WikiPage root = context.getRootPage(request.getMap());
      WikiPage suite = root.getPageCrawler().getPage(PathParser.parse(request.getResource()));

      SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(suite, SuiteFilter.MATCH_ALL, root);
      List<WikiPage> pages = suiteTestFinder.getAllPagesToRunForThisSuite();

      TestRun run = new PartitioningTestRunFactory().createRun(pages);
      MultipleTestsRunner runner = new MultipleTestsRunner(run, new InstructionTestSystemFactory(sender));
      try {
        runner.executeTestPages();
      } catch (TestExecutionException e) {
        e.printStackTrace();
      }
    }

    @Override
    public int getContentSize() { return 0; }

    private final Request request;
    private final FitNesseContext context;
  }

  static class InstructionTestSystemFactory implements TestSystemFactory {

    public InstructionTestSystemFactory(ResponseSender sender) {
      this.sender = sender;
    }

    @Override
    public TestSystem create(Descriptor descriptor) {
      return new InstructionTestSystem(sender);
    }

    private final ResponseSender sender;
  }
}
