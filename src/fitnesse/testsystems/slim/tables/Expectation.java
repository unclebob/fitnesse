package fitnesse.testsystems.slim.tables;

import java.util.Map;

public interface Expectation {

  public abstract void evaluateExpectation(Map<String, Object> returnValues);

}