/*
 * @author Rick Mugridge 12/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.table;

import fit.Parse;
import fitLibrary.ParseUtility;

/**
 * 
 */
public class Table implements TableInterface {
	private Parse parse;

	public Table(Parse parse) {
		this.parse = parse;
	}
	public Table tableAt(int i, int j, int k) {
		Parse at = parse.at(i,j,k).parts;
		return new Table(at);
	}
	public String stringAt(int i, int j, int k) {
		Parse p2 = parse.at(i,j,k);
		if (p2.body == null)
			return "null";
		return p2.text();
	}
	public Table toTable() {
		return this;
	}
	public static Table parseTable(Parse parse) {
		return new Table(parse);
	}
	public static boolean equals(Object expected, Object actual) {
		if (expected == null)
			return actual == null;
		return expected.equals(actual);
	}
	public boolean equals(Object actual) {
		if (!(actual instanceof Table))
			return false;
		Table other = (Table)actual;
		return equalsParse(parse,other.parse);
	}
	public boolean equalsParse(Parse p1, Parse p2) {
		if (p1 == null)
			return p2 == null;
		if (p2 == null)
			return false;
		return p1.tag.equals(p2.tag) &&
			equalStrings(p1.leader, p2.leader) &&
			equalStrings(p1.body, p2.body) &&
			equalStrings(p1.trailer, p2.trailer) &&
			equalsParse(p1.more, p2.more) &&
			equalsParse(p1.parts, p2.parts);
	}
	private boolean equalStrings(String s1, String s2) {
		if (s1 == null)
			return (s2 == null);
		if (s2 == null)
			return false;
		return s1.equals(s2);
	}
	public String toString() {
		return ParseUtility.toString(parse);
	}
}
