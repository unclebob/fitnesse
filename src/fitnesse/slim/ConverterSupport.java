package fitnesse.slim;

import fitnesse.slim.converters.ConverterRegistry;

class ConverterSupport {

	public static Object[] convertArgs(Object[] args, Class<?>[] argumentTypes) {
		Object[] convertedArgs = new Object[args.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			convertedArgs[i] = convertArg(args[i], argumentTypes[i]);
		}
		return convertedArgs;
	}

	@SuppressWarnings("unchecked")
	private static <T> T convertArg(Object arg, Class<T> argumentType)
			throws SlimError {
		if (arg == null || (argumentType.isInstance(arg) && !(String.class.equals(argumentType)))) {
			// arg may be a List or an instance that comes from the variable store
			// But String arguments should always pass through the registered String Converter
			return (T) arg;
		}
		Converter<T> converter = ConverterRegistry.getConverterForClass(argumentType);
		if (converter != null) {
			return converter.fromString(arg.toString());
		}
		throw new SlimError(String.format(
				"message:<<%s %s.>>",
				SlimServer.NO_CONVERTER_FOR_ARGUMENT_NUMBER,
				argumentType.getName()));
	}

}
