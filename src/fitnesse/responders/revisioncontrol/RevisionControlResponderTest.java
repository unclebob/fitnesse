package fitnesse.responders.revisioncontrol;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashSet;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.revisioncontrol.NullState;
import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionControlOperation;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;

public class RevisionControlResponderTest extends RevisionControlTestCase {
    private final String revisionControlOperation = "Test Revision Control Operation";
    private static final String pageName = "SomePage";

    @Override
    protected void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT");
        context = new FitNesseContext(root);
        request = new MockRequest();
        responder = new TestRevisionControlResponder();
    }

    public void testShouldReturnPageNotFoundMessageWhenPageDoesNotExist() throws Exception {
        final String pageName = "InvalidPageName";
        request.setResource(pageName);
        invokeResponderAndCheckResponseContains("The requested resource: <i>" + pageName + "</i> was not found.");
    }

    public void testShouldReturnInvalidWikiPageMessageIfWikiPageDoesNotExistOnFileSystem() throws Exception {
        final String inMemoryPageName = "InMemoryPage";
        root.addChildPage(inMemoryPageName);
        request.setResource(inMemoryPageName);
        invokeResponderAndCheckResponseContains("The page " + inMemoryPageName + " doesn't support '" + revisionControlOperation + "' operation.");
    }

    public void testShouldResolveSymbolicLinkToActualPageAndApplyRevisionControlOperations() throws Exception {
        final String symbolicLinkName = "SymbolicLink";
        final String pageOneName = "PageOne";
        final String symbolicLinkPageName = pageOneName + "." + symbolicLinkName;
        createSymbolicLink(symbolicLinkName, pageOneName);

        request.setResource(symbolicLinkPageName);
        invokeResponderAndCheckResponseContains("The page " + symbolicLinkPageName + " doesn't support '" + revisionControlOperation + "' operation.");
    }

    public void testShouldReportPerformRevisionControlOperation() throws Exception {
        final String expectedResponse = "Attempted to '" + revisionControlOperation + "' the page '" + pageName
                + "'. The result was:<br/><br/><pre>Operation: '" + revisionControlOperation + "' was successful.";
        revisionController = createNiceMock(RevisionController.class);
        expect(revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
        expect(revisionController.checkState((String) anyObject())).andStubReturn(NullState.UNKNOWN);
        replay(revisionController);
        createExternalRoot();
        root.getPageCrawler().addPage(root, PathParser.parse(pageName), "Test Page Content");
        request.setResource(pageName);

        invokeResponderAndCheckResponseContains(expectedResponse);
        verify(revisionController);
    }

    private void createSymbolicLink(String symbolicLinkName, String pageOneName) throws Exception {
        final String pageTwoName = "PageTwo";
        final WikiPage pageOne = root.addChildPage(pageOneName);
        root.addChildPage(pageTwoName);

        final PageData data = pageOne.getData();
        final WikiPageProperties properties = data.getProperties();
        final WikiPageProperty symLinks = getSymLinkProperty(properties);
        symLinks.set(symbolicLinkName, pageTwoName);
        pageOne.commit(data);
    }

    private WikiPageProperty getSymLinkProperty(WikiPageProperties properties) {
        WikiPageProperty symLinks = properties.getProperty(SymbolicPage.PROPERTY_NAME);
        symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
        return symLinks;
    }

    private class TestRevisionControlResponder extends RevisionControlResponder {
        public TestRevisionControlResponder() {
            super(new RevisionControlOperation(revisionControlOperation, "", "") {

                @Override
                public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
                }
            });
        }

        @Override
        protected String responseMessage(String resource) throws Exception {
            return "End of operation.";
        }

        @Override
        protected void performOperation(FileSystemPage page) throws Exception {
        }

    }
}
