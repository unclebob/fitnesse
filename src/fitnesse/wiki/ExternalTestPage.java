package fitnesse.wiki;

import util.FileSystem;

public class ExternalTestPage extends CachingPage {
    private static final long serialVersionUID = 1L;
    private FileSystem fileSystem;
    private String path;

    public ExternalTestPage(String path, String name, WikiPage parent, FileSystem fileSystem) throws Exception {
        super(name, parent);
        this.path = path;
        this.fileSystem = fileSystem;
    }
    
    @Override
    protected VersionInfo makeVersion() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doCommit(PageData data) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasChildPage(String pageName) throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected WikiPage createChildPage(String name) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void loadChildren() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected PageData makePageData() throws Exception {
        PageData pagedata = new PageData(this);
        pagedata.setContent(fileSystem.getContent(path));
        //loadAttributes(pagedata);
        //pagedata.addVersions(this.versionsController.history(this));
        return pagedata;
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
