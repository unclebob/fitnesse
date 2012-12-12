package fitnesse.wiki;

import java.util.Collections;
import java.util.Map;

import fitnesse.responders.editing.TemplateUtil;

// Work in progress, WikiPage, versions, directory should each have specific actions instances.
public class WikiPageActions {

  private WikiPage page;

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

  public Map<String, String> getNewPageTemplates() {
    if (isWithEdit()) {
      return TemplateUtil.getTemplatePageMap(page);
    }
    return Collections.emptyMap();
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

  public boolean isImported() {
    PageData data = getData();
    return data != null && WikiImportProperty.isImported(data);
  }

  private boolean hasAction(String action) {
    PageData data = getData();
    return data != null && data.hasAttribute(action);
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

  public String getLocalPageName() {
    if (page != null) {
      WikiPagePath localPagePath = page.getPageCrawler().getFullPath();
      return PathParser.render(localPagePath);
    }
    return null;
  }
}
