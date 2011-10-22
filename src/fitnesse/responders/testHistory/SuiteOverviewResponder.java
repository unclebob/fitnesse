package fitnesse.responders.testHistory;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.text.DateFormatter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.run.SuiteContentsFinder;
import fitnesse.responders.run.SuiteFilter;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SuiteOverviewResponder implements Responder {

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage root = context.root;
    WikiPage page = root.getPageCrawler().getPage(root, PathParser.parse(request.getResource()));
    
    SuiteFilter filter = new SuiteFilter(request, page.getPageCrawler().getFullPath(page).toString());
    SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
    
    List<WikiPage> pagelist = suiteTestFinder.makePageList();
    SuiteOverviewTree treeview = new SuiteOverviewTree(pagelist);
    treeview.findLatestResults(context.getTestHistoryDirectory());
    treeview.countResults();
    
    SimpleResponse response = new SimpleResponse(400);
    
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("treeRoot", treeview.getTreeRoot());
    PageTitle title = new PageTitle("Suite Overview", PathParser.parse(request.getResource()));
    velocityContext.put("pageTitle", title);
    
    
    
    String velocityTemplate = "suiteOverview.vm";
    Template template = VelocityFactory.getVelocityEngine().getTemplate(velocityTemplate);
    StringWriter writer = new StringWriter();
    template.merge(velocityContext, writer);
    response.setContent(writer.toString());
    return response;

  }
}
