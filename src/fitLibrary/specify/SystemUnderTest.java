/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.specify;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import fitLibrary.SequenceFixture;
import fit.Fixture;
import fit.exception.FitFailureException;

public class SystemUnderTest {
	private int sum = 0;
	private String concat = "";
	private int value = 0;

	public void add(int i) {
		sum += i;
	}
	public int sum() {
		return sum;
	}
	public int plus() {
		return sum();
	}
	public void addAndAppend(int i, String s) {
		add(i);
		concat += s;
	}
	public void addAmpersandAppend(int i, String s) {
		addAndAppend(i,s);
	}
	public String appends() {
		return concat;
	}
	public String plusPlus() {
		return appends();
	}
	public boolean aRightAction(int i) {
		return true;
	}
	public boolean aWrongAction(double x, double y) {
		return false;
	}
	public int anExceptionAction() {
		throw new RuntimeException("testing");
	}
	public void value(int i) {
		value = i;
	}
	public void add() {
		add(value);
	}
	public Date sameDate(Date date) {
		return date;
	}
	public void hiddenMethod() {
		throw new FitFailureException("testing");
	}
	public Fixture anotherObject() {
	    return new SequenceFixture(new A());
	}
	public static class A {
	    public boolean accessOther() {
	        return true;
	    }
	    public int otherInt() {
	        return 4;
	    }
	}
	public boolean booleanProperty() {
	    return true;
	}
	public boolean isBooleanProperty() {
	    return true;
	}
	public int getIntProperty() {
	    return 2;
	}
	public Object[] anArrayOfPoint() {
	    return new Object[] { new Point(0,0), new Point(5,5) };
	}
	public List aListOfPoint() {
	    List list = new ArrayList();
	    list.add(new Point(0,0));
	    list.add(new Point(5,5));
	    return list;
	}
	public Iterator anIteratorOfPoint() {
	    return aListOfPoint().iterator();
	}
	public Set aSetOfPoint() {
	    Set set = new HashSet();
	    set.add(new Point(0,0));
	    set.add(new Point(5,5));
	    return set;
	}
	public SortedSet aSortedSetOfPoint() {
	    SortedSet set = new TreeSet(new Comparator() {
            public int compare(Object p1, Object p2) {
                Point pt1 = (Point)p1;
                Point pt2 = (Point)p2;
                if (pt1.x < pt2.x)
                    return -1;
                if (pt1.x > pt2.x)
                    return 1;
                if (pt1.y < pt2.y)
                    return -1;
                if (pt1.y > pt2.y)
                    return 1;
                return 0;
            }});
	    set.add(new Point(0,0));
	    set.add(new Point(5,5));
	    return set;
	}
	public Map aMapOfPoint() {
	    Map map = new HashMap();
	    map.put("0,0",new Point(0,0));
	    map.put("5,5",new Point(5,5));
	    return map;
	}
	public int plusAB(int a, int b) {
	    return a + b;
	}
	public String shown() {
	    return "<ul><li>ita<li>lics</ul>";
	}
}
