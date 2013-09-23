
package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class EnvironmentVariableTool {
  private static final Pattern environmentVariablePattern = Pattern.compile("\\$\\{.*\\}");
  
  public static String replace(String originalPath){
    String value = originalPath;
    Matcher m = environmentVariablePattern.matcher(originalPath);
    while (m.find()) {
      String envVar = m.group(); 
      String envValue = System.getenv(envVar.substring(2, envVar.length()-1));
      if (envValue != null) {
          value = value.replace(envVar, envValue);
      }
    }
    return value;
  }
}
