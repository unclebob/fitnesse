package fitnesse.slim;

public class SlimException extends Exception {
  private String tag;

  public SlimException(String message) {
    super(message);
  }

  public SlimException(String message, String tag) {
    super(message);
    this.tag = tag;
  }

  public SlimException(Throwable cause) {
    super(cause);
  }

  public SlimException(Throwable cause, String tag) {
    super(cause);
    this.tag = tag;
  }

  public SlimException(String message, Throwable cause) {
    super(message, cause);
  }

  public SlimException(String message, Throwable cause, String tag) {
    super(message, cause);
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }
}
