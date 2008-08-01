package fitnesse.fixtures;

import fitlibrary.DoFixture;

import java.util.regex.Pattern;

public class JavaProperties extends DoFixture
{
	public boolean propertyShouldMatch(String property, String pattern) {
		String value = System.getProperty(property);
		return Pattern.matches(pattern, value);
	}

	public String property(String property) {
		return System.getProperty(property);
	}
}
