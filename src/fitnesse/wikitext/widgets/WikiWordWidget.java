// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import util.StringUtil;
import fitnesse.components.PageReferencer;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;
import fitnesse.wikitext.WidgetVisitor;

public class WikiWordWidget extends TextWidget implements PageReferencer {
  public static final String SINGLE_WIKIWORD_REGEXP = "\\b[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+";
  public static final String REGEXP = "(?:[<>^.])?(?:" + SINGLE_WIKIWORD_REGEXP + "[.]?)+\\b";
  public static final String REGRACE_LINK = "REGRACE_LINK";

  public WikiPage parentPage;

  public WikiWordWidget(ParentWidget parent, String text) throws Exception {
    super(parent, text);
    WikiPage wikiPage = getWikiPage();
    parentPage = wikiPage.getParent();
  }

  public String render() throws Exception {
    WikiPagePath pathOfWikiWord = PathParser.parse(getWikiWord());
    WikiPagePath fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
    String qualifiedName = PathParser.render(fullPathOfWikiWord);
    WikiPage targetPage = parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(getWikiWord()));
    if (targetPage != null)
      return makeLinkToExistingWikiPage(qualifiedName, targetPage);
    else
      return makeLinkToNonExistentWikiPage(qualifiedName);
  }

  private String makeLinkToNonExistentWikiPage(String qualifiedName) {
    StringBuffer html = new StringBuffer();
    html.append(Utils.escapeHTML(getText()));
    html.append("<a title=\"create page\" href=\"").append(qualifiedName);
    html.append("?edit&nonExistent=true");
    html.append("\">[?]</a>");
    return html.toString();
  }

  public boolean isRegracing() {
    Boolean isDoingIt = false;
    try {
      isDoingIt = "true".equals(parent.getVariable(REGRACE_LINK));
    }
    catch (Exception e) {
    }
    return isDoingIt;
  }

  private String makeLinkToExistingWikiPage(String qualifiedName, WikiPage wikiPage) throws Exception {
    HtmlTag link = HtmlUtil.makeLink(qualifiedName, Utils.escapeHTML(regrace(getText())));
    addHelpText(link, wikiPage);
    return link.htmlInline();
  }

  private void addHelpText(HtmlTag link, WikiPage wikiPage) throws Exception {
    String helpText = wikiPage.getHelpText();
    if (helpText != null) link.addAttribute("title", helpText);
  }

  // If pageToRename is referenced somewhere in this wiki word (could be a parent, etc.),
  // rename it to newName.
  public void renamePageIfReferenced(WikiPage pageToRename, String newName) throws Exception {
    String fullPathToReferent = getQualifiedWikiWord();
    WikiPagePath pathToPageBeingRenamed = pageToRename.getPageCrawler().getFullPath(pageToRename);
    pathToPageBeingRenamed.makeAbsolute();
    String absolutePathToPageBeingRenamed = PathParser.render(pathToPageBeingRenamed);

    if (refersTo(fullPathToReferent, absolutePathToPageBeingRenamed)) {
      int oldNameLength = absolutePathToPageBeingRenamed.length();
      String renamedPath = "." + rename(absolutePathToPageBeingRenamed.substring(1), newName);
      String pathAfterRenamedPage = fullPathToReferent.substring(oldNameLength);
      String fullRenamedPathToReferent = renamedPath + pathAfterRenamedPage;
      String renamedReference = makeRenamedRelativeReference(PathParser.parse(fullRenamedPathToReferent));
      setText(renamedReference);
    }
  }

  public void renameMovedPageIfReferenced(WikiPage pageToBeMoved, String newParentName) throws Exception {
    WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getPageCrawler().getFullPath(pageToBeMoved);
    pathOfPageToBeMoved.makeAbsolute();
    String QualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
    String reference = getQualifiedWikiWord();
    if (refersTo(reference, QualifiedNameOfPageToBeMoved)) {
      String referenceTail = reference.substring(QualifiedNameOfPageToBeMoved.length());
      String childPortionOfReference = pageToBeMoved.getName();
      if (referenceTail.length() > 0)
        childPortionOfReference += referenceTail;
      String newQualifiedName;
      if ("".equals(newParentName))
        newQualifiedName = "." + childPortionOfReference;
      else
        newQualifiedName = "." + newParentName + "." + childPortionOfReference;

      setText(newQualifiedName);
    }
  }

  public String makeRenamedRelativeReference(WikiPagePath renamedPathToReferent) throws Exception {
    String rawReference = getText();
    WikiPagePath parentPath = parentPage.getPageCrawler().getFullPath(parentPage);
    parentPath.makeAbsolute();

    if (rawReference.startsWith("."))
      return PathParser.render(renamedPathToReferent);
    else if (rawReference.startsWith("<")) {
      return buildBackwardSearchReference(parentPath, renamedPathToReferent);
    } else {
      boolean parentPathNotRenamed = renamedPathToReferent.startsWith(parentPath);
      if (parentPathNotRenamed) {
        WikiPagePath relativePath = renamedPathToReferent.subtractFromFront(parentPath);
        if (rawReference.startsWith("^") || rawReference.startsWith(">"))
          return ">" + PathParser.render(relativePath.getRest());
        else
          return PathParser.render(relativePath);
      }
    }
    return rawReference;
  }

  static String buildBackwardSearchReference(WikiPagePath parentPath, WikiPagePath renamedPathToReferent) {
    int branchPoint = findBranchPoint(parentPath.getNames(), renamedPathToReferent.getNames());
    List<String> referentPath = renamedPathToReferent.getNames();
    List<String> referentPathAfterBranchPoint = referentPath.subList(branchPoint, referentPath.size());
    String newRawReference = "<" + StringUtil.join(referentPathAfterBranchPoint, ".");
    return newRawReference;
  }

  private static int findBranchPoint(List<String> list1, List<String> list2) {
    int i;
    for (i = 0; i < list1.size(); i++) {
      if (!list1.get(i).equals(list2.get(i))) break;
    }
    return Math.max(0, i - 1);
  }

  static boolean refersTo(String qualifiedReference, String qualifiedTarget) {
    if (qualifiedReference.equals(qualifiedTarget))
      return true;
    if (qualifiedReference.startsWith(qualifiedTarget + "."))
      return true;
    return false;
  }

  private String getQualifiedWikiWord() throws Exception {
    String pathName = expandPrefix(getText());
    WikiPagePath expandedPath = PathParser.parse(pathName);
    if (expandedPath == null)
      return getText();
    WikiPagePath fullPath = parentPage.getPageCrawler().getFullPathOfChild(parentPage, expandedPath);
    return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
  }

  private String rename(String oldQualifiedName, String newPageName) {
    String newQualifiedName = oldQualifiedName;

    int lastDotIndex = oldQualifiedName.lastIndexOf(".");
    if (lastDotIndex < 1)
      newQualifiedName = newPageName;
    else
      newQualifiedName = oldQualifiedName.substring(0, lastDotIndex + 1) + newPageName;
    return newQualifiedName;
  }

  String getWikiWord() throws Exception {
    return expandPrefix(getText());
  }

  public static boolean isWikiWord(String word) {
    return Pattern.matches(REGEXP, word);
  }

  protected String expandPrefix(String theWord) throws Exception {
    WikiPage wikiPage = getWikiPage();
    return expandPrefix(wikiPage, theWord);
  }

  public static String expandPrefix(WikiPage wikiPage, String theWord) throws Exception {
    PageCrawler crawler = wikiPage.getPageCrawler();
    if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
      return wikiPage.getName() + "." + theWord.substring(1);
    } else if (theWord.charAt(0) == '<') {
      String undecoratedPath = theWord.substring(1);
      String[] pathElements = undecoratedPath.split("\\.");
      String target = pathElements[0];
      //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
      for (WikiPage current = wikiPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
        if (current.getName().equals(target)) {
          pathElements[0] = PathParser.render(crawler.getFullPath(current));
          return "." + StringUtil.join(Arrays.asList(pathElements), ".");
        }
      }
      return "." + undecoratedPath;
    }
    return theWord;
  }

  public WikiPage getReferencedPage() throws Exception {
    String theWord = getWikiWord();
    return parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(theWord));
  }

  public void acceptVisitor(WidgetVisitor visitor) throws Exception {
    visitor.visit(this);
  }

  public static boolean isSingleWikiWord(String s) {
    return Pattern.matches(SINGLE_WIKIWORD_REGEXP, s);
  }
}
