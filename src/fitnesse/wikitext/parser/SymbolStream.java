package fitnesse.wikitext.parser;

import java.util.LinkedList;

public class SymbolStream {
    public SymbolStream() {
        symbols = new LinkedList<>();
    }

    public SymbolStream(SymbolStream other) {
        symbols = new LinkedList<>(other.symbols);
    }

    public Symbol get(int position) {
        return symbols.size() <= position ? Symbol.emptySymbol : symbols.get(position);
    }

    public void add(Symbol value)  {
        symbols.addFirst(value);
        if (symbols.size() > streamCapacity) symbols.removeLast();
    }

    public boolean isEnd() { return symbols.isEmpty() || symbols.getFirst() == Symbol.emptySymbol; }

    private static final int streamCapacity = 3;

    private LinkedList<Symbol> symbols;
}
