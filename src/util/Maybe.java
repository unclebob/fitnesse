package util;

public class Maybe<T> {
    public static final Maybe<String> noString = new Maybe<String>("*nothing*", true);
    
    private final T value;
    private final boolean isNothing;

    public Maybe(T value) { this(value, false); }

    private Maybe(T value, boolean isNothing) {
        this.value = value;
        this.isNothing = isNothing;
    }

    public T getValue() { return value; }
    public boolean isNothing() { return isNothing; }
}
