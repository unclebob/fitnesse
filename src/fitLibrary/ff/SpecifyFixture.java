/*
 * @author Rick Mugridge 13/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.ff;

import fit.Fixture;
import fit.Parse;
import fitLibrary.ParseUtility;
import fitLibrary.DoFixture;
import fitLibrary.*;

/**
 * 
 */
public class SpecifyFixture extends DoFixture {
	protected void interpretTables(Parse tables) {
		tables = tables.more;
		while (tables != null) {
			Parse actual = tables.at(0,0,0).parts;
			Parse expectedCell = tables.at(0,1,0);
			Parse expected = expectedCell.parts;
			new Fixture().doTables(actual);
			if (reportsEqual(actual,expected))
				right(expectedCell);
			else {
				wrong(expectedCell);
				ParseUtility.printParse(actual,"actual");
//				ParseUtility.printParse(expected,"expected");
			}
			listener.tableFinished(tables);
			tables = tables.more;
		}
	}
	public boolean reportsEqual(Parse p1, Parse p2) {
		if (p1 == null)
			return p2 == null;
		if (p2 == null)
			return false;
		boolean result = equalTags(p1, p2) &&
			equalStrings(p1.leader, p2.leader) &&
			equalBodies(p1,p2) &&
			equalStrings(p1.trailer, p2.trailer) &&
			reportsEqual(p1.more, p2.more) &&
			reportsEqual(p1.parts, p2.parts);
//		if (!result) {
//			System.out.println("Difference of '"+ParseUtility.toString(p1)+"' and '"+ParseUtility.toString(p2)+"'");
//			if (!equalTags(p1, p2))
//				System.out.println("Tags differ: '"+p1.tag+"' and '"+p2.tag+"'");
//			if (!equalStrings(p1.leader, p2.leader))
//				System.out.println("Leaders differ: '"+p1.leader+"' and '"+p2.leader+"'");
//			if (!equalBodies(p1,p2))
//				System.out.println("Bodies differ: '"+p1.body+"' and '"+p2.body+"'");
//			if (!equalStrings(p1.trailer, p2.trailer))
//				System.out.println("Trailers differ: '"+p1.trailer+"' ("+p1.trailer.length()+
//						") and '"+p2.trailer+"'");
//		}
		return result;
	}
	private boolean equalBodies(Parse p1, Parse p2) {
		String body2 = p2.body;
		if (p1.body == null)
			return (body2 == null);
		if (body2 == null)
			return false;
		if (p1.body.equals(body2))
			return true;
		String stackTrace = "class=\"fit_stacktrace\">";
		if (body2.indexOf(stackTrace) >= 0) {
			int end = body2.indexOf("</pre>");
			String pattern = body2.substring(0,end);
			return p1.body.startsWith(pattern);
		}
		return false;
	}
	private boolean equalTags(Parse p1, Parse p2) {
		return p1.tag.equals(p2.tag);
	}
	private boolean equalStrings(String s1, String s2) {
		if (s1 == null)
			return (s2 == null || s2.trim().equals("") || s2.equals("\n"));
		if (s2 == null)
			return s1.trim().equals("") || s1.equals("\n");
		return s1.trim().equals(s2.trim());
	}
}
