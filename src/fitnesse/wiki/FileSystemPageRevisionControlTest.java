package fitnesse.wiki;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.util.FileUtil;

public class FileSystemPageRevisionControlTest {
    private static final String defaultPath = "./teststorage";
    private static final File base = new File(defaultPath);
    private final RevisionController revisionController = createMock(RevisionController.class);
    private PageCrawler crawler;
    private FileSystemPage root;

    @Before
    public void setUp() throws Exception {
        FileUtil.deleteFileSystemDirectory(base);
        base.mkdir();
        root = (FileSystemPage) FileSystemPage.makeRoot(defaultPath, "RooT", revisionController);
        crawler = root.getPageCrawler();
        reset(revisionController);
        expect(revisionController.history((FileSystemPage) anyObject())).andReturn(Collections.EMPTY_LIST).anyTimes();
        expect(revisionController.makeVersion((FileSystemPage) anyObject(), (PageData) anyObject())).andReturn(new VersionInfo("PageName"))
                .anyTimes();
        revisionController.prune((FileSystemPage) anyObject());
        expectLastCall().anyTimes();
    }

    @Test
    public void addingAPageShouldMarkBaseDirAdded() throws Exception {
        String basePath = defaultPath + "/RooT/PageA";
        expect(revisionController.add(file(basePath).getAbsolutePath())).andReturn(null);
        setAddExpectationFor(basePath + FileSystemPage.contentFilename);
        setAddExpectationFor(basePath + FileSystemPage.propertiesFilename);
        replay(revisionController);
        FileSystemPage levelA = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageA"), "Some Content");
        assertEquals(basePath, levelA.getFileSystemPath());
        assertTrue(file(basePath).exists());
    }

    @Test
    public void removingPageShouldMarkBaseDirDeleted() throws Exception {
        File parent = file(defaultPath + "/RooT/LevelOne");
        File child = file(defaultPath + "/RooT/LevelOne/LevelTwo");
        expect(revisionController.add(parent.getAbsolutePath())).andReturn(null);
        expect(revisionController.add(child.getAbsolutePath())).andReturn(null);
        expect(revisionController.delete(child.getAbsolutePath())).andReturn(null);
        replay(revisionController);
        WikiPage levelOne = crawler.addPage(root, PathParser.parse("LevelOne"));
        crawler.addPage(levelOne, PathParser.parse("LevelTwo"));
        levelOne.removeChildPage("LevelTwo");
        assertTrue(parent.exists());
        assertFalse(child.exists());
    }

    @Test
    public void removingParentPageShouldDeleteAllChildern() throws Exception {
        File childOne = file(defaultPath + "/RooT/LevelOne");
        File childTwo = file(defaultPath + "/RooT/LevelOne/LevelTwo");
        expect(revisionController.add(childOne.getAbsolutePath())).andReturn(null);
        expect(revisionController.add(childTwo.getAbsolutePath())).andReturn(null);
        expect(revisionController.delete(childOne.getAbsolutePath())).andReturn(null);
        replay(revisionController);
        WikiPage levelOne = crawler.addPage(root, PathParser.parse("LevelOne"));
        crawler.addPage(levelOne, PathParser.parse("LevelTwo"));
        assertTrue(childOne.exists());
        root.removeChildPage("LevelOne");
        assertFalse(childTwo.exists());
        assertFalse(childOne.exists());
    }

    @Test
    public void shouldAddPageToVersionControlOnFirstCommit() throws Exception {
        String basePath = defaultPath + "/RooT/PageA";
        expect(revisionController.add(file(basePath).getAbsolutePath())).andReturn(null);
        setAddExpectationFor(basePath + FileSystemPage.contentFilename);
        setAddExpectationFor(basePath + FileSystemPage.propertiesFilename);
        replay(revisionController);
        FileSystemPage page = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageA"));
        page.doCommit(page.getData());
    }

    @Test
    public void shouldNotAddPageToVersionControlOnSubsequentCommits() throws Exception {
        String basePath = defaultPath + "/RooT/PageA";
        expect(revisionController.add(file(basePath).getAbsolutePath())).andReturn(null);
        setAddExpectationFor(basePath + FileSystemPage.contentFilename);
        setAddExpectationFor(basePath + FileSystemPage.propertiesFilename);
        expect(revisionController.checkState(file(basePath + FileSystemPage.contentFilename).getAbsolutePath())).andReturn(VERSIONED);
        expect(revisionController.checkState(file(basePath + FileSystemPage.propertiesFilename).getAbsolutePath())).andReturn(VERSIONED);
        replay(revisionController);
        FileSystemPage page = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageA"), "Sample content");
        PageData data = page.getData();
        data.setContent("New Content");
        page.doCommit(data);
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory(base);
        FileUtil.deleteFileSystemDirectory("RooT");
        verify(revisionController);
    }

    private File file(String basePath) {
        return new File(basePath);
    }

    private void setAddExpectationFor(String filePath) throws RevisionControlException {
        File file = file(filePath);
        expect(revisionController.checkState(file.getAbsolutePath())).andReturn(UNKNOWN);
        expect(revisionController.add(file.getAbsolutePath())).andReturn(null);
    }

}
