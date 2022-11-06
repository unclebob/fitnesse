package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {

  public MatchResult() {
    this.length = 0;
    matched = true;
  }

  public int getLength() { return length; }
  public List<String> getOptions() { return options; }
  public boolean isMatched() { return matched; }

  public void addLength(int length) { this.length += length; }
  public void noMatch() { setMatched(false); }
  public void setMatched(boolean matched) { this.matched = matched; }

  public void addOption(String option) {
    addLength(option.length());
    options.add(option);
  }

  public void checkLength(int check) {
    if (check > 0) addLength(check); else noMatch();
  }

  public void advance(ScanString scan) {
    scan.moveNext(length);
  }

  private int length;
  private final ArrayList<String> options = new ArrayList<>(1);
  private boolean matched;
}
