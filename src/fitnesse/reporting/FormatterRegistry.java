package fitnesse.reporting;

public interface FormatterRegistry {
  void registerFormatter(Class<? extends Formatter> formatter);
}
