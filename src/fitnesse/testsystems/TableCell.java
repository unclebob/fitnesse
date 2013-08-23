package fitnesse.testsystems;

/**
 * This interface can be implemented by Expectation's to provide extra information for reporting.
 */
public interface TableCell {

  int getCol();

  int getRow();
}
