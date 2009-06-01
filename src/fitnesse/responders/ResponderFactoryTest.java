// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;


import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.responders.editing.*;
import fitnesse.responders.files.*;
import fitnesse.responders.refactoring.DeletePageResponder;
import fitnesse.responders.refactoring.MovePageResponder;
import fitnesse.responders.refactoring.RefactorPageResponder;
import fitnesse.responders.refactoring.RenamePageResponder;
import fitnesse.responders.run.*;
import fitnesse.responders.search.ExecuteSearchPropertiesResponder;
import fitnesse.responders.search.SearchFormResponder;
import fitnesse.responders.search.SearchResponder;
import fitnesse.responders.search.WhereUsedResponder;
import fitnesse.responders.testHistory.PageHistoryResponder;
import fitnesse.responders.testHistory.TestHistoryResponder;
import fitnesse.responders.versions.RollbackResponder;
import fitnesse.responders.versions.VersionResponder;
import fitnesse.responders.versions.VersionSelectionResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;

public class ResponderFactoryTest {
  private ResponderFactory factory;
  private MockRequest request;
  private WikiPageDummy nonExistantPage;
  private WikiPage root;
  private PageCrawler crawler;

  @Before
  public void setUp() throws Exception {
    factory = new ResponderFactory("testDir");
    request = new MockRequest();
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    nonExistantPage = new WikiPageDummy();
  }

  @Test
  public void testGetResponderKey() throws Exception {
    checkResponderKey("railroad", "railroad");
    checkResponderKey("responder=railroad", "railroad");
    checkResponderKey("", "");
  }

  private void checkResponderKey(String queryString, String key) {
    MockRequest request = new MockRequest();
    request.setQueryString(queryString);
    assertEquals(key, factory.getResponderKey(request));
  }

  @Test
  public void testWikiPageResponder() throws Exception {
    request.setResource("SomePage");
    assertResponderType(WikiPageResponder.class, root);
    request.setResource("");
    assertResponderType(WikiPageResponder.class, root);
    request.setResource("root");
    assertResponderType(WikiPageResponder.class, root);
  }

  @Test
  public void testRefactorPageResponder() throws Exception {
    assertResponderTypeMatchesInput("refactor", RefactorPageResponder.class);
  }

  @Test
  public void testDeletePageResponder() throws Exception {
    assertResponderTypeMatchesInput("deletePage", DeletePageResponder.class);
  }

  @Test
  public void testRenamePageResponder() throws Exception {
    assertResponderTypeMatchesInput("renamePage", RenamePageResponder.class);
  }

  @Test
  public void testEditResponder() throws Exception {
    request.addInput("responder", "edit");
    request.setResource("SomePage");
    assertResponderType(EditResponder.class, root);
    assertResponderType(EditResponder.class, nonExistantPage);
  }

  @Test
  public void testDontCreatePageResponder() throws Exception {
    request.addInput("responder", "dontCreatePage");
    request.setResource("SomePage");
    assertResponderType(NotFoundResponder.class, root);
    assertResponderType(NotFoundResponder.class, nonExistantPage);
  }

  @Test
  public void testPageDataResponder() throws Exception {
    request.addInput("responder", "pageData");
    request.setResource("SomePage");
    assertResponderType(PageDataWikiPageResponder.class, root);
  }

  @Test
  public void testSaveResponder() throws Exception {
    assertResponderTypeMatchesInput("saveData", SaveResponder.class);
  }

  @Test
  public void testTestResponder() throws Exception {
    assertResponderTypeMatchesInput("test", TestResponder.class);
  }

  @Test
  public void testSuiteResponder() throws Exception {
    assertResponderTypeMatchesInput("suite", SuiteResponder.class);
  }

  @Test
  public void testFileResponder() throws Exception {
    try {
      new File("testDir").mkdir();
      new File("testDir/files").mkdir();
      FileUtil.createFile("testDir/files/someFile", "this is a test");
      request.setResource("files/someFile");
      assertResponderType(FileResponder.class, nonExistantPage);
    } finally {
      FileUtil.deleteFileSystemDirectory("testDir");
    }
  }

  @Test
  public void testSearchFormResponder() throws Exception {
    assertResponderTypeMatchesInput("searchForm", SearchFormResponder.class);
  }

  @Test
  public void testSearchResponder() throws Exception {
    assertResponderTypeMatchesInput("search", SearchResponder.class);
  }

  @Test
  public void testSerializedPageResponder() throws Exception {
    assertResponderTypeMatchesInput("proxy", SerializedPageResponder.class);
  }

  @Test
  public void testVersionSelectionResponder() throws Exception {
    assertResponderTypeMatchesInput("versions", VersionSelectionResponder.class);
  }

  @Test
  public void testVersionResponder() throws Exception {
    assertResponderTypeMatchesInput("viewVersion", VersionResponder.class);
  }

  @Test
  public void testRollbackResponder() throws Exception {
    assertResponderTypeMatchesInput("rollback", RollbackResponder.class);
  }

