package fitnesse.wiki;

public class WikiPageLoadException extends RuntimeException {
  public WikiPageLoadException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public WikiPageLoadException(final Throwable cause) {
    super(cause);
  }
}
