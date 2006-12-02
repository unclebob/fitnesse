package fitnesse.testutil;

import java.awt.Point;

import fit.TypeAdapter;

public class ObjectTranslatePoint extends TranslatePoint
{
    static {
        TypeAdapter.registerParseDelegate(Point.class, new ObjectDelegatePointParser());
    }
}
