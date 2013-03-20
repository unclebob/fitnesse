package fitnesse.wiki.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.wiki.CachingPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.WikiWordPath;

public class ExternalSuitePage extends CachingPage {
    private static final long serialVersionUID = 1L;
    private String path;
    private FileSystem fileSystem;

    public ExternalSuitePage(String path, String name, WikiPage parent, FileSystem fileSystem) {
        super(name, parent);
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public String getFileSystemPath() { return path; }

    public boolean hasChildPage(String pageName) {
        return false;
    }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  public PageData getDataVersion(String versionName) {
        return null;
    }

  protected WikiPage createChildPage(String name) {
        return null;
    }

    protected void loadChildren() {
        for (WikiPage child: findChildren()) {
            if (!children.containsKey(child.getName())) {
                children.put(child.getName(), child);
            }
        }
    }


  // TODO: move WikiPage.getChildren logic over here
  protected List<WikiPage> findChildren() {
    List<WikiPage> children = new ArrayList<WikiPage>();
    for (String child : fileSystem.list(getFileSystemPath())) {
      String childPath = getFileSystemPath() + "/" + child;
      if (child.endsWith(".html")) {
        children.add(new ExternalTestPage(childPath,
                WikiWordPath.makeWikiWord(child.replace(".html", "")), parent, fileSystem));
      } else if (hasHtmlChild(childPath)) {
        children.add(new ExternalSuitePage(childPath,
                WikiWordPath.makeWikiWord(child), parent, fileSystem));
      }
    }
    return children;
  }

  private Boolean hasHtmlChild(String path) {
    if (path.endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(path + "/" + child)) return true;
    }
    return false;
  }


  protected PageData makePageData() {
        PageData pageData = new PageData(this);
        pageData.setContent("!contents");
        pageData.removeAttribute(PageData.PropertyEDIT);
        pageData.removeAttribute(PageData.PropertyPROPERTIES);
        pageData.removeAttribute(PageData.PropertyVERSIONS);
        pageData.removeAttribute(PageData.PropertyREFACTOR);
        pageData.setAttribute(PageType.SUITE.toString(), Boolean.toString(true));
        return pageData;
    }
}