package fitnesse.slim.test;

/**
 * Fixture to validate thatwe support labeled constructor arguments.
 * @author Anis Ben Hamidene
 *
 */
public class FixtureWithParametrizedConstructor {

  private String[] arguments;

  public FixtureWithParametrizedConstructor(String argument1, String argument2) {
    this.arguments = new String[]{argument1, argument2};
  }

  public FixtureWithParametrizedConstructor() {
  }
  
  public FixtureWithParametrizedConstructor(String argument1) {
    this.arguments = new String[]{argument1};
  }
  
  public String[] savedPorpertiesAre() {
    return arguments;
  }
  
  
}
