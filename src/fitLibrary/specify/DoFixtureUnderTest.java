/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.specify;

import java.text.SimpleDateFormat;
import java.util.Date;

import fit.Parse;

/**
 * @author rick
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DoFixtureUnderTest extends fitLibrary.DoFixture {
	public DoFixtureUnderTest() {
		super(StartDoSpecification.SUT);
		registerParseDelegate(Date.class,
		        new SimpleDateFormat("yyyy/MM/dd HH:mm"));
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
