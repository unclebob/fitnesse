package fitnesse.wikitext.parser;

import java.util.TreeMap;
import java.util.Map;

public class Image extends SymbolType implements Rule {
    public static final Image symbolType = new Image();
    
    public Image() {
        super("Image");
        wikiMatcher(new Matcher().string("!img-l"));
        wikiMatcher(new Matcher().string("!img-r"));
        wikiMatcher(new Matcher().string("!img"));
        wikiRule(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
    	  String imageProperty =
            current.getContent().endsWith("l") ? Link.Left
            : current.getContent().endsWith("r") ? Link.Right
            : "";

        parser.moveNext(1);
        if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;

        parser.moveNext(1);
        
        Map<String, String> options = new TreeMap<String, String>();
        while (parser.getCurrent().isType(SymbolType.Text) && parser.getCurrent().getContent().startsWith("-")) {
            String option = parser.getCurrent().getContent();
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Text)) return Symbol.nothing;
            String value = parser.getCurrent().getContent();
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            parser.moveNext(1);
            options.put(option, value);
        }

        if (parser.getCurrent().isType(Link.symbolType)) {
            Maybe<Symbol> link = Link.symbolType.getWikiRule().parse(parser.getCurrent(), parser);
            if (link.isNothing()) return Symbol.nothing;
            addOptions(link.getValue(), options);
            return makeImageLink(link.getValue(), imageProperty);
        }
        else if (parser.getCurrent().isType(SymbolType.Text)) {
            Symbol list = new Symbol(SymbolType.SymbolList).add(parser.getCurrent());
            Symbol link = new Symbol(Link.symbolType).add(list);
            addOptions(link, options);
            return makeImageLink(link, imageProperty);
        }
        else return Symbol.nothing;
    }

    private void addOptions(Symbol link, Map<String, String> options) {
      for(String key : options.keySet()) {
        if (key.equals("-w")) link.putProperty(Link.WidthProperty, options.get(key));
        if (key.equals("-m")) link.putProperty(Link.StyleProperty, String.format("%2$smargin:%1$spx %1$spx %1$spx %1$spx;", options.get(key), link.getProperty(Link.StyleProperty)));
        if (key.equals("-b")) link.putProperty(Link.StyleProperty, String.format("%2$sborder:%1$spx solid black;", options.get(key), link.getProperty(Link.StyleProperty)));
      }
    }

    private Maybe<Symbol> makeImageLink(Symbol link, String imageProperty) {
        return new Maybe<Symbol>(link.putProperty(Link.ImageProperty, imageProperty));
    }
}
