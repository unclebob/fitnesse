package fitnesse.wikitext.shared;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.FormattedExpression;
import fitnesse.wikitext.parser.Maybe;

public class ToHtml {
  public static String anchorName(String[] strings) {
    return HtmlTag.name("a").attribute("id", strings[0]).body("").htmlInline();
  }

  public static String anchorReference(String[] strings) {
    return HtmlTag.name("a").attribute("href", "#" + strings[0]).body(".#" + strings[0]).html();
  }

  public static String email(String[] strings) {
    return HtmlTag.name("a").attribute("href", "mailto:" + strings[0]).body(strings[0]).htmlInline();
  }

  public static String error(String[] strings) {
    return " " + HtmlTag.name("span").attribute("class", "fail").body(strings[0]).htmlInline() + " ";
  }

  public static String expression(String[] strings, PropertySource source) {
    String locale = source.findProperty(Names.FORMAT_LOCALE, "");
    Maybe<String> formatLocale = locale.length() > 0 ? new Maybe<>(locale) : Maybe.noString;
    Maybe<String> result = new FormattedExpression(strings[0], formatLocale).evaluate();
    return (result.isNothing())
      ? ToHtml.error(new String[] {result.because()})
      : result.getValue();
  }

  public static String header(String[] strings, PropertySource source) {
    HtmlTag result = new HtmlTag("h" + source.findProperty(Names.LEVEL, "1"));
    result.add(strings[0].trim());
    source.findProperty(Names.ID).ifPresent(id -> result.addAttribute("id", id));
    return result.html();
  }

  public static String image(String[] strings, PropertySource source) {
    HtmlTag result =  HtmlTag.name("img").attribute("src", strings[0]);
    source.findProperty(Names.IMAGE_CLASS).ifPresent(value -> result.addAttribute("class", value));
    source.findProperty(Names.IMAGE_WIDTH).ifPresent(value -> result.addAttribute("width", value));
    String style =
      source.findProperty(Names.IMAGE_BORDER).map(value -> "border:" + value + "px solid black;").orElse("") +
      source.findProperty(Names.IMAGE_MARGIN).map(value -> "margin:" + value + "px;").orElse("");
    if (style.length() > 0) result.addAttribute("style", style);
    return result.htmlInline();
  }

  public static String link(String[] strings) {
    String url = strings[0] + strings[1];
    String description = strings[2].length() > 0 ? strings[2] : url;
    int files = url.indexOf("//files/");
    return HtmlTag.name("a").attribute("href", files < 0 ? url : url.substring(files + 2)).body(description).htmlInline();
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
