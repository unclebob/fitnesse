package fitnesse.fixtures;

import fit.ColumnFixture;

public class SystemPropertySetterFixture extends ColumnFixture
{
	public String key;
	public String value;
	public void execute() {
		System.getProperties().setProperty(key, value);
	}
}
