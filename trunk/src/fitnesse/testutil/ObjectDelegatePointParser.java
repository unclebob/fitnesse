package fitnesse.testutil;

import java.awt.*;

public class ObjectDelegatePointParser
{
	public Point parse(String s)
	{
		// format = (xxxx,yyyyy)
		return ClassDelegatePointParser.parse(s);
	}

}
