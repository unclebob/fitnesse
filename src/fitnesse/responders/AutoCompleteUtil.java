package fitnesse.responders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import fitnesse.FitNesseContext;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class AutoCompleteUtil {

  // create tree structure with suites or testcases
  private static List<String> createTree(List<String> tree, FitNesseContext context, String root, String type) {
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    WikiPagePath RootPath = PathParser.parse(root);
    WikiPage RootPage = crawler.getPage(RootPath);

    if (RootPage != null) {
      // only create tree if there are nodes
      if (RootPage.getChildren().size()>0) {
        // iterate through all childs of the root page
        List<WikiPage> NodeList = RootPage.getChildren();

        WikiPage CurrentNode;
        for (int i = 0; i < NodeList.size(); i++) {
          CurrentNode = NodeList.get(i);
          String currentNodePath = getPathOfCurrentElement(CurrentNode.toString());
          // only get suites or testcases
          if (CurrentNode.getData().hasAttribute("Suite") || CurrentNode.getData().hasAttribute("Test")) {
            // if CurrentNode is suite
            if (CurrentNode.getChildren().size() > 0) {
              // get suites only if requested type is "suite"
              if (CurrentNode.getData().hasAttribute("Suite") && type.equals("Suite")) {
                tree.add(currentNodePath);
              }
              // recursive call for suites that are laying deeper in the tree
              createTree(tree, context, currentNodePath, type);
            }
            else {
              // if CurrentNode has no children and it is a test, then final test node found
              if (CurrentNode.getData().hasAttribute("Test") && type.equals("Test")) {
                tree.add(currentNodePath);
              }
              // if CurrentNode has no children and it is a suite, then final suite node found
              if (CurrentNode.getData().hasAttribute("Suite") && type.equals("Suite")) {
                tree.add(currentNodePath);
              }
            }
          }
        }
      }
    }
    return tree;
  }
  // modify path string to usable form
  private static String getPathOfCurrentElement (String currentNodeToString) {
    // needs Node.toString() as input
    String[] parts = currentNodeToString.split("\\.");
    // only take the path part
    String path = parts[(parts.length-1)];
    // cut the first 14 chars
    path = path.substring(14);
    // cut the last 7 chars
    path = path.substring(0,path.length()-7);
    // replace \ by .
    if (path.contains("\\")) {
      path = path.replace("\\", ".");
    }
    return path;
  }



  public static List<String> createTestList(FitNesseContext context) {
    List<String> TestList = new ArrayList<String>();
    TestList = createTree(TestList, context, "/", "Test");
    if ((TestList.size() > 0)) {
      return TestList;
    }
    else return null;
  }

  public static List<String> createSuiteList(FitNesseContext context) {
    List<String> SuiteList = new ArrayList<String>();
    SuiteList = createTree(SuiteList, context, "/", "Suite");
    if ((SuiteList.size() > 0)) {
      return SuiteList;
    }
    else return null;
  }
}
