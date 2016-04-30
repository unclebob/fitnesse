// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikitextPage;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Paths;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiSourcePage;

public class ClassPathBuilder {

  public List<String> getClassPath(WikiPage page) {
    List<String> paths = getInheritedPathElements(page);
    return createClassPath(paths);
  }

  private List<String> getInheritedPathElements(WikiPage page) {
    final List<String> items = new ArrayList<>();

    page.getPageCrawler().traversePageAndAncestors(new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage p) {
        items.addAll(getItemsFromPage(p));
      }
    });
    return items;
  }

  public List<String> createClassPath(List<String> paths) {
    paths = expandWildcards(paths);
    Set<String> addedPaths = new HashSet<>();
    List<String> classPath = new ArrayList<>();

    for (String path : paths) {
      if (!addedPaths.contains(path)) {
        addedPaths.add(path);
        classPath.add(path);
      }
    }

    return classPath;
  }

  private List<String> expandWildcards(List<String> paths) {
    List<String> allPaths = new ArrayList<>();
    for (String path : paths)
      allPaths.addAll(expandWildcard(path));
    return allPaths;
  }

  private List<String> expandWildcard(String path) {
    List<String> allPaths = new ArrayList<>();
    File file = new File(path);
    File dir = new File(file.getAbsolutePath()).getParentFile();
    if (isExpandableDoubleWildcard(path, dir))
      allPaths.addAll(recursivelyAddMatchingFiles(path, dir));
    else if (isExpandableSingleWildcard(path, dir))
      allPaths.addAll(getMatchingFiles(path, dir));
    else
      allPaths.add(path);
    return allPaths;
  }

  private List<String> recursivelyAddMatchingFiles(String path, File dir) {
    String singleWildcardPath = convertDoubleToSingleWildcard(path);
    return getMatchingSubfiles(singleWildcardPath, dir);
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

  private List<String> getMatchingFiles(String path, File dir) {
    String fileName = new File(path).getName();
    File[] files = dir.listFiles(new Wildcard(fileName));
    List<String> allPaths = new ArrayList<>();
    for (File file : files) {
      allPaths.add(file.getPath());
    }
    return allPaths;
  }

  private List<String> getMatchingSubfiles(String path, File dir) {
    List<String> allPaths = new ArrayList<>();
    allPaths.addAll(getMatchingFiles(path, dir));
    for (File file : dir.listFiles()) {
      if (file.isDirectory())
        allPaths.addAll(getMatchingSubfiles(path, file));
    }
    return allPaths;
  }

  protected List<String> getItemsFromPage(WikiPage page) {
    if (page instanceof WikitextPage) {
      Symbol tree = ((WikitextPage) page).getSyntaxTree();
      ParsingPage parsingPage = ((WikitextPage) page).getParsingPage();
      return new Paths(new HtmlTranslator(new WikiSourcePage(page), parsingPage)).getPaths(tree);
    }
    return Collections.emptyList();
  }


  public static class Wildcard implements FilenameFilter {
    private String pattern;
    private String prefix;
    private String suffix;
    private int length;

    public Wildcard(String pattern) {
      int starIndex = pattern.indexOf("*");
      if (starIndex > -1) {
        prefix = pattern.substring(0, starIndex);
        suffix = pattern.substring(starIndex + 1);
        length = prefix.length() + suffix.length();
      } else {
        this.pattern = pattern;
      }
    }

    @Override
    public boolean accept(File dir, String name) {
      if (pattern != null)
        return pattern.equals(name);

      boolean goodLength = name.length() >= length;
      boolean goodPrefix = name.startsWith(prefix);
      boolean goodSufix = name.endsWith(suffix);

      return goodLength && goodPrefix && goodSufix;
    }
  }
}
