package fitnesse.wikitext.parser;

import java.util.List;
import java.util.TreeMap;
import java.util.Map;

public class Image extends SymbolType implements Rule, Translation {
    public static final Image symbolType = new Image();

    public Image() {
        super("Image");
        wikiMatcher(new Matcher().string("!img-l"));
        wikiMatcher(new Matcher().string("!img-r"));
        wikiMatcher(new Matcher().string("!img"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        StringBuilder source = new StringBuilder(current.getContent());
    	  String imageProperty =
            current.getContent().endsWith("l") ? Link.Left
            : current.getContent().endsWith("r") ? Link.Right
            : "";

        parser.moveNext(1);
        if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
        source.append(parser.getCurrent().getContent());
        parser.moveNext(1);

        Map<String, String> options = new TreeMap<>();
        while (parser.getCurrent().isType(SymbolType.Text) && parser.getCurrent().getContent().startsWith("-")) {
            String option = parser.getCurrent().getContent();
            source.append(option);
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            source.append(parser.getCurrent().getContent());
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Text)) return Symbol.nothing;
            source.append(parser.getCurrent().getContent());
            String value = parser.getCurrent().getContent();
            parser.moveNext(1);
            if (!parser.getCurrent().isType(SymbolType.Whitespace)) return Symbol.nothing;
            source.append(parser.getCurrent().getContent());
            parser.moveNext(1);
            options.put(option, value);
        }
        current.setContent(source.toString());

        final Symbol name = parser.getCurrent();
        if (name.isType(Link.symbolType)) {
            Maybe<Symbol> link = Link.symbolType.getWikiRule().parse(name, parser);
            if (link.isNothing()) return Symbol.nothing;
            addOptions(link.getValue(), options);
            return makeImageLink(current, link.getValue(), imageProperty);
        }
        else if (name.isType(SymbolType.Text) || name.isType(WikiWord.symbolType)) {
            String imageDataUrl = tryParseBase64DataUrl(parser);
            String imageUrl = (imageDataUrl == null) ? name.getContent() : imageDataUrl;
            Symbol list = Symbol.listOf(new Symbol(SymbolType.Text, imageUrl));
            Symbol link = new Symbol(Link.symbolType).add(list);
            addOptions(link, options);
            return makeImageLink(current, link, imageProperty);
        }
        else return Symbol.nothing;
    }

    /**
     * @return null if the base64 data url could not be parsed
     * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
     * @see https://datatracker.ietf.org/doc/html/rfc2397
     */
    private String tryParseBase64DataUrl(Parser parser) {
        List<Symbol> nextSymbols = parser.peek(new SymbolType[]{SymbolType.Colon, SymbolType.Text, SymbolType.Comma});
        if (nextSymbols.size() == 0) return null;

        Symbol prefix = parser.getCurrent();
        if (!prefix.isType(SymbolType.Text) || !prefix.getContent().equals("data")) return null;
        StringBuilder imageUrl = new StringBuilder(prefix.getContent());

        Symbol colon = nextSymbols.get(0);
        imageUrl.append(colon.getContent());

        Symbol mediaType = nextSymbols.get(1);
        if (!mediaType.getContent().endsWith(";base64")) return null;
        imageUrl.append(mediaType.getContent());

        Symbol comma = nextSymbols.get(2);
        imageUrl.append(comma.getContent());

        Symbol dataSymbol = parser.peek(4).get(3);
        if(!isBase64Symbol(dataSymbol)) return null;
        parser.moveNext(4);
        imageUrl.append(dataSymbol.getContent());

        while(isBase64Symbol(parser.peek())) {
            Symbol symbol = parser.moveNext(1);
            imageUrl.append(symbol.getContent());
        }
        return imageUrl.toString();
    }

    private boolean isBase64Symbol(Symbol symbol) {
        if (symbol.isType(SymbolType.Whitespace)) return false;
        return symbol.isType(SymbolType.Text)
                || symbol.isType(WikiWord.symbolType)
                || symbol.isType(SymbolType.Delta);
    }

    private void addOptions(Symbol link, Map<String, String> options) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals("-w")) link.putProperty(Link.WidthProperty, value);
            if (key.equals("-m")) link.putProperty(Link.StyleProperty, String.format("%2$smargin:%1$spx;", value, link.getProperty(Link.StyleProperty)));
            if (key.equals("-b")) link.putProperty(Link.StyleProperty, String.format("%2$sborder:%1$spx solid black;", value, link.getProperty(Link.StyleProperty)));
        }
    }

    private Maybe<Symbol> makeImageLink(Symbol current, Symbol link, String imageProperty) {
        link.putProperty(Link.ImageProperty, imageProperty);
        return new Maybe<>(current.add(link));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        return translator.translate(symbol.childAt(0));
    }
}
