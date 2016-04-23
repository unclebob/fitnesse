package fitnesse.slim.test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddUpChange {
  private Integer totalCents = 0;
  private static Map<String, Integer> COIN_VALUES = new HashMap<>();
  static {
    COIN_VALUES.put("1c", 1);
    COIN_VALUES.put("5c", 5);
    COIN_VALUES.put("10c", 10);
    COIN_VALUES.put("25c", 25);
    COIN_VALUES.put("50c", 50);
    COIN_VALUES.put("$1", 100);
  }

  public void reset() {
    totalCents = 0;
  }

  public void set(String coin, Integer amount) {
    if (!COIN_VALUES.containsKey(coin)) {
      throw new IllegalArgumentException("Unknown coin type " + coin);
    }
    totalCents += amount * COIN_VALUES.get(coin);
  }

  public String get(String requestedValue) {
    if ("$ total".equals(requestedValue)) {
      return String.format(Locale.US, "%.2f", totalCents / 100.0);
    }
    return String.format("%d", totalCents);
  }
}
