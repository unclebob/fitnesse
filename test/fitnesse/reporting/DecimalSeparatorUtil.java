package fitnesse.reporting;

import java.text.DecimalFormatSymbols;

public class DecimalSeparatorUtil {

	public static String getDecimalSeparator() {
	    return String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());
	  }

	public static String getDecimalSeparatorForRegExp() {
	    return getDecimalSeparator().replace(".", "\\.");
	  }

}
