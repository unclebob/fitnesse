package fitnesse.wikitext.shared;

import fitnesse.html.HtmlTag;

public class ToHtml {
  public static String anchorName(String[] strings) {
    return HtmlTag.name("a").attribute("id", strings[0]).htmlInline();
  }

  public static String anchorReference(String[] strings) {
    return HtmlTag.name("a").attribute("href", "#" + strings[0]).body(".#" + strings[0]).html();
  }

  public static String email(String[] strings) {
    return HtmlTag.name("a").attribute("href", "mailto:" + strings[0]).body(strings[0]).htmlInline();
  }

  public static String header(String[] strings, PropertySource source) {
    HtmlTag result = new HtmlTag("h" + source.findProperty(Names.LEVEL, "1"));
    result.add(strings[0].trim());
    source.findProperty(Names.ID).ifPresent(id -> result.addAttribute("id", id));
    return result.html();
  }

  public static String nestedPair(String[] strings) {
    return HtmlTag.name(strings[0]).child(HtmlTag.name(strings[1]).body(strings[2])).htmlInline();
  }

  public static String newLine() {
    return HtmlTag.name("br").htmlInline();
  }

  public static String note(String[] strings) {
    return HtmlTag.name("p").attribute("class", "note").body(strings[0]).html();
  }

  public static String pair(String[] strings) {
    return HtmlTag.name(strings[0]).body(strings[1]).htmlInline();
  }

  public static String path(String[] strings) {
    return new HtmlTag("span").attribute("class", "meta").body("classpath: " + strings[0]).htmlInline();
  }
}
