/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

public class SetFixtureUnderTestGraphics extends fitLibrary.SetFixture {
	public SetFixtureUnderTestGraphics() throws Exception {
		super(new ArrayFixtureUnderTestGraphics.GraphicElement[]{
	   			new ArrayFixtureUnderTestGraphics.GraphicElement(1,"a"),
				new ArrayFixtureUnderTestGraphics.GraphicElement(1,"<ul><li>a</li></ul>"),
				new ArrayFixtureUnderTestGraphics.GraphicElement(2,"<ul><li>a</li><li>BB</li></ul>")});
	}
}
