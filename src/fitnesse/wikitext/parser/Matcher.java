package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Matcher {

    private interface ScanMatch {
        Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset);
    }

    private static final List<Character> defaultList =
            Collections.unmodifiableList(Arrays.asList('\0'));

    private List<ScanMatch> matches = new ArrayList<>(4);
    private List<Character> firsts = null;

    public List<Character> getFirsts() {
        return firsts != null ? firsts : defaultList;
    }

    public Matcher whitespace() {
        if (firsts == null) firsts = defaultList;
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                int length = input.whitespaceLength(offset);
                return length > 0 ? new Maybe<>(length) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher startLine() {
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                return isStartLine(input, symbols, offset) ? new Maybe<>(0) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher startLineOrCell() {
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                return isStartLine(input, symbols, offset) || isStartCell(symbols)
                 ? new Maybe<>(0) : Maybe.noInteger;
            }
        });
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
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                return input.matches(delimiter, offset) ? new Maybe<>(delimiter.length()) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher listDigit() {
        firstIsDigit('1');
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                return isDigitInput('1', input, offset) ? new Maybe<>(1) : Maybe.noInteger;
            }
        });
        return this;
    }

    private boolean isDigitInput(char firstDigit, ScanString input, int offset) {
        for (char i = firstDigit; i <= '9'; i++) {
           if (input.matches(new String(new char[] {i}), offset)) return true;
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
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                int size = 0;
                while (isDigitInput('0', input, offset + size)) size++;
                return size > 0 ? new Maybe<>(size) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher ignoreWhitespace() {
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                return new Maybe<>(input.whitespaceLength(offset));
            }
        });
        return this;
    }

    public Matcher repeat(final char delimiter) {
        if (firsts == null) {
            firsts = Collections.singletonList(delimiter);
        }
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                int size = 0;
                while (input.charAt(offset + size) == delimiter) size++;
                return size > 0 ? new Maybe<>(size) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher endsWith(final char[] terminators) {
        matches.add(new ScanMatch() {
            @Override
            public Maybe<Integer> match(ScanString input, SymbolStream symbols, int offset) {
                int size = 0;
                while (true) {
                    char candidate = input.charAt(offset + size);
                    if (candidate == 0) return Maybe.noInteger;
                    if (contains(terminators, candidate)) break;
                    size++;
                }
                return size > 0 ? new Maybe<>(size + 1) : Maybe.noInteger;
            }

            private boolean contains(char[] terminators, char candidate) {
                for (char terminator: terminators) if (candidate == terminator) return true;
                return false;
            }
        });
        return this;
    }

    public Maybe<Integer> makeMatch(ScanString input, SymbolStream symbols)  {
        int totalLength = 0;
        for (ScanMatch match: matches) {
            Maybe<Integer> matchLength = match.match(input, symbols, totalLength);
            if (matchLength.isNothing()) return Maybe.noInteger;
            totalLength += matchLength.getValue();
        }

        return new Maybe<>(totalLength);
    }

}
