// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.StandardOutAndErrorRecorder;
import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.responders.run.TestResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class WikiImportTestEventListenerTest {
  private WikiImportTestEventListener eventListener;
  private MockTestResponder testResponder;
  private MockSuiteResponder suiteResponder;
  private WikiPage pageOne;
  private MockWikiImporterFactory importerFactory;
  private WikiPage childOne;
  private WikiPage childTwo;
  private StandardOutAndErrorRecorder standardOutAndErrorRecorder;

  @Before
  public void setUp() throws Exception {
    standardOutAndErrorRecorder = new StandardOutAndErrorRecorder();

    WikiPage root = InMemoryPage.makeRoot("RooT");
    pageOne = root.addChildPage("PageOne");
    childOne = pageOne.addChildPage("ChildOne");
    childTwo = pageOne.addChildPage("ChildTwo");

    importerFactory = new MockWikiImporterFactory();
    eventListener = new WikiImportTestEventListener(importerFactory);
    testResponder = new MockTestResponder();
    suiteResponder = new MockSuiteResponder();
  }

  @After
  public void tearDown() {
    standardOutAndErrorRecorder.stopRecording(false);
  }

  @Test
  public void testRunWithTestingOnePage() throws Exception {
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals(MockWikiImporter.mockContent, pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, data.getContent());
    assertEquals("Updating imported content...done", sentMessages);
  }

  @Test
  public void testNoImportAnnouncementIfXmlFormat() throws Exception {
    testResponder.setXmlFormat();
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);
    assertEquals("", sentMessages);
  }

  @Test
  public void testRunWithTestingOnePageWithoutAutoUpdate() throws Exception {
    addImportPropertyToPage(pageOne, false, false);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals("", data.getContent());
    assertEquals("", sentMessages);
  }

  @Test
  public void testErrorOccured() throws Exception {
    importerFactory.mockWikiImporter.fail = true;
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals("", data.getContent());
    assertEquals("Updating imported content...java.lang.Exception: blah", sentMessages);
  }

  @Test
  public void testRunWithSuiteFromRoot() throws Exception {
    addImportPropertyToPage(pageOne, true, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(suiteResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childTwo.getData().getContent());
    assertEquals("Updating imported content...done", sentMessages);
  }

  @Test
  public void testRunWithSuiteFromNonRoot() throws Exception {
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(suiteResponder, data);

    assertEquals(MockWikiImporter.mockContent, pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childTwo.getData().getContent());
    assertEquals("Updating imported content...done", sentMessages);
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

  private class MockTestResponder extends TestResponder {
    private MockTestResponder() {
      response = new ChunkedResponse("html");
    }

    public void addToResponse(String output) throws Exception {
      AddMessage(output);
    }

    public void setXmlFormat() {
      response = new ChunkedResponse("xml");  
    }
  }

  private class MockSuiteResponder extends SuiteResponder {
    private MockSuiteResponder() {
      response = new ChunkedResponse("html");
    }

    public void addToResponse(String output) throws Exception {
      AddMessage(output);
    }
  }
}
