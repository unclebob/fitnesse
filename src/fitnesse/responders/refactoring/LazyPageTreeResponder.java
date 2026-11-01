package fitnesse.responders.refactoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.util.StringUtils;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class LazyPageTreeResponder implements SecureResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request)
      throws Exception {
    String path = request.getInput("path");

    WikiPage page = getPage(context, path);

    List<PageNode> children = buildChildren(page);
    sortChildren(children);

    StringBuilder json = new StringBuilder("[");
    children.forEach(pageNode -> {
      if (json.length() > 1) {
        json.append(",");
      }
      json.append(pageNode.toJsonObject());
    });
    json.append("]");

    SimpleResponse response = new SimpleResponse();
    response.setContentType("application/json");
    response.setContent(json.toString());

    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }

  private static WikiPage getPage(FitNesseContext context, String path) {
    WikiPage root = context.getRootPage();

    if (StringUtils.isBlank(path) || "ROOT".equalsIgnoreCase(path)) {
      return root;
    }

    WikiPagePath wikiPath = PathParser.parse(path);
    return root.getPageCrawler().getPage(wikiPath);
  }

  private static List<PageNode> buildChildren(WikiPage page) {
    List<PageNode> result = new ArrayList<>();
    
    if (page == null) {
      return result;
    }

    for (WikiPage child : page.getChildren()) {
      if (child.isSymbolicPage()) {
        continue;
      }
      String fullPath = child.getFullPath().toString();
      // If every child is a symbolic link we assume the page to not have any
      // children
      boolean hasChildren = !child.getChildren().stream()
          .allMatch(p -> p.isSymbolicPage());

      result.add(new PageNode(child.getName(), fullPath, hasChildren));
    }

    return result;
  }

  private static void sortChildren(List<PageNode> children) {
    children.sort(
        Comparator.comparing((PageNode n) -> Boolean.valueOf(!n.hasChildren))
            .thenComparing(n -> n.name.toLowerCase(Locale.getDefault())));
  }
}
