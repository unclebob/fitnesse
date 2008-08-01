package fitnesse.testutil;

import junit.framework.TestCase;

import java.awt.*;

public class ClassDelegatePointParserTest extends TestCase
{
	public void testParseMethodReturnsPointClassFromGivenString() throws Exception
	{
		assertEquals(new Point(1, 2), ClassDelegatePointParser.parse("(1,2)"));
		assertEquals(new Point(2, -1), ClassDelegatePointParser.parse("(2,-1)"));
		assertEquals(new Point(-99999, 99999), ClassDelegatePointParser.parse("(-99999,99999)"));
	}

	public void testShouldThrowAnExceptionIfFormatOfTheInputStringIsNotWelformed() throws Exception
	{
		assertParseException("(,)");
		assertParseException("(2,)");
		assertParseException("(,2)");
		assertParseException("(22)");
		assertParseException("222,)");
		assertParseException("(,222");
	}

	private void assertParseException(String point)
	{
		try
		{
			ClassDelegatePointParser.parse(point);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e)
		{
			assertEquals(point + " is not a valid format. (x,y) is the correct format", e.getMessage());
		}
	}
}
