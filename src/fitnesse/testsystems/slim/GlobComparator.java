package fitnesse.testsystems.slim;

import java.util.regex.Pattern;

public class GlobComparator implements CustomComparator {

  @Override
  public boolean matches(String actual, String expected) {
    return globToRegExp(expected).matcher(actual).matches();
  }

  private Pattern globToRegExp(String glob) {
    return Pattern.compile("^\\Q" + glob.replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q") + "\\E$", Pattern.DOTALL);
  }

}
