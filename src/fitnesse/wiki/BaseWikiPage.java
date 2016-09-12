package fitnesse.wiki;

/**
 * Base implementation for most types of wiki pages.
 */
public abstract class BaseWikiPage implements WikiPage {
  protected final String name;
  protected final WikiPage parent;

  public BaseWikiPage(String name, WikiPage parent) {
    this.name = name;
    this.parent = parent;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    return getChildPage(pageName) != null;
  }

  @Override
  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl(this);
  }

  @Override
  public WikiPage getParent() {
    return parent == null ? this : parent;
  }

  @Override
  public boolean isRoot() {
    return parent == null || parent == this;
  }

  @Override
  public void remove() {
    WikiPage parent = getParent();
    if (parent != this) {
      parent.removeChildPage(getName());
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ": " + name;
  }

  @Override
  public int compareTo(WikiPage other) {
    try {
      WikiPagePath path1 = getPageCrawler().getFullPath();
      WikiPagePath path2 = other.getPageCrawler().getFullPath();
      return path1.compareTo(path2);
    }
    catch (Exception e) {
      return 0;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (!(other instanceof WikiPage))
      return false;
    try {
      WikiPagePath path1 = getPageCrawler().getFullPath();
      WikiPagePath path2 = ((WikiPage) other).getPageCrawler().getFullPath();
      return path1.equals(path2);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    try {
      return getPageCrawler().getFullPath().hashCode();
    } catch (Exception e) {
      return 0;
    }
  }
}
