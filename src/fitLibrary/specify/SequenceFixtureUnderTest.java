/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.specify;

import java.text.SimpleDateFormat;
import java.util.Date;

import fit.Parse;

public class SequenceFixtureUnderTest extends fitLibrary.SequenceFixture {
	public static SimpleDateFormat DATE_FORMAT = 
		   new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public SequenceFixtureUnderTest() {
		super(StartDoSpecification.SUT);
		this.registerParseDelegate(Date.class,DATE_FORMAT);
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
}
