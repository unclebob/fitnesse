/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;
import fit.ActionFixture;
import fit.Fixture;

public class ActionFixtureUnderTest extends Fixture {
	private int result = 0;
	public void pressMethod() {
	}
	public void enterString(String s) {
	}
	public void enterResult(int result) {
		this.result = result;
	}
	public int intResultMethod() {
		return result;
	}
	public boolean booleanResultMethod() {
		return false;
	}
	public void enterThrows(String s) {
		throw new RuntimeException();
	}
	public void pressThrows() {
		throw new RuntimeException();
	}
	public String checkThrows() {
		throw new RuntimeException();
	}
	public int pressMethodReturningInt() {
		return 123;
	}
	public void enterMethodWithNoArgs() {
	}
	public void enterMethodWithTwoArgs(String a, String b) {
	}
	public void switchActor() {
		ActionFixture.actor = new AnotherActor(this);
	}
}
