package fit.exception;

public class NoDefaultConstructorFixtureException extends FixtureException
{
  public NoDefaultConstructorFixtureException(String fixtureName)
  {
    super("Class {0} has no default constructor.", fixtureName);
  }
}