  @Test
  public void testNameReponder() throws Exception {
    assertResponderTypeMatchesInput("names", NameWikiPageResponder.class);
  }

  @Test
  public void testUploadResponder() throws Exception {
    assertResponderTypeMatchesInput("upload", UploadResponder.class);
  }

  @Test
  public void testCreateDirectoryResponder() throws Exception {
    assertResponderTypeMatchesInput("createDir", CreateDirectoryResponder.class);
  }

  @Test
  public void testDeleteFileResponder() throws Exception {
    assertResponderTypeMatchesInput("deleteFile", DeleteFileResponder.class);
  }

  @Test
  public void testRenameFileResponder() throws Exception {
    assertResponderTypeMatchesInput("renameFile", RenameFileResponder.class);
  }

  @Test
  public void testDeleteConfirmationFileResponder() throws Exception {
    assertResponderTypeMatchesInput("deleteConfirmation", DeleteConfirmationResponder.class);
  }

  @Test
  public void testRenameFileConfirmationResponder() throws Exception {
    assertResponderTypeMatchesInput("renameConfirmation", RenameFileConfirmationResponder.class);
  }

  @Test
  public void testCreatePropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("properties", PropertiesResponder.class);
  }

  @Test
  public void testCreateSavePropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("saveProperties", SavePropertiesResponder.class);
  }

  @Test
  public void testCreateExecuteSearchPropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("executeSearchProperties", ExecuteSearchPropertiesResponder.class);
  }

  @Test
  public void testCreateWhereUsedResponder() throws Exception {
    assertResponderTypeMatchesInput("whereUsed", WhereUsedResponder.class);
  }

  @Test
  public void testCreateMovePageResponer() throws Exception {
    assertResponderTypeMatchesInput("movePage", MovePageResponder.class);
  }

  @Test
  public void testSocketCatcher() throws Exception {
    assertResponderTypeMatchesInput("socketCatcher", SocketCatchingResponder.class);
  }

  @Test
  public void testFitClient() throws Exception {
    assertResponderTypeMatchesInput("fitClient", FitClientResponder.class);
  }

  @Test
  public void testRawContent() throws Exception {
    assertResponderTypeMatchesInput("raw", RawContentResponder.class);
  }

  @Test
  public void testRssResponder() throws Exception {
    assertResponderTypeMatchesInput("rss", RssResponder.class);
  }

  @Test
  public void testPageImporterResponder() throws Exception {
    assertResponderTypeMatchesInput("import", WikiImportingResponder.class);
  }

  @Test
  public void testShutdownResponder() throws Exception {
    assertResponderTypeMatchesInput("shutdown", ShutdownResponder.class);
  }

  @Test
  public void testTestResultFormattingResponder() throws Exception {
    assertResponderTypeMatchesInput("format", TestResultFormattingResponder.class);
  }

  @Test
  public void testSymbolicLinkResponder() throws Exception {
    assertResponderTypeMatchesInput("symlink", SymbolicLinkResponder.class);
  }

  @Test
  public void testPacketResponder() throws Exception {
    assertResponderTypeMatchesInput("packet", PacketResponder.class);
  }

  @Test
  public void testStopTestResponder() throws Exception {
    assertResponderTypeMatchesInput("stoptest", StopTestResponder.class);
  }

  @Test
  public void testWillDisplayVirtualPages() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"));
    crawler.addPage(page1, PathParser.parse("ChildOne"), "child content");
    WikiPage page2 = crawler.addPage(root, PathParser.parse("PageTwo"));
    FitNesseUtil.bindVirtualLinkToPage(page2, page1);
    request.setResource("PageTwo.ChildOne");
    assertResponderType(WikiPageResponder.class, root);
  }

  @Test
  public void testNotFoundResponder() throws Exception {
    request.setResource("somepage");
    assertResponderType(NotFoundResponder.class, root);
  }

  @Test
  public void testAddingResponders() throws Exception {
    factory.addResponder("custom", WikiPageResponder.class);
    assertResponderTypeMatchesInput("custom", WikiPageResponder.class);
  }

  private void assertResponderType(Class<?> expectedClass, WikiPage page) throws Exception {
    Responder responder = factory.makeResponder(request, page);
    assertEquals(expectedClass, responder.getClass());
  }

  private void assertResponderTypeMatchesInput(String responderType, Class<?> responderClass) throws Exception {
    request.addInput("responder", responderType);
    assertResponderType(responderClass, root);
  }

  @Test
  public void testTestHistoryResponder() throws Exception {
    assertResponderTypeMatchesInput("testHistory", TestHistoryResponder.class);
  }

  @Test
  public void testPageHistoryResponder() throws Exception {
    assertResponderTypeMatchesInput("pageHistory", PageHistoryResponder.class);
  }

  @Test
  public void testAddChildPageResponder() throws Exception {
    assertResponderTypeMatchesInput("addChild", AddChildPageResponder.class);
  }
}
