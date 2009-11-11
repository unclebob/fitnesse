package fitnesse.wiki;

import util.FileSystem;

public class ExternalSuitePage extends CachingPage {
    private static final long serialVersionUID = 1L;
    private String path;
    private FileSystem fileSystem;

    public ExternalSuitePage(String path, String name, WikiPage parent, FileSystem fileSystem) throws Exception {
        super(name, parent);
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public String getFileSystemPath() { return path; }

    public boolean hasChildPage(String pageName) throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected WikiPage createChildPage(String name) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void loadChildren() throws Exception {
        for (WikiPage child: new PageRepository(fileSystem).findChildren(this)) {
            if (!children.containsKey(child.getName())) {
                children.put(child.getName(), child);
            }
        }
    }

    protected PageData makePageData() throws Exception {
        PageData pagedata = new PageData(this);
        pagedata.setContent("!contents");
        //loadAttributes(pagedata);
        //pagedata.addVersions(this.versionsController.history(this));
        return pagedata;
    }

    protected VersionInfo makeVersion() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void doCommit(PageData data) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}