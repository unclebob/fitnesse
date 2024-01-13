
package fitnesse.wiki;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.VariableSource;

public class VariableTool {
  private static final Pattern variablePattern = Pattern.compile("\\$\\{.*}");
  private final VariableSource variableSource;

  public VariableTool(VariableSource variableSource) {

    this.variableSource = variableSource;
  }

  public String replace(String str) {
    Matcher m = variablePattern.matcher(str);
    while (m.find()) {
      String var = m.group();
      Optional<String> value = variableSource.findVariable(var.substring(2, var.length() - 1));
      if (value.isPresent()) {
        str = str.replace(var, value.get());
      }
    }
    return str;
  }
}
