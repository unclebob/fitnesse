package fitnesse.slim;

import static org.junit.Assert.*;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.DateConverter;
import fitnesse.slim.converters.EnumConverter;
import fitnesse.slim.test.AnEnum;
import fitnesse.slim.test.AnotherEnum;

import org.junit.Test;

public class ConverterTest {
	@Test
	public void convertDate() throws Exception {
		assertConverts(new DateConverter(), "05-May-2009");
	}

	@Test
	public void convertDateWithoutLeadingZero() throws Exception {
		assertConverts("05-May-2009", new DateConverter(), "5-May-2009");
	}

	@Test
	public void convertBooleanTrue() throws Exception {
		BooleanConverter converter = new BooleanConverter();
		assertConverts(converter, "true");
		assertConverts("true", converter, "True");
		assertConverts("true", converter, "TRUE");
		assertConverts("true", converter, "YES");
		assertConverts("true", converter, "yes");
	}

	@Test
	public void convertBooleanFalse() throws Exception {
		BooleanConverter converter = new BooleanConverter();
		assertConverts(converter, "false");
		assertConverts("false", converter, "FALSE");
		assertConverts("false", converter, "False");
		assertConverts("false", converter, "no");
		assertConverts("false", converter, "NO");
		assertConverts("false", converter, "0");
		assertConverts("false", converter, "1");
		assertConverts("false", converter, "x");
	}

	@Test
	public void defaultEnumConversion() {
		assertTrue(ConverterRegistry.getConverterForClass(AnEnum.class) instanceof EnumConverter);
	}

	@Test
	public void convertEnum() throws Exception {
		assertConverts(new EnumConverter<AnEnum>(AnEnum.class), "ONE_VALUE");
	}

	@Test
	public void usesEditorForEnum() throws Exception {
		assertConverts("enum property editor called with \"some value\"",
				ConverterRegistry.getConverterForClass(AnotherEnum.class), "some value");
	}

	private <T> void assertConverts(Converter<T> converter, String value) {
		assertConverts(value, converter, value);
	}

	private <T> void assertConverts(String expected, Converter<T> converter,
			String value) {
		assertEquals(expected, converter.toString(converter.fromString(value)));
	}
}
