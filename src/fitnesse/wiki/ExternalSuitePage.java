package fitnesse.wiki;

import util.FileSystem;

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

    public PageData getDataVersion(String versionName) {
        return null;
    }

    protected WikiPage createChildPage(String name) {
        return null;
    }

    protected void loadChildren() {
        for (WikiPage child: new FileSystemPageFactory(fileSystem).findChildren(this)) {
            if (!children.containsKey(child.getName())) {
                children.put(child.getName(), child);
            }
        }
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