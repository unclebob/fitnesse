/*
 * @author Rick Mugridge on Jan 10, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.specify;

import fitLibrary.DoFixture;
import fitLibrary.graphic.DotGraphic;
import fitLibrary.DoFixture;

/**
 *
 */
public class DoGraphics extends DoFixture {
    public DotGraphic graph() {
        return new DotGraphic("digraph G {\n"+
                "lotr->luke;\n"+
                "lotr->Anna;\n"+
                "shrek->luke;\n"+
                "shrek->anna;\n"+
                "shrek->madelin;\n"+
        "}\n");
    }
}
