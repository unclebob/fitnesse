package fitnesse.slim;

import java.util.ArrayList;
import java.util.List;

public class MethodExecutionResults {

  private List<MethodExecutionResult> results = new ArrayList<>();

  public MethodExecutionResults add(MethodExecutionResult result) {
    results.add(result);
    return this;
  }

  public MethodExecutionResult getFirstResult() {
    return results.get(0);
  }

}
