/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;

import fit.Fixture;
import fit.Parse;

public class DoFixtureFlowUnderTest extends fitLibrary.DoFixture {
	private static SimpleDateFormat DATE_FORMAT = 
		   new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public DoFixtureFlowUnderTest() {
		super(new SystemUnderTest());
		registerParseDelegate(Date.class,DATE_FORMAT);
	}
	public void specialAction(Parse cells) {
		cells = cells.more;
		if (cells.text().equals("right"))
			right(cells);
		else if (cells.text().equals("wrong"))
			wrong(cells);
	}
	public void hiddenMethod() {
	}
	public Fixture fixtureObject(int initial) {
		return new MyColumnFixture(initial);
	}
	public Object aPoint() {
	    return new Point(2,3);
	}
	public Date getDate() {
	    return new Date(2004-1900,2,3);
	}
	public void getException() {
		throw new RuntimeException("Forced exception");
	}
	public Integer anInteger() {
	    return new Integer(23);
	}
	public MyClass myClass() {
	    return new MyClass(3);
	}
	public ClassWithNoTypeAdapter useToString() {
		return new ClassWithNoTypeAdapter();
	}
	public static class ClassWithNoTypeAdapter {
		public String toString() {
			return "77";
		}
	}
	public static class MyClass {
        private int i;

        public MyClass(int i) {
            this.i = i;
        }
        public static MyClass parse(String s) {
	        return new MyClass(Integer.parseInt(s));
	    }
        public String toString() {
            return ""+i;
        }
        public boolean equals(Object object) {
            if (!(object instanceof MyClass))
                return false;
            return ((MyClass)object).i == i;
        }
	}
	public static class MyColumnFixture extends fit.ColumnFixture {
		public int x = 0;
		public MyColumnFixture(int initial) {
			x = initial;
		}
		public int getX() {
			return x;
		}
	}
	public Fixture getSlice(int row, int column) {
		return new LocalRowFixture(row,column);
	}
	public static class LocalRowFixture extends fit.RowFixture {
		private Local[][][] rows = {
		  {
			{ new Local("A0a"), new Local("A0b") },
			{ new Local("A1a"), new Local("A1b") },
			{ new Local("A2a"), new Local("A2b") },
			{ new Local("A3a"), new Local("A3b") }
		  },
		  {
			{ new Local("B0a"), new Local("B0b") },
			{ new Local("B1a"), new Local("B1b") },
			{ new Local("B2a"), new Local("B2b") },
			{ new Local("B3a"), new Local("B3b") }
		  }
		};
		private int row, column;
		
		public LocalRowFixture(int row, int column) {
			this.row = row;
			this.column = column;
		}
		public Object[] query() throws Exception {
			return rows[row][column];
		}
		public Class getTargetClass() {
			return Local.class;
		}
	}
	public static class Local {
		public String s;

		public Local(String s) {
			this.s = s;
		}
	}
	public PointHolder getPointHolder() {
	    return new PointHolder();
	}
	public static class PointHolder {
	    public Point getPoint() {
            return new Point(24,7);
	    }
	}
}
