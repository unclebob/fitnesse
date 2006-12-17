package fit.decorator;

import java.text.ParseException;

import fit.Parse;
import fit.decorator.exceptions.InvalidInputException;
import fit.decorator.util.Delta;
import fit.decorator.util.TestCaseHelper;

public class IncrementColumnsValueTest extends FixtureDecoratorTestCase
{
    private static final String FIRST_HTML_ROW = "<tr><td>" + IncrementColumnsValue.class.getName()
            + "</td><td>ColumnName</td><td>of type</td><td>int</td><td>by</td><td>1</td></tr>";
    private FixtureDecorator decorator = new IncrementColumnsValue();

    protected String geDecoratorHTMLRow()
    {
        return FIRST_HTML_ROW;
    }

    protected int numberOfAssertionsOnDecorator()
    {
        return 0;
    }

    public void testSetupDecoratorShouldAddColumnnameDatatypeAndDeltaToSummary() throws Exception
    {
        decorator.setupDecorator(new String[]
        {"Column1", "int", "1"});
        assertEquals("Column1", (String) decorator.summary.get(IncrementColumnsValue.COLUMN_NAME));
        assertEquals(new Delta("int", "1"), decorator.summary.get(IncrementColumnsValue.DELTA));
    }

    public void testSetupDecoratorShouldThrowInvalidInputExceptionIfColumnNameIsNotSpecified() throws ParseException
    {
        assertInvalidInputException(new String[]
        {}, "Column name, Data type and Delta Value must be specified");
    }

    public void testSetupDecoratorShouldThrowInvalidInputExceptionIfDataTypeIsNotSpecified() throws ParseException
    {
        assertInvalidInputException(new String[]
        {"Column1"}, "Column name, Data type and Delta Value must be specified");
    }

    public void testSetupDecoratorShouldThrowInvalidInputExceptionIfDeltaValueIsNotSpecified() throws ParseException
    {
        assertInvalidInputException(new String[]
        {"Column1", "double"}, "Column name, Data type and Delta Value must be specified");
    }

    public void testSetupDecoratorShouldThrowInvalidInputExceptionIfDataTypeAndDeltaValueDoNotMatch()
            throws ParseException
    {
        assertInvalidInputException(new String[]
        {"Column1", "double", "xyz"}, "value 'xyz' is not of type 'double'");
        assertInvalidInputException(new String[]
        {"Column1", "int", "1.2"}, "value '1.2' is not of type 'int'");
    }

    public void testSetupDecoratorShouldDefaultDataTypeToStringIfItDoesNotMatch_int_integer_or_double()
            throws Exception
    {
        decorator.setupDecorator(new String[]
        {"Column1", "long", "1"});
        assertEquals(new Delta("String", "1"), decorator.summary.get(IncrementColumnsValue.DELTA));
    }

    public void testSetupDecoratorShouldDefaultDataTypeTo_int() throws Exception
    {
        decorator.setupDecorator(new String[]
        {"Column1", "integer", "1"});
        assertEquals(new Delta("INT", "1"), decorator.summary.get(IncrementColumnsValue.DELTA));
    }

    public void testShouldIncrementColumnValuesFromSecondRowForTheGivenColumnName() throws Exception
    {
        String fitPage = "<table><tr><td>" + IncrementColumnsValue.class.getName()
                + "</td><td>numerator</td><td>of type</td><td>int</td><td>by</td><td>5</td></tr>"
                + "<tr><td>eg.Division</td></tr>"
                + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
                + "<tr><td>10</td><td>2</td><td>5</td></tr><tr><td>10</td><td>3</td><td>5</td></tr>"
                + "<tr><td>10</td><td>3</td><td>5</td></tr></table>";
        decorator.doTable(new Parse(fitPage));
        TestCaseHelper.assertCounts(TestCaseHelper.counts(2, 1, 0, 0), decorator.counts);
    }

    public void testShouldLeaveTheTableAsItIsIfTablesHasLessThanFourRows() throws Exception
    {
        String fitPage = "<table><tr><td>" + IncrementColumnsValue.class.getName()
                + "</td><td>numerator</td><td>of type</td><td>int</td><td>by</td><td>5</td></tr>"
                + "<tr><td>eg.Division</td></tr>"
                + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
                + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
        decorator.doTable(new Parse(fitPage));
        TestCaseHelper.assertCounts(TestCaseHelper.counts(1, 0, 0, 0), decorator.counts);
    }

    public void testShouldHandleExceptionsByIgnoringDecorator() throws Exception
    {
        String fitPage = "<table><tr><td>" + IncrementColumnsValue.class.getName()
                + "</td><td>invalidColumnName</td><td>of type</td><td>int</td><td>by</td><td>5</td></tr>"
                + "<tr><td>eg.Division</td></tr>"
                + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
                + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
        decorator.doTable(new Parse(fitPage));
        TestCaseHelper.assertCounts(TestCaseHelper.counts(1, 0, 0, 0), decorator.counts);
    }

    public void testShouldIncrementMultipleColumnsValueIfDecoratorIsPiped() throws Exception
    {
        String fitPage = "<table><tr><td>" + IncrementColumnsValue.class.getName()
                + "</td><td>numerator</td><td>of type</td><td>int</td><td>by</td><td>5</td></tr>" + "<tr><td>"
                + IncrementColumnsValue.class.getName()
                + "</td><td>denominator</td><td>of type</td><td>int</td><td>by</td><td>1</td></tr>"
                + "<tr><td>eg.Division</td></tr>"
                + "<tr><td>numerator</td><td>denominator</td><td>quotient()</td></tr>"
                + "<tr><td>10</td><td>2</td><td>5</td></tr>" + "<tr><td>10</td><td>2</td><td>5</td></tr></table>";
        decorator.doTable(new Parse(fitPage));
        TestCaseHelper.assertCounts(TestCaseHelper.counts(2, 0, 0, 0), decorator.counts);
    }

    private void assertInvalidInputException(String[] args, String errorMsg) throws ParseException
    {
        try
        {
            decorator.setupDecorator(args);
            fail("Should blow up");
        } catch (InvalidInputException e)
        {
            assertEquals(errorMsg, e.getMessage());
        }
    }
}
