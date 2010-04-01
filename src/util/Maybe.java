package util;

public class Maybe<T> {
    public static final Maybe<String> noString = new Maybe<String>("*nothing*", true);
    public static final Maybe<Double> noDouble = new Maybe<Double>(0.0, true);
    public static final Maybe<Integer> noInteger = new Maybe<Integer>(0, true);

    private final T value;
    private final boolean isNothing;

    public Maybe(T value) { this(value, false); }

    public Maybe() { this(null, true); }

    private Maybe(T value, boolean isNothing) {
        this.value = value;
        this.isNothing = isNothing;
    }

    public T getValue() { return value; }
    public boolean isNothing() { return isNothing; }
}
