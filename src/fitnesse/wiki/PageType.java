package fitnesse.wiki;

import static fitnesse.wiki.PageData.*;

public enum PageType {

  SUITE("Suite") {
    public boolean validForPageName(String pageName) {
      return (pageName.startsWith(toString())
          && !pageName.equals(SUITE_SETUP_NAME) && !pageName.equals(SUITE_TEARDOWN_NAME))
          || pageName.endsWith(toString()) || pageName.endsWith("Examples");
    }
  },
  TEST("Test") {
    public boolean validForPageName(String pageName) {
      return pageName.startsWith(toString())
          || pageName.endsWith(toString())
          || (pageName.startsWith("Example") && !pageName
              .startsWith("Examples")) || pageName.endsWith("Example");
    }
  },
  NORMAL("Normal") {
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

  public static PageType fromWikiPage(WikiPage page) throws Exception {
    PageData data = page.getData();
    if (data.hasAttribute("Suite")) {
      return SUITE;
    }

    if (data.hasAttribute(TEST.toString())) {
      return TEST;
    }

    return NORMAL;
  }

  public static PageType getPageTypeForPageName(String pageName) {
    for (PageType type: values()) {
      if (type.validForPageName(pageName))
        return type;
    }
    return NORMAL;
  }

  private String description;

  PageType(String description) {
    this.description = description;
  }

  public String toString() {
    return description;
  }

  public abstract boolean validForPageName(String pageName);

}
