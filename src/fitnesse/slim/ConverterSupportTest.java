package fitnesse.slim;

import static org.junit.Assert.*;
import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.StringConverter;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ConverterSupportTest {

	private Converter<String> stringConverter;
	private static final String INPUT_STRING = "input string";
	private static final String CONVERTED_STRING = "converted string";

	@Before
	public void backupStringConverter() {
		stringConverter = ConverterRegistry.getConverterForClass(String.class);
	}
	
	@After
	public void restoreStringConverter() {
		ConverterRegistry.addConverter(String.class, stringConverter);
	}
	
	@Test
	public void testConvertArgs_NullValue() throws Exception {
		Object[] convertedArgs = convertSingleValue(null, String.class);
		assertEquals(1, convertedArgs.length);
		assertEquals(null, convertedArgs[0]);
	}

	@Test
	public void testConvertArgs_PassedThroughIfMatchingType() throws Exception {
		Date date = new Date();
		Object[] convertedArgs = convertSingleValue(date, Date.class);
		assertSame(date, convertedArgs[0]);
	}
	
	@Test
	public void testConvertArgs_ConvertedToDateIfStringIsPassedIn() throws Exception {
		String value = "27-FEB-2012";
		Date expectedDate = ConverterRegistry.getConverterForClass(Date.class).fromString(value);

		Object[] convertedArgs = convertSingleValue(value, Date.class);
		assertEquals(expectedDate, convertedArgs[0]);
	}
	
	@Test
	public void testConvertArgs_StringConverterApplies() throws Exception {
		Converter<String> myStringConverter = new MyStringConverter();
		ConverterRegistry.addConverter(String.class, myStringConverter );
		Object[] convertedArgs = convertSingleValue(INPUT_STRING, String.class);
		assertSame(CONVERTED_STRING, convertedArgs[0]);
	}
	
	@Test
	public void testConvertArgs_PassesThroughStringWithoutCustomConverter() throws Exception {
		Object[] convertedArgs = convertSingleValue(INPUT_STRING, String.class);
		assertSame(INPUT_STRING, convertedArgs[0]);
	}
	
	private Object[] convertSingleValue(Object value, Class<?> type) {
		Object[] args = new Object[1];
		args[0] = value;
		Class<?>[] argumentTypes = new Class<?>[1];
		argumentTypes[0] = type;
		return ConverterSupport.convertArgs(args, argumentTypes );
	}

	private class MyStringConverter extends StringConverter {
		@Override
		public String fromString(String o) {
			return CONVERTED_STRING;
		}
	}
	
}

