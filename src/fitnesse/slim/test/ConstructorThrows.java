package fitnesse.slim.test;

public class ConstructorThrows {
  public ConstructorThrows(String message) {
    throw new RuntimeException(message);
  }
}
