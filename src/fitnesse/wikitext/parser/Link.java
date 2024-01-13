package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class Link extends SymbolType implements Rule, Translation {
  public static final Link symbolType = new Link();
  public static final String ImageProperty = "image";
  public static final String WidthProperty = "width";
  public static final String StyleProperty = "style";
  public static final String Left = "left";
  public static final String Right = "right";

  public Link() {
    super("Link");
    wikiMatcher(new Matcher().string("http://"));
    wikiMatcher(new Matcher().string("https://"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    Symbol targetList = parser.parseToEnds(-1,
      SymbolProvider.linkTargetProvider,
      new SymbolType[]{SymbolType.Newline, SymbolType.Whitespace});
    return new Maybe<>(current.add(targetList));
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    String target = symbol.getContent() + translator.translate(symbol.childAt(0));
    return buildLink(translator, target, symbol);
  }

  public String buildLink(Translator translator, String body, Symbol link) {
    Reference reference = new Reference(translator.translate(link.childAt(0)));
    String prefix = link.getContent();
    HtmlTag tag;
    if (link.hasProperty(Link.ImageProperty) || reference.isImage()) {
      tag = new HtmlTag("img");
      tag.addAttribute("src", reference.makeUrl(prefix));
      String imageClass = link.getProperty(Link.ImageProperty);
      if (!imageClass.isEmpty()) tag.addAttribute("class", imageClass);
      String width = link.getProperty(Link.WidthProperty);
      if (!width.isEmpty()) tag.addAttribute("width", width);
      String style = link.getProperty(Link.StyleProperty);
      if (!style.isEmpty()) tag.addAttribute("style", style);
    } else {
      tag = new HtmlTag("a", body);
      tag.addAttribute("href", reference.makeUrl(prefix));
    }
    return tag.htmlInline();
  }

  private class Reference {
    private String reference;

    public Reference(String reference) {
      this.reference = reference;
    }

    public boolean isImage() {
      String lower = reference.toLowerCase();
      return lower.endsWith(".jpg") || lower.endsWith(".gif") || lower.endsWith(".png") || lower.endsWith(".svg");
    }

    public String makeUrl(String prefix) {
      return reference.startsWith("files/") ? reference : prefix + reference;
    }
  }
}
