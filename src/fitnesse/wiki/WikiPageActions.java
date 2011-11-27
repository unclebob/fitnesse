package fitnesse.wiki;


// Work in progress, WikiPage, versions, directory should each have specific actions instances.
public class WikiPageActions {

  private WikiPage page;
  private boolean addChild; // normal wiki page
  private boolean pageHistory; // test results
  private boolean rollback; // versions

  public WikiPageActions(WikiPage page) {
    super();
    this.page = page;
  }
  
  public boolean isTestPage() {
    return hasAction("Test");
  }
  
  public boolean isSuitePage() {
    return hasAction("Suite");
  }
  
  public boolean isDefaultPage() {
    return !(isTestPage() || isSuitePage());
  }
  
  public boolean isWithEdit() {
    return hasAction("Edit");
  }
  
  public boolean isWithProperties() {
    return hasAction("Properties");
  }
  
  public boolean isWithRefactor() {
    return hasAction("Refactor");
  }

  public boolean isWithWhereUsed() {
    return hasAction("WhereUsed");
  }

  public boolean isWithSearch() {
    return hasAction("Search");
  }
  
  public boolean isWithFiles() {
    return hasAction("Files");
  }
  
  public boolean isWithVersions() {
    return hasAction("Versions");
  }

  public boolean isWithRecentChanges() {
    return hasAction("RecentChanges");
  }

  public boolean isWithUserGuide() {
    return page != null;
  }
  
  public boolean isWithTestHistory() {
    return page != null;
  }

  public boolean isWithAddChild() {
    return addChild;
  }
  
  public WikiPageActions withAddChild() {
    addChild = true;
    return this;
  }

  public boolean isWithPageHistory() {
    return pageHistory;
  }
  
  public WikiPageActions withPageHistory() {
    pageHistory = true;
    return this;
  }

  public WikiPageActions withRollback() {
    this.rollback = true;
    return this;
  }

  public boolean isWithRollback() {
    return rollback;
  }
  
  public boolean isWithEditLocally() {
    PageData data = getData();
    return !rollback && data != null && WikiImportProperty.isImported(data);
  }

  public boolean isWithEditRemotely() {
    PageData data = getData();
    return !rollback && data != null && WikiImportProperty.isImported(data);
  }
  
  private boolean hasAction(String action) {
    PageData data = getData();
    return !rollback && data != null && data.hasAttribute(action);
  }

  private PageData getData() {
    if (page != null) {
      try {
        return page.getData();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
  
  public String getLocalPageName() throws Exception {
    if (page != null) {
      WikiPagePath localPagePath = page.getPageCrawler().getFullPath(page);
      return PathParser.render(localPagePath);
    }
    return null;
  }

  public String getLocalOrRemotePageName() throws Exception {
    String localOrRemotePageName = getLocalPageName();
    
    if (page instanceof ProxyPage) {
      localOrRemotePageName = ((ProxyPage) page).getThisPageUrl();
    }
    return localOrRemotePageName;
  }
  
  public boolean isNewWindowIfRemote() {
    if (page != null) {
      return page.isOpenInNewWindow();
    }
    return false;
  }
}
