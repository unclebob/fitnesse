/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.specify;
import fitLibrary.ExtendedCamelCase;

public class TestCamelCase extends fitLibrary.CalculateFixture {
	public String identifierName(String name) {
        return ExtendedCamelCase.camel(name);
	}
}
