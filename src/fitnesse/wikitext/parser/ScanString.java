package fitnesse.wikitext.parser;

public class ScanString {
    private final String input;
    private int offset;

    public ScanString(String input, int offset) {
        this.input = input;
        this.offset = offset;
    }

    public boolean matches(String match, int startsAt) {
        if (match.length() == 0) return false;
        if (offset + startsAt + match.length() > input.length()) return false;
        return input.regionMatches(offset + startsAt, match, 0, match.length());
    }

    public boolean startsWith(String match) {
        return matches(match, 0);
    }

    public boolean isBetween(String minimum, String maximum, int startsAt) {
        if (offset + startsAt + minimum.length() > input.length()) return false;
        String test = substring(startsAt, startsAt + minimum.length());
        return test.compareTo(minimum) >= 0 && test.compareTo(maximum) <= 0;
    }

    public boolean startsLine() {
        return offset == 0 || input.charAt(offset - 1) == '\n';
    }

    public int find(char[] matches, int startAt) {
        int current = offset + startAt;
        while (current < input.length()) {
            for (char match: matches) {
                if (input.charAt(current) == match) return current - offset;
            }
            current++;
        }
        return -1;
    }

    public String substring(int startAt, int endBefore) {
        return input.substring(offset + startAt, offset + endBefore);
    }

    public char charAt(int startAt) {
        if (offset + startAt >= input.length()) return 0;
        return input.charAt(offset + startAt);
    }

    public int wordLength(int startsAt) {
        int current = offset + startsAt;
        while (current < input.length()) {
            if (!Character.isLetterOrDigit(input.charAt(current)) && input.charAt(current) != '_') break;
            current++;
        }
        return current - offset - startsAt;
    }
}
