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
            String imageUrl = parseImageUrl(parser);
            Symbol list = new Symbol(SymbolType.SymbolList).add(new Symbol(SymbolType.Text, imageUrl));
            Symbol link = new Symbol(Link.symbolType).add(list);
            addOptions(link, options);
            return makeImageLink(current, link, imageProperty);
        }
        else return Symbol.nothing;
    }

    private String parseImageUrl(Parser parser) {
        List<Symbol> nextSymbols = parser.peek(new SymbolType[]{SymbolType.Colon, SymbolType.Text, SymbolType.Comma, SymbolType.Text});
        
        Symbol prefix = parser.getCurrent();
        String fallback = prefix.getContent();
        if (!prefix.isType(SymbolType.Text) || !prefix.getContent().equals("data")) return fallback;
        StringBuilder imageUrl = new StringBuilder(prefix.getContent());

        Symbol colon = nextSymbols.get(0);
        if (!colon.isType(SymbolType.Colon)) return fallback;
        imageUrl.append(colon.getContent());

        Symbol dataType = nextSymbols.get(1);
        if (!dataType.isType(SymbolType.Text) || !dataType.getContent().endsWith(";base64")) return fallback;
        imageUrl.append(dataType.getContent());

        Symbol comma = nextSymbols.get(2);
        if (!comma.isType(SymbolType.Comma)) return fallback;
        imageUrl.append(comma.getContent());

        Symbol data = nextSymbols.get(3);
        if (!data.isType(SymbolType.Text)) return fallback;
        imageUrl.append(data.getContent());

        parser.moveNext(nextSymbols.size());

        while(parser.peek().isType(SymbolType.Delta) && parser.peek().getContent().startsWith("+")) {
            Symbol plusDelta = parser.moveNext(1);
            imageUrl.append(plusDelta.getContent());

            if (parser.peek().isType(SymbolType.Text)) {
                Symbol dataAfterPlusDelta = parser.moveNext(1);
                imageUrl.append(dataAfterPlusDelta.getContent());
            }
        }
        return imageUrl.toString();
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
