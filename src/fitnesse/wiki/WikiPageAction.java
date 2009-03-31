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

    return eq(linkName, that.linkName) &&
      eq(pageName, that.pageName) &&
      eq(query, that.query) &&
      eq(shortcutKey, that.shortcutKey);
  }

  private boolean eq(String thisString, String thatString) {
    return
      (thisString != null && thatString != null && thisString.equals(thatString)) || 
      (thisString == null && thatString == null);
  }

  @Override
  public int hashCode() {
    int hashSum = hashString(pageName);
    hashSum = addToHash(hashSum, hashString(linkName));
    hashSum = addToHash(hashSum, hashString(query));
    hashSum = addToHash(hashSum, hashString(shortcutKey));
    hashSum = addToHash(hashSum, newWindow ? 1 : 0);
    return hashSum;
  }

  private int addToHash(int hashSum, int hashCode) {
    return 31 * hashSum + hashCode;
  }

  private int hashString(String string) {
    return string != null ? string.hashCode() : 0;
  }
}
