package fitnesse.wikitext.parser;

import java.util.Collection;

/**
 * Extension point for SymbolTypes that want to return a list of paths.
 *
 * Useful in particular when one needs to generate a list of paths from a single symbol, with optional translation.
 */
public interface PathsProvider {
    /**
     * Return the collection of paths represented by the given symbol
     * @param translator that optionally translates the symbols to a valid paths
     * @param symbol the symbol to collect the paths from
     * @return the collection of paths to be added to the classpath
     */
    Collection<String> providePaths(Translator translator, Symbol symbol);
}
