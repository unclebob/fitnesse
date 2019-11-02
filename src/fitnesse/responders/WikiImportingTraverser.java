package fitnesse.responders;

import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.IOException;

public class WikiImportingTraverser implements WikiImporterClient, Traverser<Object> {
  private final WikiImporter importer;
  private final WikiPage page;
  private final PageData data;
  private final boolean isUpdate;
  private final boolean isNonRoot;
  private final String remoteWikiUrl;
  private final WikiPagePath pagePath;
  private TraversalListener<Object> traversalListener;

  public WikiImportingTraverser(WikiImporter wikiImporter, WikiPage page, String remoteWikiUrl) throws IOException {
    this.importer = wikiImporter;
    this.page = page;
    this.data = page.getData();
    this.pagePath = page.getFullPath();
    WikiImportProperty importProperty = WikiImportProperty.createFrom(data.getProperties());
    if (importProperty != null) {
      this.remoteWikiUrl = importProperty.getSourceUrl();
      this.isUpdate = true;
      this.isNonRoot = !importProperty.isRoot();
    } else {
      this.remoteWikiUrl = remoteWikiUrl;
      this.isUpdate = false;
      this.isNonRoot = false;
    }
    initializeImporter();
  }

  public WikiImportingTraverser(WikiImporter wikiImporter, WikiPage page) throws IOException {
    this(wikiImporter, page, null);
  }

  private void initializeImporter() throws IOException {
    importer.setWikiImporterClient(this);
    importer.setLocalPath(pagePath);
    importer.parseUrl(remoteWikiUrl);
  }

  public boolean isUpdate() {
    return isUpdate;
  }

  @Override
  public void traverse(TraversalListener<Object> traversalListener) {
    this.traversalListener = traversalListener;
    try {
      if (isNonRoot) {
        importer.importRemotePageContent(page);
      }

      importer.importWiki(page);

      if (!isUpdate) {
        WikiImportProperty importProperty = new WikiImportProperty(importer.remoteUrl());
        importProperty.setRoot(true);
        importProperty.setAutoUpdate(importer.getAutoUpdateSetting());
        importProperty.addTo(data.getProperties());
        page.commit(data);
      }
    } catch (WikiImporter.WikiImporterException e) {
      traversalListener.process(new ImportError("ERROR", "The remote resource, " + importer.remoteUrl() + ", was not found."));
    } catch (WikiImporter.AuthenticationRequiredException e) {
      traversalListener.process(new ImportError("AUTH", e.getMessage()));
    } catch (Exception e) {
      traversalListener.process(new ImportError("ERROR", e.getMessage(), e));
    }

  }

  @Override
  public void pageImported(WikiPage localPage) throws IOException {
    traversalListener.process(localPage);
  }

  @Override
  public void pageImportError(WikiPage localPage, Exception e) throws IOException {
    traversalListener.process(new ImportError("PAGEERROR", e.getMessage(), e));
  }

  public static class ImportError {
    private final String message;
    private final String type;
    private final Exception exception;

    public ImportError(String type, String message) {
      this(type, message, null);
    }

    public ImportError(String type, String message, Exception exception) {
      super();
      this.type = type;
      this.message = message;
      this.exception = exception;
    }

    public String getType() {
      return type;
    }

    public String getMessage() {
      return message;
    }

    public Exception getException() {
      return exception;
    }

    @Override
    public String toString() {
      return getClass().getName() + ": " + getMessage();
    }
  }
}
