/*
 * @author Rick Mugridge 15/01/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.graphic;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * 
 */
public class TestObjectDotGraphic extends TestCase {
	public void testPoint() {
		assertDot("digraph G {\nn0 [label = \"java.awt.Point[x=0,y=0]\"];\n}\n",
				new Point());
	}
	public void testMyPoint() {
		assertDot("digraph G {\n"
				+ "n0 [label = \"fitLibrary.graphic.TestObjectDotGraphic$MyPoint\"];\n"
				+ "n1 [label = \"0\"];\n"
				+ "n0 -> n1 [label=\"x\"];\n"
				+ "n0 -> n1 [label=\"y\"];\n"
				+ "}\n",
				new MyPoint());
	}
	public void testNested() {
		assertDot("digraph G {\n"
				+ "n0 [label = \"fitLibrary.graphic.TestObjectDotGraphic$MyRectangle\"];\n"
				+ "n1 [label = \"fitLibrary.graphic.TestObjectDotGraphic$MyPoint\"];\n"
				+ "n0 -> n1 [label=\"pt1\"];\n"
				+ "n2 [label = \"java.awt.Point[x=1,y=2]\"];\n"
				+ "n0 -> n2 [label=\"pt2\"];\n"
				+ "n3 [label = \"0\"];\n"
				+ "n1 -> n3 [label=\"x\"];\n"
				+ "n1 -> n3 [label=\"y\"];\n"
				+ "}\n", new MyRectangle());
	}
//	public void testArray() {
//		assertDot("", new PointsArray());
//	}
//	public void testCollection() {
//		assertDot("", new PointsCollection());
//	}
//	public void testSet() {
//		assertDot("", new PointsSet());
//	}
	
	private static class MyPoint extends Point {
	}
	private static class MyRectangle {
		MyPoint pt1 = new MyPoint();
		Point pt2 = new Point(1,2);

		public MyPoint getPt1() {
			return pt1;
		}
		public Point getPt2() {
			return pt2;
		}
	}
	private static class PointsArray {
		MyPoint[] pts = { new MyPoint(),new MyPoint() };

		public MyPoint[] getPts() {
			return pts;
		}
	}
	private static class PointsCollection {
		MyPoint[] pts = { new MyPoint(),new MyPoint() };

		public List getPts() {
			return Arrays.asList(pts);
		}
	}
	private static class PointsSet {
		public Set pts = new HashSet(Arrays.asList(
				new Object[]{ new MyPoint(),new Point(1,2) }));

		public Set getPts() {
			return pts;
		}
	}

	private void assertDot(String expected, Object object) {
		assertEquals(expected, new ObjectDotGraphic(object).dot);
	}
}
