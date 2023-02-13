package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Matcher {

    private interface ScanMatch {
        void match(ScanString input, SymbolStream symbols, MatchResult match);
    }

    private static final List<Character> defaultList = Collections.singletonList('\0');

    private final List<ScanMatch> matches = new ArrayList<>(4);
    private List<Character> firsts = null;

    public List<Character> getFirsts() {
        return firsts != null ? firsts : defaultList;
    }

    public Matcher whitespace() {
        if (firsts == null) firsts = defaultList;
        matches.add((input, symbols, match) -> match.checkLength(input.whitespaceLength(match.getLength())));
        return this;
    }

    public Matcher startLine() {
        matches.add((input, symbols, match) -> match.setMatched(isStartLine(input, symbols, match.getLength())));
        return this;
    }

    public Matcher startLineOrCell() {
        matches.add((input, symbols, match) -> match.setMatched(isStartLine(input, symbols, match.getLength()) || isStartCell(symbols)));
        return this;
    }

    private boolean isStartLine(ScanString input, SymbolStream symbols, int offset) {
        return input.startsLine(offset) || symbols.get(0).isStartLine();
    }

    private boolean isStartCell(SymbolStream symbols) {
        return symbols.get(0).isStartCell() ||
                (symbols.get(0).isType(SymbolType.Whitespace) &&
                        (symbols.get(1).isStartCell() || symbols.get(1).isLineType()));
    }

    public Matcher string(final String delimiter) {
        if (firsts == null) {
            firsts = Collections.singletonList(delimiter.charAt(0));
        }
        matches.add((input, symbols, match) -> {
          if (input.matches(delimiter, match.getLength())) match.addLength(delimiter.length()); else match.noMatch();
        });
        return this;
    }

    public Matcher listDigit() {
        firstIsDigit('1');
        matches.add((input, symbols, match) -> {
          if (isDigitInput('1', input, match.getLength())) match.addLength(1); else match.noMatch();
        });
        return this;
    }

    private boolean isDigitInput(char firstDigit, ScanString input, int offset) {
        for (char i = firstDigit; i <= '9'; i++) {
           if (input.matches(String.valueOf(i), offset)) return true;
        }
        return false;
    }

    private void firstIsDigit(char startDigit) {
        if (firsts == null) {
            firsts = new ArrayList<>();
            for (char i = startDigit; i <= '9'; i++) firsts.add(i);
        }
    }

    public Matcher digits() {
        firstIsDigit('0');
        matches.add((input, symbols, match) -> {
            int size = 0;
            while (isDigitInput('0', input, match.getLength() + size)) size++;
            match.checkLength(size);
        });
        return this;
    }

    public Matcher ignoreWhitespace() {
        matches.add((input, symbols, match) -> match.addLength(input.whitespaceLength(match.getLength())));
        return this;
    }

    public Matcher repeat(final char delimiter) {
        if (firsts == null) {
            firsts = Collections.singletonList(delimiter);
        }
        matches.add((input, symbols, match) -> {
            int size = 0;
            while (input.charAt(match.getLength() + size) == delimiter) size++;
            match.checkLength(size);
        });
        return this;
    }

    public Matcher endsWith(int... terminators) {
        matches.add((input, symbols, match) -> {
          int size = 0;
          while (true) {
            int candidate = input.charAt(match.getLength() + size);
            if (candidate == 0) {
              match.noMatch();
              return;
            }
            if (IntStream.of(terminators).anyMatch(t -> t == candidate)) break;
            size++;
          }
          if (size > 0) match.addLength(size + 1); else match.noMatch();
        });
        return this;
    }

    public Matcher optional(String... options) {
      matches.add((input, symbols, match) -> {
        for (String option : options) {
          if (input.matches(option, match.getLength())) {
            match.addOption(option);
            return;
          }
        }
      });
      return this;
    }

    public Matcher newLine() {
      if (firsts == null) {
        firsts = new ArrayList<>();
        firsts.add('\r');
        firsts.add('\n');
      }
      matches.add((input, symbols, match) -> {
        if (input.matches("\r\n", match.getLength())) {
          match.addLength(2);
        }
        else if (input.matches("\n", match.getLength())) {
          match.addLength(1);
        }
        else match.noMatch();
      });
      return this;
    }

    public MatchResult makeMatch(ScanString input) {
      return makeMatch(input, new SymbolStream());
    }

    public MatchResult makeMatch(ScanString input, SymbolStream symbols)  {
        MatchResult result = new MatchResult();
        for (ScanMatch match: matches) {
            match.match(input, symbols, result);
            if (!result.isMatched()) return result;
        }
        return result;
    }

    public MatchResult findMatch(ScanString input) {
      while (true) {
        MatchResult match = makeMatch(input);
        if (match.isMatched() || input.isEnd()) return match;
        input.moveNext();
      }
    }
}
