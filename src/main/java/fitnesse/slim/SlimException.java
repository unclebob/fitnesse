package fitnesse.slim;

public class SlimException extends Exception {
  private static final String PRETTY_PRINT_TAG_START = "message:<<";
  private static final String PRETTY_PRINT_TAG_END = ">>";
  private String tag;
  private boolean prettyPrint;

  public SlimException(String message) {
    this(message, false);
  }

  public SlimException(String message, boolean prettyPrint) {
    super(message);
    this.prettyPrint = prettyPrint;
  }

  public SlimException(String message, String tag) {
    this(message, tag, false);
  }

  public SlimException(String message, String tag, boolean prettyPrint) {
    super(message);
    this.tag = tag;
    this.prettyPrint = prettyPrint;
  }

  public SlimException(Throwable cause) {
    this(cause, false);
  }

  public SlimException(Throwable cause, boolean prettyPrint) {
    super(cause);
    this.prettyPrint = prettyPrint;
  }

  public SlimException(Throwable cause, String tag) {
    this(cause, tag, false);
  }

  public SlimException(Throwable cause, String tag, boolean prettyPrint) {
    super(cause);
    this.tag = tag;
    this.prettyPrint = prettyPrint;
  }

  public SlimException(String message, Throwable cause) {
    this(message, cause, false);
  }

  public SlimException(String message, Throwable cause, boolean prettyPrint) {
    super(message, cause);
    this.prettyPrint = prettyPrint;
  }

  public SlimException(String message, Throwable cause, String tag) {
    this(message, cause, tag, false);
  }

  public SlimException(String message, Throwable cause, String tag, boolean prettyPrint) {
    super(message, cause);
    this.tag = tag;
    this.prettyPrint = prettyPrint;
  }

  public String getTag() {
    return tag;
  }

  public boolean prettyPrint() {
    return prettyPrint;
  }

  /*
  TODO Move this to the serialization and clean it up.
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (getCause() != null && getCause().getClass().toString().contains("StopTest")) {
      sb.append(SlimServer.EXCEPTION_STOP_TEST_TAG);
    } else {
      sb.append(SlimServer.EXCEPTION_TAG);
    }
    if (this.prettyPrint) {
      sb.append(PRETTY_PRINT_TAG_START);
    }

    if (tag != null && tag.length() > 0) {
      sb.append(tag).append(" ");
    }

    String msg = getMessage();
    if (msg != null && msg.length() > 0) {
      sb.append(msg);
    }
    if (this.prettyPrint) {
      sb.append(PRETTY_PRINT_TAG_END);
    }

    StackTraceEnricher enricher = new StackTraceEnricher();
    if (getCause() != null) {
      sb.append(enricher.getStackTraceAsString(getCause()));
    } else {
      if (this.getStackTrace() == null || this.getStackTrace().length == 0) {
        this.fillInStackTrace();
      }
      sb.append(enricher.getStackTraceAsString(this));
    }

    return sb.toString();
  }
}
