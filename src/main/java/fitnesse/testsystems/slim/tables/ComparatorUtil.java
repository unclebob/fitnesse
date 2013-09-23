package fitnesse.testsystems.slim.tables;

public class ComparatorUtil {

  private ComparatorUtil() {
    //
  }

  public static boolean approximatelyEqual(String standard, String candidate) {
    try {
      double candidateValue = Double.parseDouble(candidate);
      double standardValue = Double.parseDouble(standard);
      int point = standard.indexOf(".");
      int precision = 0;
      if (point != -1)
        precision = standard.length() - point - 1;
      double roundingFactor = 0.5;
      while (precision-- > 0)
        roundingFactor /= 10;
      return Math.abs(candidateValue - standardValue) <= roundingFactor;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
