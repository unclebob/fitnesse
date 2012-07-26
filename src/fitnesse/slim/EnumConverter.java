package fitnesse.slim;

public class EnumConverter<T extends Enum<T>> implements Converter<T> {

    private final Class<T> enumClass;

    public EnumConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String toString(T o) {
        return o.name();
    }

    @Override
    public T fromString(String name) {
        return Enum.valueOf(enumClass, name);
    }


}
