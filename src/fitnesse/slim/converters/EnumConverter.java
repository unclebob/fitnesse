package fitnesse.slim.converters;

import fitnesse.slim.Converter;

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
		if (name == null || "".equals(name.trim()))
			return null;
		else
			try {
				return Enum.valueOf(enumClass, name);
			} catch (IllegalArgumentException e) {
				for (T value : enumClass.getEnumConstants()) {
					if (value.toString().equalsIgnoreCase(name)) {
						return value;
					}
				}
				throw e;
			}
	}

}
