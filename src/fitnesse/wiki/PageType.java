package fitnesse.wiki;


public enum PageType {

  SUITE("Suite"),
  TEST("Test"),
  NORMAL("Normal");

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

  private String description;

  PageType(String description) {
    this.description = description;
  }

  public String toString() {
    return description;
  }

}
