package fitnesse.slim;

public class Library {
  public final String instanceName;
  public final Object instance;

  public Library(String instanceName, Object instance) {
    this.instanceName = instanceName;
    this.instance = instance;
  }
}
