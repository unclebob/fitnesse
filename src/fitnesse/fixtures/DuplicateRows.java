package fitnesse.fixtures;

import java.util.Arrays;
import java.util.List;

public class DuplicateRows {
  private String phase;

  public DuplicateRows(String phase) {
    this.phase = phase;
  }

  public List<Object> query() {
    List results;
    if ("A".equalsIgnoreCase(phase)) {
      results = Arrays.asList(
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteSetUp")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneOne")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneTwo")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteTearDown")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteSetUp")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneThree")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteTearDown")
        )
      );
    } else {
      results = Arrays.asList(
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteSetUp")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneThree")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteTearDown")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteSetUp")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneOne")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.TestOneTwo")
        ),
        Arrays.asList(
          Arrays.asList("x", "SuiteChildOne.SuiteTearDown")
        )
      );
    }
    return results;

  }
}
