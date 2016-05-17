package fitnesse.wiki;

import static fitnesse.wiki.PageData.*;

public enum PageType {

  SUITE("Suite") {
    @Override
    public boolean validForPageName(String pageName) {
      return (pageName.startsWith(toString())
          && !pageName.equals(SUITE_SETUP_NAME) && !pageName.equals(SUITE_TEARDOWN_NAME))
          || pageName.endsWith(toString()) || pageName.endsWith("Examples");
    }
  },
  TEST("Test") {
    @Override
    public boolean validForPageName(String pageName) {
      return pageName.startsWith(toString())
          || pageName.endsWith(toString())
          || (pageName.startsWith("Example") && !pageName
              .startsWith("Examples")) || pageName.endsWith("Example");
    }
  },
  STATIC("Static") {
    @Override
    public boolean validForPageName(String pageName) {
      return true;
    }
  };

  public static PageType fromString(String typeDescriptor) {
    for (PageType type: PageType.values()) {
      if (type.description.equalsIgnoreCase(typeDescriptor)) {
        return type;
      }
    }

    throw new IllegalArgumentException("unknown page type descriptor: " + typeDescriptor);

  }

  public static PageType fromWikiPage(WikiPage page) {
    PageData data = page.getData();
    if (data.hasAttribute(SUITE.toString())) {
      return SUITE;
    }

    if (data.hasAttribute(TEST.toString())) {
      return TEST;
    }

    return STATIC;
  }

  public static PageType getPageTypeForPageName(String pageName) {
    for (PageType type: values()) {
      if (type.validForPageName(pageName))
        return type;
    }
    return STATIC;
  }

  public static String [] valuesAsString(){
    PageType [] ee = PageType.values();
    String [] stringArray = new String [ee.length]; 
    for (int i = 0; i < ee.length; i++) {
      stringArray[i] = ee[i].toString();
    }
    return stringArray;
    
  }
  private String description;

  PageType(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }

  public abstract boolean validForPageName(String pageName);

}
