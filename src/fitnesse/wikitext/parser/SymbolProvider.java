package fitnesse.wikitext.parser;

import java.util.HashMap;
import java.util.Iterator;

public class SymbolProvider {
    /* Keeping these tables in sync with the matchers is a hassle but a major performance gain */
    /* We could build these tables automatically... */

    private static char defaultMatch = '\0';
    private static HashMap<Character, SymbolType[]> dispatch;
    static {
        dispatch = new HashMap<Character, SymbolType[]>();
        for (char letter = 'a'; letter <= 'z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char letter = 'A'; letter <= 'Z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char digit = '0'; digit <= '9'; digit++) dispatch.put(digit, new SymbolType[] {});

        dispatch.put('h', new SymbolType[] { SymbolType.Link });
        dispatch.put('|', new SymbolType[]
            { SymbolType.Table, SymbolType.EndCell });
        dispatch.put('!', new SymbolType[] {
                SymbolType.HashTable, SymbolType.HeaderLine, SymbolType.Literal, SymbolType.Collapsible,
                SymbolType.AnchorName, SymbolType.Contents, SymbolType.CenterLine, SymbolType.Define,
                SymbolType.Include, SymbolType.Meta, SymbolType.NoteLine, SymbolType.Path, SymbolType.Style,
                SymbolType.Table });
        dispatch.put('-', new SymbolType[] {
                SymbolType.HorizontalRule, SymbolType.Table, SymbolType.CloseLiteral, SymbolType.Strike});

        /* This is the default list. These can be broken out further */
        dispatch.put(defaultMatch, new SymbolType[] {
            SymbolType.Alias, SymbolType.List, SymbolType.Comment, SymbolType.Whitespace, SymbolType.CloseCollapsible,
            SymbolType.Newline, SymbolType.Colon, SymbolType.Comma,
            SymbolType.Evaluator, SymbolType.CloseEvaluator, SymbolType.Variable, SymbolType.Preformat,
            SymbolType.ClosePreformat, SymbolType.OpenParenthesis, SymbolType.OpenBrace, SymbolType.OpenBracket,
            SymbolType.CloseParenthesis, SymbolType.CloseBrace, SymbolType.CloseBracket, SymbolType.CloseLiteral,
            SymbolType.Collapsible, SymbolType.HorizontalRule, SymbolType.Bold,
            SymbolType.Italic, SymbolType.Strike, SymbolType.AnchorReference, SymbolType.WikiWord, SymbolType.Text });
    }

    public static final SymbolType[]literalTableTypes = {
            SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
    
    private HashMap<Character, SymbolType[]> currentDispatch = dispatch;
    private SymbolType[] currentTypes;
    private int currentIndex;

    private SymbolType[] getMatchTypes(Character match) {
        if (currentDispatch.containsKey(match)) return currentDispatch.get(match);
        return currentDispatch.get(defaultMatch);
    }

    public SymbolProvider setTypes(SymbolType[] types) {
        currentDispatch = new HashMap<Character, SymbolType[]>();
        currentDispatch.put(defaultMatch, types);
        return this;
    }

    public boolean hasType(SymbolType type) {
        for (SymbolType currentType: currentDispatch.get(defaultMatch)) {
            if (type == currentType) return true;
        }
        return false;
    }

    public Iterable<SymbolType> candidates(char startsWith) {
        currentTypes = getMatchTypes(startsWith);
        currentIndex = 0;
        return new Iterable<SymbolType> () {
            public Iterator<SymbolType> iterator() {
                return new Iterator<SymbolType>() {

                    public boolean hasNext() {
                        return currentIndex < currentTypes.length;
                    }

                    public SymbolType next() {
                        return currentTypes[currentIndex++];
                    }

                    public void remove() {}
                } ;
            }
        };
    }
}
