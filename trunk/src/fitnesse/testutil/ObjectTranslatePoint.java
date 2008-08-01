package fitnesse.testutil;

import fit.TypeAdapter;

import java.awt.*;

public class ObjectTranslatePoint extends TranslatePoint
{
	static
	{
		TypeAdapter.registerParseDelegate(Point.class, new ObjectDelegatePointParser());
	}
}
