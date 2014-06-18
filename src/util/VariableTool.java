
package util;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class VariableTool {
  private static final Pattern variablePattern = Pattern.compile("\\$\\{.*\\}");
  
  public static String replace(String str) {
    return replace(str, System.getenv());
  }

  public static String replace(String str, Properties properties) {
    return replace(str, (Map) properties);
  }

  public static String replace(String str, Map<String, String> properties) {
    Matcher m = variablePattern.matcher(str);
    while (m.find()) {
      String var = m.group();
      String value = properties.get(var.substring(2, var.length() - 1));
      if (value != null) {
        str = str.replace(var, value);
      }
    }
    return str;
  }
}
