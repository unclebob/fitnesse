package fitnesse.slim.test;

public class ConstructorThrows {
  public ConstructorThrows(String message) {
    if ("stop test".equals(message)) {
      throw new StopTestException(message);
    }
    if ("stop suite".equals(message)) {
      throw new StopSuiteException(message);
    }
    throw new RuntimeException(message);
  }

}

class StopTestException extends RuntimeException {
  public StopTestException(String message) {
    super(message);
  }
}

class StopSuiteException extends RuntimeException {
  public StopSuiteException(String message) {
    super(message);
  }
}
