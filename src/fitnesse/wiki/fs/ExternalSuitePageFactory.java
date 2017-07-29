package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.VariableSource;

import static fitnesse.wiki.fs.ExternalSuitePage.hasHtmlChild;

public class ExternalSuitePageFactory implements WikiPageFactory {

  private final FileSystem fileSystem;

  public ExternalSuitePageFactory(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource) {
    return new ExternalSuitePage(path, pageName, parent, fileSystem, variableSource);
  }

  @Override
  public boolean supports(File path) {
    return hasHtmlChild(fileSystem, path);
  }

}
