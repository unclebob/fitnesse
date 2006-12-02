package fitnesse.testutil;

import java.awt.Point;

import fit.TypeAdapter;

public class ClassTranslatePoint extends TranslatePoint
{
    static
    {
        TypeAdapter.registerParseDelegate(Point.class, ClassDelegatePointParser.class);
    }
}
