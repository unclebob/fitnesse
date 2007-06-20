// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.Fixture;

public class ActionFixtureTestFixture extends Fixture
{
	public void duplicateMethod(int i) {}

	public void duplicateMethod(double d) {}

	public void realMethod() {}

	public void someField(int i) {}

	public int realCheck() {return 0;}

	public void voidCheck() {}
}
