// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.responders.run.ExecutionLog;
import static fitnesse.wiki.PageType.*;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.Paths;
import util.Clock;
import util.Maybe;
import util.StringUtil;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public class PageData implements ReadOnlyPageData, Serializable {

  private static final long serialVersionUID = 1L;


  // TODO: Find a better place for us
  public static final String PropertyLAST_MODIFIED = "LastModified";
  public static final String PropertyHELP = "Help";
  public static final String PropertyPRUNE = "Prune";
  public static final String PropertySEARCH = "Search";
  public static final String PropertyRECENT_CHANGES = "RecentChanges";
  public static final String PropertyFILES = "Files";
  public static final String PropertyWHERE_USED = "WhereUsed";
  public static final String PropertyREFACTOR = "Refactor";
  public static final String PropertyPROPERTIES = "Properties";
  public static final String PropertyVERSIONS = "Versions";
  public static final String PropertyEDIT = "Edit";
  public static final String PropertySUITES = "Suites";

  public static final String PAGE_TYPE_ATTRIBUTE = "PageType";
  public static final String[] PAGE_TYPE_ATTRIBUTES = { STATIC.toString(),
      TEST.toString(), SUITE.toString() };

  public static final String[] ACTION_ATTRIBUTES = { PropertyEDIT,
      PropertyVERSIONS, PropertyPROPERTIES,
      PropertyREFACTOR, PropertyWHERE_USED };

  public static final String[] NAVIGATION_ATTRIBUTES = {
      PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };

  public static final String[] NON_SECURITY_ATTRIBUTES = StringUtil
      .combineArrays(ACTION_ATTRIBUTES, NAVIGATION_ATTRIBUTES);

  public static final String PropertySECURE_READ = "secure-read";
  public static final String PropertySECURE_WRITE = "secure-write";
  public static final String PropertySECURE_TEST = "secure-test";
  public static final String[] SECURITY_ATTRIBUTES = { PropertySECURE_READ,
      PropertySECURE_WRITE, PropertySECURE_TEST };

  public static final String LAST_MODIFYING_USER = "LastModifyingUser";

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";

  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  private transient WikiPage wikiPage;
  private String content;
  private WikiPageProperties properties = new WikiPageProperties();
  private Set<VersionInfo> versions;

  public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  public static final String TEST_RUNNER = "TEST_RUNNER";
  public static final String PATH_SEPARATOR = "PATH_SEPARATOR";

  private transient ParsedPage parsedPage;

  public PageData(WikiPage page) {
    wikiPage = page;
    initializeAttributes();
    versions = new HashSet<VersionInfo>();
  }

  public PageData(WikiPage page, String content) {
    this(page);
    setContent(content);
  }

  public PageData(PageData data) {
    this(data.getWikiPage(), data.content);
    properties = new WikiPageProperties(data.properties);
    versions.addAll(data.versions);
    parsedPage = data.parsedPage;
  }

  public void initializeAttributes() {
    if (!isErrorLogsPage()) { 
      properties.set(PropertyEDIT, Boolean.toString(true));
      properties.set(PropertyPROPERTIES, Boolean.toString(true));
      properties.set(PropertyREFACTOR, Boolean.toString(true));
    }
    properties.set(PropertyWHERE_USED, Boolean.toString(true));
    properties.set(PropertyRECENT_CHANGES, Boolean.toString(true));
    properties.set(PropertyFILES, Boolean.toString(true));
    properties.set(PropertyVERSIONS, Boolean.toString(true));
    properties.set(PropertySEARCH, Boolean.toString(true));
    properties.setLastModificationTime(Clock.currentDate());

    initTestOrSuiteProperty();
  }

  private void initTestOrSuiteProperty() {
    final String pageName = wikiPage.getName();
    if (pageName == null) {
      handleInvalidPageName(wikiPage);
      return;
    }

    if (isErrorLogsPage())
      return;

    PageType pageType = PageType.getPageTypeForPageName(pageName);

    if (STATIC.equals(pageType))
      return;

    properties.set(pageType.toString(), Boolean.toString(true));
  }

  private boolean isErrorLogsPage() {
    PageCrawler crawler = wikiPage.getPageCrawler();
    String relativePagePath = crawler.getRelativeName(
        crawler.getRoot(wikiPage), wikiPage);
    return relativePagePath.startsWith(ExecutionLog.ErrorLogName);
  }

  // TODO: Should be written to a real logger, but it doesn't like FitNesse's
  // logger is
  // really intended for general logging.
  private void handleInvalidPageName(WikiPage wikiPage) {
    try {
      String msg = "WikiPage " + wikiPage + " does not have a valid name!"
          + wikiPage.getName();
      System.err.println(msg);
      throw new RuntimeException(msg);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public WikiPageProperties getProperties() {
    return properties;
  }

  public String getAttribute(String key) {
    return properties.get(key);
  }

  public void removeAttribute(String key) {
    properties.remove(key);
  }

  public void setAttribute(String key, String value) {
    properties.set(key, value);
  }

  public void setAttribute(String key) {
    properties.set(key);
  }

  public boolean hasAttribute(String attribute) {
    return properties.has(attribute);
  }

  public void setProperties(WikiPageProperties properties) {
    this.properties = properties;
  }

  public String getContent() {
    return StringUtil.stripCarriageReturns(content);
  }

  public void setContent(String content) {
    this.content = content;
  }

  /* this is the public entry to page parse and translate */
  public String getHtml() {
      return getParsedPage().toHtml();
  }

  public String getVariable(String name) {
      Maybe<String> variable = new VariableFinder(getParsingPage()).findVariable(name);
      if (variable.isNothing()) return null;
      return getParsingPage().renderVariableValue(variable.getValue());
  }

  public ParsedPage getParsedPage() {
    if (parsedPage == null) parsedPage = new ParsedPage(new WikiSourcePage(wikiPage), content);
    return parsedPage;
  }

    private Symbol getSyntaxTree() {
        return getParsedPage().getSyntaxTree();
    }

    private ParsingPage getParsingPage() {
        return getParsedPage().getParsingPage();
    }

  public void setWikiPage(WikiPage page) {
    wikiPage = page;
  }

  public WikiPage getWikiPage() {
    return wikiPage;
  }

  public List<String> getClasspaths() {
    Symbol tree = getSyntaxTree();
    return new Paths(new HtmlTranslator(new WikiSourcePage(wikiPage), getParsingPage())).getPaths(tree);
  }

    public List<String> getXrefPages() {
        final ArrayList<String> xrefPages = new ArrayList<String>();
        getSyntaxTree().walkPreOrder(new SymbolTreeWalker() {
            public boolean visit(Symbol node) {
                if (node.isType(See.symbolType)) xrefPages.add(node.childAt(0).getContent());
                return true;
            }

            public boolean visitChildren(Symbol node) { return true; }
        });
        return xrefPages;
    }

  public Set<VersionInfo> getVersions() {
    return versions;
  }

  public void addVersions(Collection<VersionInfo> newVersions) {
    versions.addAll(newVersions);
  }

  public boolean isEmpty() {
    return getContent() == null || getContent().length() == 0;
  }
}
