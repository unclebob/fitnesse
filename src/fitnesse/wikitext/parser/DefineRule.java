package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.translator.Translator;
import util.Maybe;

import java.util.List;

public class DefineRule extends Rule {
    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text, SymbolType.Whitespace});
        if (tokens.size() == 0) return Maybe.noString;

        String name = tokens.get(1).getContent();
        if (!ScanString.isWord(name)) return Maybe.noString;

        scanner.moveNext();
        SymbolType open = scanner.getCurrent().getType();
        SymbolType close = SymbolType.closeType(open);
        if (close == SymbolType.Empty) return Maybe.noString;

        int start = scanner.getOffset();
        scanner.markStart();
        String value = new Translator(getPage()).translateIgnoreFirst(scanner, close);
        if (scanner.isEnd()) return Maybe.noString;

        try {
            getPage().getData().addVariable(name, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new Maybe<String>(HtmlUtil.metaText("variable defined: "
                + name + "=" + scanner.substring(start, scanner.getOffset() - 1)));
    }

    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text, SymbolType.Whitespace});
        if (tokens.size() == 0) return Symbol.Nothing;

        String name = tokens.get(1).getContent();
        if (!ScanString.isWord(name)) return Symbol.Nothing;

        scanner.moveNext();
        SymbolType open = scanner.getCurrent().getType();
        SymbolType close = SymbolType.closeType(open);
        if (close == SymbolType.Empty) return Symbol.Nothing;

        int start = scanner.getOffset();
        scanner.markStart();
        Phrase value = new Parser(getPage()).parseIgnoreFirst(scanner, close);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Phrase(SymbolType.Define)
                .add(name)
                .add(value)
                .add(scanner.substring(start, scanner.getOffset() - 1)));
    }
}
