package util;

public class Maybe<T> {
    public static final Maybe<String> noString = new Maybe<String>("*nothing*", "");
    public static final Maybe<Double> noDouble = new Maybe<Double>(0.0, "");
    public static final Maybe<Integer> noInteger = new Maybe<Integer>(0, "");
    public static final Maybe<Character> noCharacter = new Maybe<Character>('\0', "");

    private final T value;
    private final String nothingReason;

    public Maybe(T value) { this(value, null); }

    public Maybe() { this(null, ""); }

    public static <T> Maybe<T> nothingBecause(String nothingReason) {
        return new Maybe<T>(null, nothingReason);
    }

    private Maybe(T value, String nothingReason) {
        this.value = value;
        this.nothingReason = nothingReason;
    }

    public T getValue() { return value; }
    public boolean isNothing() { return nothingReason != null; }
    public String because() { return nothingReason; }
}
