package fitnesse.responders.editing;

import java.util.*;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class TemplateUtil {
  
  public static List<String> getTemplatesFromUncles(WikiPage page) {
    List<WikiPage> wikiUncles = new LinkedList<WikiPage>();
    final List<String> templatePaths = new ArrayList<String>();
    page.getPageCrawler().traverseUncles("TemplateLibrary", new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage uncle) {
        for (WikiPage template : uncle.getChildren()) {
          WikiPagePath templatePath = new WikiPagePath(template);
          templatePath.makeAbsolute();
          templatePaths.add(PathParser.render(templatePath));
        }
      }
    });
    return templatePaths;
  }
  
  public static Map<String, String> getTemplateMap(WikiPage page){
    return getShortTemplateNames(getTemplatesFromUncles(page));
  }
  
  public static Map<String, String> getShortTemplateNames(List<String> templatePaths) {
    Map<String, String> pathsAndNames = new HashMap<String, String>();
    for(String path : templatePaths){
      pathsAndNames.put(path, getShortTemplateName(path));
    }
    return pathsAndNames;
  }
  
  public static String getShortTemplateName(String path) {
    String pathCopy = path;
    
    String templateName = pathCopy.substring(pathCopy.lastIndexOf('.') + 1);
    pathCopy = pathCopy.substring(0, pathCopy.lastIndexOf('.'));
    
    String templateLibraryString = pathCopy.substring(pathCopy.lastIndexOf('.') + 1);
    pathCopy = pathCopy.substring(0, pathCopy.lastIndexOf('.'));
    if(templateLibraryString.equals("TemplateLibrary")) {
      templateLibraryString = "_";
    }
    
    String parentName = pathCopy.substring(pathCopy.lastIndexOf('.') + 1);
    
    return parentName + "." + templateLibraryString + "." + templateName;
  }
}
