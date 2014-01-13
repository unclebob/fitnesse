// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fitnesse.components.TraversalListener;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Paths;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiSourcePage;
import util.Wildcard;

public class ClassPathBuilder {
  private List<String> allPaths;
  private StringBuilder pathsString;
  private Set<String> addedPaths;

  public String getClasspath(WikiPage page) {
    List<String> paths = getInheritedPathElements(page);
    return createClassPathString(paths, getPathSeparator(page));
  }

  public List<String> getInheritedPathElements(WikiPage page) {
    final List<String> items = new ArrayList<String>();

    page.getPageCrawler().traversePageAndAncestors(new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage p) {
        addItemsFromPage(p, items);
      }
    });
    return items;
  }

  public String buildClassPath(List<WikiPage> testPages) {
    if (testPages.size() == 0) {
      return "";
    }
    final String pathSeparator = getPathSeparator(testPages.get(0));
    List<String> classPathElements = new ArrayList<String>();

    for (WikiPage testPage : testPages) {
      addClassPathElements(testPage, classPathElements);
    }

    return createClassPathString(classPathElements, pathSeparator);
  }

  private void addClassPathElements(WikiPage page, List<String> classPathElements) {
    List<String> pathElements = getInheritedPathElements(page);
    classPathElements.addAll(pathElements);
  }

  public String getPathSeparator(WikiPage page) {
    String separator = page.getData().getVariable(PageData.PATH_SEPARATOR);
    if (separator == null)
      separator = System.getProperty("path.separator");
    return separator;
  }


  public String createClassPathString(List<String> paths, String separator) {
    if (paths.isEmpty())
      return "defaultPath";

    pathsString = new StringBuilder();
    paths = expandWildcards(paths);
    addedPaths = new HashSet<String>();

    for (String path : paths)
      addPathToClassPathString(separator, path);

    return pathsString.toString();
  }

  private void addPathToClassPathString(String separator, String path) {
    path = surroundPathWithQuotesIfItHasSpaces(path);

    if (!addedPaths.contains(path)) {
      addedPaths.add(path);
      if (pathsString.length() > 0)
        pathsString.append(separator);
      pathsString.append(path);
    }
  }

  private String surroundPathWithQuotesIfItHasSpaces(String path) {
    if (path.matches(".*\\s.*") && !path.contains("\""))
      path = "\"" + path + "\"";
    return path;
  }

  private List<String> expandWildcards(List<String> paths) {
    allPaths = new ArrayList<String>();
    for (String path : paths)
      expandWildcards(path);

    return allPaths;
  }

  private void expandWildcards(String path) {
    File file = new File(path);
    File dir = new File(file.getAbsolutePath()).getParentFile();
    if (isExpandableDoubleWildcard(path, dir))
      recursivelyAddMatchingFiles(path, dir);
    else if (isExpandableSingleWildcard(path, dir))
      addMatchingFiles(path, dir);
    else
      allPaths.add(path);
  }

  private void recursivelyAddMatchingFiles(String path, File dir) {
    String singleWildcardPath = convertDoubleToSingleWildcard(path);
    addMatchingSubfiles(singleWildcardPath, dir);
  }

  private boolean isExpandableSingleWildcard(String path, File dir) {
    return pathHasSingleWildcard(path) && dir.exists();
  }

  private boolean isExpandableDoubleWildcard(String path, File dir) {
    return pathHasDoubleWildCard(path) && dir.exists();
  }

  private boolean pathHasSingleWildcard(String path) {
    return path.indexOf('*') != -1;
  }

  private String convertDoubleToSingleWildcard(String path) {
    path = path.replaceFirst("\\*\\*", "*");
    return path;
  }

  private boolean pathHasDoubleWildCard(String path) {
    return path.contains("**");
  }

  private void addMatchingFiles(String path, File dir) {
    String fileName = new File(path).getName();
    File[] files = dir.listFiles(new Wildcard(fileName));
    for (File file : files) {
      allPaths.add(file.getPath());
    }
  }

  private void addMatchingSubfiles(String path, File dir) {
    addMatchingFiles(path, dir);
    for (File file : dir.listFiles()) {
      if (file.isDirectory())
        addMatchingSubfiles(path, file);
    }
  }

  private void addItemsFromPage(WikiPage itemPage, List<String> items) {
    List<String> itemsOnThisPage = getItemsFromPage(itemPage);
    items.addAll(itemsOnThisPage);
  }

  protected List<String> getItemsFromPage(WikiPage page) {
    PageData data = page.getData();
    Symbol tree = data.getParsedPage().getSyntaxTree();
    ParsingPage parsingPage = data.getParsedPage().getParsingPage();
    return new Paths(new HtmlTranslator(new WikiSourcePage(page), parsingPage)).getPaths(tree);
  }


}
