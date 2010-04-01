package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.translator.Translator;
import util.Maybe;
import java.util.List;

public class VariableToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        List<Token> tokens = scanner.nextTokens(new SymbolType[] {SymbolType.Text, SymbolType.CloseBrace});
        if (tokens.size() == 0) return Maybe.noString;

        String name = tokens.get(0).getContent();
        if (!ScanString.isWord(name)) return Maybe.noString;

        return new Maybe<String>(findVariable(name));
     }

    private String findVariable(String name) {
        try {
            WikiPage page = getPage();
            while (true) {
                Maybe<String> result = page.getData().getLocalVariable(name);
                if (!result.isNothing()) return result.getValue();
                if (page.getPageCrawler().isRoot(page)) break;
                page = page.getParent();
                if (page == null) break;
                new Translator(page).translate(page.getData().getContent());
            }
            return getPage().getData().getVariable(name);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
