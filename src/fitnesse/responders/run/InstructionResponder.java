package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
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

import java.util.List;

public class InstructionResponder implements Responder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage root = context.getRootPage(request.getMap());
    WikiPage suite = root.getPageCrawler().getPage(PathParser.parse(request.getResource()));

    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(suite, SuiteFilter.MATCH_ALL, root);
    List<WikiPage> pages = suiteTestFinder.getAllPagesToRunForThisSuite();

    TestRun run = new PartitioningTestRunFactory().createRun(pages);
    StringBuilder result = new StringBuilder();
    MultipleTestsRunner runner = new MultipleTestsRunner(run, new InstructionTestSystemFactory(result));
    try {
      runner.executeTestPages();
    } catch (TestExecutionException e) {
      e.printStackTrace();
    }

    SimpleResponse response = new SimpleResponse();
    response.setContent(result.toString());
    return response;
  }

  static class InstructionTestSystemFactory implements TestSystemFactory {

    public InstructionTestSystemFactory(StringBuilder result) {
      this.result = result;
    }

    @Override
    public TestSystem create(Descriptor descriptor) {
      return new InstructionTestSystem(result);
    }

    private final StringBuilder result;
  }
}
