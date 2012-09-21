package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class TemplateUtil {
  
  public static List<String> getTemplatesFromUncles(WikiPage page) {
    List<WikiPage> wikiUncles = PageCrawlerImpl.getAllUncles("TemplateLibrary", page);
    List<String> templatePaths = new ArrayList<String>();
    for(WikiPage wikiUncle : wikiUncles){
      for(WikiPage template : wikiUncle.getChildren()){
        WikiPagePath templatePath = new WikiPagePath(template);
        templatePath.makeAbsolute();
        templatePaths.add(PathParser.render(templatePath));
      }
    }
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
