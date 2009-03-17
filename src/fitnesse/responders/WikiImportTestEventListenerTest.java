// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import junit.framework.TestCase;
import util.StandardOutAndErrorRecorder;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.responders.run.TestResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class WikiImportTestEventListenerTest extends TestCase {
  private WikiImportTestEventListener eventListener;
  private MockTestResponder testResponder;
  private MockSuiteResponder suiteResponder;
  private WikiPage pageOne;
  private MockWikiImporterFactory importerFactory;
  private WikiPage childOne;
  private WikiPage childTwo;
  private StandardOutAndErrorRecorder standardOutAndErrorRecorder;

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

  public void tearDown() {
    standardOutAndErrorRecorder.stopRecording(false);
  }

  public void testRunWithTestingOnePage() throws Exception {
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals(MockWikiImporter.mockContent, pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, data.getContent());
    assertEquals("Updating imported content...done", sentMessages);
  }

  public void testRunWithTestingOnePageWithoutAutoUpdate() throws Exception {
    addImportPropertyToPage(pageOne, false, false);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals("", data.getContent());
    assertEquals("", sentMessages);
  }

  public void testErrorOccured() throws Exception {
    importerFactory.mockWikiImporter.fail = true;
    addImportPropertyToPage(pageOne, false, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(testResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals("", data.getContent());
    assertEquals("Updating imported content...java.lang.Exception: blah", sentMessages);
  }

  public void testRunWithSuiteFromRoot() throws Exception {
    addImportPropertyToPage(pageOne, true, true);

    PageData data = pageOne.getData();
    eventListener.notifyPreTest(suiteResponder, data);

    assertEquals("", pageOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childOne.getData().getContent());
    assertEquals(MockWikiImporter.mockContent, childTwo.getData().getContent());
    assertEquals("Updating imported content...done", sentMessages);
  }

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
    public void addToResponse(String output) throws Exception {
      AddMessage(output);
    }
  }

  private class MockSuiteResponder extends SuiteResponder {
    public void addToResponse(String output) throws Exception {
      AddMessage(output);
    }
  }
}
