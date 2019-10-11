package fitnesse.testsystems;

import java.util.regex.Pattern;

/**
 * This interface can be implemented by Expectation's to provide extra information for reporting.
 */
public interface TableCell {

  Pattern ALREADY_REPLACED_SYMBOL = Pattern.compile(".*\\$.+<?->?\\[.+].*");

  int getCol();

  int getRow();
}
