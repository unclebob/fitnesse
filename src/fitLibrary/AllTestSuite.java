/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary;

import junit.framework.*;

public class AllTestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("fitLibrary");
        suite.addTestSuite(ExtendedCamelCaseTest.class);
        suite.addTestSuite(fitLibrary.ff.ColoringTest.class);
        suite.addTestSuite(fitLibrary.ff.FixtureFixtureTest.class);
        suite.addTestSuite(fitLibrary.ff.FlowFixtureFixtureTest.class);
        suite.addTestSuite(fitLibrary.tree.TestListTree.class);
        suite.addTestSuite(fitLibrary.graphic.TestObjectDotGraphic.class);
        return suite;
    }
}
