package fitnesse.wikitext.parser;

public interface Translation {
    String toTarget(Translator translator, Symbol symbol);
}
