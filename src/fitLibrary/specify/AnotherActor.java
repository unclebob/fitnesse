/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */

package fitLibrary.specify;
import fit.ActionFixture;
import fit.Fixture;

public class AnotherActor extends Fixture {
	private ActionFixtureUnderTest test;
	
	public AnotherActor() {
		this(new ActionFixtureUnderTest());
	}
	public AnotherActor(ActionFixtureUnderTest test) {
		this.test = test;
	}
	public void start() {
	}
	public void stop() {
	}
	public void switchBack() {
		ActionFixture.actor = test;
	}
}
