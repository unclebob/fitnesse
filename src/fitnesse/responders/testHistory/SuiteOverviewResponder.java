package fitnesse.responders.testHistory;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.text.DateFormatter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.htmlparser.util.ParserException;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.run.SuiteContentsFinder;
import fitnesse.responders.run.SuiteFilter;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class SuiteOverviewResponder implements Responder {

  private FitNesseContext context;
  
  public Response makeResponse(FitNesseContext context, Request request) throws ParserException {
    this.context = context;
    WikiPage root = context.root;
    WikiPage page = root.getPageCrawler().getPage(root, PathParser.parse(request.getResource()));
    
    SuiteFilter filter = new SuiteFilter(request, page.getPageCrawler().getFullPath(page).toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    
    List<WikiPage> pagelist = suiteTestFinder.makePageList();

    SuiteOverviewTree treeview = new SuiteOverviewTree(pagelist);
    treeview.findLatestResults(context.getTestHistoryDirectory());
    treeview.countResults();
    
    WikiPagePath path = PathParser.parse(request.getResource());
    SimpleResponse response = makeResponse(treeview, path);
    return response;

  }

  private SimpleResponse makeResponse(SuiteOverviewTree treeview, WikiPagePath path) {
    SimpleResponse response = new SimpleResponse();
    
    HtmlPage page = context.htmlPageFactory.newPage();
    page.setTitle("Suite Overview");
    page.setPageTitle(new PageTitle("Suite Overview", path));
    page.put("treeRoot", treeview.getTreeRoot());
    
    page.setMainTemplate("suiteOverview.vm");
    response.setContent(page.html());
    return response;
  }
}
