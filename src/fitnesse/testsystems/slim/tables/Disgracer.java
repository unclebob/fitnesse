package fitnesse.testsystems.slim.tables;

import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;

public class Disgracer {
  public boolean capitalizeNextWord;
  public StringBuilder disgracedName;
  private String name;

  public Disgracer(String name) {
    this.name = name;
  }

  public static String disgraceClassName(String name) {
    return new Disgracer(name).disgraceClassNameIfNecessary();
  }

  public static String disgraceMethodName(String name) {
    return new Disgracer(name).disgraceMethodNameIfNecessary();
  }

  private String disgraceMethodNameIfNecessary() {
	if (nameHasDotsBeforeEnd()) return name;
    if (isGraceful()) {
      return disgraceMethodName();
    } else {
      return name;
    }
  }

  private String disgraceMethodName() {
    capitalizeNextWord = false;
    return disgraceName();
  }

  private String disgraceClassNameIfNecessary() {
    if (nameHasDotsBeforeEnd() || nameHasDollars())
      return name;
    else if (isGraceful()) {
      return disgraceClassName();
    } else {
      return name;
    }
  }

  private boolean nameHasDollars() {
    return name.contains("$");
  }

  private String disgraceClassName() {
    capitalizeNextWord = true;
    return disgraceName();
  }

  private boolean nameHasDotsBeforeEnd() {
    int dotIndex = name.indexOf(".");
    return dotIndex != -1 && dotIndex != name.length() - 1;
  }

  private String disgraceName() {
    disgracedName = new StringBuilder();
    for (char c : name.toCharArray())
      appendCharInProperCase(c);

    return disgracedName.toString();
  }

  private void appendCharInProperCase(char c) {
    if (isGraceful(c)) {
      capitalizeNextWord = true;
    } else {
      appendProperlyCapitalized(c);
    }
  }

  private void appendProperlyCapitalized(char c) {
    disgracedName.append(capitalizeNextWord ? toUpperCase(c) : c);
    capitalizeNextWord = false;
  }

  private boolean isGraceful() {
    boolean isGraceful = false;
    for (char c : name.toCharArray()) {
      if (isGraceful(c))
        return true;
    }
    return isGraceful;
  }

  private boolean isGraceful(char c) {
    return !(isLetterOrDigit(c) || c == '_');
  }
}
