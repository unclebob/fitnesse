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
        return null;
    }

    @Override
    protected void doCommit(PageData data) throws Exception {
    }

    @Override
    public boolean hasChildPage(String pageName) throws Exception {
        return false;
    }

    @Override
    protected WikiPage createChildPage(String name) throws Exception {
        return null;
    }

    @Override
    protected void loadChildren() throws Exception {

    }

    @Override
    protected PageData makePageData() throws Exception {
        PageData pageData = new PageData(this);
        String content = fileSystem.getContent(path);
        pageData.setContent("!-" + content + "-!");
        pageData.removeAttribute(PageData.PropertyEDIT);
        pageData.removeAttribute(PageData.PropertyPROPERTIES);
        pageData.removeAttribute(PageData.PropertyVERSIONS);
        pageData.removeAttribute(PageData.PropertyREFACTOR);
        if (content.contains("<table")) {
            pageData.setAttribute(PageType.TEST.toString(), Boolean.toString(true));
        }
        return pageData;
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return null;  
    }
}
