/*
 * @author Rick Mugridge 2/02/2005
 * Copyright (c) 2005 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary.specify;

import fit.Fixture;
import fitLibrary.GridFixture;
import fitLibrary.ImageFixture;
import fitLibrary.graphic.ImageNameGraphic;
import fitLibrary.tree.ListTree;
import fitLibrary.DoFixture;
import fitLibrary.*;

/**
 * 
 */
public class GridFixtureUnderTest extends DoFixture {
	public Fixture empty() {
		return new GridFixture(new Object[][] {});
	}
	public Fixture strings() {
		return new GridFixture(new String[][] { {"a", "b"}, {"c", "d"} });
	}
	public Fixture ints() {
		return new GridFixture(new Integer[][] {
				{new Integer(1), new Integer(2)},
				{new Integer(3), new Integer(4)} });
	}
	public Fixture trees() {
		return new GridFixture(new ListTree[][] {
				{ ListTree.parse("a"),
				  ListTree.parse("<ul><li>a</li></ul>") },
			    { ListTree.parse("<ul><li>BB</li></ul>"),
				  ListTree.parse("<ul><li>a</li><li>BB</li></ul>")} });
	}
    public Fixture images() {
        return new GridFixture(new ImageNameGraphic[][] {
        		{   new ImageNameGraphic("images/wall.jpg"),
        			new ImageNameGraphic("images/space.jpg"),
        			new ImageNameGraphic("images/box.jpg"),
        			new ImageNameGraphic("images/space.jpg"),
        			new ImageNameGraphic("images/wall.jpg") }});
    }
    public Fixture imagesForImageFixture() {
        return new ImageFixture(new String[][] {
        		{   "images/wall.jpg",
        			"images/space.jpg",
        			"images/box.jpg",
        			"images/space.jpg",
        			"images/wall.jpg" }});
    }
}
