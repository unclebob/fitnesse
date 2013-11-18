package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Date;

public class ConverterRegistryTest {

	@Test
	public void checkInitialisationSuccessful() {
		Converter<Date> converter = ConverterRegistry
				.getConverterForClass(Date.class);
		Date converted = (Date) converter.fromString("27-FEB-2012");
		assertNotNull(converted);
	}

	@Test
	public void useConverterFromCustomizing() {
		ConverterRegistry
				.addConverter(CustomClass.class, new CustomConverter());

		Converter<CustomClass> converter = ConverterRegistry
				.getConverterForClass(CustomClass.class);
		assertEquals("customConverter", converter.toString(new CustomClass()));
	}

	static class CustomClass {

	}

	static class CustomConverter implements Converter<CustomClass> {

		public String toString(CustomClass o) {
			return "customConverter";
		}

		public CustomClass fromString(String arg) {
			return new CustomClass();
		}
	}
}
