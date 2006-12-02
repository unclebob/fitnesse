// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import java.awt.Point;

import fit.ColumnFixture;

public class TranslatePoint extends ColumnFixture
{
    public Point p1;
    public Point p2;
    public Point sum()
    {
        p1.translate(p2.x, p2.y);
        return p1;
    }
}
