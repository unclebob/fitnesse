package fit.decorator.util;

import java.util.HashMap;
import java.util.Map;

import fit.decorator.exceptions.InvalidInputException;

public abstract class DataType
{
    private static final String STRING_TYPE = "string";
    private static final String DOUBLE_TYPE = "double";
    private static final String INTEGER_TYPE = "integer";
    private static final String INT_TYPE = "int";

    private static final DataType INTEGER = new DataType()
    {
        public Object validate(String value) throws InvalidInputException
        {
            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new InvalidInputException("value '" + value + "' is not of type '" + INT_TYPE + "'");
            }
        }

        public String addTo(String originalValue, Object value, int numberofTime)
        {
            int intValue = ((Integer) value).intValue() * numberofTime;
            return String.valueOf(Integer.parseInt(originalValue) + intValue);
        }

        public String toString()
        {
            return "DataType = " + INT_TYPE;
        }
    };

    private static final DataType DOUBLE = new DataType()
    {
        public Object validate(String value) throws InvalidInputException
        {
            try
            {
                return Double.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new InvalidInputException("value '" + value + "' is not of type '" + DOUBLE_TYPE + "'");
            }
        }

        public String addTo(String originalValue, Object value, int numberofTime)
        {
            double doubleValue = ((Double) value).doubleValue() * numberofTime;
            double results = Double.parseDouble(originalValue) + doubleValue;
            return String.valueOf(new Double(results).floatValue());
        }

        public String toString()
        {
            return "DataType = " + DOUBLE_TYPE;
        }
    };

    private static final DataType STRING = new DataType()
    {
        public Object validate(String value) throws InvalidInputException
        {
            return value;
        }

        public String addTo(String originalValue, Object value, int numberofTime)
        {
            return originalValue + multiply((String) value, numberofTime);
        }

        private String multiply(String value, int numberofTime)
        {
            String result = value;
            for (int i = 1; i < numberofTime; ++i)
            {
                result += value;
            }
            return result;
        }

        public String toString()
        {
            return "DataType = " + STRING_TYPE;
        }
    };

    private static final Map<String, DataType> types = new HashMap<String, DataType>();

    static
    {
        types.put(INT_TYPE, INTEGER);
        types.put(INTEGER_TYPE, INTEGER);
        types.put(DOUBLE_TYPE, DOUBLE);
        types.put(STRING_TYPE, STRING);
    }

    public abstract String addTo(String originalValue, Object value, int numberofTime);

    public abstract Object validate(String value) throws InvalidInputException;

    static DataType instance(String dataType) throws InvalidInputException
    {
        Object type = types.get(dataType.toLowerCase());
        if (type == null)
        {
            type = STRING;
        }
        return (DataType) type;
    }

}
