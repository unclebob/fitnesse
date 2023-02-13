package fitnesse.util;

public class StringTransform {
  public StringTransform(String input) {
    this.input = input;
    start = 0;
  }

  public boolean find(String value) {
    int index = input.indexOf(value, start);
    if (index < 0) return false;
    copy(index);
    current = index + value.length();
    return true;
  }

  public void copy() {
    copy(current);
  }

  public void skipTo(int newStart) {
    start = newStart;
  }

  public void skipOver(String value) {
    int index = input.indexOf(value, start);
    if (index < 0) return;
    skipTo(index + value.length());
  }

  public void insert(String segment) {
    output.append(segment);
  }

  public boolean startsWith(String prefix) {
    return input.startsWith(prefix, current);
  }

  public int getCurrent() {
    return current;
  }

  public String getOutput() {
    output.append(input.substring(start));
    return output.toString();
  }

  public String from(int begin) {
    return input.substring(begin);
  }

  private void copy(int end) {
    output.append(input, start, end);
    skipTo(end);
  }

  private int start;
  private int current;

  private final String input;

  private final StringBuilder output = new StringBuilder();
}
