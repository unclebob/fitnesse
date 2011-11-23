package fitnesse.wiki;

public class WikiPageActions {

  private PageData data;

  public WikiPageActions(PageData data) {
    super();
    this.data = data;
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
    return true;
  }
  
  public boolean isWithTestHistory() {
    return true;
  }

  private boolean hasAction(String action) {
    return data.hasAttribute(action);
  }
}
