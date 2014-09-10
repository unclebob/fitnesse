
package fitnesse.wiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.Maybe;


public class VariableTool {
  private static final Pattern variablePattern = Pattern.compile("\\$\\{.*\\}");
  private final VariableSource variableSource;

  public VariableTool(VariableSource variableSource) {

    this.variableSource = variableSource;
  }

  public String replace(String str) {
    Matcher m = variablePattern.matcher(str);
    while (m.find()) {
      String var = m.group();
      Maybe<String> value = variableSource.findVariable(var.substring(2, var.length() - 1));
      if (!value.isNothing()) {
        str = str.replace(var, value.getValue());
      }
    }
    return str;
  }
}
