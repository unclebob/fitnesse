package fit.decorator.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import fit.decorator.exceptions.InvalidInputException;

public abstract class DataType {
  private static final String STRING_TYPE = "string";
  private static final String DOUBLE_TYPE = "double";
  private static final String INTEGER_TYPE = "integer";
  private static final String INT_TYPE = "int";
  private static final String DATE_TYPE = "date";

  protected static final DataType INTEGER = new DataType() {
    @Override
    protected Object valueOf(String value) throws Exception {
      return Integer.valueOf(value);
    }

    @Override
    protected String addTo(String originalValue, Object value, int numberofTime) {
      int intValue = ((Integer) value).intValue() * numberofTime;
      return String.valueOf(Integer.parseInt(originalValue) + intValue);
    }

    @Override
    public String toString() {
      return "DataType = '" + INT_TYPE + "'";
    }
  };

  protected static final DataType DOUBLE = new DataType() {
    @Override
    protected Object valueOf(String value) throws Exception {
      return Double.valueOf(value);
    }

    @Override
    protected String addTo(String originalValue, Object value, int numberofTime) {
      double doubleValue = ((Double) value).doubleValue() * numberofTime;
      double results = Double.parseDouble(originalValue) + doubleValue;
      return String.valueOf(new Double(results).floatValue());
    }

    @Override
    public String toString() {
      return "DataType = '" + DOUBLE_TYPE + "'";
    }
  };

  protected static final DataType STRING = new DataType() {
    @Override
    protected Object valueOf(String value) throws Exception {
      return value;
    }

    @Override
    protected String addTo(String originalValue, Object value, int numberofTime) {
      return originalValue + multiply(value.toString(), numberofTime);
    }

    private String multiply(String value, int numberofTime) {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < numberofTime; ++i) {
        result.append(value);
      }
      return result.toString();
    }

    @Override
    public String toString() {
      return "DataType = '" + STRING_TYPE + "'";
    }
  };

  protected static final DataType DATE = new DataType() {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    protected String addTo(String originalValue, Object value, int numberofTime) {
      GregorianCalendar gc = new GregorianCalendar();
      Date originalDate;
      try {
        originalDate = dateFormat.parse(originalValue);
        gc.setTime(originalDate);
      } catch (ParseException e) {
        // print stack trace and continue
        e.printStackTrace();
      }
      int totalNnumberOfDaysToAdd = ((Integer) value).intValue() * numberofTime;
      gc.add(Calendar.DAY_OF_YEAR, totalNnumberOfDaysToAdd);
      return dateFormat.format(gc.getTime());
    }

    @Override
    protected Object valueOf(String value) throws Exception {
      return Integer.valueOf(value);
    }

    @Override
    public String toString() {
      return "DataType = '" + DATE_TYPE + "'";
    }
  };

  private static final Map<String, DataType> predefinedTypes = new HashMap<>();
  private static final Map<String, DataType> userDefinedTypes = new HashMap<>();

  static {
    predefinedTypes.put(INT_TYPE, INTEGER);
    predefinedTypes.put(INTEGER_TYPE, INTEGER);
    predefinedTypes.put(DOUBLE_TYPE, DOUBLE);
    predefinedTypes.put(STRING_TYPE, STRING);
    predefinedTypes.put(DATE_TYPE, DATE);
  }

  public Object parse(String value) throws InvalidInputException {
    try {
      return valueOf(value);
    } catch (Exception e) {
      throw new InvalidInputException("value '" + value + "' is not a valid " + toString());
    }
  }

  protected abstract String addTo(String originalValue, Object value, int numberofTime);

  protected abstract Object valueOf(String value) throws Exception;

  static DataType instance(String dataType) throws InvalidInputException {
    Object type = getDataType(dataType, predefinedTypes);
    if (type == null) {
      type = getDataType(dataType, userDefinedTypes);
      type = defaultToStringTypeIfNull(type);
    }
    return (DataType) type;
  }

  private static Object defaultToStringTypeIfNull(Object type) {
    if (type == null)
      type = STRING;
    return type;
  }

  private static Object getDataType(String dataType, Map<String, DataType> types) {
    return types.get(dataType.toLowerCase());
  }

  public static void registerUserDefinedDataTypes(String key, DataType dataType) {
    userDefinedTypes.put(key.toLowerCase(), dataType);
  }

  public static void clearUserDefinedDataTypes(String key) {
    userDefinedTypes.remove(key.toLowerCase());
  }

  public static void clearUserDefinedDataTypes() {
    userDefinedTypes.clear();
  }

}
