// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.responders.run.TestEventListener;
import fitnesse.responders.run.TestResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class WikiImportTestEventListener implements TestEventListener {
  public static void register() {
    TestResponder.registerListener(new WikiImportTestEventListener(new WikiImporterFactory()));
  }

  private WikiImporterFactory importerFactory;

  public WikiImportTestEventListener(WikiImporterFactory importerFactory) {
    this.importerFactory = importerFactory;
  }

  public void notifyPreTest(TestResponder testResponder, PageData data) throws Exception {
    TestEventProcessor eventProcessor;
    if (testResponder instanceof SuiteResponder)
      eventProcessor = new SuiteEventProcessor();
    else
      eventProcessor = new TestEventProcessor();

    eventProcessor.run(testResponder, data);
  }

  private class TestEventProcessor implements WikiImporterClient {
    private TestResponder testResponder;
    private boolean errorOccured;
    protected WikiImporter wikiImporter;
    protected WikiPage wikiPage;
    protected PageData data;
    protected WikiImportProperty importProperty;

    public void run(TestResponder testResponder, PageData data) throws Exception {
      this.testResponder = testResponder;
      this.data = data;
      importProperty = WikiImportProperty.createFrom(data.getProperties());
      if (importProperty != null && importProperty.isAutoUpdate()) {
        announceImportAttempt(testResponder);
        doImport(testResponder, data);
        closeAnnouncement(testResponder);
      }
    }

    private void closeAnnouncement(TestResponder testResponder) throws Exception {
      if (testResponder.getResponse().isHtmlFormat())
        testResponder.addToResponse("</span>");
    }

    private void announceImportAttempt(TestResponder testResponder) throws Exception {
      if (testResponder.getResponse().isHtmlFormat()) {
        testResponder.addToResponse("<span class=\"meta\">Updating imported content...</span>");
        testResponder.addToResponse("<span class=\"meta\">");
      }
    }

    private void doImport(TestResponder testResponder, PageData data) throws Exception {
      try {
        wikiImporter = importerFactory.newImporter(this);
        wikiImporter.parseUrl(importProperty.getSourceUrl());
        wikiPage = data.getWikiPage();

        doUpdating();

        if (!errorOccured)
          announceDone(testResponder);
      }
      catch (Exception e) {
        pageImportError(data.getWikiPage(), e);
      }
    }

    private void announceDone(TestResponder testResponder) throws Exception {
      if (testResponder.getResponse().isHtmlFormat())
        testResponder.addToResponse("done");
    }

    protected void doUpdating() throws Exception {
      updatePagePassedIn();
    }

    protected void updatePagePassedIn() throws Exception {
      wikiImporter.importRemotePageContent(wikiPage);
      data.setContent(wikiPage.getData().getContent());
    }

    public void pageImported(WikiPage localPage) throws Exception {
    }

    public void pageImportError(WikiPage localPage, Exception e) throws Exception {
      errorOccured = true;
      System.out.println("Exception while importing \"local page\": " + localPage.getName() + ", exception: " + e.getMessage());
      if (e.getCause() != null)
        System.out.println("  cause: " + e.getCause().getMessage());
      e.printStackTrace(System.out);
      testResponder.addToResponse(e.toString());
    }
  }

  private class SuiteEventProcessor extends TestEventProcessor {
    protected void doUpdating() throws Exception {
      if (!importProperty.isRoot())
        updatePagePassedIn();
      wikiImporter.setAutoUpdateSetting(true);
      wikiImporter.importWiki(wikiPage);
    }
  }
}
