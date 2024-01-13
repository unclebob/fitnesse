package fitnesse.reporting;

import fitnesse.wiki.WikiPage;

public abstract class BaseFormatter implements Formatter {

  private final WikiPage page;

  protected BaseFormatter() {
    this.page = null;
  }

  protected BaseFormatter(final WikiPage page) {
    this.page = page;
  }

  protected WikiPage getPage() {
    return page;
  }

  /**
   * This implementation remains to allow existing subclasses that override it to work.
   * Code should be rewritten to override #testOutputChunk(TestPage, String) and not this overload.
   * @param output content to append
   * @deprecated implement {@link Formatter#testOutputChunk(fitnesse.testsystems.TestPage, String)}
   */
  @Override
  @Deprecated
  public void testOutputChunk(String output) {
  }

  public int getErrorCount() {
    return 0;
  }
}
