package fitnesse.wiki;

public class WikiPageAction {
  private String pageName;
  private String linkName;
  private String query;
  private String shortcutKey;
  private boolean newWindow;

  public WikiPageAction(String pageName, String linkName) {
    this.pageName = pageName;
    this.linkName = linkName;
    this.query = linkName.toLowerCase();
    this.shortcutKey = query.substring(0, 1);
    this.newWindow = false;
  }

  public String getPageName() {
    return pageName;
  }

  public String getLinkName() {
    return linkName;
  }

  public String getQuery() {
    return query;
  }

  public String getShortcutKey() {
    return shortcutKey;
  }

  public boolean isNewWindow() {
    return newWindow;
  }

  public void setPageName(String pageName) {
    this.pageName = pageName;
  }

  public void setLinkName(String linkName) {
    this.linkName = linkName;
  }

  public void setQuery(String inputName) {
    this.query = inputName;
  }

  public void setShortcutKey(String shortcutKey) {
    this.shortcutKey = shortcutKey;
  }

  public void setNewWindow(boolean newWindow) {
    this.newWindow = newWindow;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WikiPageAction that = (WikiPageAction) o;

    if (newWindow != that.newWindow) return false;
    if (linkName != null ? !linkName.equals(that.linkName) : that.linkName != null) return false;
    if (pageName != null ? !pageName.equals(that.pageName) : that.pageName != null) return false;
    if (query != null ? !query.equals(that.query) : that.query != null) return false;
    if (shortcutKey != null ? !shortcutKey.equals(that.shortcutKey) : that.shortcutKey != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = pageName != null ? pageName.hashCode() : 0;
    result = 31 * result + (linkName != null ? linkName.hashCode() : 0);
    result = 31 * result + (query != null ? query.hashCode() : 0);
    result = 31 * result + (shortcutKey != null ? shortcutKey.hashCode() : 0);
    result = 31 * result + (newWindow ? 1 : 0);
    return result;
  }
}
