// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.components.PageReferencer;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class IncludeWidget extends ParentWidget implements PageReferencer {
  public static final String REGEXP =
    "^!include(?: +-setup| +-teardown| +-seamless| +-c)? " + WikiWordWidget.REGEXP + LINE_BREAK_PATTERN + "?";
  static final Pattern pattern = Pattern.compile("^!include *(-setup|-teardown|-seamless|-c)? (.*)");

  public static final String COLLAPSE_SETUP = "COLLAPSE_SETUP";
  public static final String COLLAPSE_TEARDOWN = "COLLAPSE_TEARDOWN";

  protected String pageName;
  protected WikiPage includingPage;
  protected WikiPage includedPage; //Retain from getIncludedPageContent()
  protected WikiPage parentPage;

  private static Map<String, String> optionPrefixMap = buildOptionPrefixMap();
  private static Map<String, String> optionCssMap = buildOptionsCssMap();

  public IncludeWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      pageName = getPageName(matcher);
      includingPage = parent.getWikiPage();
      parentPage = includingPage.getParent();
      buildWidget(getOption(matcher));
    }
  }

  public String getPageName() {
    return pageName;
  }

  protected String getIncludedPageContent() throws Exception {
    PageCrawler crawler = parentPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    WikiPagePath pagePath = PathParser.parse(pageName);
    includedPage = crawler.getSiblingPage(includingPage, pagePath); //Retain this

    if (includedPage != null) {
      includedPage.setParentForVariables(getWikiPage().getParentForVariables());
      return includedPage.getData().getContent();
    } else if (includingPage instanceof ProxyPage) {
      ProxyPage proxy = (ProxyPage) includingPage;
      String host = proxy.getHost();
      int port = proxy.getHostPort();
      try {
        ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath);
        return remoteIncludedPage.getData().getContent();
      }
      catch (Exception e) {
        return "!meta '''Remote page " + host + ":" + port + "/" + pageName + " does not exist.'''";
      }
    } else {
      return "!meta '''Page include failed because the page " + pageName + " does not exist.'''";
    }
  }

  protected WikiPage getIncludedPage() throws Exception {
    PageCrawler crawler = parentPage.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    return crawler.getPage(parentPage, PathParser.parse(pageName));
  }

  protected WikiPage getParentPage() throws Exception {
    return parent.getWikiPage().getParent();
  }

  private String getOption(Matcher match) {
    return match.group(1);
  }

  private String getPageName(Matcher match) {
    return match.group(2);
  }

  //TODO MDM I know this is bad...  But it seems better then creating two new widgets.
  private void buildWidget(String option) throws Exception {
    String widgetText = processLiterals(getIncludedPageContent(option));

    //Create imposter root with alias = this if included page found.
    ParentWidget incRoot = (includedPage == null) ? this : new WidgetRoot(includedPage, this);

    if (isSeamLess(option) || getRoot().isGatheringInfo()) {  //Use the imposter if found.
      incRoot.addChildWidgets(widgetText + "\n");
    } else {  //Use new constructor with dual scope.
      new CollapsableWidget(incRoot, this, getPrefix(option) + pageName, widgetText, getCssClass(option), isCollapsed(
          option
      )
      );
    }
  }

  //TODO MG There was no better way to nest in this behaviour. As future evolution point we can
  //        expand the if clause to also accept regular includes and replace PAGE_NAME all the time.
  private String getIncludedPageContent(String option) throws Exception {

    if (isSetup(option) || isTeardown(option)) {
      return replaceSpecialVariables(getIncludedPageContent());
    }

    return getIncludedPageContent();
  }

  //TODO MG What about PAGE_PATH?
  private String replaceSpecialVariables(String includedPageContent) throws Exception {
    return includedPageContent.replaceAll("\\$\\{PAGE_NAME\\}", includingPage.getName());
  }

  private boolean isSeamLess(String option) {
    return "-seamless".equals(option);
  }

  private String getCssClass(String option) {
    return optionCssMap.get(option);
  }

  private String getPrefix(String option) {
    return optionPrefixMap.get(option);
  }

  private boolean isCollapsed(String option)
  throws Exception {
    if (isSetup(option) && isSetupCollapsed())
      return true;
    else if (isTeardown(option) && isTeardownCollapsed())
      return true;
    else if ("-c".equals(option))
      return true;
    return false;
  }

  private static Map<String, String> buildOptionsCssMap() {
    Map<String, String> optionCssMap = new HashMap<String, String>();
    optionCssMap.put("-setup", "setup");
    optionCssMap.put("-teardown", "teardown");
    optionCssMap.put("-c", "included");
    optionCssMap.put(null, "included");
    return optionCssMap;
  }

  private static Map<String, String> buildOptionPrefixMap() {
    Map<String, String> optionPrefixMap = new HashMap<String, String>();
    optionPrefixMap.put("-setup", "Set Up: ");
    optionPrefixMap.put("-teardown", "Tear Down: ");
    optionPrefixMap.put("-c", "Included page: ");
    optionPrefixMap.put(null, "Included page: ");
    return optionPrefixMap;
  }

  private boolean isTeardownCollapsed()
  throws Exception {
    final String teardownCollapseVariable = parent.getVariable(COLLAPSE_TEARDOWN);
    return teardownCollapseVariable == null || "true".equals(teardownCollapseVariable);
  }

  private boolean isTeardown(String option) {
    return "-teardown".equals(option);
  }

  private boolean isSetupCollapsed()
  throws Exception {
    final String setupCollapseVariable = parent.getVariable(COLLAPSE_SETUP);
    return setupCollapseVariable == null || "true".equals(setupCollapseVariable);
  }

  private boolean isSetup(String option) {
    return "-setup".equals(option);
  }

  public String render() throws Exception {
    return childHtml();
  }

  public WikiPage getReferencedPage() throws Exception {
    return getParentPage().getPageCrawler().getPage(getParentPage(), PathParser.parse(pageName));
  }

  public String asWikiText() throws Exception {
    return "";
  }

  @Override
  public String processLiterals(String value) throws Exception {
    // TODO Auto-generated method stub
    return super.processLiterals(value);
  }
}
