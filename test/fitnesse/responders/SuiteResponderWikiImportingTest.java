// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockChunkedDataProvider;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.StandardOutAndErrorRecorder;

import static org.junit.Assert.assertEquals;

public class SuiteResponderWikiImportingTest {
  public MockWikiImporter mockWikiImporter;
  private MockTestResponder testResponder;
  private WikiPage pageOne;
  private WikiPage childOne;
  private WikiPage childTwo;
  private StandardOutAndErrorRecorder standardOutAndErrorRecorder;

  @Before
  public void setUp() throws Exception {
    standardOutAndErrorRecorder = new StandardOutAndErrorRecorder();

    WikiPage root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "");
    childOne = WikiPageUtil.addPage(pageOne, PathParser.parse("ChildOne"), "");
    childTwo = WikiPageUtil.addPage(pageOne, PathParser.parse("ChildTwo"), "");

    mockWikiImporter = new MockWikiImporter();

    testResponder = new MockTestResponder(mockWikiImporter);

    testResponder.page = pageOne;
  }

  @After
  public void tearDown() {
    standardOutAndErrorRecorder.stopRecording(false);
  }

  @Test
  public void testRunWithTestingOnePage() throws Exception {
    addImportPropertyToPage(pageOne, false, true);

    testResponder.importWikiPages();

    assertEquals(MockWikiImporter.mockContent, pageOne.getData().getContent());
//    assertEquals(MockWikiImporter.mockContent, data.getContent());
    assertEquals("Updating imported content... Done.", sentMessages);
  }

  @Test
  public void testNoImportAnnouncementIfXmlFormat() throws Exception {
    testResponder.setXmlFormat();
    addImportPropertyToPage(pageOne, false, true);

    testResponder.importWikiPages();
    assertEquals("", sentMessages);
  }

  @Test
  public void testRunWithTestingOnePageWithoutAutoUpdate() throws Exception {
    addImportPropertyToPage(pageOne, false, false);

    testResponder.importWikiPages();

    assertEquals("", pageOne.getData().getContent());
    assertEquals("", sentMessages);
  }

  @Test
  public void testErrorOccured() throws Exception {
    mockWikiImporter.fail = true;
    addImportPropertyToPage(pageOne, false, true);

    testResponder.importWikiPages();

    assertEquals("Updating imported content... fitnesse.responders.WikiImportingTraverser$ImportError: Import of remote page content failed. Done.", sentMessages);
    assertEquals("", pageOne.getData().getContent());
  }

  @Test
  public void testRunWithSuiteFromRoot() throws Exception {
    addImportPropertyToPage(pageOne, true, true);

    testResponder.importWikiPages();

    assertEquals("", pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childTwo.getData().getContent());
    assertEquals("Updating imported content... Done.", sentMessages);
  }

  @Test
  public void testRunWithSuiteFromNonRoot() throws Exception {
    addImportPropertyToPage(pageOne, false, true);

    testResponder.importWikiPages();

    assertEquals(MockWikiImporter.mockContent, pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childTwo.getData().getContent());
    assertEquals("Updating imported content... Done.", sentMessages);
  }

  private void addImportPropertyToPage(WikiPage page, boolean isRoot, boolean autoUpdate) throws Exception {
    PageData data = page.getData();
    String sourceUrl = FitNesseUtil.URL + "PageOne";
    WikiImportProperty importProps = new WikiImportProperty(sourceUrl);
    importProps.setRoot(isRoot);
    importProps.setAutoUpdate(autoUpdate);
    importProps.addTo(data.getProperties());
    pageOne.commit(data);
  }

  private String sentMessages = "";

  private void AddMessage(String output) {
    sentMessages += output.replaceAll("<.*?>", "");
  }

  private class MockTestResponder extends SuiteResponder {

    public MockTestResponder(MockWikiImporter mockWikiImporter) {
      super(mockWikiImporter);
      response = new ChunkedResponse("html", new MockChunkedDataProvider());
    }

    @Override
    public void addToResponse(String output) {
      AddMessage(output);
    }

    public void setXmlFormat() {
      response = new ChunkedResponse("xml", new MockChunkedDataProvider());  
    }
  }
}
