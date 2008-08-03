package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.util.FileUtil;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;

public abstract class RevisionControlTestCase extends TestCase {
    protected static final String FS_PARENT_PAGE = "ExternalParent";
    protected static final String FS_CHILD_PAGE = "ExternalChild";
    protected static final String FS_GRAND_CHILD_PAGE = "ExternalGrandChild";
    protected static final String ROOT = "testDir";
    protected static final String FS_SIBLING_CHILD_PAGE = "ExternalChildTwo";
    protected FitNesseContext context;
    protected MockRequest request;
    protected SimpleResponse response;
    protected RevisionControlResponder responder;
    protected WikiPage root;
    protected FileSystemPage parentPage;
    protected FileSystemPage childPage;
    protected FileSystemPage grandChildPage;
    protected RevisionController revisionController;

    @Override
    protected void setUp() throws Exception {
        revisionController = createMock(RevisionController.class);
        createExternalRoot();
        request = new MockRequest();
        expect(revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
        revisionController.add((String) anyObject());
        expectLastCall().anyTimes();
        expect(revisionController.checkState(rootContentAndPropertiesFilePath())).andStubReturn(VERSIONED);
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory(ROOT);
    }

    protected void createPage(String pageName) throws Exception {
        final PageCrawler crawler = root.getPageCrawler();
        if (FS_PARENT_PAGE.equals(pageName))
            parentPage = (FileSystemPage) crawler.addPage(root, PathParser.parse(FS_PARENT_PAGE));
        if (FS_CHILD_PAGE.equals(pageName)) {
            createPage(FS_PARENT_PAGE);
            childPage = (FileSystemPage) crawler.addPage(parentPage, PathParser.parse(FS_CHILD_PAGE));
        }
        if (FS_GRAND_CHILD_PAGE.equals(pageName)) {
            createPage(FS_CHILD_PAGE);
            grandChildPage = (FileSystemPage) crawler.addPage(childPage, PathParser.parse(FS_GRAND_CHILD_PAGE));
        }
    }

    protected FileSystemPage createPage(String pageName, FileSystemPage parent) throws Exception {
        final PageCrawler crawler = root.getPageCrawler();
        return (FileSystemPage) crawler.addPage(parent, PathParser.parse(pageName));
    }

    protected void invokeResponderAndCheckSuccessStatus() throws Exception {
        invokeResponderAndGetResponse();
        assertEquals(200, response.getStatus());
    }

    protected void createExternalRoot() throws Exception {
        FileUtil.createDir(ROOT);
        root = FileSystemPage.makeRoot(ROOT, "ExternalRoot", revisionController);
        context = new FitNesseContext(root);
    }

    protected void invokeResponderAndCheckResponseContains(String responseMessage) throws Exception {
        invokeResponderAndGetResponse();
        assertSubString(responseMessage, response.getContent());
    }

    private void invokeResponderAndGetResponse() throws Exception {
        response = (SimpleResponse) responder.makeResponse(context, request);
    }

    protected String[] contentAndPropertiesFilePathFor(String page) throws Exception {
        final String pageName = folderPath(page);
        return new String[] { contentFilePathFor(pageName), propertiesFilePathFor(pageName) };
    }

    private String folderPath(String page) {
        String pageName = ROOT + "/ExternalRoot";
        if (page != null)
            if (FS_PARENT_PAGE.equals(page))
                pageName += "/" + FS_PARENT_PAGE;
            else if (FS_CHILD_PAGE.equals(page))
                pageName += "/" + FS_PARENT_PAGE + "/" + FS_CHILD_PAGE;
            else if (FS_SIBLING_CHILD_PAGE.equals(page))
                pageName += "/" + FS_PARENT_PAGE + "/" + FS_SIBLING_CHILD_PAGE;
            else if (FS_GRAND_CHILD_PAGE.equals(page))
                pageName += "/" + FS_PARENT_PAGE + "/" + FS_CHILD_PAGE + "/" + FS_GRAND_CHILD_PAGE;
        return pageName;
    }

    protected String rootFolderFilePath() {
        return new File(folderPath(null)).getAbsolutePath();
    }

    protected String folderFilePath(String pageName) {
        return new File(folderPath(pageName)).getAbsolutePath();
    }

    protected String[] rootContentAndPropertiesFilePath() throws Exception {
        return contentAndPropertiesFilePathFor(null);
    }

    private String propertiesFilePathFor(String page) throws Exception {
        return fileSystemPath(page, FileSystemPage.propertiesFilename);
    }

    private String contentFilePathFor(String page) throws Exception {
        return fileSystemPath(page, FileSystemPage.contentFilename);
    }

    private String fileSystemPath(String page, String fileName) throws Exception {
        return new File((page + fileName).replace('/', File.separatorChar)).getAbsolutePath();
    }
}
