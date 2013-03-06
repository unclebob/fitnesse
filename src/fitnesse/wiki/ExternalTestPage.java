package fitnesse.wiki;

import java.io.IOException;

import util.FileSystem;

public class ExternalTestPage extends CachingPage {
    private static final long serialVersionUID = 1L;
    private FileSystem fileSystem;
    private String path;

    public ExternalTestPage(String path, String name, WikiPage parent, FileSystem fileSystem) {
        super(name, parent);
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean hasChildPage(String pageName) {
        return false;
    }

    @Override
    protected WikiPage createChildPage(String name) {
        return null;
    }

    @Override
    protected void loadChildren() {

    }

    @Override
    protected PageData makePageData() {
        PageData pageData = new PageData(this);
        String content;
        try {
          content = fileSystem.getContent(path);
        } catch (IOException e) {
          throw new RuntimeException("Unable to fetch page content", e);
        }
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

    public PageData getDataVersion(String versionName) {
        return null;  
    }
}
