package fitnesse.http;

public class EmptyRequestException extends HttpException {
  private static final long serialVersionUID = -2983710189855195820L;

  public EmptyRequestException(String message) {
    super(message);
  }

}
